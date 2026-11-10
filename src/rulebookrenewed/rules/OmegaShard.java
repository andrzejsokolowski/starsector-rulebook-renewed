package rulebookrenewed.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;

import rulebookrenewed.RuleSpec;

/** Positive, only with Omega Core Everywhere: puts Omega cores in the player's hold, once per campaign. */
public final class OmegaShard implements RuleEffect {

    private static final String DONE_KEY = "rbr_omegaShardGiven";

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        if (Global.getSector().getPersistentData().containsKey(DONE_KEY)) return;
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return;
        fleet.getCargo().addCommodity("omega_core", Math.max(1f, value));
        Global.getSector().getPersistentData().put(DONE_KEY, Boolean.TRUE);
        try {
            Global.getSector().getCampaignUI().addMessage("Omega Shard: an Omega core was added to your cargo.");
        } catch (Throwable ignored) {
            // No campaign UI yet.
        }
    }
}
