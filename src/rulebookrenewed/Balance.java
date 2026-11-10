package rulebookrenewed;

import java.util.Locale;

import com.fs.starfarer.api.Global;

/**
 * The balance meter. Every active rule level adds its points: hard rules are negative, helpful rules
 * positive. The meter only informs; it never blocks a pick.
 */
public final class Balance {

    private Balance() {}

    /** Net points of the rules active in this save. */
    public static int net() {
        int sum = 0;
        for (RuleSpec rule : RuleCatalog.all()) sum += rule.points(RuleState.level(rule));
        return sum;
    }

    /** "You're 20 points over budget - Slightly overpowered", in Andrzej's wording. */
    public static String describe(int net) {
        if (net == 0) return "Balanced";
        int size = Math.abs(net);
        String unit = size == 1 ? " point " : " points ";
        if (net > 0) return "You're " + size + unit + "over budget - " + overLabel(size);
        return "You're " + size + unit + "under budget - " + underLabel(size);
    }

    private static String overLabel(int size) {
        if (size <= 25) return "Slightly overpowered";
        if (size <= 60) return "Overpowered";
        return "Wildly overpowered";
    }

    private static String underLabel(int size) {
        if (size <= 25) return "Slightly harder";
        if (size <= 60) return "Hard";
        return "Brutal";
    }

    /**
     * Career point multiplier for New Game Plus - Renewed: each point under budget adds 1 percent, each
     * point over budget takes 1 percent away, never below 0.1.
     */
    public static float ngpMultiplier(int net) {
        return Math.max(0.1f, 1f - net / 100f);
    }

    public static String formatMultiplier(float mult) {
        return String.format(Locale.ROOT, "x%.2f", mult);
    }

    /** Writes the multiplier to sector memory, where New Game Plus - Renewed reads it. */
    public static void publish() {
        if (Global.getSector() == null) return;
        Global.getSector().getMemoryWithoutUpdate().set(Ids.NGP_MULTIPLIER_KEY, ngpMultiplier(net()));
    }

    public static boolean ngpInstalled() {
        for (String id : Ids.NGP_MOD_IDS) {
            if (Global.getSettings().getModManager().isModEnabled(id)) return true;
        }
        return false;
    }
}
