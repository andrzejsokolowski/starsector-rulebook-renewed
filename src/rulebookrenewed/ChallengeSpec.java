package rulebookrenewed;

import java.util.Collections;
import java.util.List;

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
    /** A feat whose count must be at least goalAmount when the maximum level is reached, or null. */
    public final Feats.Feat goalFeat;
    public final int goalAmount;
    /** Most positive points allowed while the challenge runs; -1 for no cap. */
    public final int helpfulLimit;
    /** The campaign day by which the maximum level must be reached, or 0 for no time limit. */
    public final int deadlineDays;
    public final float order;

    ChallengeSpec(String id, String name, List<Requirement> requirements, Feats.Feat goalFeat, int goalAmount,
                  int helpfulLimit, int deadlineDays, float order) {
        this.id = id;
        this.name = name;
        this.requirements = Collections.unmodifiableList(requirements);
        this.goalFeat = goalFeat;
        this.goalAmount = goalAmount;
        this.helpfulLimit = helpfulLimit;
        this.deadlineDays = Math.max(0, deadlineDays);
        this.order = order;
    }

    /** The feat id recorded in saves/common when the challenge is done. */
    public String featId() {
        return "challenge_" + id;
    }

    public String goalText() {
        String text = "Have these rules at full level by day " + Challenges.START_DAYS
                + " and keep them until you reach level " + Challenges.maxLevel();
        if (deadlineDays > 0) text += " by day " + deadlineDays;
        if (goalFeat != null) {
            String what = goalFeat.name.isEmpty() ? goalFeat.id
                    : Character.toLowerCase(goalFeat.name.charAt(0)) + goalFeat.name.substring(1);
            text += ", and " + what + (goalAmount > 1 ? " (" + goalAmount + ")" : "");
        }
        return text;
    }

    /** What this challenge asks beyond the common rule: the cap on positive rules, the time limit, the goal. */
    public String extraText() {
        StringBuilder out = new StringBuilder();
        String limit = limitText();
        if (!limit.isEmpty()) out.append(limit).append('.');
        if (deadlineDays > 0) {
            out.append(out.length() > 0 ? "  " : "").append("Reach level ").append(Challenges.maxLevel())
                    .append(" by day ").append(deadlineDays).append('.');
        }
        if (goalFeat != null) {
            out.append(out.length() > 0 ? "  " : "").append("At the end: ").append(goalFeat.name)
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
