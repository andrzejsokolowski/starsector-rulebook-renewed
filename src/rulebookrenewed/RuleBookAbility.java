package rulebookrenewed;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.ui.RuleBookDialog;

/**
 * The Rule Book ability: opens the rule picker, as the Achievements ability of Achievements - Renewed opens
 * its list. Every save gets it on load; the player puts it on the ability bar from the ability list.
 */
public final class RuleBookAbility extends BaseDurationAbility {

    public static final String ID = "rbr_rulebook";

    /** On game load: the ability, for the player and the player fleet. Never placed on the bar. */
    public static void give() {
        try {
            if (!Global.getSector().getCharacterData().getAbilities().contains(ID)) {
                Global.getSector().getCharacterData().addAbility(ID);
            }
            CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
            if (fleet != null && !fleet.hasAbility(ID)) fleet.addAbility(ID);
        } catch (Throwable t) {
            Global.getLogger(RuleBookAbility.class).error("Rule Book: could not add the ability", t);
        }
    }

    @Override
    protected void activateImpl() {
        CampaignUIAPI ui = Global.getSector().getCampaignUI();
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (ui == null || fleet == null || ui.isShowingDialog()) return;
        ui.showInteractionDialog(new RuleBookDialog(), fleet);
    }

    @Override
    protected void applyEffect(float amount, float level) {
    }

    @Override
    protected void deactivateImpl() {
    }

    @Override
    protected void cleanupImpl() {
    }

    @Override
    public boolean isUsable() {
        return true;
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        tooltip.addTitle("Rule Book");
        tooltip.addPara("Opens the Rule Book.", 10f);
        tooltip.addPara(Balance.describe(Balance.net()), Misc.getGrayColor(), 10f);
    }
}
