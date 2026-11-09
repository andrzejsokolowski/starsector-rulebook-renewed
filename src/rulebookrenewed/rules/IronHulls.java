package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Helpful challenge reward: the player's ships and allies take less armor and hull damage.
 */
public final class IronHulls extends CombatStatRule {

    @Override
    protected boolean targetsEnemy() {
        return false;
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign) {
        Scale.by(stats.getArmorDamageTakenMult(), id, value, -1f, null);
        Scale.by(stats.getHullDamageTakenMult(), id, value, -1f, null);
    }
}
