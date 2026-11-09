package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import rulebookrenewed.RuleSpec;

/**
 * A rule that changes the stats of every ship and fighter on one side of a campaign battle, on the
 * first frame it is on the field. Only for stats the game reads live (damage, speed, range, overload).
 * Armor and hull are fixed when a ship is built, so they go through EnemyMemberStatRule instead.
 */
public abstract class CombatStatRule implements RuleEffect {

    /** True for the enemy side, false for the player's ships and allies. */
    protected abstract boolean targetsEnemy();

    /** sign: +1 pushes the stat in favour of the side it targets, -1 against it. */
    protected abstract void modify(MutableShipStatsAPI stats, String id, float value, float sign);

    @Override
    public void applyToShip(RuleSpec rule, ShipAPI ship, boolean enemy, int level, float value) {
        if (enemy != targetsEnemy()) return;
        float sign = targetsEnemy() ? RuleEffect.enemySign(rule) : RuleEffect.playerSign(rule);
        modify(ship.getMutableStats(), RuleEffect.modId(rule), value, sign);
    }
}
