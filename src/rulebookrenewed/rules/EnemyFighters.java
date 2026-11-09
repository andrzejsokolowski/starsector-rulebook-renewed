package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Enemy fighters are replaced faster (hard) or slower (helpful). Refit time is the stat, so a
 * stronger enemy has a shorter time.
 */
public final class EnemyFighters extends CombatStatRule {

    @Override
    protected boolean targetsEnemy() {
        return true;
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign) {
        Scale.by(stats.getFighterRefitTimeMult(), id, value, -sign, null);
    }
}
