package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import rulebookrenewed.RuleCatalog;
import rulebookrenewed.RuleManager;
import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Helpful: one cycle (365 days) after the rule is switched on it switches itself off and a random
 * positive rule comes on at level I in its place. Costlier rules are more likely. Golden rules, locked
 * rules, rules whose mirror is on and rules without a cost are never picked. Switching the rule off
 * starts the wait over. Adapted from Birthday Present in Unofficial
 * New Game Plus by Originem.
 */
public final class BirthdayPresent implements RuleEffect {

    private static final String SINCE_KEY = "rbr_birthdaySince";
    private static final float DAYS = 365f;

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        if (Tools.daysSince(SINCE_KEY) < DAYS) return;
        WeightedRandomPicker<RuleSpec> picker = new WeightedRandomPicker<>(Tools.RANDOM);
        for (RuleSpec other : RuleCatalog.all()) {
            if (other.hard || other.golden || other.points(1) <= 0) continue;
            if (!RandomRules.canRoll(other) || RandomRules.mirrorIsOn(other)) continue;
            picker.add(other, other.points(1));
        }
        RuleSpec gift = picker.pick();
        RuleManager.setLevel(rule, 0);
        Tools.forget(SINCE_KEY);
        if (gift == null) return;
        RuleManager.setLevel(gift, 1);
        Tools.note(rule.name + ": " + gift.name + " switched on.", Misc.getPositiveHighlightColor());
        Tools.uiSound("rbr_birthday_present");
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        Tools.forget(SINCE_KEY);
    }
}
