package rulebookrenewed.rules.ungp;

import java.awt.Color;
import java.util.Map;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;

/**
 * Small helpers for the rules adapted from Unofficial New Game Plus: random geometry in place of the
 * LazyLib calls its code used, state kept on the combat engine, and timers kept in the save.
 */
final class Tools {

    static final Random RANDOM = new Random();

    private Tools() {}

    /** State a combat rule keeps for one battle, or null. */
    @SuppressWarnings("unchecked")
    static <T> T data(CombatEngineAPI engine, String key) {
        return (T) engine.getCustomData().get(key);
    }

    static boolean roll(float chance) {
        return RANDOM.nextFloat() < chance;
    }

    static float between(float min, float max) {
        return min + RANDOM.nextFloat() * (max - min);
    }

    static Vector2f pointOnCircle(Vector2f center, float radius, float degrees) {
        double angle = Math.toRadians(degrees);
        return new Vector2f(center.x + (float) Math.cos(angle) * radius, center.y + (float) Math.sin(angle) * radius);
    }

    static Vector2f randomPointOnCircle(Vector2f center, float radius) {
        return pointOnCircle(center, radius, RANDOM.nextFloat() * 360f);
    }

    static Vector2f randomPointInCircle(Vector2f center, float radius) {
        return pointOnCircle(center, radius * (float) Math.sqrt(RANDOM.nextFloat()), RANDOM.nextFloat() * 360f);
    }

    /** True once each time the month changes while the rule is on. The key lives in the save. */
    static boolean monthPassed(String key) {
        CampaignClockAPI clock = Global.getSector().getClock();
        int month = clock.getCycle() * 12 + clock.getMonth();
        Map<String, Object> data = Global.getSector().getPersistentData();
        Object last = data.get(key);
        data.put(key, month);
        return last instanceof Integer && (Integer) last != month;
    }

    /** Days since the time kept under the key; starts the clock (and returns 0) when there is none. */
    static float daysSince(String key) {
        Map<String, Object> data = Global.getSector().getPersistentData();
        Object since = data.get(key);
        if (!(since instanceof Long)) {
            restart(key);
            return 0f;
        }
        return Global.getSector().getClock().getElapsedDaysSince((Long) since);
    }

    static void restart(String key) {
        Global.getSector().getPersistentData().put(key, Global.getSector().getClock().getTimestamp());
    }

    static void forget(String key) {
        Global.getSector().getPersistentData().remove(key);
    }

    /** A line in the campaign message list. */
    static void note(String text, Color color) {
        try {
            Global.getSector().getCampaignUI().addMessage(text, color);
        } catch (Throwable ignored) {
            // No campaign UI yet.
        }
    }

    static void uiSound(String id) {
        try {
            Global.getSoundPlayer().playUISound(id, 1f, 1f);
        } catch (Throwable ignored) {
            // A missing sound must not stop a rule.
        }
    }
}
