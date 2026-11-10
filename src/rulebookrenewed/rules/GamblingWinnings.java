package rulebookrenewed.rules;

import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleSpec;

/**
 * Positive, only with Gambling Den: every slot machine win pays extra credits. Reads only the win counter
 * Gambling Den keeps in the save (persistent data "gd_wins"), never its classes.
 */
public final class GamblingWinnings implements RuleEffect {

    private static final String WINS_KEY = "gd_wins";
    private static final String SEEN_KEY = "rbr_gamblingWinsSeen";

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        Map<String, Object> data = Global.getSector().getPersistentData();
        Object stored = data.get(WINS_KEY);
        int wins = stored instanceof Number ? ((Number) stored).intValue() : 0;
        Object seen = data.get(SEEN_KEY);
        data.put(SEEN_KEY, wins);
        if (!(seen instanceof Integer) || wins <= (Integer) seen) return;
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return;
        int amount = Math.round((wins - (Integer) seen) * value);
        fleet.getCargo().getCredits().add(amount);
        try {
            Global.getSector().getCampaignUI().addMessage("Lucky Streak: " + Misc.getDGSCredits(amount) + " on top of your winnings.",
                    Misc.getPositiveHighlightColor());
        } catch (Throwable ignored) {
            // No campaign UI yet.
        }
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        Global.getSector().getPersistentData().remove(SEEN_KEY);
    }
}
