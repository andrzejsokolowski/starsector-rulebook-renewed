package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Fighters in the player's fleet are replaced faster (helpful) or slower (hard).
 */
public final class PlayerFighters extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        Scale.by(stats.getFighterRefitTimeMult(), id, value, -sign, source);
    }
}
