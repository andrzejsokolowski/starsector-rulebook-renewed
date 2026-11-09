package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Deploying ships of the player's fleet costs less (helpful) or more (hard) combat readiness, and so
 * less or more supplies to recover.
 */
public final class PlayerDeployCost extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        Scale.by(stats.getCRPerDeploymentPercent(), id, value, -sign, source);
        Scale.by(stats.getSuppliesToRecover(), id, value, -sign, source);
    }
}
