package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Ships in the player's fleet keep peak performance longer (helpful) or shorter (hard).
 */
public final class PlayerEndurance extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        Scale.by(stats.getPeakCRDuration(), id, value, sign, source);
    }
}
