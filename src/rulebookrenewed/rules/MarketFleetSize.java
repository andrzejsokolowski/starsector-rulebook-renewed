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
        Scale.by(market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT), id, value, RuleEffect.enemySign(rule), rule.name);
    }

    @Override
    protected void clear(MarketAPI market, String id) {
        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodify(id);
    }
}
