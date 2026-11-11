package rulebookrenewed;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;

import rulebookrenewed.ui.RuleBookDialog;

/**
 * Campaign upkeep, added as a transient script on every load: re-applies rules every few hours of game
 * time (new ships, new colonies), checks achievements and challenges once a day, and shows the message that
 * offers the picker once on a new game.
 */
public final class RuleBookScript implements EveryFrameScript {

    private static final float REFRESH_DAYS = 0.25f;
    private static final float CHECK_DAYS = 1f;
    private static final float PICKER_DELAY_SECONDS = 2f;

    private float refreshDays;
    private float checkDays;
    private float pickerSeconds;

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        if (Global.getCurrentState() != GameState.CAMPAIGN) return;
        if (RuleState.isPickerPending()) tryOpenPicker(amount);
        if (Global.getSector().isPaused()) return;

        for (RuleSpec rule : RuleCatalog.all()) {
            int level = RuleState.level(rule);
            if (level <= 0) continue;
            try {
                rule.effect.advanceCampaign(rule, amount, level, rule.value(level));
            } catch (Throwable t) {
                Global.getLogger(RuleBookScript.class).error("Rule Book: rule " + rule.id + " failed on the campaign map", t);
            }
        }

        float days = Global.getSector().getClock().convertToDays(amount);
        refreshDays += days;
        if (refreshDays >= REFRESH_DAYS) {
            refreshDays = 0f;
            RuleManager.refreshAll();
        }
        checkDays += days;
        if (checkDays >= CHECK_DAYS) {
            checkDays = 0f;
            achievementsrenewed.api.Achievements.check();
            Challenges.check();
        }
    }

    private void tryOpenPicker(float amount) {
        CampaignUIAPI ui = Global.getSector().getCampaignUI();
        if (ui.isShowingDialog() || ui.isShowingMenu() || Global.getSector().getPlayerFleet() == null) {
            pickerSeconds = 0f;
            return;
        }
        pickerSeconds += amount;
        if (pickerSeconds < PICKER_DELAY_SECONDS) return;
        RuleState.setPickerPending(false);
        if (Settings.openOnNewGame()) {
            ui.showInteractionDialog(new RuleBookDialog(true), Global.getSector().getPlayerFleet());
        }
    }
}
