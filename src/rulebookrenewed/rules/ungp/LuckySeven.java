package rulebookrenewed.rules.ungp;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Helpful: every 7th projectile or missile hit by the ship the player flies deals 7% more damage, with
 * a 7% chance of 77% more instead, and a 7% chance of that being 777% more. Beams do not count. The
 * count is kept per ship. Adapted from Lucky Seven in Unofficial New Game Plus by Originem.
 */
public final class LuckySeven implements RuleEffect {

    private static final float CHANCE = 0.07f;

    @Override
    public void applyToShip(RuleSpec rule, ShipAPI ship, boolean enemy, int level, float value) {
        if (enemy || ship.isFighter() || ship.hasListenerOfClass(Seventh.class)) return;
        ship.addListener(new Seventh(ship, RuleEffect.modId(rule)));
    }

    private static final class Seventh implements DamageDealtModifier {
        private final ShipAPI ship;
        private final String id;
        private int hits;

        Seventh(ShipAPI ship, String id) {
            this.ship = ship;
            this.id = id;
        }

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            CombatEngineAPI engine = Global.getCombatEngine();
            if (engine == null || engine.getPlayerShip() != ship || !(param instanceof DamagingProjectileAPI)) return null;
            if (++hits < 7) return null;
            hits = 0;
            float percent = 7f;
            float showFrom = 250f;
            float size = 20f;
            if (Tools.roll(CHANCE)) {
                if (Tools.roll(CHANCE)) {
                    percent = 777f;
                    showFrom = 0f;
                    size = 40f;
                } else {
                    percent = 77f;
                    showFrom = 150f;
                    size = 30f;
                }
            }
            if (damage.getDamage() >= showFrom && point != null) {
                engine.addFloatingText(point, "+" + (int) percent + "%", size, Color.red, target, 1f, 0f);
            }
            damage.getModifier().modifyPercent(id, percent);
            return id;
        }
    }
}
