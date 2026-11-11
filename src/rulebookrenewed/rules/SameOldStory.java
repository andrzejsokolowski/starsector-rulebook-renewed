package rulebookrenewed.rules;

import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;

import rulebookrenewed.RuleSpec;

/**
 * Hard: every day a share of the player's bonus experience (the stored experience story points give) is
 * lost. Adapted from Same Old Story in Unofficial Rule Book by DemonX3 (MIT licence, see CREDITS.txt).
 */
public final class SameOldStory implements RuleEffect {

    /** Game timestamp of the last daily loss, kept in the save. */
    private static final String LAST_KEY = "rbr_sameOldStoryLast";

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        Map<String, Object> data = Global.getSector().getPersistentData();
        CampaignClockAPI clock = Global.getSector().getClock();
        Object last = data.get(LAST_KEY);
        if (!(last instanceof Long)) {
            data.put(LAST_KEY, clock.getTimestamp()); // switched on now: the first loss comes in a day
            return;
        }
        int days = (int) clock.getElapsedDaysSince((Long) last);
        if (days < 1) return;
        data.put(LAST_KEY, clock.getTimestamp());
        MutableCharacterStatsAPI stats = Global.getSector().getPlayerStats();
        long bonus = stats.getBonusXp();
        if (bonus <= 0L) return;
        double keep = Math.pow(1.0 - value / 100.0, Math.min(days, 30));
        stats.setBonusXp((long) (bonus * keep));
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        Global.getSector().getPersistentData().remove(LAST_KEY);
    }
}
