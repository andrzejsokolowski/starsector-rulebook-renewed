package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/** Hard: enemy ships take less damage to shields, armor and hull. */
public final class HardenedFoes extends CombatStatRule {

    @Override
    protected boolean targetsEnemy() {
        return true;
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value) {
        float mult = Math.max(0.05f, 1f - value / 100f);
        stats.getShieldDamageTakenMult().modifyMult(id, mult);
        stats.getArmorDamageTakenMult().modifyMult(id, mult);
        stats.getHullDamageTakenMult().modifyMult(id, mult);
    }
}
