package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Hard: at the start of every month the player loses a percent of their credits. Adapted from Inflation
 * in Unofficial New Game Plus by Originem.
 */
public final class Inflation implements RuleEffect {

    private static final String MONTH_KEY = "rbr_inflationMonth";

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        if (!Tools.monthPassed(MONTH_KEY)) return;
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return;
        int lost = (int) (fleet.getCargo().getCredits().get() * value / 100f);
        if (lost <= 0) return;
        fleet.getCargo().getCredits().subtract(lost);
        Tools.note(rule.name + ": " + Misc.getDGSCredits(lost) + " lost this month.", Misc.getNegativeHighlightColor());
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        Tools.forget(MONTH_KEY);
    }
}
