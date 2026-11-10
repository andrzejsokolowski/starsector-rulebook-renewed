package rulebookrenewed.rules.ungp;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import rulebookrenewed.RuleSpec;

/**
 * Helpful: once the player's fleet has not moved for a day, its ships repair faster and use fewer
 * supplies for maintenance, until it moves again. Adapted from Bustling Fungus in Unofficial New Game
 * Plus by Originem.
 */
public final class BustlingFungus extends ConditionalMemberRule {

    private static final float WAIT_DAYS = 1f;
    private static final Color NOTICE = new Color(0, 213, 140);

    private float stillDays;
    private boolean refitting;

    @Override
    public void advanceCampaign(RuleSpec rule, float amount, int level, float value) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet != null) {
            if (fleet.getCurrBurnLevel() < 0.01f) stillDays += Global.getSector().getClock().convertToDays(amount);
            else stillDays = 0f;
            boolean now = stillDays >= WAIT_DAYS;
            if (now && !refitting) {
                fleet.addFloatingText("Refitting", NOTICE, 1f);
                Tools.uiSound("rbr_fungus");
            }
            refitting = now;
        }
        super.advanceCampaign(rule, amount, level, value);
    }

    @Override
    protected float fleetFactor(RuleSpec rule, CampaignFleetAPI fleet, int level, float value) {
        return stillDays >= WAIT_DAYS ? value : 0f;
    }

    @Override
    protected void modify(FleetMemberAPI member, String id, float strength, RuleSpec rule) {
        MutableShipStatsAPI stats = member.getStats();
        stats.getRepairRatePercentPerDay().modifyPercent(id, strength);
        stats.getSuppliesPerMonth().modifyMult(id, Math.max(0.05f, 1f - strength / 100f), rule.name);
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        stillDays = 0f;
        refitting = false;
        super.unapplyCampaign(rule);
    }
}
