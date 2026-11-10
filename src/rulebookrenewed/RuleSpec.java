package rulebookrenewed;

import rulebookrenewed.rules.RuleEffect;

/** One line of data/config/rulebook/rules.csv. Levels count from 1; level 0 means the rule is off. */
public final class RuleSpec {

    public static final int MAX_LEVELS = 5;

    public final String id;
    public final String name;
    public final boolean hard;
    /** A rule that is neither hard nor helpful (fluff): no points, never hard. */
    public final boolean neutral;
    public final int levels;
    private final int[] points;
    private final float[] values;
    private final String[] valueTexts;
    public final String requiredMod;
    /** "feat:<id>", "challenge:<id>" or empty. */
    public final String unlock;
    public final String icon;
    public final String description;
    public final String flavor;
    public final String unlockHint;
    public final float order;
    /** Rules with the same pair key are mirrors of each other and sit together in the picker. */
    public final String pair;
    /** The name of a mirrored pair's shared tile in the picker, e.g. "Enemy armor"; the rule's name otherwise. */
    public final String pairName;
    /** Heading the rule sits under in the picker. */
    public final String section;
    public final RuleEffect effect;

    RuleSpec(String id, String name, boolean hard, boolean neutral, int[] points, String[] valueTexts, String requiredMod,
             String unlock, String icon, String description, String flavor, String unlockHint, float order,
             String pair, String pairName, String section, RuleEffect effect) {
        this.id = id;
        this.name = name;
        this.hard = hard;
        this.neutral = neutral;
        this.levels = points.length;
        this.points = points;
        this.valueTexts = valueTexts;
        this.values = new float[valueTexts.length];
        for (int i = 0; i < valueTexts.length; i++) values[i] = Float.parseFloat(valueTexts[i]);
        this.requiredMod = requiredMod;
        this.unlock = unlock;
        this.icon = icon;
        this.description = description;
        this.flavor = flavor;
        this.unlockHint = unlockHint;
        this.order = order;
        this.pair = pair;
        this.pairName = pairName;
        this.section = section;
        this.effect = effect;
    }

    /** Balance points at a level; 0 when off. Hard rules are negative. */
    public int points(int level) {
        return level <= 0 ? 0 : points[Math.min(level, levels) - 1];
    }

    public float value(int level) {
        return level <= 0 ? 0f : values[Math.min(level, levels) - 1];
    }

    /** The effect line for a level, with {value} filled in. */
    public String describe(int level) {
        if (level <= 0) return "";
        return description.replace("{value}", valueTexts[Math.min(level, levels) - 1]);
    }

    /** The effect line with every level's value, e.g. "Enemy ships have 10/20/30/40/50% more armor." */
    public String describeAllLevels() {
        return description.replace("{value}", String.join("/", valueTexts));
    }

    /** Points of every level, e.g. "-5/-10/-15". */
    public String pointsText() {
        StringBuilder out = new StringBuilder();
        for (int p : points) {
            if (out.length() > 0) out.append('/');
            out.append(p > 0 ? "+" : "").append(p);
        }
        return out.toString();
    }

    /** The feat that opens this rule: a feat id, "challenge_<id>" for a challenge reward, or null. */
    public String featId() {
        if (unlock.startsWith("feat:")) return unlock.substring(5);
        if (unlock.startsWith("challenge:")) return "challenge_" + unlock.substring(10);
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
