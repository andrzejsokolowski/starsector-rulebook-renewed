package rulebookrenewed.rules.ungp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Helpful: while the ship the player flies vents, every release of the vent key speeds the venting up
 * for a third of a second; two presses count at once. Each press throws a small arc on the hull.
 * Adapted from V for Vent in Unofficial New Game Plus by Originem.
 */
public final class VForVent implements RuleEffect {

    private static final String KEY = "rbr_v_for_vent";
    private static final String VENT_CONTROL = "SHIP_VENT_FLUX";
    private static final float BONUS_SECONDS = 0.33f;
    private static final int MAX_STACK = 2;

    private static final class State {
        ShipAPI ship;
        final List<float[]> presses = new ArrayList<>();
    }

    @Override
    public void processCombatInput(RuleSpec rule, CombatEngineAPI engine, List<InputEventAPI> events, int level, float value) {
        State state = Tools.data(engine, KEY);
        if (state == null) {
            state = new State();
            engine.getCustomData().put(KEY, state);
        }
        String id = RuleEffect.modId(rule);
        ShipAPI player = engine.getPlayerShip();
        if (state.ship != player) {
            if (state.ship != null) state.ship.getMutableStats().getVentRateMult().unmodify(id);
            state.ship = player;
            state.presses.clear();
        }
        if (player == null) return;
        if (!player.getFluxTracker().isVenting()) {
            if (!state.presses.isEmpty()) {
                state.presses.clear();
                player.getMutableStats().getVentRateMult().unmodify(id);
            }
            return;
        }
        if (events != null) {
            for (InputEventAPI event : events) {
                if (event.isConsumed() || !event.isControlUpEvent(VENT_CONTROL)) continue;
                state.presses.add(new float[] {BONUS_SECONDS});
                float angle = Tools.RANDOM.nextFloat() * 360f;
                float radius = player.getCollisionRadius() * Tools.between(0.2f, 0.4f);
                Vector2f from = Tools.pointOnCircle(player.getLocation(), radius, angle);
                Vector2f to = Tools.pointOnCircle(player.getLocation(), radius * 2f, angle);
                engine.spawnEmpArcVisual(from, player, to, player, 10f, player.getVentFringeColor(), player.getVentCoreColor());
                break;
            }
        }
        float amount = engine.getElapsedInLastFrame();
        float longest = 0f;
        Iterator<float[]> it = state.presses.iterator();
        while (it.hasNext()) {
            float[] press = it.next();
            press[0] -= amount;
            if (press[0] <= 0f) it.remove();
            else longest = Math.max(longest, press[0]);
        }
        int stack = Math.min(MAX_STACK, state.presses.size());
        if (stack <= 0) {
            player.getMutableStats().getVentRateMult().unmodify(id);
            return;
        }
        player.getMutableStats().getVentRateMult().modifyPercent(id, value * stack);
        engine.maintainStatusForPlayerShip(KEY, "graphics/icons/tactical/venting_flux2.png", rule.name,
                "Vent rate +" + (int) (value * stack) + "%", false);
    }
}
