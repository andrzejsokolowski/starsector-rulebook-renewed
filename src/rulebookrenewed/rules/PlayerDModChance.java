package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

/**
 * Disabled ships in the player's fleet are less (helpful) or more (hard) likely to be destroyed
 * outright and to gain new D-mods. The hard side is adapted from Brittle Framing in
 * Unofficial Rule Book by DemonX3 (MIT licence, see CREDITS.txt).
 */
public final class PlayerDModChance extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        Scale.by(stats.getBreakProb(), id, value, -sign, source);
        Scale.by(stats.getDynamic().getMod(Stats.DMOD_ACQUIRE_PROB_MOD), id, value, -sign, source);
    }
}
