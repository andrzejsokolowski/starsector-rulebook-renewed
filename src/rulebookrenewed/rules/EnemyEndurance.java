package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Enemy ships keep peak performance longer (hard), or lose it sooner and then lose combat
 * readiness faster (helpful). The helpful side is adapted from Mental Warfare in Unofficial Rule Book
 * by DemonX3 (MIT licence, see CREDITS.txt).
 */
public final class EnemyEndurance extends CombatStatRule {

    @Override
    protected boolean targetsEnemy() {
        return true;
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign) {
        Scale.by(stats.getPeakCRDuration(), id, value, sign, null);
        if (sign < 0f) Scale.by(stats.getCRLossPerSecondPercent(), id, value, 1f, null);
        else stats.getCRLossPerSecondPercent().unmodify(id);
    }
}
