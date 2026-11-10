package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

import rulebookrenewed.rules.CombatStatRule;
import rulebookrenewed.rules.Scale;

/**
 * Hard: missiles of enemy ships fly faster and turn better, both by the
 * rule's value (there the turning rose by less). Adapted from
 * Explosive Sniper in Unofficial New Game Plus by Originem.
 */
public final class ExplosiveSniper extends CombatStatRule {

    @Override
    protected boolean targetsEnemy() {
        return true;
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign) {
        Scale.by(stats.getMissileMaxSpeedBonus(), id, value, sign, null);
        Scale.by(stats.getMissileAccelerationBonus(), id, value, sign, null);
        Scale.by(stats.getMissileMaxTurnRateBonus(), id, value, sign, null);
        Scale.by(stats.getMissileTurnAccelerationBonus(), id, value, sign, null);
    }
}
