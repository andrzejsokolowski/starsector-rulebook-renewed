package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Enemy weapons fire faster (hard) or slower (helpful).
 */
public final class EnemyRateOfFire extends CombatStatRule {

    @Override
    protected boolean targetsEnemy() {
        return true;
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign) {
        Scale.by(stats.getBallisticRoFMult(), id, value, sign, null);
        Scale.by(stats.getEnergyRoFMult(), id, value, sign, null);
        Scale.by(stats.getMissileRoFMult(), id, value, sign, null);
    }
}
