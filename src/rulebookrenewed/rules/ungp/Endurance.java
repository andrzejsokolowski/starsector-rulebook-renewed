package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.RepairTrackerAPI;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Helpful: a ship of the player's fleet whose combat readiness is below half of its maximum gets back
 * the readiness one deployment costs it (at most half of its maximum), then waits out a cooldown, kept
 * as a buff on the ship. Mothballed ships are left alone. Adapted from Endurance in Unofficial New Game
 * Plus by Originem.
 */
public final class Endurance implements RuleEffect {

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return;
        String id = RuleEffect.modId(rule);
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (member.isMothballed() || member.getBuffManager().getBuff(id) != null) continue;
            RepairTrackerAPI repairs = member.getRepairTracker();
            if (repairs.getCR() >= repairs.getMaxCR() * 0.5f) continue;
            float gain = member.getStats().getCRPerDeploymentPercent()
                    .computeEffective(member.getHullSpec().getCRToDeploy()) / 100f;
            gain = Math.min(gain, repairs.getMaxCR() * 0.5f);
            if (gain <= 0f) continue;
            repairs.applyCREvent(gain, id, rule.name);
            member.getBuffManager().addBuffOnlyUpdateStat(new CooldownBuff(id, value));
        }
    }
}
