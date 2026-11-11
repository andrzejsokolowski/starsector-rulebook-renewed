package rulebookrenewed;

import java.util.Collections;
import java.util.List;

import achievementsrenewed.api.AchievementSpec;
import achievementsrenewed.api.Achievements;

/**
 * One line of data/config/rulebook/challenges.csv: hard rules that must be at full level from the first
 * days of the campaign until the maximum level, an optional cap on positive rules, an optional time limit
 * and an optional goal that must hold at the end. Finishing it unlocks every rule whose unlock column
 * says challenge:&lt;id&gt;.
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
    /** Achievements - Renewed prefix of a finished challenge; New Game Plus - Renewed skips these ids. */
    public static final String PREFIX = "rbr_challenge_";

    /**
     * An Achievements - Renewed achievement whose count in this save must be at least goalAmount when the
     * maximum level is reached, or null.
     */
    public final String goalId;
    public final int goalAmount;
    /** Most positive points allowed while the challenge runs; -1 for no cap. */
    public final int helpfulLimit;
    public final float order;

    ChallengeSpec(String id, String name, List<Requirement> requirements, String goalId, int goalAmount,
                  int helpfulLimit, float order) {
        this.id = id;
        this.name = name;
        this.requirements = Collections.unmodifiableList(requirements);
        this.goalId = goalId;
        this.goalAmount = goalAmount;
        this.helpfulLimit = helpfulLimit;
        this.order = order;
    }

    /** The achievement that records this challenge as done. */
    public String achievementId() {
        return PREFIX + id;
    }

    /** The goal achievement's name, or its id while Achievements - Renewed does not know it. */
    private String goalName() {
        AchievementSpec spec = Achievements.get(goalId);
        return spec == null || spec.name.isEmpty() ? goalId : spec.name;
    }

    public String goalText() {
        String text = "Switch these rules on at full level while you are level " + Challenges.START_LEVEL
                + " and keep them until you reach level " + Challenges.maxLevel();
        if (goalId != null) {
            String name = goalName();
            String what = Character.toLowerCase(name.charAt(0)) + name.substring(1);
            text += ", and " + what + (goalAmount > 1 ? " (" + goalAmount + ")" : "");
        }
        return text;
    }

    /** What this challenge asks beyond the common rule: the cap on positive rules and the goal. */
    public String extraText() {
        StringBuilder out = new StringBuilder();
        String limit = limitText();
        if (!limit.isEmpty()) out.append(limit).append('.');
        if (goalId != null) {
            out.append(out.length() > 0 ? "  " : "").append("At the end: ").append(goalName())
                    .append(goalAmount > 1 ? " (" + goalAmount + ")" : "").append('.');
        }
        return out.toString();
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
        if (helpfulLimit < 0) return "";
        if (helpfulLimit == 0) return "No positive rules";
        return "Positive rules up to " + helpfulLimit + " points";
    }
}
