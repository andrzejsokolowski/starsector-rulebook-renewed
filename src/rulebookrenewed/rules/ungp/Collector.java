package rulebookrenewed.rules.ungp;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageListener;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Helpful: when the ship the player flies, or one of its fighters, damages the hull of an enemy ship
 * (not a fighter) that is at or below a share of its hull, that ship is destroyed on the spot and the
 * player gains credits by its size (250 for a frigate up to 5,000 for a capital ship). One
 * battle-wide listener hears every hit; it can fire once every 0.15 seconds. Adapted from Collector in Unofficial New Game Plus by Originem.
 */
public final class Collector implements RuleEffect {

    private static final float PAUSE = 0.15f;

    @Override
    public void advanceCombat(RuleSpec rule, CombatEngineAPI engine, float amount, int level, float value) {
        if (engine.getListenerManager().hasListenerOfClass(Reaper.class)) return;
        engine.getListenerManager().addListener(new Reaper(RuleEffect.modId(rule), value / 100f));
    }

    private static final class Reaper implements DamageListener {
        private final String id;
        private final float hullShare;
        private float readyAt;
        private boolean busy;

        Reaper(String id, float hullShare) {
            this.id = id;
            this.hullShare = hullShare;
        }

        @Override
        public void reportDamageApplied(Object source, CombatEntityAPI target, ApplyDamageResultAPI result) {
            CombatEngineAPI engine = Global.getCombatEngine();
            if (busy || engine == null || result == null || engine.getTotalElapsedTime(false) < readyAt) return;
            if (!(source instanceof ShipAPI) || !(target instanceof ShipAPI) || source == target) return;
            ShipAPI attacker = (ShipAPI) source;
            ShipAPI victim = (ShipAPI) target;
            ShipAPI player = engine.getPlayerShip();
            if (player == null || victim == player || attacker.getOwner() != 0 || victim.getOwner() == 0) return;
            if (!victim.isAlive() || victim.isFighter() || result.getDamageToHull() <= 0f) return;
            if (attacker.isFighter()) {
                if (attacker.getWing() == null || attacker.getWing().getSourceShip() != player) return;
            } else if (attacker != player) {
                return;
            }
            if (victim.getHullLevel() > hullShare) return;

            busy = true;
            try {
                finish(engine, attacker, victim, result);
            } finally {
                busy = false;
            }
            readyAt = engine.getTotalElapsedTime(false) + PAUSE;
        }

        /** Strips the armor around a random spot on the ship, then deals more damage than its hull has left. */
        private void finish(CombatEngineAPI engine, ShipAPI attacker, ShipAPI victim, ApplyDamageResultAPI result) {
            ArmorGridAPI armor = victim.getArmorGrid();
            Vector2f center = victim.getLocation();
            MutableShipStatsAPI stats = victim.getMutableStats();
            stats.getMinArmorFraction().modifyMult(id, 0f);
            if (armor != null) {
                for (int tries = 0; tries < 20; tries++) {
                    int[] cell = armor.getCellAtLocation(Tools.randomPointInCircle(center, victim.getCollisionRadius()));
                    if (cell == null) continue;
                    clearArmor(armor, cell[0], cell[1]);
                    break;
                }
            }
            victim.setHitpoints(1f);
            // No sound of its own: the only sign of the rule is the credit text.
            engine.applyDamage(victim, center, victim.getMaxHitpoints() * hullShare + 999f, result.getType(), 0f, true,
                    false, attacker, false);
            stats.getMinArmorFraction().unmodify(id);
            CampaignFleetAPI fleet = Global.getSector() == null ? null : Global.getSector().getPlayerFleet();
            if (fleet == null) return;
            float credits = credits(victim.getHullSize());
            fleet.getCargo().getCredits().add(credits);
            engine.addFloatingText(center, "+" + Misc.getDGSCredits(credits), 50f, Color.yellow, null, 0f, 0f);
        }

        private static float credits(HullSize size) {
            switch (size) {
                case CAPITAL_SHIP: return 5000f;
                case CRUISER: return 2500f;
                case DESTROYER: return 1000f;
                default: return 250f;
            }
        }

        private static void clearArmor(ArmorGridAPI armor, int x, int y) {
            float[][] grid = armor.getGrid();
            for (int i = -2; i <= 2; i++) {
                for (int j = -2; j <= 2; j++) {
                    if (Math.abs(i) == 2 && Math.abs(j) == 2) continue; // not the corners
                    int cx = x + i;
                    int cy = y + j;
                    if (cx < 0 || cx >= grid.length || cy < 0 || cy >= grid[0].length) continue;
                    armor.setArmorValue(cx, cy, 0f);
                }
            }
        }
    }
}
