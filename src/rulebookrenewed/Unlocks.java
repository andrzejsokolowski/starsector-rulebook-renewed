package rulebookrenewed;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;

import achievementsrenewed.api.AchievementSpec;
import achievementsrenewed.api.Achievements;
import rulebookrenewed.achievements.FeatAchievement;

/**
 * What the player has unlocked. Feats and finished challenges are achievements of Achievements - Renewed
 * (ids rbr_<feat id> and rbr_challenge_<id>), which keeps them across saves and announces them. Before
 * 0.7.0 they were kept in this mod's own file in saves/common; that file is carried over once.
 */
public final class Unlocks {

    private static final Logger LOG = Global.getLogger(Unlocks.class);
    private static final String OLD_FILE = "rulebook_renewed_unlocks.json";

    private Unlocks() {}

    public static boolean hasFeat(String featId) {
        return Achievements.isDone(FeatAchievement.PREFIX + featId);
    }

    /** Records a feat. Returns true when it is new. */
    public static boolean addFeat(String featId) {
        return Achievements.complete(FeatAchievement.PREFIX + featId);
    }

    /** How the picker names the achievement that opens a rule: its name once done, its hint (or how-to) before. */
    public static String label(String achievementId) {
        AchievementSpec spec = Achievements.get(achievementId);
        if (spec == null) return achievementId;
        if (spec.isDone()) return spec.name;
        return Achievements.showHowTo() && !spec.howTo.isEmpty() ? spec.howTo : spec.hint;
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
                else addFeat(id);
            }
            Global.getSettings().deleteTextFileFromCommon(OLD_FILE);
        } catch (Exception e) {
            LOG.error("Rule Book: could not carry over " + OLD_FILE, e);
        }
    }
}
