package rulebookrenewed.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleSpec;

/**
 * Helpful: gate travel costs no fuel. The gate dialog (GateCMD.computeFuelCost) multiplies the fuel
 * cost by Misc.GATE_FUEL_COST_MULT, a static the API leaves writable. It lives for the whole game
 * session, so it is restored whenever the rule is off, including after loading another save.
 */
public final class LuddsSong implements RuleEffect {

    private static boolean changedByUs;

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        if (Misc.GATE_FUEL_COST_MULT != 0f) {
            Misc.GATE_FUEL_COST_MULT = 0f;
            changedByUs = true;
        }
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        if (!changedByUs) return;
        Misc.GATE_FUEL_COST_MULT = Global.getSettings().getFloat("gateTransitFuelCostMult");
        changedByUs = false;
    }
}
