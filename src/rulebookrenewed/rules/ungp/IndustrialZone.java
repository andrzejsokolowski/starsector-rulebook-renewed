package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.MarketRule;

/**
 * Helpful: the player's colonies can hold more industries. A colony of size 5 or smaller that has more
 * industries than it could without the rule pays for it: each of its industries produces 1 less of
 * every commodity. Adapted from Industrial Zone in Unofficial New Game Plus by Originem.
 */
public final class IndustrialZone extends MarketRule {

    private static final int SMALL_COLONY = 5;

    @Override
    protected boolean playerMarkets() {
        return true;
    }

    @Override
    protected void modify(MarketAPI market, String id, float value, RuleSpec rule) {
        market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).unmodify(id);
        boolean crowded = market.getSize() <= SMALL_COLONY && Misc.getNumIndustries(market) > Misc.getMaxIndustries(market);
        for (Industry industry : market.getIndustries()) {
            if (!industry.isIndustry()) continue;
            for (MutableCommodityQuantity supply : allLines(industry)) {
                if (crowded) supply.getQuantity().modifyFlat(id, -1f, rule.name);
                else supply.getQuantity().unmodify(id);
            }
        }
        market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).modifyFlat(id, value);
    }

    @Override
    protected void clear(MarketAPI market, String id) {
        market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).unmodify(id);
        for (Industry industry : market.getIndustries()) {
            for (MutableCommodityQuantity supply : allLines(industry)) supply.getQuantity().unmodify(id);
        }
    }

    /**
     * Every supply line, including the ones at 0. getAllSupply skips lines at or below 0, and the -1 can
     * bring a line to exactly 0, so removing it through getAllSupply would leave that line at -1.
     */
    private static Iterable<MutableCommodityQuantity> allLines(Industry industry) {
        if (industry instanceof BaseIndustry) return ((BaseIndustry) industry).getSupply().values();
        return industry.getAllSupply();
    }
}
