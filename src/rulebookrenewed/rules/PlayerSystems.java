package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Ship systems in the player's fleet cool down and recharge faster (helpful) or slower (hard).
 */
public final class PlayerSystems extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        Scale.by(stats.getSystemCooldownBonus(), id, value, -sign, source);
        Scale.by(stats.getSystemRegenBonus(), id, value, sign, source);
    }
}
