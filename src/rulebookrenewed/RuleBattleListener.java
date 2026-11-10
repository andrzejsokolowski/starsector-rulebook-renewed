package rulebookrenewed;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

/**
 * Prepares enemy ships when an encounter opens, cleans them up when the battle ends, and counts the
 * player's wins for feats and challenges. Transient.
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
        try {
            recordWin(primaryWinner, battle);
        } catch (Throwable t) {
            Global.getLogger(RuleBattleListener.class).error("Rule Book: could not record a battle", t);
        }
    }

    /** Counts a won battle, and what it was won against, for feats and challenge goals. */
    private static void recordWin(CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (battle == null || primaryWinner == null || !battle.isPlayerInvolved()) return;
        if (!battle.onPlayerSide(primaryWinner)) return;
        List<CampaignFleetAPI> enemies = battle.getNonPlayerSideSnapshot();
        if (enemies == null || enemies.isEmpty()) enemies = battle.getNonPlayerSide();
        if (enemies == null || enemies.isEmpty()) return;
        boolean remnant = false, abyss = false, station = false;
        float enemyPoints = 0f, ownPoints = 0f;
        List<CampaignFleetAPI> own = battle.getPlayerSideSnapshot();
        if (own == null || own.isEmpty()) own = battle.getPlayerSide();
        if (own != null) for (CampaignFleetAPI fleet : own) if (fleet != null) ownPoints += fleet.getFleetPoints();
        for (CampaignFleetAPI fleet : enemies) {
            if (fleet == null) continue;
            enemyPoints += fleet.getFleetPoints();
            String faction = fleet.getFaction() == null ? "" : fleet.getFaction().getId();
            if (Factions.REMNANTS.equals(faction)) remnant = true;
            if (Factions.OMEGA.equals(faction) || Factions.THREAT.equals(faction) || Factions.DWELLER.equals(faction)) abyss = true;
            if (fleet.isStationMode()) station = true;
        }
        Counters.add(Counters.WINS);
        if (remnant) Counters.add(Counters.REMNANT);
        if (abyss) Counters.add(Counters.ABYSS);
        if (station) Counters.add(Counters.STATION);
        if (ownPoints > 0f && enemyPoints >= ownPoints * 2f) Counters.add(Counters.GIANT);
        Feats.check();
        Challenges.check();
    }
}
