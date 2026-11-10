package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Hard: fighters of the player's side fight worse as their carrier's fighter replacement rate falls.
 * Nothing at a rate of 100%, the full value at 50% or lower: they take that much more damage and their
 * weapon damage is divided by the same factor. Adapted from Emergent Airforce Rebuild in Unofficial New
 * Game Plus by Originem.
 */
public final class AirforceRebuild implements RuleEffect {

    private static final float FULL_AT_RATE = 0.5f;

    @Override
    public void advanceCombat(RuleSpec rule, CombatEngineAPI engine, float amount, int level, float value) {
        String id = RuleEffect.modId(rule);
        for (ShipAPI ship : engine.getShips()) {
            if (ship.getOwner() != 0 || ship.isFighter() || !ship.isAlive() || ship.getNumFighterBays() <= 0) continue;
            float healthy = Math.max(0f, (ship.getSharedFighterReplacementRate() - FULL_AT_RATE) / (1f - FULL_AT_RATE));
            float mult = 1f + value / 100f * (1f - Math.min(1f, healthy));
            for (FighterWingAPI wing : ship.getAllWings()) {
                for (ShipAPI fighter : wing.getWingMembers()) {
                    MutableShipStatsAPI stats = fighter.getMutableStats();
                    stats.getArmorDamageTakenMult().modifyMult(id, mult);
                    stats.getShieldDamageTakenMult().modifyMult(id, mult);
                    stats.getHullDamageTakenMult().modifyMult(id, mult);
                    stats.getBallisticWeaponDamageMult().modifyMult(id, 1f / mult);
                    stats.getEnergyWeaponDamageMult().modifyMult(id, 1f / mult);
                    stats.getMissileWeaponDamageMult().modifyMult(id, 1f / mult);
                }
            }
        }
    }
}
