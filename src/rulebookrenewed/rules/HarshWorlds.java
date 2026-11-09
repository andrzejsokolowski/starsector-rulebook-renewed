package rulebookrenewed.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

import rulebookrenewed.RuleSpec;

/**
 * Hard: the player's colonies have a higher hazard rating.
 * Effect adapted from Grief of Demeter in Unofficial Rule Book by DemonX3 (MIT licence, see CREDITS.txt).
 */
public final class HarshWorlds implements RuleEffect {

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        String id = RuleEffect.modId(rule);
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market.isPlayerOwned()) market.getHazard().modifyFlat(id, value / 100f, rule.name);
            else market.getHazard().unmodifyFlat(id);
        }
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        String id = RuleEffect.modId(rule);
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            market.getHazard().unmodifyFlat(id);
        }
    }
}
