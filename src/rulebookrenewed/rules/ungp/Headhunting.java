package rulebookrenewed.rules.ungp;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatAssignmentType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.CombatTaskManagerAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Helpful: an enemy ship the player orders eliminated is marked for 20 seconds and takes more damage of
 * every type; cancelling the order does not end the mark. Then 60 seconds of cooldown. With several new
 * orders at once the first one found is marked. A turning ring shows the marked ship. Adapted from
 * Headhunting in Unofficial New Game Plus by Originem (there the duration moved with the difficulty;
 * here the damage does).
 */
public final class Headhunting implements RuleEffect {

    private static final String KEY = "rbr_headhunting";
    private static final float DURATION = 20f;
    private static final float COOLDOWN = 60f;
    private static final float CHECK_SECONDS = 0.1f;

    private static final class State {
        float sinceCheck;
        float cooldown;
        boolean announced = true;
        List<ShipAPI> previous = new ArrayList<>();
    }

    @Override
    public void advanceCombat(RuleSpec rule, CombatEngineAPI engine, float amount, int level, float value) {
        State state = Tools.data(engine, KEY);
        if (state == null) {
            state = new State();
            engine.getCustomData().put(KEY, state);
        }
        if (state.cooldown > 0f) state.cooldown -= amount;
        state.sinceCheck += amount;
        if (state.sinceCheck < CHECK_SECONDS) return;
        state.sinceCheck = 0f;

        CombatFleetManagerAPI manager = engine.getFleetManager(FleetSide.PLAYER);
        CombatTaskManagerAPI tasks = manager == null ? null : manager.getTaskManager(false);
        if (tasks == null) return;
        List<ShipAPI> targets = new ArrayList<>();
        for (CombatFleetManagerAPI.AssignmentInfo order : tasks.getAllAssignments()) {
            if (order.getType() != CombatAssignmentType.INTERCEPT || !(order.getTarget() instanceof DeployedFleetMemberAPI)) continue;
            ShipAPI target = ((DeployedFleetMemberAPI) order.getTarget()).getShip();
            if (target != null && target.isAlive()) targets.add(target);
        }
        if (state.cooldown <= 0f) {
            if (!state.announced) {
                state.announced = true;
                message(engine, Misc.getPositiveHighlightColor(), rule.name + ": ready.");
                Tools.uiSound("rbr_headhunting_ready");
            }
            for (ShipAPI target : targets) {
                if (state.previous.contains(target)) continue;
                state.cooldown = COOLDOWN;
                state.announced = false;
                engine.addLayeredRenderingPlugin(new Mark(target, value));
                Tools.uiSound("rbr_headhunting_mark");
                message(engine, Misc.getHighlightColor(), rule.name + ": " + target.getName() + " ("
                        + target.getHullSpec().getHullNameWithDashClass() + ") takes " + (int) value + "% more damage for "
                        + (int) DURATION + " seconds.");
                break;
            }
        }
        state.previous = targets;
    }

    private static void message(CombatEngineAPI engine, Color color, String text) {
        try {
            engine.getCombatUI().addMessage(0, color, text);
        } catch (Throwable ignored) {
            // No combat UI.
        }
    }

    /** The mark on one ship: raises the damage it takes while it lasts and draws the ring under it. */
    private static final class Mark extends BaseCombatLayeredRenderingPlugin {
        private final ShipAPI target;
        private final float mult;
        private float elapsed;
        private boolean over;

        Mark(ShipAPI target, float percent) {
            super(CombatEngineLayers.BELOW_SHIPS_LAYER);
            this.target = target;
            this.mult = 1f + percent / 100f;
        }

        @Override
        public void advance(float amount) {
            if (over) return;
            elapsed += amount;
            if (entity != null) entity.getLocation().set(target.getLocation());
            MutableShipStatsAPI stats = target.getMutableStats();
            if (elapsed > DURATION || !target.isAlive()) {
                over = true;
                stats.getEnergyDamageTakenMult().unmodify(KEY);
                stats.getKineticDamageTakenMult().unmodify(KEY);
                stats.getHighExplosiveDamageTakenMult().unmodify(KEY);
                stats.getFragmentationDamageTakenMult().unmodify(KEY);
                return;
            }
            stats.getEnergyDamageTakenMult().modifyMult(KEY, mult);
            stats.getKineticDamageTakenMult().modifyMult(KEY, mult);
            stats.getHighExplosiveDamageTakenMult().modifyMult(KEY, mult);
            stats.getFragmentationDamageTakenMult().modifyMult(KEY, mult);
        }

        @Override
        public boolean isExpired() {
            return over;
        }

        @Override
        public float getRenderRadius() {
            return target.getCollisionRadius() * 2f;
        }

        @Override
        public void render(CombatEngineLayers layer, ViewportAPI viewport) {
            if (over) return;
            float alpha = 1f;
            if (elapsed < 0.5f) alpha = elapsed / 0.5f;
            else if (DURATION - elapsed < 0.5f) alpha = Math.max(0f, (DURATION - elapsed) / 0.5f);
            float scale = 1f + 0.05f * (float) Math.sin(elapsed * 2f);
            float outer = target.getCollisionRadius();
            float inner = outer - 60f;
            int segments = 60;
            float texturePerSegment = 4f / segments;
            SpriteAPI sprite = Global.getSettings().getSprite("rbr_fx", "headhunting_edge");

            GL11.glPushMatrix();
            GL11.glTranslatef(target.getLocation().x, target.getLocation().y, 0f);
            GL11.glScalef(scale, scale, 1f);
            GL11.glRotatef(-15f * elapsed % 360f, 0f, 0f, 1f);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            sprite.bindTexture();
            Misc.setColor(Color.white, 0.5f * alpha * viewport.getAlphaMult());
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            for (int i = 0; i <= segments; i++) {
                double angle = Math.toRadians(360f / segments * i);
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);
                GL11.glTexCoord2f(texturePerSegment * i, 0f);
                GL11.glVertex2f(cos * inner, sin * inner);
                GL11.glTexCoord2f(texturePerSegment * i, 1f);
                GL11.glVertex2f(cos * outer, sin * outer);
            }
            GL11.glEnd();
            GL11.glPopMatrix();
        }
    }
}
