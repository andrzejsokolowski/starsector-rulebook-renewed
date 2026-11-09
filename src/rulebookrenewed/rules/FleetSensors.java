package rulebookrenewed.rules;

import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;

/** The player's fleet has a longer (helpful) or shorter (hard) sensor range. */
public final class FleetSensors extends FleetStatRule {

    @Override
    protected void modify(MutableFleetStatsAPI stats, String id, float value, float sign, String source) {
        Scale.by(stats.getSensorRangeMod(), id, value, sign, source);
    }

    @Override
    protected void clear(MutableFleetStatsAPI stats, String id) {
        stats.getSensorRangeMod().unmodify(id);
    }
}
