package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Shields in the player's fleet take less (helpful) or more (hard) damage.
 */
public final class PlayerShields extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        Scale.by(stats.getShieldDamageTakenMult(), id, value, -sign, source);
    }
}
