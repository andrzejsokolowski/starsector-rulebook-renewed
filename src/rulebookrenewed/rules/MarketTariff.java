package rulebookrenewed.rules;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

import rulebookrenewed.RuleSpec;

/** Tariffs at markets that are not the player's are higher (hard) or lower (helpful), never below zero. */
public final class MarketTariff extends MarketRule {

    @Override
    protected boolean playerMarkets() {
        return false;
    }

    @Override
    protected void modify(MarketAPI market, String id, float value, RuleSpec rule) {
        Tariffs.change(market, id, -RuleEffect.playerSign(rule) * value / 100f, rule.name);
    }

    @Override
    protected void clear(MarketAPI market, String id) {
        market.getTariff().unmodify(id);
    }
}
