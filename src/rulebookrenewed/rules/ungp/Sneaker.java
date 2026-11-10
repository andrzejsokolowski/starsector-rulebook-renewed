package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import rulebookrenewed.rules.FleetStatRule;

/**
 * Helpful: the player's fleet has a higher burn level while moving slowly, which is also the speed of
 * Go Dark. Adapted from Sneaker in Unofficial New Game Plus by Originem.
 */
public final class Sneaker extends FleetStatRule {

    @Override
    protected void modify(MutableFleetStatsAPI stats, String id, float value, float sign, String source) {
        stats.getDynamic().getMod(Stats.MOVE_SLOW_SPEED_BONUS_MOD).modifyFlat(id, sign * value, source);
    }

    @Override
    protected void clear(MutableFleetStatsAPI stats, String id) {
        stats.getDynamic().getMod(Stats.MOVE_SLOW_SPEED_BONUS_MOD).unmodify(id);
    }
}
