package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Positive: the crew and marines take less pay (CrewPay changes the wages). At the top level they take no
 * pay at all, and the ships lose maximum combat readiness for it.
 */
public final class LoyalHands extends CrewPay {

    private static final float NO_PAY = 100f, READINESS_LOST = 0.1f;

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        super.modify(stats, id, value, sign, source);
        if (value >= NO_PAY) stats.getMaxCombatReadiness().modifyFlat(id, -READINESS_LOST, source);
        else stats.getMaxCombatReadiness().unmodify(id);
    }
}
