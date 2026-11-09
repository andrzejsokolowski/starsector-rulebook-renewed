package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Enemy ships and fighters have a higher (hard) or lower (helpful) top speed.
 */
public final class EnemySpeed extends CombatStatRule {

    @Override
    protected boolean targetsEnemy() {
        return true;
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign) {
        stats.getMaxSpeed().modifyPercent(id, Math.max(-95f, sign * value));
    }
}
