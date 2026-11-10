package rulebookrenewed.rules;

import rulebookrenewed.RuleSpec;

/**
 * A rule that scales a group of settings.json values. Subclasses name the keys and say whether a helpful
 * rule raises them (bounties, XP) or lowers them (prices).
 */
public abstract class SettingRule implements RuleEffect {

    protected abstract String[] keys();

    /** True when a bigger value is good for the player. */
    protected abstract boolean higherHelps();

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        float sign = RuleEffect.playerSign(rule) * (higherHelps() ? 1f : -1f);
        SettingScale.apply(keys(), Scale.mult(value, sign));
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        SettingScale.restore(keys());
    }
}
