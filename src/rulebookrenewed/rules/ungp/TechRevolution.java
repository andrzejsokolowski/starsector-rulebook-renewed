package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Golden: ships the player commands have more ordnance points. Above 240 deployment points of combat
 * ships in the fleet the bonus shrinks, down to half at 480 (the fleet total the game's own fleet-wide
 * skills count). Adapted from Technological Revolution in Unofficial New Game Plus by Originem.
 */
public final class TechRevolution implements RuleEffect {

    private static final float THRESHOLD = 240f;

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        MutableCharacterStatsAPI stats = Global.getSector().getPlayerStats();
        if (stats == null) return;
        float mult = 1f;
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet != null) {
            float total = BaseSkillEffectDescription.getTotalCombatOP(fleet.getFleetData(), stats);
            if (total > THRESHOLD) mult = Math.max(0.5f, 1f - 0.5f * (total - THRESHOLD) / THRESHOLD);
        }
        stats.getShipOrdnancePointBonus().modifyPercent(RuleEffect.modId(rule), value * mult, rule.name);
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        MutableCharacterStatsAPI stats = Global.getSector().getPlayerStats();
        if (stats != null) stats.getShipOrdnancePointBonus().unmodify(RuleEffect.modId(rule));
    }
}
