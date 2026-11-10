package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SubmarketPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Helpful: black market trade leaves no suspicion. For every market in the star system the player's
 * fleet is in, the record of what the player traded on its black market (and on submarkets of a faction
 * hostile to the market's owner) is set back to zero, and so is the market's extra suspicion. Done once
 * a second and on every rule refresh. Adapted from Black in Black in Unofficial New Game Plus by
 * Originem (which did this for every market in the Sector on each economy step).
 */
public final class BlackInBlack implements RuleEffect {

    private float sinceCheck;

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        clearSuspicion();
    }

    @Override
    public void advanceCampaign(RuleSpec rule, float amount, int level, float value) {
        sinceCheck += amount;
        if (sinceCheck < 1f) return;
        sinceCheck = 0f;
        clearSuspicion();
    }

    private static void clearSuspicion() {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null || fleet.getContainingLocation() == null) return;
        for (MarketAPI market : Misc.getMarketsInLocation(fleet.getContainingLocation())) {
            for (SubmarketAPI submarket : market.getSubmarketsCopy()) {
                SubmarketPlugin plugin = submarket.getPlugin();
                if (plugin == null || !plugin.isParticipatesInEconomy()) continue;
                boolean shady = plugin.isBlackMarket()
                        || (submarket.getFaction() != null && submarket.getFaction().isHostileTo(market.getFaction()));
                if (shady) SharedData.getData().getPlayerActivityTracker().getPlayerTradeData(submarket).setTotalPlayerTradeValue(0f);
            }
            market.getMemoryWithoutUpdate().set(MemFlags.MARKET_EXTRA_SUSPICION, 0f);
        }
    }
}
