package rulebookrenewed.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;

import rulebookrenewed.RuleSpec;

/**
 * Hard: ships the player loses can never be recovered. FleetEncounterContext skips recovery of every ship
 * whose source fleet has MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, and the player's own ships come from the
 * player fleet, so the flag on the player fleet blocks only the player's own losses. Enemy ships can
 * still be recovered.
 */
public final class Ironman implements RuleEffect {

    private static final String SOURCE_KEY = "$rbr_ironman";

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return;
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);
        fleet.getMemoryWithoutUpdate().set(SOURCE_KEY, true);
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        // Only clear the flag when this rule set it, so another mod's flag is left alone.
        if (fleet == null || !fleet.getMemoryWithoutUpdate().contains(SOURCE_KEY)) return;
        fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY);
        fleet.getMemoryWithoutUpdate().unset(SOURCE_KEY);
    }
}
