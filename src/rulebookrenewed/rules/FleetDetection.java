package rulebookrenewed.rules;

import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;

/** Other fleets see the player's fleet from farther away (hard) or closer (helpful). */
public final class FleetDetection extends FleetStatRule {

    @Override
    protected void modify(MutableFleetStatsAPI stats, String id, float value, float sign, String source) {
        Scale.by(stats.getDetectedRangeMod(), id, value, -sign, source);
    }

    @Override
    protected void clear(MutableFleetStatsAPI stats, String id) {
        stats.getDetectedRangeMod().unmodify(id);
    }
}
