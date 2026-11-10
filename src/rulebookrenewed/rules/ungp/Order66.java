package rulebookrenewed.rules.ungp;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Hard: ships of the player's side take more damage from their own side: projectiles, missiles, beams
 * and collisions. Adapted from Order 66 in Unofficial New Game Plus by Originem.
 */
public final class Order66 implements RuleEffect {

    @Override
    public void applyToShip(RuleSpec rule, ShipAPI ship, boolean enemy, int level, float value) {
        if (enemy || ship.hasListenerOfClass(FriendlyFire.class)) return;
        ship.addListener(new FriendlyFire(RuleEffect.modId(rule), value));
    }

    private static final class FriendlyFire implements DamageTakenModifier {
        private final String id;
        private final float percent;

        FriendlyFire(String id, float percent) {
            this.id = id;
            this.percent = percent;
        }

        @Override
        public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (target == null || !fromOwnSide(param, target.getOwner())) return null;
            damage.getModifier().modifyPercent(id, percent);
            return id;
        }

        private static boolean fromOwnSide(Object param, int owner) {
            if (param instanceof DamagingProjectileAPI) return ((DamagingProjectileAPI) param).getOwner() == owner;
            if (param instanceof BeamAPI) {
                ShipAPI source = ((BeamAPI) param).getSource();
                return source != null && source.getOwner() == owner;
            }
            if (param instanceof CombatEntityAPI && !(param instanceof EmpArcEntityAPI)) {
                return ((CombatEntityAPI) param).getOwner() == owner; // a collision
            }
            return false;
        }
    }
}
