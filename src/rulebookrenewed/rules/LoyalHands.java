package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

import rulebookrenewed.RuleSpec;

/**
 * Positive: the crew gives back part of its wages each month (CrewPay does the books). At the top level
 * it takes no pay at all, and the ships lose maximum combat readiness for it.
 */
public final class LoyalHands extends MemberStatRule {

    private static final float NO_PAY = 100f, READINESS_LOST = 0.1f;

    private final CrewPay pay = new CrewPay();

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        pay.applyCampaign(rule, level, value);
        if (value >= NO_PAY) super.applyCampaign(rule, level, value);
        else super.unapplyCampaign(rule);
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        pay.unapplyCampaign(rule);
        super.unapplyCampaign(rule);
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        if (value >= NO_PAY) stats.getMaxCombatReadiness().modifyFlat(id, -READINESS_LOST, source);
        else stats.getMaxCombatReadiness().unmodify(id);
    }
}
