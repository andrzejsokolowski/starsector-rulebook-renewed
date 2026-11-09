package rulebookrenewed;

import rulebookrenewed.rules.RuleEffect;

/** One line of data/config/rulebook/rules.csv. Levels count from 1; level 0 means the rule is off. */
public final class RuleSpec {

    public static final int MAX_LEVELS = 5;

    public final String id;
    public final String name;
    public final boolean hard;
    public final int levels;
    private final int[] points;
    private final float[] values;
    private final String[] valueTexts;
    public final String requiredMod;
    /** "feat:<id>" or empty. */
    public final String unlock;
    public final String icon;
    public final String description;
    public final String flavor;
    public final String unlockHint;
    public final float order;
    public final RuleEffect effect;

    RuleSpec(String id, String name, boolean hard, int[] points, String[] valueTexts, String requiredMod,
             String unlock, String icon, String description, String flavor, String unlockHint, float order,
             RuleEffect effect) {
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
        this.flavor = flavor;
        this.unlockHint = unlockHint;
        this.order = order;
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

    public String featId() {
        return unlock.startsWith("feat:") ? unlock.substring(5) : null;
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
