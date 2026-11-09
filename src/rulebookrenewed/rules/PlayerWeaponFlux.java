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
        Scale.by(stats.getBeamWeaponFluxCostMult(), id, value, -sign, source);
    }
}
