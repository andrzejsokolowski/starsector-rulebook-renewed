package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Enemy ships take less damage (hard) or more damage (helpful) to shields, armor and hull.
 */
public final class EnemyDamageTaken extends CombatStatRule {

    @Override
    protected boolean targetsEnemy() {
        return true;
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign) {
        float mult = Math.max(0.05f, 1f - sign * value / 100f);
        stats.getShieldDamageTakenMult().modifyMult(id, mult);
        stats.getArmorDamageTakenMult().modifyMult(id, mult);
        stats.getHullDamageTakenMult().modifyMult(id, mult);
    }
}
