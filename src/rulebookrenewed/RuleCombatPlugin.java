package rulebookrenewed;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;

/**
 * Applies the combat side of active rules to every ship in a campaign battle, on the first frame the
 * ship is on the field (reinforcements included). Registered in data/config/settings.json, so it is
 * also created for the title screen's background battle, missions and the simulator: it stands down
 * there, since rules belong to one campaign.
 */
public final class RuleCombatPlugin extends BaseEveryFrameCombatPlugin {

    private static final Logger LOG = Global.getLogger(RuleCombatPlugin.class);

    private static final class Active {
        final RuleSpec rule;
        final int level;
        final float value;

        Active(RuleSpec rule, int level) {
            this.rule = rule;
            this.level = level;
            this.value = rule.value(level);
        }
    }

    private final List<Active> active = new ArrayList<>();
    private final Set<ShipAPI> seen = new HashSet<>();
    private CombatEngineAPI engine;

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        active.clear();
        seen.clear();
        if (Global.getCurrentState() != GameState.COMBAT) return; // title screen battle
        if (engine == null || !engine.isInCampaign() || engine.isInCampaignSim() || Global.getSector() == null) return;
        for (RuleSpec rule : RuleCatalog.all()) {
            int level = RuleState.level(rule);
            if (level > 0) active.add(new Active(rule, level));
        }
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (active.isEmpty() || engine == null) return;
        for (ShipAPI ship : engine.getShips()) {
            if (!seen.add(ship)) continue;
            int owner = ship.getOwner();
            if (owner != 0 && owner != 1) continue; // hulks and neutrals
            boolean enemy = owner == 1;
            for (Active a : active) {
                try {
                    a.rule.effect.applyToShip(a.rule, ship, enemy, a.level, a.value);
                } catch (Throwable t) {
                    LOG.error("Rule Book: rule " + a.rule.id + " failed on a ship in combat", t);
                }
            }
        }
    }
}
