package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Enemy ships have more (hard) or less (helpful) hull, a percent of each ship's own hull. Hull points are
 * fixed when a ship is built, so this goes on the fleet member before the battle, like enemy armor.
 */
public final class EnemyHull extends EnemyMemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        Scale.by(stats.getHullBonus(), id, value, sign, source);
    }
}
