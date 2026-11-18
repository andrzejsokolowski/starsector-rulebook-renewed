package rulebookrenewed.rules;

import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

/**
 * Salvage from derelicts, debris and battles is richer (helpful) or poorer (hard). The two fleet stats the
 * game reads for salvage value:
 * - SalvageEntity.getValueRecoveryStat turns only the FLAT mods of SALVAGE_VALUE_MULT_FLEET_INCLUDES_RARE
 *   into its amount bonus (0.2 = +20%), the way the Salvaging skill does it; percent and mult mods there
 *   only change rare item rolls.
 * - FleetEncounterContext reads BATTLE_SALVAGE_MULT_FLEET as a multiplier.
 */
public final class FleetSalvage extends FleetStatRule {

    @Override
    protected void modify(MutableFleetStatsAPI stats, String id, float value, float sign, String source) {
        stats.getDynamic().getStat(Stats.SALVAGE_VALUE_MULT_FLEET_INCLUDES_RARE).modifyFlat(id, sign * value / 100f, source);
        Scale.by(stats.getDynamic().getStat(Stats.BATTLE_SALVAGE_MULT_FLEET), id, value, sign, source);
    }

    @Override
    protected void clear(MutableFleetStatsAPI stats, String id) {
        stats.getDynamic().getStat(Stats.SALVAGE_VALUE_MULT_FLEET_INCLUDES_RARE).unmodify(id);
        stats.getDynamic().getStat(Stats.BATTLE_SALVAGE_MULT_FLEET).unmodify(id);
    }
}
