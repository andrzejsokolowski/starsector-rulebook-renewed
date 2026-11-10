package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

import rulebookrenewed.RuleSpec;

/**
 * Hard: every hostile market within 15 light-years of one of the player's colonies lowers that colony's
 * stability. Adapted from Spy in Unofficial New Game Plus by Originem.
 */
public final class Spy extends HostileNeighbours {

    @Override
    protected void modify(MarketAPI market, String id, float value, RuleSpec rule) {
        int hostile = hostileNearby(market);
        if (hostile <= 0) market.getStability().unmodify(id);
        else market.getStability().modifyFlat(id, -hostile * value, rule.name);
    }

    @Override
    protected void clear(MarketAPI market, String id) {
        market.getStability().unmodify(id);
    }
}
