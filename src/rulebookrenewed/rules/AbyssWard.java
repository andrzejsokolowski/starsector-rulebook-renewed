package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Helpful, unlocked by beating Omega, the Threat or the Dwellers: ships in the player's fleet take less
 * energy damage.
 */
public final class AbyssWard extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        Scale.by(stats.getEnergyDamageTakenMult(), id, value, -1f, source);
        Scale.by(stats.getEnergyShieldDamageTakenMult(), id, value, -1f, source);
    }
}
