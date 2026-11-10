package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;
import rulebookrenewed.rules.Scale;

/**
 * Hard: enemy stations and their modules take less damage and their ballistic and energy weapons reach
 * farther. Adapted from Century War in Unofficial New Game Plus by Originem.
 */
public final class CenturyWar implements RuleEffect {

    @Override
    public void applyToShip(RuleSpec rule, ShipAPI ship, boolean enemy, int level, float value) {
        if (!enemy) return;
        boolean station = ship.isStation()
                || (ship.isStationModule() && ship.getParentStation() != null && ship.getParentStation().isStation());
        if (!station) return;
        String id = RuleEffect.modId(rule);
        MutableShipStatsAPI stats = ship.getMutableStats();
        Scale.by(stats.getHullDamageTakenMult(), id, value, -1f, null);
        Scale.by(stats.getArmorDamageTakenMult(), id, value, -1f, null);
        Scale.by(stats.getShieldDamageTakenMult(), id, value, -1f, null);
        Scale.by(stats.getEnergyWeaponRangeBonus(), id, value, 1f, null);
        Scale.by(stats.getBallisticWeaponRangeBonus(), id, value, 1f, null);
    }
}
