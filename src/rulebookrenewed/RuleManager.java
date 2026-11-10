package rulebookrenewed;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;

/** Applies the chosen rules to the campaign and switches them when the player changes the book. */
public final class RuleManager {

    private static final Logger LOG = Global.getLogger(RuleManager.class);

    private RuleManager() {}

    /** Brings every rule in line with this save's choices. Cheap enough to run every few hours of game time. */
    public static void refreshAll() {
        if (Global.getSector() == null) return;
        for (RuleSpec rule : RuleCatalog.all()) refresh(rule);
        Balance.publish();
    }

    private static void refresh(RuleSpec rule) {
        try {
            int level = RuleState.level(rule);
            if (level > 0) rule.effect.applyCampaign(rule, level, rule.value(level));
            else rule.effect.unapplyCampaign(rule);
        } catch (Throwable t) {
            LOG.error("Rule Book: rule " + rule.id + " failed to apply", t);
        }
    }

    /**
     * The picker's one entry point: sets a level (0 = off) and applies it at once. A hard rule and its
     * helpful mirror cannot both be on, so switching one on switches the other off.
     */
    public static void setLevel(RuleSpec rule, int level) {
        int old = RuleState.level(rule);
        if (level > 0 && old == 0 && !Unlocks.isUnlocked(rule)) return;
        if (level > 0) {
            for (RuleSpec mirror : RuleCatalog.all()) {
                if (mirror != rule && mirror.hard != rule.hard && mirror.pair.equals(rule.pair) && RuleState.level(mirror) > 0) {
                    setLevel(mirror, 0);
                }
            }
        }
        RuleState.setLevel(rule, level);
        if (old != level) {
            try {
                if (level > 0) rule.effect.unapplyCampaign(rule);
                if (old == 0 && level > 0) rule.effect.onSwitchedOn(rule, level, rule.value(level));
            } catch (Throwable t) {
                LOG.error("Rule Book: rule " + rule.id + " failed to switch", t);
            }
        }
        refresh(rule);
        Balance.publish();
        if (old != level) Challenges.check();
    }
}
