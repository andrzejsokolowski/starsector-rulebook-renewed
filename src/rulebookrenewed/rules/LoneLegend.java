package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import rulebookrenewed.RuleSpec;

/** Helpful challenge reward: the player's flagship deals more damage and takes less. */
public final class LoneLegend implements RuleEffect {

    @Override
    public void applyToShip(RuleSpec rule, ShipAPI ship, boolean enemy, int level, float value) {
        if (enemy || ship.isAlly() || ship.isFighter()) return;
        FleetMemberAPI member = ship.getFleetMember();
        if (member == null || !member.isFlagship()) return;
        String id = RuleEffect.modId(rule);
        MutableShipStatsAPI stats = ship.getMutableStats();
        Scale.by(stats.getBallisticWeaponDamageMult(), id, value, 1f, null);
        Scale.by(stats.getEnergyWeaponDamageMult(), id, value, 1f, null);
        Scale.by(stats.getMissileWeaponDamageMult(), id, value, 1f, null);
        Scale.by(stats.getShieldDamageTakenMult(), id, value, -1f, null);
        Scale.by(stats.getArmorDamageTakenMult(), id, value, -1f, null);
        Scale.by(stats.getHullDamageTakenMult(), id, value, -1f, null);
    }
}
