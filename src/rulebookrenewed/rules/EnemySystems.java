package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Enemy ship systems cool down and recharge faster (hard) or slower (helpful).
 */
public final class EnemySystems extends CombatStatRule {

    @Override
    protected boolean targetsEnemy() {
        return true;
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign) {
        Scale.by(stats.getSystemCooldownBonus(), id, value, -sign, null);
        Scale.by(stats.getSystemRegenBonus(), id, value, sign, null);
    }
}
