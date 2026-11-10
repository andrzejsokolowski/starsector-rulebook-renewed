package rulebookrenewed.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import rulebookrenewed.RuleSpec;

/**
 * Enemy officers have more (hard) or fewer (helpful) skills, and their level moves with them. Applied once
 * per officer, the first time the player meets their fleet in an encounter (BattlePrep), and marked in
 * the officer's memory so it never stacks. AI cores and ship-less default captains are left alone. The
 * change stays with that officer, as officer skills do.
 */
public final class EnemyOfficers implements RuleEffect {

    private static final String DONE_KEY = "$rbr_officerSkillsChanged";

    @Override
    public void prepareEnemyFleet(RuleSpec rule, CampaignFleetAPI fleet, int level, float value) {
        int change = (int) value;
        if (change <= 0) return;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            PersonAPI captain = member.getCaptain();
            if (captain == null || captain.isDefault() || captain.isAICore() || captain.isPlayer()) continue;
            if (captain.getMemoryWithoutUpdate().contains(DONE_KEY)) continue;
            captain.getMemoryWithoutUpdate().set(DONE_KEY, true);
            if (rule.hard) addSkills(captain, change);
            else removeSkills(captain, change);
        }
    }

    private static List<String> combatSkills() {
        List<String> out = new ArrayList<>();
        for (String id : Global.getSettings().getSkillIds()) {
            SkillSpecAPI spec = Global.getSettings().getSkillSpec(id);
            if (spec == null || !spec.isCombatOfficerSkill() || spec.isAptitudeEffect()) continue;
            if (spec.hasTag("npc_only") || spec.hasTag("deprecated") || spec.hasTag("ai_core_only")) continue;
            out.add(id);
        }
        return out;
    }

    private static void addSkills(PersonAPI captain, int count) {
        MutableCharacterStatsAPI stats = captain.getStats();
        List<String> pool = combatSkills();
        pool.removeIf(id -> stats.getSkillLevel(id) > 0f);
        Random random = new Random(captain.getId().hashCode());
        int added = 0;
        while (added < count && !pool.isEmpty()) {
            stats.setSkillLevel(pool.remove(random.nextInt(pool.size())), 1f);
            added++;
        }
        if (added > 0) stats.setLevel(stats.getLevel() + added);
    }

    private static void removeSkills(PersonAPI captain, int count) {
        MutableCharacterStatsAPI stats = captain.getStats();
        List<String> owned = new ArrayList<>();
        for (MutableCharacterStatsAPI.SkillLevelAPI skill : stats.getSkillsCopy()) {
            if (skill.getLevel() > 0f && skill.getSkill().isCombatOfficerSkill() && !skill.getSkill().isAptitudeEffect()) {
                owned.add(skill.getSkill().getId());
            }
        }
        int removed = 0;
        // Always keep one skill, so the officer is still an officer.
        while (removed < count && owned.size() > 1) {
            stats.setSkillLevel(owned.remove(owned.size() - 1), 0f);
            removed++;
        }
        if (removed > 0) stats.setLevel(Math.max(1, stats.getLevel() - removed));
    }
}
