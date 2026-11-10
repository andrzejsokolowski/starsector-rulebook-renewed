package rulebookrenewed.achievements;

import achievementsrenewed.api.AchievementScript;
import achievementsrenewed.api.AchievementSpec;

import rulebookrenewed.Feats;

/**
 * The script behind every Rule Book feat in data/config/achievements_renewed/achievements.csv. The
 * achievement rbr_<feat id> reports that feat's count in this save; Achievements - Renewed polls it,
 * records it across saves and shows the notification.
 */
public final class FeatAchievement implements AchievementScript {

    public static final String PREFIX = "rbr_";

    @Override
    public int count(AchievementSpec spec) {
        if (!spec.id.startsWith(PREFIX)) return 0;
        Feats.Feat feat = Feats.get(spec.id.substring(PREFIX.length()));
        return feat == null || !feat.available() ? 0 : feat.count();
    }
}
