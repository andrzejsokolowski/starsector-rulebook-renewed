package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import rulebookrenewed.rules.MemberStatRule;
import rulebookrenewed.rules.Scale;

/**
 * Hard: star coronas and the terrain that works like them (pulsar beams, event horizons) drain the
 * combat readiness of the player's ships faster. Adapted from Dangerous Sector in Unofficial New Game
 * Plus by Originem.
 */
public final class DangerousSector extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, Scale.mult(value, -sign));
    }
}
