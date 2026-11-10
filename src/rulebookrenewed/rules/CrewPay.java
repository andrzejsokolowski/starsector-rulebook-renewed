package rulebookrenewed.rules;

import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleSpec;

/**
 * Crew and marines want more pay (hard) or settle for less (helpful). At the start of every month the
 * difference to the game's own wages (settings crewSalary and marineSalary) is taken from, or paid back
 * to, the player's credits.
 */
public final class CrewPay implements RuleEffect {

    private static final String MONTH_KEY = "rbr_crewPayMonth";

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        CampaignClockAPI clock = Global.getSector().getClock();
        int month = clock.getCycle() * 12 + clock.getMonth();
        Map<String, Object> data = Global.getSector().getPersistentData();
        Object last = data.get(MONTH_KEY);
        data.put(MONTH_KEY, month);
        if (last instanceof Integer && (Integer) last != month) settle(rule, value);
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        Global.getSector().getPersistentData().remove(MONTH_KEY);
    }

    private static void settle(RuleSpec rule, float percent) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return;
        CargoAPI cargo = fleet.getCargo();
        float wages = cargo.getCrew() * Global.getSettings().getFloat("crewSalary")
                + cargo.getMarines() * Global.getSettings().getFloat("marineSalary");
        int amount = Math.round(wages * percent / 100f);
        if (amount <= 0) return;
        String text;
        if (rule.hard) {
            cargo.getCredits().subtract(amount);
            text = "Your crew demanded " + Misc.getDGSCredits(amount) + " more pay this month.";
        } else {
            cargo.getCredits().add(amount);
            text = "Your crew took " + Misc.getDGSCredits(amount) + " less pay this month.";
        }
        try {
            Global.getSector().getCampaignUI().addMessage(text, rule.hard ? Misc.getNegativeHighlightColor() : Misc.getPositiveHighlightColor());
        } catch (Throwable ignored) {
            // No campaign UI yet.
        }
    }
}
