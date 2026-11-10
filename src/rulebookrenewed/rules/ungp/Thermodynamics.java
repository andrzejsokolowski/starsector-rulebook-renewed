package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Helpful: while Emergency Burn runs, the player's fleet gets a burn bonus so large that it moves at
 * the game's top burn level. Checked every frame. Adapted from Thermodynamics in Unofficial New Game
 * Plus by Originem.
 */
public final class Thermodynamics implements RuleEffect {

    private static final float BONUS = 100f;

    @Override
    public void advanceCampaign(RuleSpec rule, float amount, int level, float value) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return;
        AbilityPlugin burn = fleet.getAbility(Abilities.EMERGENCY_BURN);
        if (burn != null && burn.isActiveOrInProgress()) {
            fleet.getStats().getFleetwideMaxBurnMod().modifyFlat(RuleEffect.modId(rule), BONUS, rule.name);
        } else {
            fleet.getStats().getFleetwideMaxBurnMod().unmodifyFlat(RuleEffect.modId(rule));
        }
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet != null) fleet.getStats().getFleetwideMaxBurnMod().unmodifyFlat(RuleEffect.modId(rule));
    }
}
