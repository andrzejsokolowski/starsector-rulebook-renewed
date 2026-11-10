package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import rulebookrenewed.RuleSpec;

/**
 * Helpful: while the ship the player flies is the last of the player's own ships on the field, it has more
 * flux capacity and dissipation, its peak performance time does not run down, and its hull slowly repairs
 * up to half. Allies and fighters do not count. Adapted from The Unyielding in Unofficial Rule Book by
 * DemonX3 (MIT licence, see CREDITS.txt).
 */
public final class LastStand implements RuleEffect {

    private static final float REPAIR_UP_TO = 0.5f;
    private static final float REPAIR_PER_SECOND = 0.01f;
    private static final String STATUS_KEY = "rbr_last_stand";

    @Override
    public void advanceCombat(RuleSpec rule, CombatEngineAPI engine, float amount, int level, float value) {
        String id = RuleEffect.modId(rule);
        ShipAPI player = engine.getPlayerShip();
        int own = 0;
        for (ShipAPI ship : engine.getShips()) {
            if (ship.getOwner() != 0 || ship.isAlly() || ship.isFighter() || ship.isStationModule()
                    || ship.isShuttlePod() || !ship.isAlive() || ship.isHulk()) continue;
            own++;
            if (ship != player) clear(ship.getMutableStats(), id);
        }
        if (player == null) return;
        boolean alone = own == 1 && player.isAlive() && !player.isShuttlePod() && player.getOwner() == 0 && !player.isAlly();
        MutableShipStatsAPI stats = player.getMutableStats();
        if (!alone) {
            clear(stats, id);
            return;
        }
        stats.getFluxCapacity().modifyPercent(id, value);
        stats.getFluxDissipation().modifyPercent(id, value);
        // Peak performance time counts up while deployed; holding it still keeps the ship at peak.
        player.setTimeDeployed(Math.max(0f, player.getTimeDeployedForCRReduction() - amount * stats.getTimeMult().getModifiedValue()));
        float max = player.getMaxHitpoints();
        if (player.getHitpoints() < max * REPAIR_UP_TO) {
            player.setHitpoints(Math.min(max * REPAIR_UP_TO, player.getHitpoints() + max * REPAIR_PER_SECOND * amount));
        }
        engine.maintainStatusForPlayerShip(STATUS_KEY, "graphics/icons/hullsys/damper_field.png", rule.name,
                "Flux +" + (int) value + "%, holding at peak", false);
    }

    private static void clear(MutableShipStatsAPI stats, String id) {
        stats.getFluxCapacity().unmodify(id);
        stats.getFluxDissipation().unmodify(id);
    }
}
