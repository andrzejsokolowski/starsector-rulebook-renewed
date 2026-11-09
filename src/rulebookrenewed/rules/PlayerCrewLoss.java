package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Ships in the player's fleet lose fewer (helpful) or more (hard) crew to hull damage.
 * The hard side is adapted from War Time War Crime in Unofficial Rule Book by DemonX3 (MIT licence, see CREDITS.txt).
 */
public final class PlayerCrewLoss extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        Scale.by(stats.getCrewLossMult(), id, value, -sign, source);
    }
}
