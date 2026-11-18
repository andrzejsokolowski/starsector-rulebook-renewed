package rulebookrenewed.rules;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

import rulebookrenewed.RuleSpec;

/** Tariffs at markets that are not the player's are higher (hard) or lower (helpful) by a percent of their own value. */
public final class MarketTariff extends MarketRule {

    @Override
    protected boolean playerMarkets() {
        return false;
    }

    @Override
    protected void modify(MarketAPI market, String id, float value, RuleSpec rule) {
        // A market's tariff is a flat amount on a base of 0, so a percent change would add nothing: use a
        // multiplier. A cut is capped at 100%, so a tariff never goes below zero.
        float mult = RuleEffect.playerSign(rule) > 0 ? 1f - Math.min(value, 100f) / 100f : 1f + value / 100f;
        market.getTariff().modifyMult(id, mult, rule.name);
    }

    @Override
    protected void clear(MarketAPI market, String id) {
        market.getTariff().unmodify(id);
    }
}
