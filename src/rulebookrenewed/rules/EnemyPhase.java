package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

/**
 * Enemy phase ships gain more (hard) or less (helpful) time flow while phased, and their cloak
 * cools down faster or slower. PhaseCloakStats reads Stats.PHASE_TIME_BONUS_MULT live every frame.
 */
public final class EnemyPhase extends CombatStatRule {

    @Override
    protected boolean targetsEnemy() {
        return true;
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign) {
        Scale.by(stats.getDynamic().getStat(Stats.PHASE_TIME_BONUS_MULT), id, value, sign, null);
        Scale.by(stats.getPhaseCloakCooldownBonus(), id, value, -sign, null);
    }
}
