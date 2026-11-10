package rulebookrenewed;

import rulebookrenewed.rules.RuleEffect;

/** One rule from rules.csv: its levels with points and values, what unlocks it, and its effect. */
public final class RuleSpec {

    public static final int MAX_LEVELS = 5;

    public final String id;
    public final String name;
    /** A negative rule (makes the game harder, gives points). Otherwise positive (costs points, or none). */
    public final boolean hard;
    public final int levels;
    private final int[] points;
    private final String[] valueTexts;
    private final float[] values;
    /** Mod ids, `|` between them, of which one must be enabled; empty for none. */
    public final String requiredMod;
    /** `challenge:<id>`, `achievement:<id of an Achievements - Renewed achievement>` or empty. */
    public final String unlock;
    public final String icon;
    /** The effect text; `||` separates a text per level (the last one serves the levels after it). */
    public final String description;
    private final String[] descriptions;
    /** A golden rule: only one of them can be on. */
    public final boolean golden;
    public final float order;
    /** Shared by a negative rule and its positive mirror; the two are never on together. */
    public final String pair;
    public final String section;
    public final RuleEffect effect;

    RuleSpec(String id, String name, boolean hard, int[] points, String[] valueTexts, String requiredMod,
             String unlock, String icon, String description, float order, String pair, String section,
             boolean golden, RuleEffect effect) {
        this.id = id;
        this.name = name;
        this.hard = hard;
        this.levels = points.length;
        this.points = points;
        this.valueTexts = valueTexts;
        this.values = new float[valueTexts.length];
        for (int i = 0; i < valueTexts.length; i++) values[i] = Float.parseFloat(valueTexts[i]);
        this.requiredMod = requiredMod;
        this.unlock = unlock;
        this.icon = icon;
        this.description = description;
        this.descriptions = description.split("\\|\\|");
        this.golden = golden;
        this.order = order;
        this.pair = pair;
        this.section = section;
        this.effect = effect;
    }

    /** Balance points at a level; 0 when off. Negative rules are below zero, positive rules above. */
    public int points(int level) {
        return level <= 0 ? 0 : points[Math.min(level, levels) - 1];
    }

    public float value(int level) {
        return level <= 0 ? 0f : values[Math.min(level, levels) - 1];
    }

    /** The number shown for a level, as written in rules.csv. */
    public String valueText(int level) {
        return level <= 0 ? "" : valueTexts[Math.min(level, levels) - 1];
    }

    /** The effect line for a level, with {value} filled in. */
    public String describe(int level) {
        if (level <= 0) return "";
        int index = Math.min(level, levels) - 1;
        return descriptions[Math.min(index, descriptions.length - 1)].trim().replace("{value}", valueTexts[index]);
    }

    /** The challenge whose reward this rule is, or null. */
    public String challengeId() {
        return unlock.startsWith("challenge:") ? unlock.substring(10) : null;
    }

    /** The achievement that opens this rule (Achievements - Renewed id), or null. */
    public String achievementId() {
        if (unlock.startsWith("challenge:")) return ChallengeSpec.PREFIX + unlock.substring(10);
        if (unlock.startsWith("achievement:")) return unlock.substring(12);
        return null;
    }

    public static String roman(int level) {
        switch (level) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            default: return String.valueOf(level);
        }
    }
}
