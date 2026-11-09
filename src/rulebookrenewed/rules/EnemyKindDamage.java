package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import rulebookrenewed.RuleSpec;

/** Helpful: one kind of enemy ship (automated ships, stations, phase ships) takes more damage. */
public abstract class EnemyKindDamage implements RuleEffect {

    protected abstract boolean matches(ShipAPI ship);

    @Override
    public void applyToShip(RuleSpec rule, ShipAPI ship, boolean enemy, int level, float value) {
        if (!enemy || !matches(ship)) return;
        String id = RuleEffect.modId(rule);
        MutableShipStatsAPI stats = ship.getMutableStats();
        Scale.by(stats.getShieldDamageTakenMult(), id, value, 1f, null);
        Scale.by(stats.getArmorDamageTakenMult(), id, value, 1f, null);
        Scale.by(stats.getHullDamageTakenMult(), id, value, 1f, null);
    }
}
