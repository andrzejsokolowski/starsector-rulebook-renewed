package rulebookrenewed.rules;

import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.plugins.LevelupPlugin;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleSpec;

/**
 * Hard: the first time it is switched on in a save, the player gains some levels at once (the rule's
 * value, 4) but none of their skill or story points. Those points come when the player reaches the maximum level with the rule still
 * on. Nothing is given when the player is already that close to the maximum. Challenges cannot run with
 * it (the levels would shorten them). Adapted from Delayed Potential in Unofficial Rule Book by DemonX3
 * (MIT licence, see CREDITS.txt); unlike there, experience is set to the new level instead of doubled, and
 * the points are only paid back when levels were given.
 */
public final class DelayedPotential implements RuleEffect {

    /** The level the gift started from; present once the levels were given in this save. */
    private static final String FROM_KEY = "rbr_delayedPotentialFrom";
    private static final String LEVELS_KEY = "rbr_delayedPotentialLevels";
    private static final String PAID_KEY = "rbr_delayedPotentialPaid";

    @Override
    public boolean blocksChallenges() {
        return true;
    }

    @Override
    public void onSwitchedOn(RuleSpec rule, int level, float value) {
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (data.containsKey(FROM_KEY)) return;
        int levels = Math.round(value);
        data.put(LEVELS_KEY, levels);
        MutableCharacterStatsAPI stats = Global.getSector().getPlayerStats();
        LevelupPlugin plugin = Global.getSettings().getLevelupPlugin();
        int from = stats.getLevel();
        if (from + levels > plugin.getMaxLevel()) {
            data.put(FROM_KEY, -1);
            return;
        }
        data.put(FROM_KEY, from);
        stats.setLevel(from + levels);
        stats.setXP(Math.max(stats.getXP(), plugin.getXPForLevel(from + levels)));
        Global.getSector().getCampaignUI().addMessage(rule.name + ": level " + (from + levels)
                + ", skill and story points at level " + plugin.getMaxLevel(), Misc.getHighlightColor());
    }

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        Map<String, Object> data = Global.getSector().getPersistentData();
        Object from = data.get(FROM_KEY);
        if (!(from instanceof Integer) || (Integer) from < 1 || data.containsKey(PAID_KEY)) return;
        MutableCharacterStatsAPI stats = Global.getSector().getPlayerStats();
        LevelupPlugin plugin = Global.getSettings().getLevelupPlugin();
        if (stats.getLevel() < plugin.getMaxLevel()) return;
        Object given = data.get(LEVELS_KEY);
        int levels = given instanceof Integer ? (Integer) given : Math.round(value);
        int points = 0;
        for (int l = (Integer) from + 1; l <= (Integer) from + levels; l++) points += plugin.getPointsAtLevel(l);
        int story = plugin.getStoryPointsPerLevel() * levels;
        stats.addPoints(points);
        stats.addStoryPoints(story);
        data.put(PAID_KEY, true);
        Global.getSector().getCampaignUI().addMessage(rule.name + ": " + points + " skill points and " + story
                + " story points", Misc.getPositiveHighlightColor());
    }
}
