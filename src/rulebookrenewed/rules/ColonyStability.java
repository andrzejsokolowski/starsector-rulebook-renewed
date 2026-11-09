package rulebookrenewed.rules;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

import rulebookrenewed.RuleSpec;

/** The player's colonies have more (helpful) or less (hard) stability. */
public final class ColonyStability extends MarketRule {

    @Override
    protected boolean playerMarkets() {
        return true;
    }

    @Override
    protected void modify(MarketAPI market, String id, float value, RuleSpec rule) {
        market.getStability().modifyFlat(id, RuleEffect.playerSign(rule) * value, rule.name);
    }

    @Override
    protected void clear(MarketAPI market, String id) {
        market.getStability().unmodify(id);
    }
}
