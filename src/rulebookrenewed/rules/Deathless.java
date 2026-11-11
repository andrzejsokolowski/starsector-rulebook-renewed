package rulebookrenewed.rules;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import rulebookrenewed.RuleSpec;

/**
 * Hard: once per battle, an enemy destroyer, cruiser or fleet flagship that takes a fatal hit shrugs it off
 * and recovers: hull, armor and missile ammunition back to a share of their maximum, flux emptied, peak
 * performance time gone. Capital ships (only as flagships) recover half as much. Adapted from Deathless in
 * Unofficial Rule Book by DemonX3 (MIT licence, see CREDITS.txt); unlike there, a frigate flagship counts
 * too, as its text says.
 */
public final class Deathless implements RuleEffect {

    private static final Color TEXT = new Color(255, 80, 100);

    @Override
    public void applyToShip(RuleSpec rule, ShipAPI ship, boolean enemy, int level, float value) {
        if (!enemy || ship.isFighter() || ship.isStation() || ship.isStationModule()) return;
        if (ship.hasListenerOfClass(Recovery.class)) return;
        FleetMemberAPI member = ship.getFleetMember();
        boolean flagship = member != null && member.isFlagship();
        boolean midSize = ship.isDestroyer() || ship.isCruiser();
        if (!midSize && !flagship) return;
        float share = Math.min(1f, value / 100f);
        if (ship.isCapital()) share *= 0.5f;
        ship.addListener(new Recovery(ship, share, RuleEffect.modId(rule), rule.name));
    }

    private static final class Recovery implements HullDamageAboutToBeTakenListener, AdvanceableListener {
        private final ShipAPI ship;
        private final float share;
        private final String id;
        private final String name;
        private boolean used;
        private boolean pending;

        Recovery(ShipAPI ship, float share, String id, String name) {
            this.ship = ship;
            this.share = share;
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean notifyAboutToTakeHullDamage(Object param, ShipAPI target, Vector2f point, float damageAmount) {
            if (pending) return true; // more hits in the same frame as the fatal one
            if (used || damageAmount < target.getHitpoints()) return false;
            used = true;
            pending = true;
            if (Global.getCombatEngine() != null) {
                Global.getCombatEngine().addFloatingText(target.getLocation(), name, 30f, TEXT, target, 0.5f, 1.5f);
            }
            return true;
        }

        @Override
        public void advance(float amount) {
            if (!pending || amount <= 0f) return;
            pending = false;
            ship.setHitpoints(Math.max(ship.getHitpoints(), ship.getMaxHitpoints() * share));
            ship.getFluxTracker().setCurrFlux(0f);
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (!weapon.usesAmmo()) continue;
                int target = (int) (weapon.getMaxAmmo() * share);
                if (weapon.getAmmo() < target) weapon.setAmmo(target);
            }
            ArmorGridAPI armor = ship.getArmorGrid();
            if (armor != null) {
                float cellTarget = armor.getMaxArmorInCell() * share;
                float[][] grid = armor.getGrid();
                for (int x = 0; x < grid.length; x++) {
                    for (int y = 0; y < grid[x].length; y++) {
                        if (grid[x][y] < cellTarget) armor.setArmorValue(x, y, cellTarget);
                    }
                }
            }
            // Peak performance time is gone: combat readiness starts to drop.
            ship.getMutableStats().getPeakCRDuration().modifyFlat(id, -100000f);
        }
    }
}
