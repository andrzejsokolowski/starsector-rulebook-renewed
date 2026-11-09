package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Ships in the player's fleet have more (helpful) or less (hard) flux capacity and dissipation.
 */
public final class PlayerFlux extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        Scale.by(stats.getFluxCapacity(), id, value, sign, source);
        Scale.by(stats.getFluxDissipation(), id, value, sign, source);
    }
}
