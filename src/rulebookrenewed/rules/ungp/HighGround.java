package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.combat.BattleObjectiveAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Helpful: ships of the player's side within 2000 units of any battle objective, whoever holds it, have
 * longer ballistic and energy weapon range. Fighters are left out. Adapted from I Have the High Ground!
 * in Unofficial New Game Plus by Originem (there the distance moved with the difficulty; here the range
 * bonus does).
 */
public final class HighGround implements RuleEffect {

    private static final float DISTANCE = 2000f;

    @Override
    public void advanceCombat(RuleSpec rule, CombatEngineAPI engine, float amount, int level, float value) {
        String id = RuleEffect.modId(rule);
        ShipAPI player = engine.getPlayerShip();
        for (ShipAPI ship : engine.getShips()) {
            if (ship.getOwner() != 0 || ship.isFighter() || !ship.isAlive()) continue;
            boolean near = false;
            for (BattleObjectiveAPI objective : engine.getObjectives()) {
                if (Misc.getDistance(objective.getLocation(), ship.getLocation()) <= DISTANCE) {
                    near = true;
                    break;
                }
            }
            MutableShipStatsAPI stats = ship.getMutableStats();
            if (near) {
                stats.getBallisticWeaponRangeBonus().modifyPercent(id, value);
                stats.getEnergyWeaponRangeBonus().modifyPercent(id, value);
                if (ship == player) {
                    engine.maintainStatusForPlayerShip(id, "graphics/icons/sensor_array.png", rule.name,
                            "Weapon range +" + (int) value + "%", false);
                }
            } else {
                stats.getBallisticWeaponRangeBonus().unmodify(id);
                stats.getEnergyWeaponRangeBonus().unmodify(id);
            }
        }
    }
}
