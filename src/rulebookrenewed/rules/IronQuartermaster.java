package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Helpful challenge reward: ships in the player's fleet use fewer supplies and less fuel.
 */
public final class IronQuartermaster extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        Scale.by(stats.getSuppliesPerMonth(), id, value, -1f, source);
        Scale.by(stats.getFuelUseMod(), id, value, -1f, source);
    }
}
