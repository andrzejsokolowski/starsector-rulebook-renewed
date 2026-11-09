package rulebookrenewed.rules;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import rulebookrenewed.RuleSpec;

/** Ships in fleets from markets that are not the player's have fewer (hard) or more (helpful) D-mods. */
public final class MarketFleetQuality extends MarketRule {

    @Override
    protected boolean playerMarkets() {
        return false;
    }

    @Override
    protected void modify(MarketAPI market, String id, float value, RuleSpec rule) {
        market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat(id, RuleEffect.enemySign(rule) * value / 100f, rule.name);
    }

    @Override
    protected void clear(MarketAPI market, String id) {
        market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).unmodify(id);
    }
}
