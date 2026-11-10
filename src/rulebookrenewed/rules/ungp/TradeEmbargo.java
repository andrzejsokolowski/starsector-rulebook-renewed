package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

import rulebookrenewed.RuleSpec;

/**
 * Hard: every hostile market within 15 light-years of one of the player's colonies lowers that colony's
 * accessibility. Adapted from Trade Embargo in Unofficial New Game Plus by Originem.
 */
public final class TradeEmbargo extends HostileNeighbours {

    @Override
    protected void modify(MarketAPI market, String id, float value, RuleSpec rule) {
        int hostile = hostileNearby(market);
        if (hostile <= 0) market.getAccessibilityMod().unmodify(id);
        else market.getAccessibilityMod().modifyFlat(id, -hostile * value / 100f, rule.name);
    }

    @Override
    protected void clear(MarketAPI market, String id) {
        market.getAccessibilityMod().unmodify(id);
    }
}
