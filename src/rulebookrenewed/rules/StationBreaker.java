package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.ShipAPI;

/** Helpful challenge reward: enemy stations and their modules take more damage. */
public final class StationBreaker extends EnemyKindDamage {

    @Override
    protected boolean matches(ShipAPI ship) {
        return ship.isStation() || ship.isStationModule() || ship.getParentStation() != null;
    }
}
