package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.campaign.BuffManagerAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import rulebookrenewed.RuleCatalog;
import rulebookrenewed.RuleSpec;

/**
 * The buff a ConditionalMemberRule keeps on a ship of the player's fleet while the rule acts on it. It
 * carries the strength worked out for that ship and runs out a day after the last refresh.
 */
public final class ConditionalMemberBuff implements BuffManagerAPI.Buff {

    private static final float LIFETIME_DAYS = 1f;

    private final String ruleId;
    private final String buffId;
    final float strength;
    private float daysLeft = LIFETIME_DAYS;

    ConditionalMemberBuff(String ruleId, String buffId, float strength) {
        this.ruleId = ruleId;
        this.buffId = buffId;
        this.strength = strength;
    }

    void refresh() {
        daysLeft = LIFETIME_DAYS;
    }

    @Override
    public void apply(FleetMemberAPI member) {
        RuleSpec rule = RuleCatalog.get(ruleId);
        if (rule != null && rule.effect instanceof ConditionalMemberRule) {
            ((ConditionalMemberRule) rule.effect).modify(member, buffId, strength, rule);
        }
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
