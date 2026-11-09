package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Helpful: every ship in the player's fleet has a higher maximum burn level.
 * Effect adapted from Hyperspace Drifter in Unofficial Rule Book by DemonX3 (MIT licence, see CREDITS.txt).
 */
public final class HyperspaceDrifter extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, String source) {
        stats.getMaxBurnLevel().modifyFlat(id, value, source);
    }
}
