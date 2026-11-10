package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.rules.MarketRule;

/** Shared by Trade Embargo and Spy: a colony of the player's is hurt by every hostile market within 15 light-years. */
abstract class HostileNeighbours extends MarketRule {

    private static final float RANGE_LY = 15f;

    @Override
    protected boolean playerMarkets() {
        return true;
    }

    static int hostileNearby(MarketAPI market) {
        int hostile = 0;
        for (MarketAPI other : Misc.getNearbyMarkets(market.getLocationInHyperspace(), RANGE_LY)) {
            if (other != market && other.getFaction() != null && other.getFaction().isHostileTo(market.getFaction())) hostile++;
        }
        return hostile;
    }
}
