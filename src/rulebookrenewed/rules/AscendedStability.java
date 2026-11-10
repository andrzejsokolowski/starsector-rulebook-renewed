package rulebookrenewed.rules;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

import rulebookrenewed.RuleSpec;

/**
 * Positive, only with Astral Ascension Renewed: the player's ascended colonies (past size 10, marked by
 * that mod with $aar_ascended) gain stability.
 */
public final class AscendedStability extends MarketRule {

    private static final String ASCENDED = "$aar_ascended";

    @Override
    protected boolean playerMarkets() {
        return true;
    }

    @Override
    protected void modify(MarketAPI market, String id, float value, RuleSpec rule) {
        if (market.getMemoryWithoutUpdate().contains(ASCENDED)) market.getStability().modifyFlat(id, value, rule.name);
        else market.getStability().unmodify(id);
    }

    @Override
    protected void clear(MarketAPI market, String id) {
        market.getStability().unmodify(id);
    }
}
