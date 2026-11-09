package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

/** Helpful challenge reward: automated enemy ships (Remnants, derelict drones and the like) take more damage. */
public final class OrdoBreaker extends EnemyKindDamage {

    @Override
    protected boolean matches(ShipAPI ship) {
        return ship.getVariant() != null && ship.getVariant().hasHullMod(HullMods.AUTOMATED);
    }
}
