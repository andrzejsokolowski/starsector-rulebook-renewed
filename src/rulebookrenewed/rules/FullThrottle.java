package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;

import rulebookrenewed.RuleSpec;

/**
 * Helpful: the player's own ships moving at 90% of their top speed or more have more mass, so they push
 * harder in collisions and are pushed less. Their engines are untouched. Adapted from Full Throttle in
 * Unofficial Rule Book by DemonX3 (MIT licence, see CREDITS.txt).
 */
public final class FullThrottle implements RuleEffect {

    private static final float SPEED_SHARE = 0.9f;

    @Override
    public void applyToShip(RuleSpec rule, ShipAPI ship, boolean enemy, int level, float value) {
        if (enemy || ship.isAlly() || ship.isFighter() || ship.isStation() || ship.isStationModule()) return;
        if (ship.hasListenerOfClass(Ram.class)) return;
        ship.addListener(new Ram(ship, 1f + value / 100f));
    }

    private static final class Ram implements AdvanceableListener {
        private final ShipAPI ship;
        private final float baseMass;
        private final float fastMass;

        Ram(ShipAPI ship, float mult) {
            this.ship = ship;
            this.baseMass = ship.getMass();
            this.fastMass = baseMass * mult;
        }

        @Override
        public void advance(float amount) {
            if (amount <= 0f) return;
            boolean fast = ship.isAlive() && ship.getVelocity().length() >= ship.getMaxSpeed() * SPEED_SHARE;
            float want = fast ? fastMass : baseMass;
            if (ship.getMass() != want) ship.setMass(want);
        }
    }
}
