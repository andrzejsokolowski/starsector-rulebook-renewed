package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Overloads on the player's and allied ships last longer (hard) or end sooner (helpful).
 * Effect adapted from Thin Insulator in Unofficial Rule Book by DemonX3 (MIT licence, see CREDITS.txt).
 */
public final class PlayerOverload extends CombatStatRule {

    @Override
    protected boolean targetsEnemy() {
        return false;
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign) {
        if (sign > 0f) stats.getOverloadTimeMod().modifyMult(id, Math.max(0.05f, 1f - value / 100f));
        else stats.getOverloadTimeMod().modifyPercent(id, value);
    }
}
