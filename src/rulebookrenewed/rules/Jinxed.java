package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Hard: weapons and engines of the player's ships and allies malfunction now and then, at any combat
 * readiness. Adapted from Jinxed in Unofficial Rule Book by DemonX3 (MIT licence, see CREDITS.txt).
 */
public final class Jinxed extends CombatStatRule {

    @Override
    protected boolean targetsEnemy() {
        return false;
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign) {
        stats.getWeaponMalfunctionChance().modifyFlat(id, value / 100f);
        stats.getEngineMalfunctionChance().modifyFlat(id, value / 1000f);
    }
}
