package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import rulebookrenewed.RuleSpec;

/**
 * Golden: while the player's flagship is a Paragon (any hull whose id contains "paragon", or with the
 * tag rbr_paragon or ungp_paragon), its weapons hit harder and fire faster, its shield unfolds and turns
 * faster for less upkeep, and it has more maximum combat readiness. Adapted from TTS Invicible in
 * Unofficial New Game Plus by Originem.
 */
public final class TtsInvincible extends ConditionalMemberRule {

    private static final float SHIELD_PERCENT = 50f;
    private static final float CR_BONUS = 0.1f;

    @Override
    protected float strength(RuleSpec rule, FleetMemberAPI member, float fleetFactor) {
        return member.isFlagship() && isParagon(member.getHullSpec()) ? fleetFactor : 0f;
    }

    @Override
    protected void modify(FleetMemberAPI member, String id, float strength, RuleSpec rule) {
        MutableShipStatsAPI stats = member.getStats();
        stats.getShieldUpkeepMult().modifyMult(id, 1f - SHIELD_PERCENT / 100f);
        stats.getShieldUnfoldRateMult().modifyPercent(id, SHIELD_PERCENT);
        stats.getShieldTurnRateMult().modifyPercent(id, SHIELD_PERCENT);
        stats.getEnergyWeaponDamageMult().modifyPercent(id, strength);
        stats.getBallisticWeaponDamageMult().modifyPercent(id, strength);
        stats.getMissileWeaponDamageMult().modifyPercent(id, strength);
        stats.getEnergyRoFMult().modifyPercent(id, strength);
        stats.getBallisticRoFMult().modifyPercent(id, strength);
        stats.getMissileRoFMult().modifyPercent(id, strength);
        stats.getMaxCombatReadiness().modifyFlat(id, CR_BONUS, rule.name);
    }

    private static boolean isParagon(ShipHullSpecAPI hull) {
        if (hull == null) return false;
        return hull.getHullId().contains("paragon") || hull.getTags().contains("rbr_paragon")
                || hull.getTags().contains("ungp_paragon");
    }
}
