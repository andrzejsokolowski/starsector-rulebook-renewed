package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import rulebookrenewed.RuleSpec;

/**
 * A rule that changes enemy ships before they are built for a battle, for stats the game fixes when a
 * ship is created (armor). BattlePrep puts an EnemyMemberBuff on every enemy ship when the encounter
 * opens and again on every ship still in reserve during the battle.
 */
public abstract class EnemyMemberStatRule implements RuleEffect {

    /** sign: +1 makes enemies stronger (hard rule), -1 weaker (helpful rule). */
    protected abstract void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source);

    @Override
    public boolean usesEnemyMemberBuff() {
        return true;
    }

    @Override
    public void applyToEnemyMember(RuleSpec rule, FleetMemberAPI member, int level, float value) {
        modify(member.getStats(), RuleEffect.modId(rule), value, RuleEffect.enemySign(rule), rule.name);
    }
}
