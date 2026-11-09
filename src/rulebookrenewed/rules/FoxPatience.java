package rulebookrenewed.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleSpec;

/**
 * Helpful: once the player's fleet has held still for half a day, other fleets see it from closer and its
 * own sensors reach farther, until it moves again. Adapted from Fox in Unofficial Rule Book by DemonX3 (MIT
 * licence, see CREDITS.txt).
 */
public final class FoxPatience implements RuleEffect {

    private static final float WAIT_DAYS = 0.5f;

    private static float stillDays;
    private static long lastCheck = -1L;
    private static boolean hidden;

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return;
        CampaignClockAPI clock = Global.getSector().getClock();
        float elapsed = lastCheck < 0L ? 0f : clock.getElapsedDaysSince(lastCheck);
        lastCheck = clock.getTimestamp();
        // A gap of more than a day means another save was loaded since the last check: start counting again.
        if (elapsed < 0f || elapsed > 1f) elapsed = 0f;
        if (fleet.getCurrBurnLevel() < 0.01f) stillDays += Math.max(0f, elapsed);
        else stillDays = 0f;

        String id = RuleEffect.modId(rule);
        boolean nowHidden = stillDays >= WAIT_DAYS;
        if (nowHidden) {
            Scale.by(fleet.getStats().getDetectedRangeMod(), id, value, -1f, rule.name);
            Scale.by(fleet.getStats().getSensorRangeMod(), id, value, 1f, rule.name);
            if (!hidden) fleet.addFloatingText("Lying in wait", Misc.getHighlightColor(), 1f);
        } else {
            clear(fleet.getStats(), id);
        }
        hidden = nowHidden;
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        stillDays = 0f;
        lastCheck = -1L;
        hidden = false;
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet != null) clear(fleet.getStats(), RuleEffect.modId(rule));
    }

    private static void clear(MutableFleetStatsAPI stats, String id) {
        stats.getDetectedRangeMod().unmodify(id);
        stats.getSensorRangeMod().unmodify(id);
    }
}
