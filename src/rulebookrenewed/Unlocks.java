package rulebookrenewed;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;

import achievementsrenewed.api.AchievementSpec;
import achievementsrenewed.api.Achievements;

/**
 * What the player has unlocked: achievements of Achievements - Renewed, which keeps them across saves.
 * Since 0.11.0 that mod also holds and checks the achievements that used to be Rule Book feats (ids
 * rbr_<feat id>); finished challenges stay here (rbr_challenge_<id>). Before 0.7.0 feats were kept in this
 * mod's own file in saves/common; that file is carried over once.
 */
public final class Unlocks {

    private static final Logger LOG = Global.getLogger(Unlocks.class);
    private static final String OLD_FILE = "rulebook_renewed_unlocks.json";

    private Unlocks() {}

    /** How the picker names the achievement that opens a rule: its exact condition (the how-to), else its name. */
    public static String label(String achievementId) {
        AchievementSpec spec = Achievements.get(achievementId);
        if (spec == null) return achievementId;
        return spec.howTo.isEmpty() ? spec.name : spec.howTo;
    }

    /** Whether the player may pick this rule. Rules already active stay usable regardless. */
    public static boolean isUnlocked(RuleSpec rule) {
        String id = rule.achievementId();
        if (id == null) return true;
        return Settings.unlockAll() || Achievements.isDone(id);
    }

    /** Carries feats from the pre-0.7.0 file over to Achievements - Renewed, then removes the file. */
    public static void migrate() {
        try {
            if (!Global.getSettings().fileExistsInCommon(OLD_FILE)) return;
            String text = Global.getSettings().readTextFileFromCommon(OLD_FILE);
            JSONArray list = text == null || text.trim().isEmpty() ? null : new JSONObject(text).optJSONArray("feats");
            for (int i = 0; list != null && i < list.length(); i++) {
                String id = list.optString(i, "").trim();
                if (id.isEmpty() || id.equals("null")) continue;
                if (id.equals("level15")) Achievements.complete("acr_level15"); // now the base mod's achievement
                else Achievements.complete("rbr_" + id);
            }
            Global.getSettings().deleteTextFileFromCommon(OLD_FILE);
        } catch (Exception e) {
            LOG.error("Rule Book: could not carry over " + OLD_FILE, e);
        }
    }
}
