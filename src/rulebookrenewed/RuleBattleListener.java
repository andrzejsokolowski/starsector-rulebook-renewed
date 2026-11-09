package rulebookrenewed;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;

/** Prepares enemy ships when an encounter opens and cleans them up when the battle ends. Transient. */
public final class RuleBattleListener extends BaseCampaignEventListener {

    public RuleBattleListener() {
        super(false);
    }

    @Override
    public void reportShownInteractionDialog(InteractionDialogAPI dialog) {
        try {
            CampaignFleetAPI player = Global.getSector().getPlayerFleet();
            BattleAPI battle = player == null ? null : player.getBattle();
            if (battle == null && dialog != null) {
                SectorEntityToken target = dialog.getInteractionTarget();
                if (target instanceof CampaignFleetAPI) battle = ((CampaignFleetAPI) target).getBattle();
            }
            BattlePrep.prepareBattle(battle);
        } catch (Throwable ignored) {
            // The combat-side pass covers ships still in reserve.
        }
    }

    @Override
    public void reportBattleFinished(CampaignFleetAPI primaryWinner, BattleAPI battle) {
        try {
            BattlePrep.clearBattle(battle);
        } catch (Throwable ignored) {
            // The buffs run out on their own after two days.
        }
    }
}
