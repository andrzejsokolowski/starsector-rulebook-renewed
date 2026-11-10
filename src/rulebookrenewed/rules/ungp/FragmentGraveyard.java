package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Hard: when a ship is destroyed its wreck flies off several times faster and heavier (the value, and
 * one less for the mass), and every ship is twice as likely to break apart. Each wreck is changed once;
 * the pieces it breaks into get the mass only. Adapted from Fragment Graveyard in Unofficial New Game Plus by Originem.
 */
public final class FragmentGraveyard implements RuleEffect {

    private static final float BREAK_PERCENT = 100f;

    @Override
    public void applyToShip(RuleSpec rule, ShipAPI ship, boolean enemy, int level, float value) {
        ship.getMutableStats().getBreakProb().modifyPercent(RuleEffect.modId(rule), BREAK_PERCENT);
    }

    @Override
    public void advanceCombat(RuleSpec rule, CombatEngineAPI engine, float amount, int level, float value) {
        String id = RuleEffect.modId(rule);
        for (ShipAPI ship : engine.getShips()) {
            if (!ship.isHulk() || ship.getCustomData().containsKey(id)) continue;
            ship.setCustomData(id, Boolean.TRUE);
            // A piece keeps the speed of the wreck it broke from, which was already raised.
            if (!ship.isPiece()) ship.getVelocity().scale(value);
            ship.setMass(ship.getMass() * Math.max(1f, value - 1f));
        }
    }
}
