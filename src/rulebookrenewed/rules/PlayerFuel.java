package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Ships in the player's fleet use less (helpful) or more (hard) fuel.
 */
public final class PlayerFuel extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        Scale.by(stats.getFuelUseMod(), id, value, -sign, source);
    }
}
