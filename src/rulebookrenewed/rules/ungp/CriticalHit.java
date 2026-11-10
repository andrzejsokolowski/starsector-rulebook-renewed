package rulebookrenewed.rules.ungp;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Helpful: every hit by the ship the player flies has a chance to deal double damage. Every ship of the
 * player's own gets the listener, which acts only while the player flies that ship, so the rule follows
 * a change of ship. Hits of 100 damage or more show a floating "Critical hit". Adapted from Critical
 * Hit in Unofficial New Game Plus by Originem.
 */
public final class CriticalHit implements RuleEffect {

    @Override
    public void applyToShip(RuleSpec rule, ShipAPI ship, boolean enemy, int level, float value) {
        if (enemy || ship.isFighter() || ship.hasListenerOfClass(Crit.class)) return;
        ship.addListener(new Crit(ship, RuleEffect.modId(rule), value / 100f));
    }

    private static final class Crit implements DamageDealtModifier {
        private final ShipAPI ship;
        private final String id;
        private final float chance;

        Crit(ShipAPI ship, String id, float chance) {
            this.ship = ship;
            this.id = id;
            this.chance = chance;
        }

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            CombatEngineAPI engine = Global.getCombatEngine();
            if (engine == null || engine.getPlayerShip() != ship || !Tools.roll(chance)) return null;
            if (damage.getDamage() >= 100f && point != null) {
                engine.addFloatingText(point, "Critical hit", 30f, Color.red, target, 1f, 0f);
            }
            damage.getModifier().modifyMult(id, 2f);
            return id;
        }
    }
}
