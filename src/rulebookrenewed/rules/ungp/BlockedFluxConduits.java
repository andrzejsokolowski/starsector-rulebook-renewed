package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Hard: while a ship of the player's side has its shield raised, its flux dissipation is cut, more the
 * wider the shield has opened: the full value at the full arc. Adapted from Blocked Flux Conduits in
 * Unofficial New Game Plus by Originem.
 */
public final class BlockedFluxConduits implements RuleEffect {

    @Override
    public void advanceCombat(RuleSpec rule, CombatEngineAPI engine, float amount, int level, float value) {
        String id = RuleEffect.modId(rule);
        ShipAPI player = engine.getPlayerShip();
        for (ShipAPI ship : engine.getShips()) {
            if (ship.getOwner() != 0 || ship.isFighter() || !ship.isAlive()) continue;
            ShieldAPI shield = ship.getShield();
            if (shield == null) continue;
            MutableShipStatsAPI stats = ship.getMutableStats();
            if (!shield.isOn() || shield.getArc() <= 0f) {
                stats.getFluxDissipation().unmodify(id);
                continue;
            }
            float closed = Math.min(1f, Math.max(0f, 1f - shield.getActiveArc() / shield.getArc()));
            float cut = value * (1f - closed * closed);
            stats.getFluxDissipation().modifyPercent(id, -cut);
            if (ship == player) {
                engine.maintainStatusForPlayerShip(id, rule.icon, rule.name, "Flux dissipation -" + (int) cut + "%", true);
            }
        }
    }
}
