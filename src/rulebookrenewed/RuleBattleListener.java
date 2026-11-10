package rulebookrenewed;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;

/**
 * Prepares enemy ships when an encounter opens, cleans them up when the battle ends, and runs the rules'
 * and challenges' after-battle checks. Transient. Battles are counted by Achievements - Renewed.
 */
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
        if (battle == null || !battle.isPlayerInvolved()) return;
        for (RuleSpec rule : RuleCatalog.all()) {
            int level = RuleState.level(rule);
            if (level <= 0) continue;
            try {
                rule.effect.onPlayerBattleFinished(rule, battle, level, rule.value(level));
            } catch (Throwable t) {
                Global.getLogger(RuleBattleListener.class).error("Rule Book: rule " + rule.id + " failed after a battle", t);
            }
        }
        // The battle's experience can reach the maximum level a challenge waits for.
        Challenges.check();
    }
}
