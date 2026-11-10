package rulebookrenewed.rules.ungp;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Hard: about once a minute the enemy may receive a phase ship (Afflictor, Shade, Harbinger or Doom,
 * by the largest hull on the field), which fades in next to a random enemy ship. A success cuts the
 * next chance to 45%, a failure doubles it; the hull that came last is half as likely to come again.
 * Adapted from Phasing Ghost in Unofficial New Game Plus by Originem.
 */
public final class PhasingGhost implements RuleEffect {

    private static final String KEY = "rbr_phasing_ghost";
    private static final float MIN_WAIT = 55f, MAX_WAIT = 65f;
    private static final float FADE_SECONDS = 1f;
    private static final Color JITTER = new Color(61, 5, 122, 50);

    private static final class Arrival {
        final ShipAPI ship;
        final CollisionClass collision;
        float elapsed;

        Arrival(ShipAPI ship) {
            this.ship = ship;
            this.collision = ship.getCollisionClass();
        }
    }

    private static final class State {
        float wait = Tools.between(MIN_WAIT, MAX_WAIT);
        float chance;
        String lastHull;
        final List<Arrival> arrivals = new ArrayList<>();
    }

    @Override
    public void advanceCombat(RuleSpec rule, CombatEngineAPI engine, float amount, int level, float value) {
        State state = Tools.data(engine, KEY);
        if (state == null) {
            state = new State();
            state.chance = value / 100f;
            engine.getCustomData().put(KEY, state);
        }
        fadeIn(state, amount);
        state.wait -= amount;
        if (state.wait > 0f) return;
        state.wait = Tools.between(MIN_WAIT, MAX_WAIT);
        if (engine.isCombatOver()) return;
        if (!Tools.roll(state.chance)) {
            state.chance *= 2f;
            return;
        }
        ShipAPI ghost = spawn(engine, state);
        if (ghost == null) return;
        state.chance *= 0.45f;
        state.arrivals.add(new Arrival(ghost));
        ghost.setAlphaMult(0f);
        try {
            engine.getCombatUI().addMessage(0, Misc.getNegativeHighlightColor(), rule.name + ": an enemy phase ship has arrived.");
        } catch (Throwable ignored) {
            // No combat UI.
        }
        Global.getSoundPlayer().playSound("rbr_phasingghost_arrive", 1f, 1f, ghost.getLocation(), new Vector2f());
        Global.getSoundPlayer().playUISound("rbr_phasingghost_warning", 1f, 0.4f);
    }

    private static ShipAPI spawn(CombatEngineAPI engine, State state) {
        ShipAPI.HullSize biggest = ShipAPI.HullSize.DEFAULT;
        List<ShipAPI> anchors = new ArrayList<>();
        for (ShipAPI ship : engine.getShips()) {
            if (!ship.isAlive() || ship.isFighter() || ship.getHullSize() == null) continue;
            if (ship.getHullSize().ordinal() > biggest.ordinal()) biggest = ship.getHullSize();
            if (ship.getOwner() == 1) anchors.add(ship);
        }
        if (anchors.isEmpty()) return null;
        WeightedRandomPicker<String> hulls = new WeightedRandomPicker<>(Tools.RANDOM);
        switch (biggest) {
            case FRIGATE:
                add(hulls, state, "afflictor", 10f);
                add(hulls, state, "shade", 10f);
                break;
            case DESTROYER:
                add(hulls, state, "afflictor", 8f);
                add(hulls, state, "shade", 8f);
                add(hulls, state, "harbinger", 10f);
                break;
            case CRUISER:
                add(hulls, state, "afflictor", 8f);
                add(hulls, state, "shade", 8f);
                add(hulls, state, "harbinger", 8f);
                add(hulls, state, "doom", 4f);
                break;
            case CAPITAL_SHIP:
                add(hulls, state, "afflictor", 10f);
                add(hulls, state, "shade", 10f);
                add(hulls, state, "harbinger", 10f);
                add(hulls, state, "doom", 10f);
                break;
            default:
                return null;
        }
        String hull = hulls.pick();
        if (hull == null) return null;
        List<String> variants = Global.getSettings().getHullIdToVariantListMap().get(hull);
        if (variants == null || variants.isEmpty()) return null;
        String variant = variants.get(Tools.RANDOM.nextInt(variants.size()));

        ShipAPI anchor = anchors.get(Tools.RANDOM.nextInt(anchors.size()));
        float distance = Tools.between(anchor.getCollisionRadius(), anchor.getCollisionRadius() * 3f);
        Vector2f at = Tools.randomPointOnCircle(anchor.getLocation(), distance);
        CombatFleetManagerAPI manager = engine.getFleetManager(FleetSide.ENEMY);
        boolean quiet = manager.isSuppressDeploymentMessages();
        manager.setSuppressDeploymentMessages(true);
        ShipAPI ghost;
        try {
            ghost = manager.spawnShipOrWing(variant, at, Tools.RANDOM.nextFloat() * 360f);
        } finally {
            manager.setSuppressDeploymentMessages(quiet);
        }
        if (ghost != null) state.lastHull = hull;
        return ghost;
    }

    private static void add(WeightedRandomPicker<String> hulls, State state, String hull, float weight) {
        hulls.add(hull, hull.equals(state.lastHull) ? weight * 0.5f : weight);
    }

    /** A new ghost fades in over a second: it cannot act, collide or take hull damage until it is mostly there. */
    private static void fadeIn(State state, float amount) {
        Iterator<Arrival> it = state.arrivals.iterator();
        while (it.hasNext()) {
            Arrival arrival = it.next();
            ShipAPI ship = arrival.ship;
            arrival.elapsed += amount;
            float progress = Math.min(1f, arrival.elapsed / FADE_SECONDS);
            if (progress >= 1f || !ship.isAlive()) {
                ship.setAlphaMult(1f);
                ship.setHoldFire(false);
                ship.setCollisionClass(arrival.collision);
                ship.getMutableStats().getHullDamageTakenMult().unmodify(KEY);
                it.remove();
                continue;
            }
            ship.setAlphaMult(progress);
            if (progress < 0.5f) {
                ship.blockCommandForOneFrame(ShipCommand.ACCELERATE);
                ship.blockCommandForOneFrame(ShipCommand.TURN_LEFT);
                ship.blockCommandForOneFrame(ShipCommand.TURN_RIGHT);
                ship.blockCommandForOneFrame(ShipCommand.STRAFE_LEFT);
                ship.blockCommandForOneFrame(ShipCommand.STRAFE_RIGHT);
            }
            ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
            ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
            ship.blockCommandForOneFrame(ShipCommand.FIRE);
            ship.blockCommandForOneFrame(ShipCommand.PULL_BACK_FIGHTERS);
            ship.blockCommandForOneFrame(ShipCommand.VENT_FLUX);
            ship.setHoldFireOneFrame(true);
            ship.setHoldFire(true);
            if (progress > 0.75f) {
                ship.setCollisionClass(arrival.collision);
                ship.getMutableStats().getHullDamageTakenMult().unmodify(KEY);
            } else {
                ship.setCollisionClass(CollisionClass.NONE);
                ship.getMutableStats().getHullDamageTakenMult().modifyMult(KEY, 0f);
            }
            float jitter = progress < 0.5f ? progress * 2f : (1f - progress) * 2f;
            ship.setJitter(KEY, JITTER, jitter, 25, 0f, (1f - progress) * 50f);
        }
    }
}
