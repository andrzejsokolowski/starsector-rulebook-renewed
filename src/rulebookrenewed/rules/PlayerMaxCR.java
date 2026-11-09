package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Ships in the player's fleet have more (helpful) or less (hard) maximum combat readiness.
 */
public final class PlayerMaxCR extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        stats.getMaxCombatReadiness().modifyFlat(id, sign * value / 100f, source);
    }
}
