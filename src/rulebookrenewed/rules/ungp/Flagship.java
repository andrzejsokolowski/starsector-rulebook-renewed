package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import rulebookrenewed.RuleSpec;

/**
 * Golden: the player's flagship, whatever its hull, gets more hull, armor, flux, weapon damage and rate
 * of fire, faster engines and shields, and more maximum combat readiness. Merges HSS Phoenix (Onslaught
 * only) and TTS Invicible (Paragon only) from Unofficial New Game Plus by Originem into one rule, since
 * modded fleets rarely fly those two hulls.
 */
public final class Flagship extends ConditionalMemberRule {

    private static final float MANEUVER_PERCENT = 30f, SHIELD_PERCENT = 50f;
    private static final float CR_BONUS = 0.1f;

    @Override
    protected float strength(RuleSpec rule, FleetMemberAPI member, float fleetFactor) {
        return member.isFlagship() ? fleetFactor : 0f;
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
        stats.getEnergyWeaponDamageMult().modifyPercent(id, strength);
        stats.getBallisticWeaponDamageMult().modifyPercent(id, strength);
        stats.getMissileWeaponDamageMult().modifyPercent(id, strength);
        stats.getEnergyRoFMult().modifyPercent(id, strength);
        stats.getBallisticRoFMult().modifyPercent(id, strength);
        stats.getMissileRoFMult().modifyPercent(id, strength);
        stats.getShieldUpkeepMult().modifyMult(id, 1f - SHIELD_PERCENT / 100f);
        stats.getShieldUnfoldRateMult().modifyPercent(id, SHIELD_PERCENT);
        stats.getShieldTurnRateMult().modifyPercent(id, SHIELD_PERCENT);
        stats.getMaxCombatReadiness().modifyFlat(id, CR_BONUS, rule.name);
    }
}
