package rulebookrenewed.rules.ungp;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Helpful: projectiles and missiles from weapons with a base range of 500 or less, fired by the
 * player's side, deal more damage. Beams are not affected. Adapted from Guide to Dogfighting in
 * Unofficial New Game Plus by Originem.
 */
public final class Dogfighting implements RuleEffect {

    private static final float MAX_BASE_RANGE = 500f;

    @Override
    public void applyToShip(RuleSpec rule, ShipAPI ship, boolean enemy, int level, float value) {
        if (enemy || ship.hasListenerOfClass(ShortRange.class)) return;
        ship.addListener(new ShortRange(RuleEffect.modId(rule), value));
    }

    private static final class ShortRange implements DamageDealtModifier {
        private final String id;
        private final float percent;

        ShortRange(String id, float percent) {
            this.id = id;
            this.percent = percent;
        }

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (!(param instanceof DamagingProjectileAPI)) return null;
            WeaponAPI weapon = ((DamagingProjectileAPI) param).getWeapon();
            if (weapon == null || weapon.getSpec() == null || weapon.getSpec().getMaxRange() > MAX_BASE_RANGE) return null;
            damage.getModifier().modifyMult(id, 1f + percent / 100f);
            return id;
        }
    }
}
