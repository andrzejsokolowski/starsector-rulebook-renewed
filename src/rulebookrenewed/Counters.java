package rulebookrenewed;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;

/**
 * Counts kept in this save: battles won, and battles won against special enemies. Feats and challenge
 * goals read them. Stored in the save's persistent data, so they belong to one campaign.
 */
public final class Counters {

    public static final String WINS = "wins";
    public static final String REMNANT = "remnant";
    public static final String ABYSS = "abyss";
    public static final String STATION = "station";
    public static final String GIANT = "giant";

    private static final String KEY = "rbr_counters";

    private Counters() {}

    @SuppressWarnings("unchecked")
    private static Map<String, Integer> map() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        Object stored = data.get(KEY);
        if (!(stored instanceof Map)) {
            stored = new HashMap<String, Integer>();
            data.put(KEY, stored);
        }
        return (Map<String, Integer>) stored;
    }

    public static int get(String key) {
        if (Global.getSector() == null) return 0;
        Integer value = map().get(key);
        return value == null ? 0 : value;
    }

    public static void add(String key) {
        map().put(key, get(key) + 1);
    }
}
