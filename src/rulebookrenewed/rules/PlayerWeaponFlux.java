package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Weapons in the player's fleet cost less (helpful) or more (hard) flux to fire.
 */
public final class PlayerWeaponFlux extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        Scale.by(stats.getBallisticWeaponFluxCostMod(), id, value, -sign, source);
        Scale.by(stats.getEnergyWeaponFluxCostMod(), id, value, -sign, source);
        Scale.by(stats.getMissileWeaponFluxCostMod(), id, value, -sign, source);
        // No beam change: the game (computeActualFluxCost in 0.98.5a) applies the weapon type's change to
        // beams first and then the beam multiplier on top, so a beam line here would count twice.
    }
}
