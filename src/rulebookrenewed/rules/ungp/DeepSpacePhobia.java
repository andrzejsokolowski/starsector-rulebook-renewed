package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Hard: in hyperspace the player's fleet sees half as far and is seen from farther away. Checked every
 * frame, so it starts and ends with the jump. Adapted from Deep Space Phobia in Unofficial New Game
 * Plus by Originem (which changed each ship's sensor strength and profile; this changes the fleet's
 * two ranges).
 */
public final class DeepSpacePhobia implements RuleEffect {

    private static final float SENSOR_MULT = 0.5f;

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        advanceCampaign(rule, 0f, level, value);
    }

    @Override
    public void advanceCampaign(RuleSpec rule, float amount, int level, float value) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return;
        String id = RuleEffect.modId(rule);
        MutableFleetStatsAPI stats = fleet.getStats();
        if (fleet.isInHyperspace()) {
            stats.getSensorRangeMod().modifyMult(id, SENSOR_MULT, rule.name);
            stats.getDetectedRangeMod().modifyMult(id, 1f + value / 100f, rule.name);
        } else {
            stats.getSensorRangeMod().unmodify(id);
            stats.getDetectedRangeMod().unmodify(id);
        }
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return;
        String id = RuleEffect.modId(rule);
        fleet.getStats().getSensorRangeMod().unmodify(id);
        fleet.getStats().getDetectedRangeMod().unmodify(id);
    }
}
