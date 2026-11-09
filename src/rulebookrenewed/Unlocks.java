package rulebookrenewed;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;

/**
 * Feats earned in any save, kept in saves/common so they last across campaigns. SettingsAPI's common
 * files are the only file access mods have (java.io is refused), so this is read and written through it.
 */
public final class Unlocks {

    private static final Logger LOG = Global.getLogger(Unlocks.class);
    private static final String FILE = "rulebook_renewed_unlocks.json";

    private static Set<String> feats;

    private Unlocks() {}

    private static Set<String> feats() {
        if (feats == null) feats = read();
        return feats;
    }

    private static Set<String> read() {
        Set<String> out = new LinkedHashSet<>();
        try {
            if (!Global.getSettings().fileExistsInCommon(FILE)) return out;
            String text = Global.getSettings().readTextFileFromCommon(FILE);
            if (text == null || text.trim().isEmpty()) return out;
            JSONArray list = new JSONObject(text).optJSONArray("feats");
            if (list == null) return out;
            for (int i = 0; i < list.length(); i++) {
                if (list.isNull(i)) continue;
                String id = list.optString(i, "").trim();
                if (!id.isEmpty() && !id.equals("null")) out.add(id);
            }
        } catch (Exception e) {
            LOG.error("Rule Book: could not read " + FILE + ", starting with no unlocks", e);
        }
        return out;
    }

    private static void write() {
        try {
            JSONObject root = new JSONObject();
            root.put("version", 1);
            root.put("feats", new JSONArray(feats()));
            Global.getSettings().writeTextFileToCommon(FILE, root.toString(2));
        } catch (Exception e) {
            LOG.error("Rule Book: could not save " + FILE, e);
        }
    }

    public static boolean hasFeat(String featId) {
        return feats().contains(featId);
    }

    /** Records a feat. Returns true when it is new. */
    public static boolean addFeat(String featId) {
        if (!feats().add(featId)) return false;
        write();
        return true;
    }

    /** Whether the player may pick this rule. Rules already active stay usable regardless. */
    public static boolean isUnlocked(RuleSpec rule) {
        String feat = rule.featId();
        if (feat == null) return true;
        return Settings.unlockAll() || hasFeat(feat);
    }
}
