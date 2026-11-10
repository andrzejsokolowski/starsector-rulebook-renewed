package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Hard: every so many days the player loses a story point. With none to lose, the next one earned goes
 * at once. Adapted from Monotonous Teller in Unofficial New Game Plus by Originem.
 */
public final class MonotonousTeller implements RuleEffect {

    private static final String SINCE_KEY = "rbr_tellerSince";

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        if (Tools.daysSince(SINCE_KEY) < value) return;
        MutableCharacterStatsAPI stats = Global.getSector().getPlayerStats();
        if (stats == null || stats.getStoryPoints() <= 0) return;
        stats.setStoryPoints(stats.getStoryPoints() - 1);
        Tools.restart(SINCE_KEY);
        Tools.note(rule.name + ": 1 story point lost.", Misc.getNegativeHighlightColor());
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        Tools.forget(SINCE_KEY);
    }
}
