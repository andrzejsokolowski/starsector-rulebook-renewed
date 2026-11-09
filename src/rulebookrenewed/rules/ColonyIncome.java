package rulebookrenewed.rules;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

import rulebookrenewed.RuleSpec;

/** The player's colonies earn more (helpful) or less (hard) income. */
public final class ColonyIncome extends MarketRule {

    @Override
    protected boolean playerMarkets() {
        return true;
    }

    @Override
    protected void modify(MarketAPI market, String id, float value, RuleSpec rule) {
        market.getIncomeMult().modifyMult(id, Scale.mult(value, RuleEffect.playerSign(rule)), rule.name);
    }

    @Override
    protected void clear(MarketAPI market, String id) {
        market.getIncomeMult().unmodify(id);
    }
}
