package rulebookrenewed;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;

/** The rules chosen in this save: rule id to level, kept in the save's persistent data. */
public final class RuleState {

    private static final String LEVELS_KEY = "rbr_ruleLevels";
    private static final String PICKER_PENDING_KEY = "rbr_pickerPending";

    private RuleState() {}

    @SuppressWarnings("unchecked")
    private static Map<String, Integer> levels() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        Object stored = data.get(LEVELS_KEY);
        if (!(stored instanceof Map)) {
            stored = new HashMap<String, Integer>();
            data.put(LEVELS_KEY, stored);
        }
        return (Map<String, Integer>) stored;
    }

    public static int level(RuleSpec rule) {
        if (Global.getSector() == null) return 0;
        Integer level = levels().get(rule.id);
        if (level == null || level <= 0) return 0;
        return Math.min(level, rule.levels);
    }

    public static void setLevel(RuleSpec rule, int level) {
        if (level <= 0) levels().remove(rule.id);
        else levels().put(rule.id, Math.min(level, rule.levels));
    }

    public static boolean isPickerPending() {
        return Boolean.TRUE.equals(Global.getSector().getPersistentData().get(PICKER_PENDING_KEY));
    }

    public static void setPickerPending(boolean pending) {
        if (pending) Global.getSector().getPersistentData().put(PICKER_PENDING_KEY, Boolean.TRUE);
        else Global.getSector().getPersistentData().remove(PICKER_PENDING_KEY);
    }
}
