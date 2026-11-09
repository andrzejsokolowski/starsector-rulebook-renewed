package rulebookrenewed.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

import rulebookrenewed.RuleSpec;

/**
 * A rule that changes markets: either the player's own colonies or every market that is not the player's.
 * Refreshed every few hours of game time, so a market that changes hands moves to the other group.
 */
public abstract class MarketRule implements RuleEffect {

    /** True for the player's colonies, false for every other market. */
    protected abstract boolean playerMarkets();

    protected abstract void modify(MarketAPI market, String id, float value, RuleSpec rule);

    protected abstract void clear(MarketAPI market, String id);

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        String id = RuleEffect.modId(rule);
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market.isPlayerOwned() == playerMarkets()) modify(market, id, value, rule);
            else clear(market, id);
        }
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        String id = RuleEffect.modId(rule);
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) clear(market, id);
    }
}
