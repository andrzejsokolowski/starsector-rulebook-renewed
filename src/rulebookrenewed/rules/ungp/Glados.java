package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.MarketRule;

/**
 * Helpful: at the player's colonies, every industry and structure run by an Alpha Core produces more
 * of each commodity it supplies. Adapted from GLaDos in Unofficial New Game Plus by Originem.
 */
public final class Glados extends MarketRule {

    @Override
    protected boolean playerMarkets() {
        return true;
    }

    @Override
    protected void modify(MarketAPI market, String id, float value, RuleSpec rule) {
        for (Industry industry : market.getIndustries()) {
            boolean alpha = Commodities.ALPHA_CORE.equals(industry.getAICoreId());
            for (MutableCommodityQuantity supply : industry.getAllSupply()) {
                if (alpha) supply.getQuantity().modifyFlat(id, value, rule.name);
                else supply.getQuantity().unmodify(id);
            }
        }
    }

    @Override
    protected void clear(MarketAPI market, String id) {
        for (Industry industry : market.getIndustries()) {
            for (MutableCommodityQuantity supply : industry.getAllSupply()) supply.getQuantity().unmodify(id);
        }
    }
}
