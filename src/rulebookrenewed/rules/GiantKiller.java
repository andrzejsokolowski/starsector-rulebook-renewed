package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.ShipAPI;

/** Helpful challenge reward: enemy capital ships take more damage. */
public final class GiantKiller extends EnemyKindDamage {

    @Override
    protected boolean matches(ShipAPI ship) {
        return ship.getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP;
    }
}
