package rulebookrenewed.rules.ungp;

import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;

import rulebookrenewed.RuleSpec;

/**
 * Hard: with more than 2 design types among the combat ships of the player's fleet (mothballed and
 * civilian ships do not count), every ship loses maximum combat readiness: the rule's value times
 * (1 - 2 / design types). Adapted from Arms Limitation Treaty in Unofficial New Game Plus by Originem.
 */
public final class ArmsTreaty extends ConditionalMemberRule {

    private static final int FREE_TYPES = 2;

    @Override
    protected float fleetFactor(RuleSpec rule, CampaignFleetAPI fleet, int level, float value) {
        Set<String> types = new HashSet<>();
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (member.isMothballed() || BaseSkillEffectDescription.isCivilian(member)) continue;
            types.add(member.getHullSpec().getManufacturer());
        }
        if (types.size() <= FREE_TYPES) return 0f;
        return value * (1f - (float) FREE_TYPES / types.size());
    }

    @Override
    protected void modify(FleetMemberAPI member, String id, float strength, RuleSpec rule) {
        member.getStats().getMaxCombatReadiness().modifyFlat(id, -strength / 100f, rule.name);
    }
}
