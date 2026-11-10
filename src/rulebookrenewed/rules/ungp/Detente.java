package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import rulebookrenewed.RuleSpec;

/**
 * Hard: every ship in the player's fleet above 15 raises the maintenance supply use of all its ships.
 * Adapted from The Detente in Unofficial New Game Plus by Originem (there the limit moved with the
 * difficulty; here the percent per ship does).
 */
public final class Detente extends ConditionalMemberRule {

    private static final int SHIP_LIMIT = 15;

    @Override
    protected float fleetFactor(RuleSpec rule, CampaignFleetAPI fleet, int level, float value) {
        return Math.max(0, fleet.getNumMembersFast() - SHIP_LIMIT) * value;
    }

    @Override
    protected void modify(FleetMemberAPI member, String id, float strength, RuleSpec rule) {
        member.getStats().getSuppliesPerMonth().modifyPercent(id, strength, rule.name);
    }
}
