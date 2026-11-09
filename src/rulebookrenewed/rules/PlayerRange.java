package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Ballistic and energy weapons on the player's ships reach farther (helpful) or shorter (hard).
 */
public final class PlayerRange extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        float percent = Math.max(-95f, sign * value);
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, percent, source);
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, percent, source);
    }
}
