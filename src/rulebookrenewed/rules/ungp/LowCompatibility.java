package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

import rulebookrenewed.rules.CombatStatRule;
import rulebookrenewed.rules.Scale;

/**
 * Hard: weapons and engines of the player's ships and allies take more damage and take longer to
 * repair in combat. Adapted from Low Compatibility in Unofficial New Game Plus by Originem.
 */
public final class LowCompatibility extends CombatStatRule {

    @Override
    protected boolean targetsEnemy() {
        return false;
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign) {
        float mult = Scale.mult(value, -sign);
        stats.getCombatEngineRepairTimeMult().modifyMult(id, mult);
        stats.getCombatWeaponRepairTimeMult().modifyMult(id, mult);
        stats.getEngineDamageTakenMult().modifyMult(id, mult);
        stats.getWeaponDamageTakenMult().modifyMult(id, mult);
    }
}
