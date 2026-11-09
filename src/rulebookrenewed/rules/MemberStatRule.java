package rulebookrenewed.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BuffManagerAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import rulebookrenewed.RuleSpec;

/** A rule that changes a stat on every ship in the player's fleet, through a RuleMemberBuff. */
public abstract class MemberStatRule implements RuleEffect {

    protected abstract void modify(MutableShipStatsAPI stats, String id, float value, String source);

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return;
        String id = RuleEffect.modId(rule);
        boolean changed = false;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            BuffManagerAPI buffs = member.getBuffManager();
            BuffManagerAPI.Buff existing = buffs.getBuff(id);
            if (existing instanceof RuleMemberBuff) {
                RuleMemberBuff buff = (RuleMemberBuff) existing;
                if (buff.level == level && buff.value == value) {
                    buff.refresh();
                    continue;
                }
            }
            if (existing != null) buffs.removeBuff(id);
            buffs.addBuff(new RuleMemberBuff(rule.id, id, level, value));
            member.updateStats();
            changed = true;
        }
        if (changed) sync(fleet);
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return;
        String id = RuleEffect.modId(rule);
        boolean changed = false;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (member.getBuffManager().getBuff(id) == null) continue;
            member.getBuffManager().removeBuff(id);
            member.updateStats();
            changed = true;
        }
        if (changed) sync(fleet);
    }

    @Override
    public void applyToMember(RuleSpec rule, FleetMemberAPI member, int level, float value) {
        modify(member.getStats(), RuleEffect.modId(rule), value, rule.name);
    }

    private static void sync(CampaignFleetAPI fleet) {
        fleet.getFleetData().setSyncNeeded();
        fleet.getFleetData().syncIfNeeded();
    }
}
