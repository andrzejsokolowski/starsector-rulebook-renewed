package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Helpful: every day the player's fleet gains Volturnian Lobster, as long as there is more than one
 * unit of cargo space left. Adapted from Premium Seafood in Unofficial New Game Plus by Originem.
 */
public final class PremiumSeafood implements RuleEffect {

    private static final String DAY_KEY = "rbr_seafoodDay";

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        if (Tools.daysSince(DAY_KEY) < 1f) return;
        Tools.restart(DAY_KEY);
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet != null && fleet.getCargo().getSpaceLeft() > 1f) fleet.getCargo().addCommodity(Commodities.LOBSTER, value);
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        Tools.forget(DAY_KEY);
    }
}
