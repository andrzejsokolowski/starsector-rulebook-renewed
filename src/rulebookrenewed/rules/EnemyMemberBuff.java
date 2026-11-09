package rulebookrenewed.rules;

import com.fs.starfarer.api.campaign.BuffManagerAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import rulebookrenewed.RuleCatalog;
import rulebookrenewed.RuleSpec;

/**
 * A buff on one enemy ship, put there before a battle by BattlePrep. Being part of the ship's stats, it is
 * built into the ship when it deploys, so armor bonuses land on the real armor grid. Removed when the
 * battle ends, and runs out on its own two days after it was placed.
 */
public final class EnemyMemberBuff implements BuffManagerAPI.Buff {

    private static final float LIFETIME_DAYS = 2f;

    private final String ruleId;
    private final String buffId;
    final int level;
    final float value;
    private float daysLeft = LIFETIME_DAYS;

    public EnemyMemberBuff(String ruleId, String buffId, int level, float value) {
        this.ruleId = ruleId;
        this.buffId = buffId;
        this.level = level;
        this.value = value;
    }

    public boolean matches(int level, float value) {
        return this.level == level && this.value == value;
    }

    @Override
    public void apply(FleetMemberAPI member) {
        RuleSpec rule = RuleCatalog.get(ruleId);
        if (rule != null) rule.effect.applyToEnemyMember(rule, member, level, value);
    }

    @Override
    public String getId() {
        return buffId;
    }

    @Override
    public boolean isExpired() {
        return daysLeft <= 0f;
    }

    @Override
    public void advance(float days) {
        daysLeft -= days;
    }
}
