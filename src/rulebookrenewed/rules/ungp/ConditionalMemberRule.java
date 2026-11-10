package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BuffManagerAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * A rule that changes ships of the player's fleet only while a condition holds, or by an amount that
 * moves with the fleet: the flagship only, in hyperspace only, by the number of ships. Each ship gets a
 * strength; a ship's stats are rebuilt when its strength changes. Checked twice a second on the campaign
 * map and on every rule refresh.
 */
public abstract class ConditionalMemberRule implements RuleEffect {

    private static final float CHECK_SECONDS = 0.5f;

    private float sinceCheck;

    /** A number for the whole fleet, worked out once per pass and handed to strength(). */
    protected float fleetFactor(RuleSpec rule, CampaignFleetAPI fleet, int level, float value) {
        return value;
    }

    /** How strongly the rule acts on this ship now; 0 for not at all. */
    protected float strength(RuleSpec rule, FleetMemberAPI member, float fleetFactor) {
        return fleetFactor;
    }

    /** Called by the buff whenever the ship's stats are built. */
    protected abstract void modify(FleetMemberAPI member, String id, float strength, RuleSpec rule);

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        sync(rule, level, value);
    }

    @Override
    public void advanceCampaign(RuleSpec rule, float amount, int level, float value) {
        sinceCheck += amount;
        if (sinceCheck < CHECK_SECONDS) return;
        sinceCheck = 0f;
        sync(rule, level, value);
    }

    private void sync(RuleSpec rule, int level, float value) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return;
        String id = RuleEffect.modId(rule);
        float factor = fleetFactor(rule, fleet, level, value);
        boolean changed = false;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            float strength = factor == 0f ? 0f : strength(rule, member, factor);
            BuffManagerAPI buffs = member.getBuffManager();
            BuffManagerAPI.Buff existing = buffs.getBuff(id);
            if (strength == 0f) {
                if (existing == null) continue;
                buffs.removeBuff(id);
            } else {
                if (existing instanceof ConditionalMemberBuff && ((ConditionalMemberBuff) existing).strength == strength) {
                    ((ConditionalMemberBuff) existing).refresh();
                    continue;
                }
                if (existing != null) buffs.removeBuff(id);
                buffs.addBuff(new ConditionalMemberBuff(rule.id, id, strength));
            }
            member.updateStats();
            changed = true;
        }
        if (changed) {
            fleet.getFleetData().setSyncNeeded();
            fleet.getFleetData().syncIfNeeded();
        }
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
        if (changed) {
            fleet.getFleetData().setSyncNeeded();
            fleet.getFleetData().syncIfNeeded();
        }
    }
}
