package rulebookrenewed.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

import rulebookrenewed.RuleSpec;

/** Helpful challenge reward: lower tariffs at every market not the player's, more income at the player's own. */
public final class MerchantPrince implements RuleEffect {

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        String id = RuleEffect.modId(rule);
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market.isPlayerOwned()) {
                market.getTariff().unmodify(id);
                market.getIncomeMult().modifyMult(id, Scale.mult(value, 1f), rule.name);
            } else {
                market.getIncomeMult().unmodify(id);
                Tariffs.change(market, id, -value / 100f, rule.name);
            }
        }
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        String id = RuleEffect.modId(rule);
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            market.getTariff().unmodify(id);
            market.getIncomeMult().unmodify(id);
        }
    }
}
