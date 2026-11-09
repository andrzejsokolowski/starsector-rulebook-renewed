package rulebookrenewed.rules;

import com.fs.starfarer.api.campaign.BuffManagerAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import rulebookrenewed.RuleCatalog;
import rulebookrenewed.RuleSpec;

/**
 * A buff on one ship of the player's fleet. Buffs are re-applied whenever the game rebuilds a ship's
 * stats, which a plain stat change on a fleet member would not survive. It runs out a day after the
 * last refresh, so a ship that leaves the fleet loses it on its own.
 */
public final class RuleMemberBuff implements BuffManagerAPI.Buff {

    private static final float LIFETIME_DAYS = 1f;

    private final String ruleId;
    private final String buffId;
    final int level;
    final float value;
    private float daysLeft = LIFETIME_DAYS;

    RuleMemberBuff(String ruleId, String buffId, int level, float value) {
        this.ruleId = ruleId;
        this.buffId = buffId;
        this.level = level;
        this.value = value;
    }

    void refresh() {
        daysLeft = LIFETIME_DAYS;
    }

    @Override
    public void apply(FleetMemberAPI member) {
        RuleSpec rule = RuleCatalog.get(ruleId);
        if (rule != null) rule.effect.applyToMember(rule, member, level, value);
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
