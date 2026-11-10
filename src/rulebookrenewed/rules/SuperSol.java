package rulebookrenewed.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;

import rulebookrenewed.RuleSpec;

/**
 * Positive, only with Sol Renewed: once per campaign, every planet in the Sol system gets one random
 * resource deposit improved by one grade (farmland_adequate becomes farmland_rich, and so on). Sol is
 * found by the memory flag Sol Renewed puts on its system; until the system exists, the rule waits.
 */
public final class SuperSol implements RuleEffect {

    private static final String DONE_KEY = "rbr_superSolDone";
    private static final String SOL_FLAG = "$sr_sol";
    /** Each resource, poorest grade first. */
    private static final String[][] GRADES = {
            {"ore_sparse", "ore_moderate", "ore_abundant", "ore_rich", "ore_ultrarich"},
            {"rare_ore_sparse", "rare_ore_moderate", "rare_ore_abundant", "rare_ore_rich", "rare_ore_ultrarich"},
            {"organics_trace", "organics_common", "organics_abundant", "organics_plentiful"},
            {"volatiles_trace", "volatiles_diffuse", "volatiles_abundant", "volatiles_plentiful"},
            {"farmland_poor", "farmland_adequate", "farmland_rich", "farmland_bountiful"},
    };

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        if (Global.getSector().getPersistentData().containsKey(DONE_KEY)) return;
        StarSystemAPI sol = null;
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (system.getMemoryWithoutUpdate().contains(SOL_FLAG)) sol = system;
        }
        if (sol == null) return;
        Random random = new Random();
        int improved = 0;
        for (PlanetAPI planet : sol.getPlanets()) {
            MarketAPI market = planet.getMarket();
            if (planet.isStar() || market == null) continue;
            List<String[]> options = new ArrayList<>();
            for (String[] grades : GRADES) {
                for (int i = 0; i < grades.length - 1; i++) {
                    if (market.hasCondition(grades[i])) options.add(new String[] {grades[i], grades[i + 1]});
                }
            }
            if (options.isEmpty()) continue;
            String[] pick = options.get(random.nextInt(options.size()));
            MarketConditionAPI old = market.getCondition(pick[0]);
            boolean surveyed = old != null && old.isSurveyed();
            market.removeCondition(pick[0]);
            String token = market.addCondition(pick[1]);
            MarketConditionAPI added = market.getSpecificCondition(token);
            if (added != null) added.setSurveyed(surveyed);
            improved++;
        }
        Global.getSector().getPersistentData().put(DONE_KEY, Boolean.TRUE);
        try {
            Global.getSector().getCampaignUI().addMessage("Super Sol: resource deposits improved on " + improved
                    + (improved == 1 ? " planet" : " planets") + " in the Sol system.");
        } catch (Throwable ignored) {
            // No campaign UI yet.
        }
    }
}
