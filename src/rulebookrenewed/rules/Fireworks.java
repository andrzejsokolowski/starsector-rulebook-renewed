package rulebookrenewed.rules;

import java.awt.Color;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import rulebookrenewed.RuleSpec;

/**
 * Fluff: every ship destroyed in a campaign battle bursts into coloured sparks, more for bigger hulls.
 * No effect on the fight. Each wreck bursts once; the pieces it breaks into do not.
 */
public final class Fireworks implements RuleEffect {

    private static final String KEY = "rbr_fireworks";
    private static final Random RANDOM = new Random();

    @Override
    @SuppressWarnings("unchecked")
    public void advanceCombat(RuleSpec rule, CombatEngineAPI engine, float amount, int level, float value) {
        Set<ShipAPI> done = (Set<ShipAPI>) engine.getCustomData().get(KEY);
        if (done == null) {
            done = new HashSet<>();
            engine.getCustomData().put(KEY, done);
            // Wrecks already on the field when the rule starts watching do not burst.
            for (ShipAPI ship : engine.getShips()) if (ship.isHulk()) done.add(ship);
            return;
        }
        for (ShipAPI ship : engine.getShips()) {
            if (!ship.isHulk() || ship.isFighter() || ship.isPiece() || !done.add(ship)) continue;
            burst(engine, ship);
        }
    }

    private static void burst(CombatEngineAPI engine, ShipAPI ship) {
        int size = ship.getHullSize() == null ? 1 : Math.max(1, ship.getHullSize().ordinal() - 1);
        int sparks = 30 + 25 * size;
        float speed = 120f + 60f * size;
        Vector2f center = ship.getLocation();
        for (int i = 0; i < sparks; i++) {
            float angle = RANDOM.nextFloat() * (float) (Math.PI * 2.0);
            float v = speed * (0.4f + RANDOM.nextFloat() * 0.6f);
            Vector2f velocity = new Vector2f((float) Math.cos(angle) * v, (float) Math.sin(angle) * v);
            Color color = Color.getHSBColor(RANDOM.nextFloat(), 0.75f, 1f);
            engine.addHitParticle(new Vector2f(center), velocity, 8f + RANDOM.nextFloat() * 10f, 1f,
                    0.8f + RANDOM.nextFloat() * 1.2f, color);
        }
    }
}
