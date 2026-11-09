package rulebookrenewed.rules;

import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;

import rulebookrenewed.RuleSpec;

/**
 * Fleets hostile to the player have a higher (hard) or lower (helpful) maximum burn on the campaign map.
 * A fleet-wide burn bonus on every hostile fleet in the player's star system or in hyperspace, refreshed
 * every few hours of game time, so a fleet that turns friendly loses it. When the rule goes off, every
 * fleet in the Sector is cleared once.
 */
public final class HostileFleetBurn implements RuleEffect {

    /** Rules that have touched fleets since the game started; only those need the full clean-up. */
    private static final Set<String> TOUCHED = new HashSet<>();

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        if (player == null) return;
        String id = RuleEffect.modId(rule);
        float flat = RuleEffect.enemySign(rule) * value;
        TOUCHED.add(id);
        update(player.getContainingLocation(), player, id, flat);
        LocationAPI hyper = Global.getSector().getHyperspace();
        if (hyper != player.getContainingLocation()) update(hyper, player, id, flat);
    }

    private static void update(LocationAPI location, CampaignFleetAPI player, String id, float flat) {
        if (location == null) return;
        for (CampaignFleetAPI fleet : location.getFleets()) {
            if (fleet == player || fleet.isPlayerFleet()) continue;
            if (fleet.isHostileTo(player) && !fleet.isStationMode()) {
                fleet.getStats().getFleetwideMaxBurnMod().modifyFlat(id, flat, "Rule Book");
            } else {
                fleet.getStats().getFleetwideMaxBurnMod().unmodifyFlat(id);
            }
        }
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        String id = RuleEffect.modId(rule);
        if (!TOUCHED.remove(id)) return;
        for (LocationAPI location : Global.getSector().getAllLocations()) {
            for (CampaignFleetAPI fleet : location.getFleets()) {
                fleet.getStats().getFleetwideMaxBurnMod().unmodifyFlat(id);
            }
        }
    }
}
