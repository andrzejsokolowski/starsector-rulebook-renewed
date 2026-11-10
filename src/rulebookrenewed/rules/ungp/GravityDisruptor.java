package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.combat.ShipAPI;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Golden: time runs faster for the player's ships and allies and slower for enemy ships, fighters
 * included. Adapted from Gravity Disruptor in Unofficial New Game Plus by Originem (whose code sped the
 * enemy up as well; this follows its description).
 */
public final class GravityDisruptor implements RuleEffect {

    @Override
    public void applyToShip(RuleSpec rule, ShipAPI ship, boolean enemy, int level, float value) {
        float mult = enemy ? Math.max(0.05f, 1f - value / 100f) : 1f + value / 100f;
        ship.getMutableStats().getTimeMult().modifyMult(RuleEffect.modId(rule), mult);
    }
}
