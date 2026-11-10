package rulebookrenewed;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.BuffManagerAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import rulebookrenewed.rules.EnemyMemberBuff;
import rulebookrenewed.rules.RuleEffect;

/**
 * Puts the enemy-ship rules (Plated Foes, Rusted Foes) on enemy ships before they are built for a battle.
 *
 * A ship's armor grid is sized once, when the ship is created from its fleet member's stats: the
 * per-cell maximum is a field set in the grid's constructor and nothing changes it later. So a
 * percent of each ship's own armor must already be in the member's stats at that moment. Two passes do
 * this: when the encounter dialog opens (every fleet on the enemy side of the player's battle), and in
 * combat every frame for ships still in the enemy reserve. The buffs come off when the battle ends.
 */
public final class BattlePrep {

    private static final Logger LOG = Global.getLogger(BattlePrep.class);

    private BattlePrep() {}

    private static List<RuleSpec> activeEnemyRules() {
        List<RuleSpec> out = new ArrayList<>();
        if (Global.getSector() == null) return out;
        for (RuleSpec rule : RuleCatalog.all()) {
            if (rule.effect.usesEnemyMemberBuff() && RuleState.level(rule) > 0) out.add(rule);
        }
        return out;
    }

    /**
     * Lets every active rule change the fleets on the side fighting the player (enemy officers), then buffs
     * every ship of those fleets.
     */
    public static void prepareBattle(BattleAPI battle) {
        if (battle == null || Global.getSector() == null) return;
        List<CampaignFleetAPI> enemies = battle.getNonPlayerSide();
        if (enemies == null) return;
        for (RuleSpec rule : RuleCatalog.all()) {
            int level = RuleState.level(rule);
            if (level <= 0) continue;
            for (CampaignFleetAPI fleet : enemies) {
                if (fleet == null || fleet.isPlayerFleet()) continue;
                try {
                    rule.effect.prepareEnemyFleet(rule, fleet, level, rule.value(level));
                } catch (Throwable t) {
                    LOG.error("Rule Book: rule " + rule.id + " failed on an enemy fleet", t);
                }
            }
        }
        List<RuleSpec> rules = activeEnemyRules();
        if (rules.isEmpty()) return;
        for (CampaignFleetAPI fleet : enemies) {
            if (fleet == null || fleet.isPlayerFleet()) continue;
            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) prepare(member, rules);
        }
    }

    /** Buffs the given enemy ships. Returns true when any changed. */
    public static boolean prepareMembers(List<FleetMemberAPI> members) {
        List<RuleSpec> rules = activeEnemyRules();
        if (rules.isEmpty()) return false;
        boolean changed = false;
        for (FleetMemberAPI member : members) changed |= prepare(member, rules);
        return changed;
    }

    private static boolean prepare(FleetMemberAPI member, List<RuleSpec> rules) {
        boolean changed = false;
        try {
            BuffManagerAPI buffs = member.getBuffManager();
            for (RuleSpec rule : rules) {
                int level = RuleState.level(rule);
                float value = rule.value(level);
                String id = RuleEffect.modId(rule);
                BuffManagerAPI.Buff existing = buffs.getBuff(id);
                if (existing instanceof EnemyMemberBuff && ((EnemyMemberBuff) existing).matches(level, value)) continue;
                if (existing != null) buffs.removeBuff(id);
                buffs.addBuff(new EnemyMemberBuff(rule.id, id, level, value));
                changed = true;
            }
            if (changed) member.updateStats();
        } catch (Throwable t) {
            LOG.error("Rule Book: could not prepare an enemy ship", t);
        }
        return changed;
    }

    /** Takes the enemy-ship buffs off every fleet in a finished battle. */
    public static void clearBattle(BattleAPI battle) {
        if (battle == null) return;
        List<CampaignFleetAPI> fleets = new ArrayList<>();
        if (battle.getSideOne() != null) fleets.addAll(battle.getSideOne());
        if (battle.getSideTwo() != null) fleets.addAll(battle.getSideTwo());
        for (CampaignFleetAPI fleet : fleets) {
            if (fleet == null || fleet.isPlayerFleet()) continue;
            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                boolean changed = false;
                for (BuffManagerAPI.Buff buff : new ArrayList<>(member.getBuffManager().getBuffs())) {
                    if (buff instanceof EnemyMemberBuff) {
                        member.getBuffManager().removeBuff(buff.getId());
                        changed = true;
                    }
                }
                if (changed) member.updateStats();
            }
        }
    }
}
