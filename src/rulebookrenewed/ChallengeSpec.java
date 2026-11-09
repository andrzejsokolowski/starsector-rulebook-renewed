package rulebookrenewed;

import java.util.Collections;
import java.util.List;

/**
 * One line of data/config/rulebook/challenges.csv: rules that must stay at or above a level, a goal, and
 * an optional cap on helpful rules. Finishing it unlocks every rule whose unlock column says
 * challenge:&lt;id&gt;.
 */
public final class ChallengeSpec {

    public static final class Requirement {
        public final RuleSpec rule;
        public final int level;

        Requirement(RuleSpec rule, int level) {
            this.rule = rule;
            this.level = level;
        }
    }

    public final String id;
    public final String name;
    public final List<Requirement> requirements;
    /** A feat id; the goal is met when its count rises by goalAmount after the challenge starts. Null for a days goal. */
    public final Feats.Feat goalFeat;
    /** Days to hold out when goalFeat is null, otherwise how far the feat's count must rise. */
    public final int goalAmount;
    /** Most helpful points allowed while the challenge runs; -1 for no cap. */
    public final int helpfulLimit;
    public final String description;
    public final String flavor;
    public final float order;

    ChallengeSpec(String id, String name, List<Requirement> requirements, Feats.Feat goalFeat, int goalAmount,
                  int helpfulLimit, String description, String flavor, float order) {
        this.id = id;
        this.name = name;
        this.requirements = Collections.unmodifiableList(requirements);
        this.goalFeat = goalFeat;
        this.goalAmount = goalAmount;
        this.helpfulLimit = helpfulLimit;
        this.description = description;
        this.flavor = flavor;
        this.order = order;
    }

    /** The feat id recorded in saves/common when the challenge is done. */
    public String featId() {
        return "challenge_" + id;
    }

    public int levelFor(RuleSpec rule) {
        for (Requirement r : requirements) if (r.rule == rule) return r.level;
        return 0;
    }

    public String goalText() {
        if (goalFeat == null) return "Hold out for " + goalAmount + " days";
        if (goalFeat.id.equals(Counters.WINS)) return "Win " + goalAmount + (goalAmount == 1 ? " battle" : " battles");
        if (goalAmount <= 1) return goalFeat.name;
        return goalFeat.name + ", " + goalAmount + " times";
    }

    public String requirementsText() {
        StringBuilder out = new StringBuilder();
        for (Requirement r : requirements) {
            if (out.length() > 0) out.append(", ");
            out.append(r.rule.name);
            if (r.rule.levels > 1) out.append(' ').append(RuleSpec.roman(r.level));
        }
        return out.toString();
    }

    public String limitText() {
        if (helpfulLimit < 0) return "Helpful rules allowed";
        if (helpfulLimit == 0) return "No helpful rules";
        return "Helpful rules up to " + helpfulLimit + " points";
    }
}
