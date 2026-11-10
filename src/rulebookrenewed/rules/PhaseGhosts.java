package rulebookrenewed.rules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import rulebookrenewed.RuleSpec;

/**
 * Hard: enemy ships fade out completely while phased and fade back in when they drop out. Uses the
 * ship's second extra alpha multiplier, which the phase cloak itself does not touch, so the two do not
 * fight over the ship's look. Only the look changes; the ship still shows on the HUD when targeted.
 */
public final class PhaseGhosts implements RuleEffect {

    private static final String KEY = "rbr_phase_ghosts";
    /** Alpha change per second, so the fade takes about a third of a second. */
    private static final float FADE_RATE = 3f;

    @Override
    @SuppressWarnings("unchecked")
    public void advanceCombat(RuleSpec rule, CombatEngineAPI engine, float amount, int level, float value) {
        Map<ShipAPI, Float> alphas = (Map<ShipAPI, Float>) engine.getCustomData().get(KEY);
        if (alphas == null) {
            alphas = new HashMap<>();
            engine.getCustomData().put(KEY, alphas);
        }
        for (ShipAPI ship : engine.getShips()) {
            if (ship.getOwner() != 1 || ship.isFighter() || !ship.isPhased() || alphas.containsKey(ship)) continue;
            alphas.put(ship, 1f);
        }
        Iterator<Map.Entry<ShipAPI, Float>> it = alphas.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ShipAPI, Float> entry = it.next();
            ShipAPI ship = entry.getKey();
            boolean hide = ship.isPhased() && ship.isAlive() && !ship.isHulk();
            float alpha = entry.getValue();
            alpha = hide ? Math.max(0f, alpha - FADE_RATE * amount) : Math.min(1f, alpha + FADE_RATE * amount);
            ship.setApplyExtraAlphaToEngines(true);
            ship.setExtraAlphaMult2(alpha);
            if (!hide && alpha >= 1f) it.remove();
            else entry.setValue(alpha);
        }
    }
}
