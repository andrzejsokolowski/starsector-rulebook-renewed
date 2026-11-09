package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Ships in the player's fleet use fewer (helpful) or more (hard) supplies each month.
 */
public final class PlayerSupplies extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        if (sign > 0f) stats.getSuppliesPerMonth().modifyMult(id, Math.max(0.05f, 1f - value / 100f), source);
        else stats.getSuppliesPerMonth().modifyPercent(id, value, source);
    }
}
