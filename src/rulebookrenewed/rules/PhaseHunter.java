package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.ShipAPI;

/** Helpful challenge reward: enemy phase ships take more damage. */
public final class PhaseHunter extends EnemyKindDamage {

    @Override
    protected boolean matches(ShipAPI ship) {
        return ship.getHullSpec() != null && ship.getHullSpec().isPhase();
    }
}
