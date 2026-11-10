package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import rulebookrenewed.RuleSpec;

/**
 * Helpful: in combat, the ship the player flies regrows a share of its maximum armor every second, up to
 * each cell's maximum. Follows the player when they switch ships.
 */
public final class LivingPlating implements RuleEffect {

    @Override
    public void advanceCombat(RuleSpec rule, CombatEngineAPI engine, float amount, int level, float value) {
        ShipAPI ship = engine.getPlayerShip();
        if (ship == null || amount <= 0f || ship.getOwner() != 0 || ship.isHulk() || !ship.isAlive()
                || ship.isShuttlePod()) return;
        ArmorGridAPI armor = ship.getArmorGrid();
        if (armor == null) return;
        float max = armor.getMaxArmorInCell();
        float regrow = max * value / 100f * amount;
        float[][] grid = armor.getGrid();
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                float cell = grid[x][y];
                if (cell < max) armor.setArmorValue(x, y, Math.min(max, cell + regrow));
            }
        }
    }
}
