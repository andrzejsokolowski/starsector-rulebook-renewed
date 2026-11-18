package rulebookrenewed.rules;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import rulebookrenewed.RuleSpec;

/** Fleets sent out by markets that are not the player's are larger (hard) or smaller (helpful). */
public final class MarketFleetSize extends MarketRule {

    @Override
    protected boolean playerMarkets() {
        return false;
    }

    @Override
    protected void modify(MarketAPI market, String id, float value, RuleSpec rule) {
        // The game reads this with computeEffective(0f): the size comes from flat bonuses, so a percent rise
        // on the base of 0 would add nothing. A multiplier works both ways.
        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT)
                .modifyMult(id, Scale.mult(value, RuleEffect.enemySign(rule)), rule.name);
    }

    @Override
    protected void clear(MarketAPI market, String id) {
        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodify(id);
    }
}
