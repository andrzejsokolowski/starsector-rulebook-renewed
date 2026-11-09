package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Enemy ships have more (hard) or less (helpful) armor, a percent of each ship's own armor, so a frigate
 * and a capital change by the same share. Built into the ship before it deploys, see BattlePrep.
 */
public final class EnemyArmor extends EnemyMemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        stats.getArmorBonus().modifyPercent(id, Math.max(-95f, sign * value), source);
    }
}
