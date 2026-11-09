package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/** Hard: enemy weapons deal more damage. Applied to fighters too, since they are ships in combat. */
public final class DeadlyFoes extends CombatStatRule {

    @Override
    protected boolean targetsEnemy() {
        return true;
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value) {
        stats.getBallisticWeaponDamageMult().modifyPercent(id, value);
        stats.getEnergyWeaponDamageMult().modifyPercent(id, value);
        stats.getMissileWeaponDamageMult().modifyPercent(id, value);
    }
}
