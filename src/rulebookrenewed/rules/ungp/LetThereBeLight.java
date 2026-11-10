package rulebookrenewed.rules.ungp;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Helpful: beams of the ship the player flies deal more damage to shields. Every ship of the player's
 * own gets the listener, which acts only while the player flies that ship. Adapted from Let there be
 * Light! in Unofficial New Game Plus by Originem.
 */
public final class LetThereBeLight implements RuleEffect {

    @Override
    public void applyToShip(RuleSpec rule, ShipAPI ship, boolean enemy, int level, float value) {
        if (enemy || ship.isFighter() || ship.hasListenerOfClass(Beams.class)) return;
        ship.addListener(new Beams(ship, RuleEffect.modId(rule), value));
    }

    private static final class Beams implements DamageDealtModifier {
        private final ShipAPI ship;
        private final String id;
        private final float percent;

        Beams(ShipAPI ship, String id, float percent) {
            this.ship = ship;
            this.id = id;
            this.percent = percent;
        }

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (!shieldHit || !(param instanceof BeamAPI)) return null;
            CombatEngineAPI engine = Global.getCombatEngine();
            if (engine == null || engine.getPlayerShip() != ship) return null;
            damage.getModifier().modifyPercent(id, percent);
            return id;
        }
    }
}
