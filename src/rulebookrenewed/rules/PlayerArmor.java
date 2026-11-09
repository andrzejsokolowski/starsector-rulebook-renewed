package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Ships in the player's fleet have more (helpful) or less (hard) armor, a percent of each ship's own armor.
 */
public final class PlayerArmor extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        stats.getArmorBonus().modifyPercent(id, Math.max(-95f, sign * value), source);
    }
}
