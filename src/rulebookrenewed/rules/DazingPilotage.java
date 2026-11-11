package rulebookrenewed.rules;

import java.awt.Color;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;

import rulebookrenewed.RuleSpec;

/**
 * Hard: every 7 seconds each of the player's own ships has a chance to doze off: for 1 to 5 seconds by hull
 * size it cannot move, turn, fire, vent, use its system or toggle its shield, and its shield and weapons do
 * not turn. Allies and fighters are left alone. Adapted from Dazing Pilotage in Unofficial Rule Book by
 * DemonX3 (MIT licence, see CREDITS.txt), with its seconds by hull size (1/2/4/5).
 */
public final class DazingPilotage implements RuleEffect {

    private static final float INTERVAL = 7f;
    private static final Color TEXT = new Color(130, 211, 169);
    private static final Random RANDOM = new Random();
    private static final ShipCommand[] BLOCKED = {
            ShipCommand.USE_SYSTEM, ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, ShipCommand.FIRE,
            ShipCommand.PULL_BACK_FIGHTERS, ShipCommand.VENT_FLUX, ShipCommand.ACCELERATE,
            ShipCommand.ACCELERATE_BACKWARDS, ShipCommand.DECELERATE, ShipCommand.TURN_LEFT,
            ShipCommand.TURN_RIGHT, ShipCommand.STRAFE_LEFT, ShipCommand.STRAFE_RIGHT};

    @Override
    public void applyToShip(RuleSpec rule, ShipAPI ship, boolean enemy, int level, float value) {
        if (enemy || ship.isAlly() || ship.isFighter() || ship.isStation() || ship.isStationModule()) return;
        if (ship.hasListenerOfClass(Doze.class)) return;
        ship.addListener(new Doze(ship, value / 100f, RuleEffect.modId(rule), rule.name));
    }

    private static float seconds(ShipAPI ship) {
        switch (ship.getHullSize()) {
            case DESTROYER: return 2f;
            case CRUISER: return 4f;
            case CAPITAL_SHIP: return 5f;
            default: return 1f;
        }
    }

    private static final class Doze implements AdvanceableListener {
        private final ShipAPI ship;
        private final float chance;
        private final String id;
        private final String name;
        private float sinceRoll;
        private float asleepFor;

        Doze(ShipAPI ship, float chance, String id, String name) {
            this.ship = ship;
            this.chance = chance;
            this.id = id;
            this.name = name;
        }

        @Override
        public void advance(float amount) {
            if (amount <= 0f) return;
            MutableShipStatsAPI stats = ship.getMutableStats();
            if (!ship.isAlive() || ship.isHulk()) {
                wake(stats);
                return;
            }
            if (asleepFor > 0f) {
                asleepFor -= amount;
                if (asleepFor <= 0f) {
                    wake(stats);
                    return;
                }
                for (ShipCommand command : BLOCKED) ship.blockCommandForOneFrame(command);
                stats.getShieldTurnRateMult().modifyMult(id, 0f);
                stats.getWeaponTurnRateBonus().modifyMult(id, 0f);
                stats.getBeamWeaponTurnRateBonus().modifyMult(id, 0f);
                CombatEngineAPI engine = Global.getCombatEngine();
                if (engine != null && engine.getPlayerShip() == ship) {
                    engine.maintainStatusForPlayerShip(id, "graphics/icons/hullsys/temporal_shell.png", name,
                            "Controls locked", true);
                }
                return;
            }
            sinceRoll += amount;
            if (sinceRoll < INTERVAL) return;
            sinceRoll = 0f;
            if (RANDOM.nextFloat() >= chance) return;
            asleepFor = seconds(ship);
            Global.getCombatEngine().addFloatingText(ship.getLocation(), "Dozing off", 24f, TEXT, ship, 0.5f, 1f);
        }

        private void wake(MutableShipStatsAPI stats) {
            asleepFor = 0f;
            stats.getShieldTurnRateMult().unmodify(id);
            stats.getWeaponTurnRateBonus().unmodify(id);
            stats.getBeamWeaponTurnRateBonus().unmodify(id);
        }
    }
}
