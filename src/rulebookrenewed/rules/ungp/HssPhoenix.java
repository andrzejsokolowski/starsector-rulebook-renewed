package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import rulebookrenewed.RuleSpec;

/**
 * Golden: while the player's flagship is an Onslaught (any hull whose id contains "onslaught", or with
 * the tag rbr_onslaught or ungp_onslaught), it has more hull, armor and flux, twice the speed and
 * maneuverability and more maximum combat readiness. Adapted from HSS Phoenix in Unofficial New Game
 * Plus by Originem.
 */
public final class HssPhoenix extends ConditionalMemberRule {

    private static final float MANEUVER_PERCENT = 100f;
    private static final float CR_BONUS = 0.1f;

    @Override
    protected float strength(RuleSpec rule, FleetMemberAPI member, float fleetFactor) {
        return member.isFlagship() && isOnslaught(member.getHullSpec()) ? fleetFactor : 0f;
    }

    @Override
    protected void modify(FleetMemberAPI member, String id, float strength, RuleSpec rule) {
        MutableShipStatsAPI stats = member.getStats();
        stats.getHullBonus().modifyPercent(id, strength);
        stats.getArmorBonus().modifyPercent(id, strength);
        stats.getFluxCapacity().modifyPercent(id, strength);
        stats.getFluxDissipation().modifyPercent(id, strength);
        stats.getMaxSpeed().modifyPercent(id, MANEUVER_PERCENT);
        stats.getAcceleration().modifyPercent(id, MANEUVER_PERCENT);
        stats.getDeceleration().modifyPercent(id, MANEUVER_PERCENT);
        stats.getMaxTurnRate().modifyPercent(id, MANEUVER_PERCENT);
        stats.getTurnAcceleration().modifyPercent(id, MANEUVER_PERCENT);
        stats.getMaxCombatReadiness().modifyFlat(id, CR_BONUS, rule.name);
    }

    private static boolean isOnslaught(ShipHullSpecAPI hull) {
        if (hull == null) return false;
        return hull.getHullId().contains("onslaught") || hull.getTags().contains("rbr_onslaught")
                || hull.getTags().contains("ungp_onslaught");
    }
}
