package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/** Helpful: every ship in the player's fleet has more maximum combat readiness. */
public final class VeteranCrews extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, String source) {
        stats.getMaxCombatReadiness().modifyFlat(id, value / 100f, source);
    }
}
