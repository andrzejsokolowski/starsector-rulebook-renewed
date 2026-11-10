package rulebookrenewed;

import java.awt.Color;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.ui.RuleBookDialog;

/**
 * The Rule Book's permanent intel entry: the rules in force, the balance, challenges under way, and a
 * button to change them.
 */
public final class RuleBookIntel extends BaseIntelPlugin {

    public static final String TAG = "Rule Book";
    private static final String BUTTON_OPEN = "rbr_open";
    private static final String BUTTON_ACHIEVEMENTS = "rbr_achievements";

    @Override
    public String getName() {
        return "Rule Book";
    }

    @Override
    public String getIcon() {
        return Ids.BOOK_ICON;
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(TAG);
        return tags;
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        info.addPara(getName(), getTitleColor(mode), 0f);
        int net = Balance.net();
        info.addPara(Balance.describe(net), Misc.getGrayColor(), 3f);
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        float pad = 10f;

        int net = Balance.net();
        info.addPara(Balance.describe(net), balanceColor(net), pad);
        if (Balance.ngpInstalled()) {
            info.addPara("Career points " + Balance.formatMultiplier(Balance.ngpMultiplier(net)), Misc.getGrayColor(), 3f);
        }

        info.addSectionHeading("Rules in force", Alignment.MID, pad);
        boolean any = false;
        for (RuleSpec rule : RuleCatalog.all()) {
            int level = RuleState.level(rule);
            if (level <= 0) continue;
            any = true;
            String levelText = rule.levels > 1 ? " " + RuleSpec.roman(level) : "";
            int points = rule.points(level);
            String pointText = points > 0 ? "+" + points : String.valueOf(points);
            info.addPara(rule.name + levelText + "   " + pointText, rule.hard ? hardColor() : helpfulColor(), 5f);
            info.addPara(rule.describe(level), Misc.getGrayColor(), 1f);
        }
        if (!any) info.addPara("None.", Misc.getGrayColor(), 5f);

        boolean anyChallenge = false;
        for (ChallengeSpec spec : Challenges.all()) {
            if (Challenges.isComplete(spec) || !Challenges.isRunning(spec)) continue;
            if (!anyChallenge) info.addSectionHeading("Challenges in progress", Alignment.MID, pad);
            anyChallenge = true;
            info.addPara(spec.name + "   " + Challenges.statusText(spec), Misc.getHighlightColor(), 5f);
            info.addPara(spec.goalText() + ".", Misc.getGrayColor(), 1f);
        }

        info.addButton("Open the Rule Book", BUTTON_OPEN, width, 24f, pad * 2f);
        info.addButton("Achievements", BUTTON_ACHIEVEMENTS, width, 24f, 5f);
    }

    @Override
    public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
        if (BUTTON_ACHIEVEMENTS.equals(buttonId)) {
            achievementsrenewed.api.Achievements.openList(ui);
            return;
        }
        if (!BUTTON_OPEN.equals(buttonId)) return;
        SectorEntityToken target = Global.getSector().getPlayerFleet();
        ui.showDialog(target, new RuleBookDialog());
    }

    @Override
    public boolean shouldRemoveIntel() {
        return false;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    public static Color hardColor() {
        return new Color(230, 110, 90);
    }

    public static Color helpfulColor() {
        return new Color(120, 210, 130);
    }

    public static Color balanceColor(int net) {
        if (net > 0) return helpfulColor();
        if (net < 0) return hardColor();
        return Misc.getTextColor();
    }

    /** Adds the intel entry once per save. */
    public static void ensure() {
        if (Global.getSector().getIntelManager().hasIntelOfClass(RuleBookIntel.class)) return;
        RuleBookIntel intel = new RuleBookIntel();
        intel.setNew(false);
        Global.getSector().getIntelManager().addIntel(intel, true);
    }
}
