package rulebookrenewed.rules.ungp;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Golden: when enemy missiles that will reach the ship the player flies within 2 seconds carry more
 * damage than a fifth of its maximum hull, the battle slows to a third of its speed for 2 seconds while
 * the player's ship keeps its own pace; then 15 seconds of cooldown. A red frame shows while it lasts.
 * Adapted from Stay Calm in Unofficial New Game Plus by Originem.
 */
public final class StayCalm implements RuleEffect {

    private static final String KEY = "rbr_stay_calm";
    private static final float LOOK_AHEAD = 2f;
    private static final float HULL_SHARE = 0.2f;
    private static final float TIME_MULT = 0.33f;
    private static final float COOLDOWN = 15f;
    private static final float CHECK_SECONDS = 0.2f;
    private static final float SEARCH_RANGE = 3000f;
    private static final float FADE_SPEED = 10f;
    private static final Color FRAME_COLOR = new Color(160, 0, 0);

    private static final int IDLE = 0, ACTIVE = 1, COOLING = 2;

    private static final class State {
        int phase = IDLE;
        float elapsed;
        float sinceCheck;
        float timeMult = 1f;
        ShipAPI ship;
    }

    @Override
    public void advanceCombat(RuleSpec rule, CombatEngineAPI engine, float amount, int level, float value) {
        State state = Tools.data(engine, KEY);
        if (state == null) {
            state = new State();
            engine.getCustomData().put(KEY, state);
        }
        ShipAPI player = engine.getPlayerShip();
        state.elapsed += amount;
        if (state.phase == IDLE) {
            follow(state, player);
            state.sinceCheck += amount;
            if (player == null || !player.isAlive() || state.sinceCheck < CHECK_SECONDS) return;
            state.sinceCheck = 0f;
            if (incomingDamage(engine, player) <= player.getMaxHitpoints() * HULL_SHARE) return;
            state.phase = ACTIVE;
            state.elapsed = 0f;
            Tools.uiSound("rbr_staycalm_start");
            engine.addLayeredRenderingPlugin(new Frame(state));
            return;
        }
        float target = state.phase == ACTIVE ? TIME_MULT : 1f;
        state.timeMult = Misc.interpolate(state.timeMult, target, Math.min(1f, FADE_SPEED * amount));
        engine.getTimeMult().modifyMult(KEY, state.timeMult);
        if (state.ship != null) state.ship.getMutableStats().getTimeMult().modifyMult(KEY, 1f / state.timeMult);
        if (state.phase == ACTIVE) {
            if (state.elapsed > LOOK_AHEAD) {
                state.phase = COOLING;
                state.elapsed = 0f;
                Tools.uiSound("rbr_staycalm_end");
            }
            status(engine, rule, "Time slowed: " + seconds(LOOK_AHEAD - state.elapsed), false);
        } else {
            if (state.elapsed > COOLDOWN) {
                state.phase = IDLE;
                state.elapsed = 0f;
                state.timeMult = 1f;
                engine.getTimeMult().unmodify(KEY);
                if (state.ship != null) state.ship.getMutableStats().getTimeMult().unmodify(KEY);
                return;
            }
            status(engine, rule, "Cooldown: " + seconds(COOLDOWN - state.elapsed), true);
        }
    }

    /** The ship that keeps its pace is the one the player flies when the slowdown starts. */
    private static void follow(State state, ShipAPI player) {
        if (state.ship == player) return;
        if (state.ship != null) state.ship.getMutableStats().getTimeMult().unmodify(KEY);
        state.ship = player;
    }

    private static float incomingDamage(CombatEngineAPI engine, ShipAPI player) {
        float damage = 0f;
        Vector2f at = player.getLocation();
        for (MissileAPI missile : engine.getMissiles()) {
            if (missile.getOwner() == player.getOwner() || missile.didDamage() || missile.isFizzling()) continue;
            if (Misc.getDistance(at, missile.getLocation()) > SEARCH_RANGE) continue;
            Vector2f ahead = new Vector2f(missile.getVelocity());
            ahead.scale(LOOK_AHEAD);
            Vector2f.add(ahead, missile.getLocation(), ahead);
            float gap = Misc.getDistance(at, ahead) - player.getCollisionRadius();
            if (gap <= 0f || (missile.isGuided() && gap <= player.getCollisionRadius() * 2f)) {
                damage += missile.getDamageAmount();
            }
        }
        return damage;
    }

    private static void status(CombatEngineAPI engine, RuleSpec rule, String text, boolean debuff) {
        engine.maintainStatusForPlayerShip(KEY, rule.icon, rule.name, text, debuff);
    }

    private static String seconds(float left) {
        return String.format("%.1f s", Math.max(0f, left));
    }

    /** The red frame around the screen, faded in while time is slowed and out afterwards. */
    private static final class Frame extends BaseCombatLayeredRenderingPlugin {
        private final State state;
        private float alpha;

        Frame(State state) {
            this.state = state;
        }

        @Override
        public void advance(float amount) {
            float target = state.phase == ACTIVE ? 0.6f : 0f;
            alpha = Misc.interpolate(alpha, target, Math.min(1f, (state.phase == ACTIVE ? 10f : 5f) * amount));
        }

        @Override
        public boolean isExpired() {
            return state.phase == IDLE;
        }

        @Override
        public float getRenderRadius() {
            return 100000f;
        }

        @Override
        public void render(CombatEngineLayers layer, ViewportAPI viewport) {
            if (alpha <= 0.05f) return;
            SpriteAPI sprite = Global.getSettings().getSprite("rbr_fx", "frame");
            sprite.setColor(FRAME_COLOR);
            sprite.setWidth(viewport.getVisibleWidth() * 1.1f);
            sprite.setHeight(viewport.getVisibleHeight() * 1.1f);
            sprite.setAdditiveBlend();
            sprite.setAlphaMult(alpha * viewport.getAlphaMult());
            Vector2f center = viewport.getCenter();
            sprite.renderAtCenter(center.x, center.y);
        }
    }
}
