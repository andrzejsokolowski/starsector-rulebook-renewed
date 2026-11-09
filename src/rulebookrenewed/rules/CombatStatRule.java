package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import rulebookrenewed.RuleSpec;

/** A rule that changes the stats of every ship on one side of a campaign battle. */
public abstract class CombatStatRule implements RuleEffect {

    /** True for the enemy side, false for the player's ships and allies. */
    protected abstract boolean targetsEnemy();

    protected abstract void modify(MutableShipStatsAPI stats, String id, float value);

    @Override
    public void applyToShip(RuleSpec rule, ShipAPI ship, boolean enemy, int level, float value) {
        if (enemy != targetsEnemy()) return;
        modify(ship.getMutableStats(), RuleEffect.modId(rule), value);
    }
}
