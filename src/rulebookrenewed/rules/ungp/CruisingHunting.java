package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.MemberStatRule;
import rulebookrenewed.rules.RuleEffect;

/**
 * Golden: cruisers in the player's fleet cost fewer deployment points: a percent of their base cost,
 * rounded up, and never fewer than 2. Adapted from Cruising Hunting in Unofficial New Game Plus by
 * Originem.
 */
public final class CruisingHunting extends MemberStatRule {

    private static final float MIN_CUT = 2f;

    @Override
    public void applyToMember(RuleSpec rule, FleetMemberAPI member, int level, float value) {
        if (member.getHullSpec() == null || member.getHullSpec().getHullSize() != ShipAPI.HullSize.CRUISER) return;
        MutableShipStatsAPI stats = member.getStats();
        // The base deployment cost is the hull's supplies to recover, as the game itself counts it.
        float base = stats.getSuppliesToRecover().getBaseValue();
        int cut = (int) Math.ceil(Math.max(MIN_CUT, base * value / 100f));
        stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(RuleEffect.modId(rule), -cut);
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        // Everything is done in applyToMember, which needs the ship's hull size.
    }
}
