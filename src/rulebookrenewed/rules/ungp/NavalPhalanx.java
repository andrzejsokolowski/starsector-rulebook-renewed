package rulebookrenewed.rules.ungp;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;
import rulebookrenewed.rules.Scale;

/**
 * Helpful: a ship of the player's side with another one of its side within 600 units (fighters do not
 * count) dissipates flux faster and takes less damage on its shield. Adapted from Naval Phalanx in
 * Unofficial New Game Plus by Originem (whose range check also counted the ship itself).
 */
public final class NavalPhalanx implements RuleEffect {

    private static final float RANGE = 600f;

    @Override
    public void advanceCombat(RuleSpec rule, CombatEngineAPI engine, float amount, int level, float value) {
        String id = RuleEffect.modId(rule);
        List<ShipAPI> own = new ArrayList<>();
        for (ShipAPI ship : engine.getShips()) {
            if (ship.getOwner() == 0 && !ship.isFighter() && ship.isAlive()) own.add(ship);
        }
        ShipAPI player = engine.getPlayerShip();
        for (ShipAPI ship : own) {
            boolean close = false;
            for (ShipAPI other : own) {
                if (other != ship && Misc.getDistance(ship.getLocation(), other.getLocation()) <= RANGE) {
                    close = true;
                    break;
                }
            }
            MutableShipStatsAPI stats = ship.getMutableStats();
            if (close) {
                Scale.by(stats.getFluxDissipation(), id, value, 1f, null);
                Scale.by(stats.getShieldDamageTakenMult(), id, value, -1f, null);
                if (ship == player) {
                    engine.maintainStatusForPlayerShip(id, rule.icon, rule.name,
                            "Flux dissipation +" + (int) value + "%, shield damage -" + (int) value + "%", false);
                }
            } else {
                stats.getFluxDissipation().unmodify(id);
                stats.getShieldDamageTakenMult().unmodify(id);
            }
        }
    }
}
