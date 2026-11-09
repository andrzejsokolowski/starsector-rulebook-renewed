package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Ships in the player's fleet have more (helpful) or less (hard) hull, a percent of their own.
 */
public final class PlayerHull extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        Scale.by(stats.getHullBonus(), id, value, sign, source);
    }
}
