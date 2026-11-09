package rulebookrenewed.rules;

import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;

/** Helpful challenge reward: the player's fleet is seen from closer and sees farther. */
public final class ShadowFleet extends FleetStatRule {

    @Override
    protected void modify(MutableFleetStatsAPI stats, String id, float value, float sign, String source) {
        Scale.by(stats.getDetectedRangeMod(), id, value, -1f, source);
        Scale.by(stats.getSensorRangeMod(), id, value, 1f, source);
    }

    @Override
    protected void clear(MutableFleetStatsAPI stats, String id) {
        stats.getDetectedRangeMod().unmodify(id);
        stats.getSensorRangeMod().unmodify(id);
    }
}
