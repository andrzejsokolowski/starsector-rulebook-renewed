package rulebookrenewed.rules;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.ungp.ConditionalMemberRule;

/**
 * Helpful: while the player's fleet is in a star corona or a black hole's event horizon, its ships lose
 * less combat readiness to it and, in battles fought there, are more maneuverable and faster (more for
 * smaller hulls). Adapted from Wish Upon a Star in Unofficial Rule Book by DemonX3 (MIT licence, see
 * CREDITS.txt).
 */
public final class WishUponAStar extends ConditionalMemberRule {

    private static final float CORONA_LOSS_CUT = 40f;

    @Override
    protected float fleetFactor(RuleSpec rule, CampaignFleetAPI fleet, int level, float value) {
        LocationAPI where = fleet.getContainingLocation();
        if (where == null) return 0f;
        for (CampaignTerrainAPI terrain : where.getTerrainCopy()) {
            String type = terrain.getType();
            if (!"corona".equals(type) && !"event_horizon".equals(type)) continue;
            if (terrain.getPlugin() != null && terrain.getPlugin().containsEntity(fleet)) return value;
        }
        return 0f;
    }

    @Override
    protected void modify(FleetMemberAPI member, String id, float strength, RuleSpec rule) {
        MutableShipStatsAPI stats = member.getStats();
        stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, 1f - CORONA_LOSS_CUT / 100f);
        stats.getAcceleration().modifyPercent(id, strength);
        stats.getDeceleration().modifyPercent(id, strength);
        stats.getTurnAcceleration().modifyPercent(id, strength);
        stats.getMaxTurnRate().modifyPercent(id, strength);
        stats.getMaxSpeed().modifyFlat(id, speed(member));
    }

    private static float speed(FleetMemberAPI member) {
        switch (member.getHullSpec().getHullSize()) {
            case FRIGATE: return 60f;
            case DESTROYER: return 50f;
            case CRUISER: return 30f;
            case CAPITAL_SHIP: return 20f;
            default: return 0f;
        }
    }
}
