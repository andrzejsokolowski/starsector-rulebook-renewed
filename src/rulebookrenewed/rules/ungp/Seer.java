package rulebookrenewed.rules.ungp;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FogOfWarAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Helpful: every 20 seconds the fog of war lifts from the whole battlefield for a few seconds. Adapted
 * from Seer in Unofficial New Game Plus by Originem.
 */
public final class Seer implements RuleEffect {

    private static final String KEY = "rbr_seer";
    private static final float WAIT = 20f;

    private static final class State {
        float left = WAIT;
        boolean revealing;
    }

    @Override
    public void advanceCombat(RuleSpec rule, CombatEngineAPI engine, float amount, int level, float value) {
        State state = Tools.data(engine, KEY);
        if (state == null) {
            state = new State();
            engine.getCustomData().put(KEY, state);
        }
        state.left -= amount;
        if (state.left <= 0f) {
            state.revealing = !state.revealing;
            state.left = state.revealing ? value : WAIT;
            ShipAPI player = engine.getPlayerShip();
            if (state.revealing && player != null) {
                Global.getSoundPlayer().playSound("world_sensor_burst_on", 1f, 1f, player.getLocation(), new Vector2f());
            }
        }
        if (!state.revealing) return;
        FogOfWarAPI fog = engine.getFogOfWar(0);
        if (fog != null) fog.revealAroundPoint(this, 0f, 0f, engine.getMapWidth() * 2f + engine.getMapHeight() * 2f);
    }
}
