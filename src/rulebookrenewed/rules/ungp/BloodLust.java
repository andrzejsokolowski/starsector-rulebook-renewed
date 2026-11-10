package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleSpec;

/**
 * Hard: after 30 days without a battle the player's ships lose maximum combat readiness, until the
 * next battle the fleet takes part in. The count starts when the rule is switched on. Adapted from
 * Blood Lust in Unofficial New Game Plus by Originem (there the days moved with the difficulty; here
 * the loss does).
 */
public final class BloodLust extends ConditionalMemberRule {

    private static final String SINCE_KEY = "rbr_bloodLustSince";
    private static final float DAYS = 30f;

    private boolean restless;

    @Override
    protected float fleetFactor(RuleSpec rule, CampaignFleetAPI fleet, int level, float value) {
        boolean now = Tools.daysSince(SINCE_KEY) >= DAYS;
        if (now && !restless) Tools.note(rule.name + ": no battle for " + (int) DAYS + " days.", Misc.getNegativeHighlightColor());
        restless = now;
        return now ? value : 0f;
    }

    @Override
    protected void modify(FleetMemberAPI member, String id, float strength, RuleSpec rule) {
        member.getStats().getMaxCombatReadiness().modifyFlat(id, -strength / 100f, rule.name);
    }

    @Override
    public void onPlayerBattleFinished(RuleSpec rule, BattleAPI battle, int level, float value) {
        Tools.restart(SINCE_KEY);
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        Tools.forget(SINCE_KEY);
        restless = false;
        super.unapplyCampaign(rule);
    }
}
