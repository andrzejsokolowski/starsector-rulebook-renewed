package rulebookrenewed.rules.ungp;

import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import rulebookrenewed.RuleCatalog;
import rulebookrenewed.RuleManager;
import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Golden: every few months a random positive rule switches on at level I. The first comes after 3 months
 * and each wait is 3 months longer than the last. Golden rules, rules with an unlock and rules whose
 * mirror is on are never picked; cheaper rules are more likely. The count of months stays in the save
 * when the rule is switched off. Adapted from Still Growing Up in Unofficial New Game Plus by Originem.
 */
public final class StillGrowingUp implements RuleEffect {

    private static final String MONTH_KEY = "rbr_growingMonth";
    private static final String ELAPSED_KEY = "rbr_growingElapsed";
    private static final String WAIT_KEY = "rbr_growingWait";
    private static final int STEP_MONTHS = 3;

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        if (!Tools.monthPassed(MONTH_KEY)) return;
        Map<String, Object> data = Global.getSector().getPersistentData();
        int elapsed = number(data.get(ELAPSED_KEY), 0) + 1;
        int wait = number(data.get(WAIT_KEY), STEP_MONTHS);
        if (elapsed >= wait) {
            grow(rule);
            elapsed = 0;
            wait += STEP_MONTHS;
        }
        data.put(ELAPSED_KEY, elapsed);
        data.put(WAIT_KEY, wait);
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        Tools.forget(MONTH_KEY);
    }

    private static void grow(RuleSpec self) {
        WeightedRandomPicker<RuleSpec> picker = new WeightedRandomPicker<>(Tools.RANDOM);
        for (RuleSpec rule : RuleCatalog.all()) {
            if (rule.hard || rule.golden || !rule.unlock.isEmpty() || rule.points(1) <= 0) continue;
            if (!RandomRules.canRoll(rule) || RandomRules.mirrorIsOn(rule)) continue;
            picker.add(rule, Math.max(1f, 12f - rule.points(1)));
        }
        RuleSpec gained = picker.pick();
        if (gained == null) return;
        RuleManager.setLevel(gained, 1);
        Tools.note(self.name + ": " + gained.name + " switched on.", Misc.getPositiveHighlightColor());
        Tools.uiSound("rbr_still_growing_up");
    }

    private static int number(Object stored, int fallback) {
        return stored instanceof Integer ? (Integer) stored : fallback;
    }
}
