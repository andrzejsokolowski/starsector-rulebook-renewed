package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import rulebookrenewed.RuleSpec;

/**
 * Helpful: crew beyond the minimum the player's ships need raises their maximum combat readiness: the
 * full value at twice the minimum crew, in steps of half a percent. Adapted from Elite Reserves in
 * Unofficial New Game Plus by Originem.
 */
public final class EliteReserves extends ConditionalMemberRule {

    @Override
    protected float fleetFactor(RuleSpec rule, CampaignFleetAPI fleet, int level, float value) {
        float needed = fleet.getFleetData().getMinCrew();
        if (needed <= 0f) return 0f;
        float spare = Math.min(1f, Math.max(0f, (fleet.getCargo().getCrew() - needed) / needed));
        return Math.round(spare * value * 2f) / 2f;
    }

    @Override
    protected void modify(FleetMemberAPI member, String id, float strength, RuleSpec rule) {
        member.getStats().getMaxCombatReadiness().modifyFlat(id, strength / 100f, rule.name);
    }
}
