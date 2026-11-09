package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Ships in the player's fleet repair hull and recover combat readiness faster (helpful) or slower (hard).
 */
public final class PlayerRepairs extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        Scale.by(stats.getRepairRatePercentPerDay(), id, value, sign, source);
        Scale.by(stats.getBaseCRRecoveryRatePercentPerDay(), id, value, sign, source);
    }
}
