package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;

import rulebookrenewed.RuleSpec;

/**
 * Hard: in combat, the armor of the player's ships and allies corrodes every second, down to half of each
 * cell's maximum. Adapted from Blight in Unofficial Rule Book by DemonX3 (MIT licence, see CREDITS.txt).
 */
public final class Blight implements RuleEffect {

    private static final float FLOOR = 0.5f;

    @Override
    public void applyToShip(RuleSpec rule, ShipAPI ship, boolean enemy, int level, float value) {
        if (enemy || ship.isFighter() || ship.hasListenerOfClass(Corrosion.class)) return;
        ship.addListener(new Corrosion(ship, value));
    }

    private static final class Corrosion implements AdvanceableListener {
        private final ShipAPI ship;
        private final float percentPerSecond;

        Corrosion(ShipAPI ship, float percentPerSecond) {
            this.ship = ship;
            this.percentPerSecond = percentPerSecond;
        }

        @Override
        public void advance(float amount) {
            if (amount <= 0f || ship.isHulk() || !ship.isAlive()) return;
            ArmorGridAPI armor = ship.getArmorGrid();
            if (armor == null) return;
            float max = armor.getMaxArmorInCell();
            float floor = max * FLOOR;
            float drain = max * percentPerSecond / 100f * amount;
            float[][] grid = armor.getGrid();
            for (int x = 0; x < grid.length; x++) {
                for (int y = 0; y < grid[x].length; y++) {
                    float cell = grid[x][y];
                    if (cell > floor) armor.setArmorValue(x, y, Math.max(floor, cell - drain));
                }
            }
        }
    }
}
