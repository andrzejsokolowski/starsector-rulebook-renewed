package rulebookrenewed.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;

import rulebookrenewed.RuleSpec;

/** A rule that changes a stat of the player's fleet as a whole on the campaign map (sensors, salvage). */
public abstract class FleetStatRule implements RuleEffect {

    /** sign: +1 when the rule helps the player, -1 when it hurts. */
    protected abstract void modify(MutableFleetStatsAPI stats, String id, float value, float sign, String source);

    protected abstract void clear(MutableFleetStatsAPI stats, String id);

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet != null) modify(fleet.getStats(), RuleEffect.modId(rule), value, RuleEffect.playerSign(rule), rule.name);
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet != null) clear(fleet.getStats(), RuleEffect.modId(rule));
    }
}
