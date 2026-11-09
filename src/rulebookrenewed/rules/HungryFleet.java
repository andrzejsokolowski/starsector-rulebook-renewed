package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/** Hard: every ship in the player's fleet uses more supplies each month. */
public final class HungryFleet extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, String source) {
        stats.getSuppliesPerMonth().modifyPercent(id, value, source);
    }
}
