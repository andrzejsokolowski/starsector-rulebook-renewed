package rulebookrenewed.rules;

import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.ungp.ConditionalMemberRule;

/**
 * Helpful: once the player's fleet has moved faster than burn 15 for 2 days without a break, its ships
 * repair faster and use fewer supplies (the rule's value, 25%), until it drops to burn 15 or slower.
 * Adapted from Weeping Fungus in Unofficial Rule Book by DemonX3 (MIT licence, see CREDITS.txt).
 */
public final class WeepingFungus extends ConditionalMemberRule {

    private static final float BURN = 15f;
    private static final float DAYS = 2f;
    /** Days spent above the burn without a break, kept in the save. */
    private static final String DAYS_KEY = "rbr_weepingFungusDays";

    @Override
    public void advanceCampaign(RuleSpec rule, float amount, int level, float value) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet != null) {
            Map<String, Object> data = Global.getSector().getPersistentData();
            Object stored = data.get(DAYS_KEY);
            float days = stored instanceof Float ? (Float) stored : 0f;
            if (fleet.getCurrBurnLevel() > BURN) {
                float before = days;
                days += Global.getSector().getClock().convertToDays(amount);
                if (before < DAYS && days >= DAYS) fleet.addFloatingText(rule.name, Misc.getPositiveHighlightColor(), 1f);
            } else {
                days = 0f;
            }
            data.put(DAYS_KEY, days);
        }
        super.advanceCampaign(rule, amount, level, value);
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        Global.getSector().getPersistentData().remove(DAYS_KEY);
        super.unapplyCampaign(rule);
    }

    @Override
    protected float fleetFactor(RuleSpec rule, CampaignFleetAPI fleet, int level, float value) {
        Object stored = Global.getSector().getPersistentData().get(DAYS_KEY);
        return stored instanceof Float && (Float) stored >= DAYS ? value : 0f;
    }

    @Override
    protected void modify(FleetMemberAPI member, String id, float strength, RuleSpec rule) {
        MutableShipStatsAPI stats = member.getStats();
        stats.getRepairRatePercentPerDay().modifyPercent(id, strength);
        stats.getSuppliesPerMonth().modifyMult(id, Math.max(0.05f, 1f - strength / 100f), rule.name);
    }
}
