package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Enemy ballistic and energy weapons reach farther (hard) or shorter (helpful).
 */
public final class EnemyRange extends CombatStatRule {

    @Override
    protected boolean targetsEnemy() {
        return true;
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign) {
        float percent = Math.max(-95f, sign * value);
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, percent);
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, percent);
    }
}
