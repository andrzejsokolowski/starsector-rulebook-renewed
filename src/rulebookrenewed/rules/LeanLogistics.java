package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/** Helpful: every ship in the player's fleet uses fewer supplies each month. */
public final class LeanLogistics extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, String source) {
        stats.getSuppliesPerMonth().modifyMult(id, Math.max(0.05f, 1f - value / 100f), source);
    }
}
