package rulebookrenewed.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import rulebookrenewed.RuleSpec;

/**
 * Hard: every mothballed ship lowers the maximum combat readiness of every ship in the player's fleet.
 * Adapted from Pestilence in Unofficial Rule Book by DemonX3 (MIT licence, see CREDITS.txt).
 */
public final class DeadWeight extends MemberStatRule {

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return;
        int mothballed = 0;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (member.isMothballed()) mothballed++;
        }
        super.applyCampaign(rule, level, value * mothballed);
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        if (value <= 0f) stats.getMaxCombatReadiness().unmodify(id);
        else stats.getMaxCombatReadiness().modifyFlat(id, -value / 100f, source);
    }
}
