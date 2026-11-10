package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import rulebookrenewed.rules.CharacterStatRule;

/**
 * Hard: the player starts a battle with fewer deployment points, by a share of the battle size (both
 * the usual share and the guaranteed minimum). Adapted from Besieged in Unofficial New Game Plus by
 * Originem.
 */
public final class Besieged extends CharacterStatRule {

    @Override
    protected void modify(MutableCharacterStatsAPI stats, String id, float value, float sign, String source) {
        float share = sign * value / 100f;
        stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_FRACTION_OF_BATTLE_SIZE_BONUS_MOD).modifyFlat(id, share, source);
        stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MIN_FRACTION_OF_BATTLE_SIZE_BONUS_MOD).modifyFlat(id, share, source);
    }

    @Override
    protected void clear(MutableCharacterStatsAPI stats, String id) {
        stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_FRACTION_OF_BATTLE_SIZE_BONUS_MOD).unmodify(id);
        stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MIN_FRACTION_OF_BATTLE_SIZE_BONUS_MOD).unmodify(id);
    }
}
