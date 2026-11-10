package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

import rulebookrenewed.rules.CombatStatRule;

/**
 * Helpful: ships of the player's side dissipate hard flux while their shields are raised, at a share
 * of their flux dissipation. Adapted from Experimental Shunt in Unofficial New Game Plus by Originem.
 */
public final class ExperimentalShunt extends CombatStatRule {

    @Override
    protected boolean targetsEnemy() {
        return false;
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign) {
        stats.getHardFluxDissipationFraction().modifyFlat(id, sign * value / 100f);
    }
}
