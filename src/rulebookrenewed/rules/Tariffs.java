package rulebookrenewed.rules;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

/** Tariff changes that never push a market's tariff below zero (free ports already sit at zero). */
final class Tariffs {

    private Tariffs() {}

    static void change(MarketAPI market, String id, float flat, String source) {
        market.getTariff().unmodify(id);
        if (flat < 0f) flat = Math.max(flat, -market.getTariff().getModifiedValue());
        if (flat != 0f) market.getTariff().modifyFlat(id, flat, source);
    }
}
