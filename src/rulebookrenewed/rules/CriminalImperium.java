package rulebookrenewed.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleSpec;

/**
 * Helpful: every colony of the player's that is a free port lowers the accessibility of each hostile
 * market within 10 light-years. Several free ports stack. Adapted from Criminal Imperium in Unofficial
 * Rule Book by DemonX3 (MIT licence, see CREDITS.txt).
 */
public final class CriminalImperium extends MarketRule {

    private static final float RANGE_LY = 10f;

    @Override
    protected boolean playerMarkets() {
        return false;
    }

    @Override
    protected void modify(MarketAPI market, String id, float value, RuleSpec rule) {
        FactionAPI player = Global.getSector().getPlayerFaction();
        if (market.getFaction() == null || !market.getFaction().isHostileTo(player)) {
            clear(market, id);
            return;
        }
        int ports = 0;
        for (MarketAPI other : Misc.getNearbyMarkets(market.getLocationInHyperspace(), RANGE_LY)) {
            if (other != market && other.isPlayerOwned() && other.isFreePort()) ports++;
        }
        if (ports == 0) clear(market, id);
        else market.getAccessibilityMod().modifyFlat(id, -ports * value / 100f, rule.name);
    }

    @Override
    protected void clear(MarketAPI market, String id) {
        market.getAccessibilityMod().unmodify(id);
    }
}
