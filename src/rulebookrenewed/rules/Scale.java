package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;

/**
 * Percent changes in one direction or the other. Rule values stay positive in rules.csv; the sign says
 * which way they push. A rise is a percent bonus, a cut is a multiplier, so a 50 percent cut halves the
 * stat instead of taking half of the base away (and never reaches zero).
 */
public final class Scale {

    private static final float FLOOR = 0.05f;

    private Scale() {}

    /** sign +1 raises the stat by pct percent, -1 lowers it by pct percent. */
    public static void by(MutableStat stat, String id, float pct, float sign, String source) {
        stat.unmodify(id);
        if (pct <= 0f) return;
        if (sign > 0f) stat.modifyPercent(id, pct, source);
        else stat.modifyMult(id, Math.max(FLOOR, 1f - pct / 100f), source);
    }

    public static void by(StatBonus stat, String id, float pct, float sign, String source) {
        stat.unmodify(id);
        if (pct <= 0f) return;
        if (sign > 0f) stat.modifyPercent(id, pct, source);
        else stat.modifyMult(id, Math.max(FLOOR, 1f - pct / 100f), source);
    }

    /** A multiplier: sign +1 gives 1 + pct, -1 gives 1 - pct, never below the floor. */
    public static float mult(float pct, float sign) {
        return Math.max(FLOOR, 1f + sign * pct / 100f);
    }
}
