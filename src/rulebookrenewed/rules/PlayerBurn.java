package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Ships in the player's fleet have a higher (helpful) or lower (hard) maximum burn level.
 * Effect adapted from Hyperspace Drifter in Unofficial Rule Book by DemonX3 (MIT licence, see CREDITS.txt).
 */
public final class PlayerBurn extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        stats.getMaxBurnLevel().modifyFlat(id, sign * value, source);
    }
}
