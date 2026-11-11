package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.ungp.ConditionalMemberRule;

/**
 * Golden, helpful: while the player's flagship is a Conquest (any hull whose id contains "conquest", or one
 * tagged ungpdx_conquest by another mod), its system recharges twice as fast, it vents 50% faster, its
 * weapons reach 30% farther and its maximum combat readiness is 10% higher. Adapted from MSS Garuda in
 * Unofficial Rule Book by DemonX3 (MIT licence, see CREDITS.txt); there the system cooldown was made
 * longer by mistake, here it is halved as the text says.
 */
public final class MssGaruda extends ConditionalMemberRule {

    private static final float COOLDOWN_MULT = 0.5f;
    private static final float VENT_PERCENT = 50f;
    private static final float RANGE_PERCENT = 30f;
    private static final float MAX_CR = 0.1f;

    @Override
    protected float strength(RuleSpec rule, FleetMemberAPI member, float fleetFactor) {
        return member.isFlagship() && isConquest(member.getHullSpec()) ? 1f : 0f;
    }

    static boolean isConquest(ShipHullSpecAPI hull) {
        if (hull == null) return false;
        String id = hull.getHullId();
        String base = hull.getBaseHullId();
        return (id != null && id.contains("conquest")) || (base != null && base.contains("conquest"))
                || hull.hasTag("ungpdx_conquest");
    }

    @Override
    protected void modify(FleetMemberAPI member, String id, float strength, RuleSpec rule) {
        MutableShipStatsAPI stats = member.getStats();
        stats.getSystemCooldownBonus().modifyMult(id, COOLDOWN_MULT);
        stats.getVentRateMult().modifyPercent(id, VENT_PERCENT);
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_PERCENT);
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_PERCENT);
        stats.getMissileWeaponRangeBonus().modifyPercent(id, RANGE_PERCENT);
        stats.getMaxCombatReadiness().modifyFlat(id, MAX_CR, rule.name);
    }
}
