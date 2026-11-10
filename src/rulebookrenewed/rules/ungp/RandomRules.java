package rulebookrenewed.rules.ungp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import rulebookrenewed.RuleCatalog;
import rulebookrenewed.RuleSpec;
import rulebookrenewed.RuleState;
import rulebookrenewed.Unlocks;

/** Which rules the rules that hand out other rules (Still Growing Up, Birthday Present, Entropius Mind) may pick. */
final class RandomRules {

    /** Rules that hand out rules themselves, or act once when chosen. */
    private static final Set<String> NEVER = new HashSet<>(Arrays.asList(
            "rbr_still_growing_up", "rbr_birthday_present", "rbr_entropius_mind", "rbr_tach_labour",
            "rbr_full_of_company", "rbr_peerless"));

    private RandomRules() {}

    /** True for a rule that is off, open to the player and not on the list above. */
    static boolean canRoll(RuleSpec rule) {
        return RuleState.level(rule) == 0 && !NEVER.contains(rule.id) && Unlocks.isUnlocked(rule);
    }

    /** True when the rule's mirror is on: switching the rule on would switch that one off. */
    static boolean mirrorIsOn(RuleSpec rule) {
        for (RuleSpec other : RuleCatalog.all()) {
            if (other != rule && other.hard != rule.hard && other.pair.equals(rule.pair) && RuleState.level(other) > 0) {
                return true;
            }
        }
        return false;
    }
}
