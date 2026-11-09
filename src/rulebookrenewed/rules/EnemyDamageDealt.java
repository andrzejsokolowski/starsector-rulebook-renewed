package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Enemy weapons deal more damage (hard) or less damage (helpful). Fighters too.
 */
public final class EnemyDamageDealt extends CombatStatRule {

    @Override
    protected boolean targetsEnemy() {
        return true;
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign) {
        float percent = Math.max(-95f, sign * value);
        stats.getBallisticWeaponDamageMult().modifyPercent(id, percent);
        stats.getEnergyWeaponDamageMult().modifyPercent(id, percent);
        stats.getMissileWeaponDamageMult().modifyPercent(id, percent);
    }
}
