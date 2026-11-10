package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

import rulebookrenewed.RuleSpec;

/**
 * Hard: while the player's fleet is in a star system with the Remnant theme, its ships have less
 * maximum combat readiness. Adapted from Ancient Threat in Unofficial New Game Plus by Originem.
 */
public final class AncientThreat extends ConditionalMemberRule {

    @Override
    protected float fleetFactor(RuleSpec rule, CampaignFleetAPI fleet, int level, float value) {
        return fleet.getContainingLocation() != null && fleet.getContainingLocation().hasTag(Tags.THEME_REMNANT) ? value : 0f;
    }

    @Override
    protected void modify(FleetMemberAPI member, String id, float strength, RuleSpec rule) {
        member.getStats().getMaxCombatReadiness().modifyFlat(id, -strength / 100f, rule.name);
    }
}
