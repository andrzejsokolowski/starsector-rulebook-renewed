package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

/**
 * D-mods on ships in the player's fleet hurt less (helpful) or more (hard).
 * The hard side is adapted from Reaper's Tax in Unofficial Rule Book by DemonX3 (MIT licence, see CREDITS.txt).
 */
public final class PlayerDModEffect extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        Scale.by(stats.getDynamic().getStat(Stats.DMOD_EFFECT_MULT), id, value, -sign, source);
    }
}
