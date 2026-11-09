package rulebookrenewed.rules;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

import rulebookrenewed.RuleSpec;

/** The player's colonies cost less (helpful) or more (hard) upkeep. */
public final class ColonyUpkeep extends MarketRule {

    @Override
    protected boolean playerMarkets() {
        return true;
    }

    @Override
    protected void modify(MarketAPI market, String id, float value, RuleSpec rule) {
        market.getUpkeepMult().modifyMult(id, Scale.mult(value, -RuleEffect.playerSign(rule)), rule.name);
    }

    @Override
    protected void clear(MarketAPI market, String id) {
        market.getUpkeepMult().unmodify(id);
    }
}
