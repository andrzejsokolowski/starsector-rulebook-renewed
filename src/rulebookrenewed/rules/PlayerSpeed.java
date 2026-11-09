package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Ships in the player's fleet have a higher (helpful) or lower (hard) top speed in combat.
 */
public final class PlayerSpeed extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        stats.getMaxSpeed().modifyPercent(id, Math.max(-95f, sign * value), source);
    }
}
