package rulebookrenewed.rules;

import java.awt.Color;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleSpec;

/**
 * Hard: at the start of every month, crew and marines in the player's fleet eat food from the cargo hold.
 * Without enough food, part of them leave. Adapted from Famine in Unofficial Rule Book by DemonX3 (MIT
 * licence, see CREDITS.txt).
 */
public final class Famine implements RuleEffect {

    private static final String MONTH_KEY = "rbr_famineMonth";
    /** Share of crew and marines that leave when the fleet has no food at all. */
    private static final float LEAVE_FRACTION = 0.1f;

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        CampaignClockAPI clock = Global.getSector().getClock();
        int month = clock.getCycle() * 12 + clock.getMonth();
        Map<String, Object> data = Global.getSector().getPersistentData();
        Object last = data.get(MONTH_KEY);
        data.put(MONTH_KEY, month);
        if (last instanceof Integer && (Integer) last != month) feed(value);
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        Global.getSector().getPersistentData().remove(MONTH_KEY);
    }

    private static void feed(float perHundred) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return;
        CargoAPI cargo = fleet.getCargo();
        int heads = cargo.getCrew() + cargo.getMarines();
        if (heads <= 0) return;
        float need = Math.max(1f, (float) Math.ceil(heads * perHundred / 100f));
        float food = cargo.getCommodityQuantity(Commodities.FOOD);
        if (food >= need) {
            cargo.removeCommodity(Commodities.FOOD, need);
            message("Your crew ate " + (int) need + " food this month.", Misc.getGrayColor());
            return;
        }
        if (food > 0f) cargo.removeCommodity(Commodities.FOOD, food);
        float shortfall = (need - food) / need;
        int crewLost = Math.round(cargo.getCrew() * LEAVE_FRACTION * shortfall);
        int marinesLost = Math.round(cargo.getMarines() * LEAVE_FRACTION * shortfall);
        if (crewLost > 0) cargo.removeCrew(crewLost);
        if (marinesLost > 0) cargo.removeMarines(marinesLost);
        message("Not enough food: " + crewLost + " crew and " + marinesLost + " marines left the fleet.",
                Misc.getNegativeHighlightColor());
    }

    private static void message(String text, Color color) {
        try {
            Global.getSector().getCampaignUI().addMessage(text, color);
        } catch (Throwable ignored) {
            // No campaign UI yet.
        }
    }
}
