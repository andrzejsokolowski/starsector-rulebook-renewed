package rulebookrenewed;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;

/**
 * Saved rule sets for the picker (the idea and the four slots follow Unofficial New Game Plus). Kept in
 * saves/common, so a preset made in one campaign can be loaded in another.
 */
public final class Presets {

    public static final int SLOTS = 4;
    private static final Logger LOG = Global.getLogger(Presets.class);
    private static final String FILE = "rulebook_renewed_presets.json";

    private Presets() {}

    private static JSONObject root() {
        try {
            if (Global.getSettings().fileExistsInCommon(FILE)) {
                String text = Global.getSettings().readTextFileFromCommon(FILE);
                if (text != null && !text.trim().isEmpty()) return new JSONObject(text);
            }
        } catch (Exception e) {
            LOG.error("Rule Book: could not read " + FILE, e);
        }
        return new JSONObject();
    }

    public static boolean exists(int slot) {
        JSONObject preset = root().optJSONObject("preset" + slot);
        return preset != null && preset.length() > 0;
    }

    /** Rule ids and levels of a slot; empty when the slot is empty. */
    public static Map<String, Integer> load(int slot) {
        Map<String, Integer> out = new HashMap<>();
        JSONObject preset = root().optJSONObject("preset" + slot);
        if (preset == null) return out;
        Iterator<?> keys = preset.keys();
        while (keys.hasNext()) {
            String id = String.valueOf(keys.next());
            int level = preset.optInt(id, 0);
            if (level > 0) out.put(id, level);
        }
        return out;
    }

    public static void save(int slot, Map<String, Integer> levels) {
        try {
            JSONObject root = root();
            JSONObject preset = new JSONObject();
            for (Map.Entry<String, Integer> e : levels.entrySet()) preset.put(e.getKey(), e.getValue());
            root.put("preset" + slot, preset);
            Global.getSettings().writeTextFileToCommon(FILE, root.toString(2));
        } catch (Exception e) {
            LOG.error("Rule Book: could not save " + FILE, e);
        }
    }
}
