package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleSpec;

/**
 * Helpful: enemy ships near the ship the player flies lose maneuverability and some flux dissipation;
 * phase ships lose far more and pay more to stay phased. Checked every frame, so ships leaving the aura
 * recover. Adapted from Doomslayer in Unofficial Rule Book by DemonX3 (MIT licence, see CREDITS.txt).
 */
public final class Doomslayer implements RuleEffect {

    private static final float RANGE = 1000f;

    @Override
    public void advanceCombat(RuleSpec rule, CombatEngineAPI engine, float amount, int level, float value) {
        String id = RuleEffect.modId(rule);
        ShipAPI player = engine.getPlayerShip();
        boolean aura = player != null && player.isAlive() && !player.isShuttlePod() && player.getOwner() == 0;
        for (ShipAPI ship : engine.getShips()) {
            if (ship.getOwner() != 1 || ship.isFighter()) continue;
            MutableShipStatsAPI stats = ship.getMutableStats();
            boolean inside = aura && ship.isAlive()
                    && Misc.getDistance(ship.getLocation(), player.getLocation()) - ship.getCollisionRadius() <= RANGE;
            if (!inside) {
                clear(stats, id);
                continue;
            }
            boolean phase = ship.getHullSpec() != null && ship.getHullSpec().isPhase();
            float handling = phase ? Math.min(75f, value * 2.5f) : value;
            float flux = phase ? value / 3f : value / 6f;
            float handlingMult = 1f - handling / 100f;
            stats.getMaxTurnRate().modifyMult(id, handlingMult);
            stats.getTurnAcceleration().modifyMult(id, 1f - handling / 200f);
            stats.getAcceleration().modifyMult(id, 1f - handling / 200f);
            stats.getDeceleration().modifyMult(id, handlingMult);
            stats.getFluxDissipation().modifyMult(id, 1f - flux / 100f);
            if (phase) stats.getPhaseCloakUpkeepCostBonus().modifyMult(id, 1f + value / 30f);
            else stats.getPhaseCloakUpkeepCostBonus().unmodify(id);
        }
    }

    private static void clear(MutableShipStatsAPI stats, String id) {
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getFluxDissipation().unmodify(id);
        stats.getPhaseCloakUpkeepCostBonus().unmodify(id);
    }
}
