package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Hard: the player's and allied ships stay overloaded longer.
 * Effect adapted from Thin Insulator in Unofficial Rule Book by DemonX3 (MIT licence, see CREDITS.txt).
 */
public final class ThinInsulation extends CombatStatRule {

    @Override
    protected boolean targetsEnemy() {
        return false;
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value) {
        stats.getOverloadTimeMod().modifyPercent(id, value);
    }
}
