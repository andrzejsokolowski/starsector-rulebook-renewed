package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import rulebookrenewed.rules.MemberStatRule;

/**
 * Helpful: ships of the player's fleet can carry more logistics hullmods. Adapted from Logistics
 * Overhaul in Unofficial New Game Plus by Originem.
 */
public final class LogisticsOverhaul extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        stats.getDynamic().getMod(Stats.MAX_LOGISTICS_HULLMODS_MOD).modifyFlat(id, sign * value);
    }
}
