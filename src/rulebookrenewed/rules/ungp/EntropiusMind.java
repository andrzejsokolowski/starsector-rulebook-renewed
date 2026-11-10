package rulebookrenewed.rules.ungp;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleCatalog;
import rulebookrenewed.RuleManager;
import rulebookrenewed.RuleSpec;
import rulebookrenewed.RuleState;
import rulebookrenewed.rules.RuleEffect;

/**
 * Helpful (it costs nothing): every 7 days every other rule is switched off and 1 to 12 random rules,
 * positive or negative, come on at a random level. Locked rules are never picked; of two mirrors or two
 * golden rules the one picked later stays. Adapted from Entropius Mind in Unofficial New Game Plus by
 * Originem (which could switch on any number of rules, up to all of them).
 */
public final class EntropiusMind implements RuleEffect {

    private static final String SINCE_KEY = "rbr_entropiusSince";
    private static final float DAYS = 7f;
    private static final int MAX_RULES = 12;

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        if (Tools.daysSince(SINCE_KEY) < DAYS) return;
        Tools.restart(SINCE_KEY);
        for (RuleSpec other : RuleCatalog.all()) {
            if (other != rule && RuleState.level(other) > 0) RuleManager.setLevel(other, 0);
        }
        List<RuleSpec> pool = new ArrayList<>();
        for (RuleSpec other : RuleCatalog.all()) {
            if (other != rule && RandomRules.canRoll(other)) pool.add(other);
        }
        int count = 1 + Tools.RANDOM.nextInt(MAX_RULES);
        for (int i = 0; i < count && !pool.isEmpty(); i++) {
            RuleSpec picked = pool.remove(Tools.RANDOM.nextInt(pool.size()));
            RuleManager.setLevel(picked, 1 + Tools.RANDOM.nextInt(picked.levels));
        }
        Tools.note(rule.name + ": your rules have changed.", Misc.getHighlightColor());
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        Tools.forget(SINCE_KEY);
    }
}
