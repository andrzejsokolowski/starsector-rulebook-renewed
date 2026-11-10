package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import rulebookrenewed.rules.CharacterStatRule;

/**
 * Helpful: the cap on the ECM rating the player's side can reach in battle is higher. The game reads
 * the cap from the commander's stats. Adapted from EW Smog in Unofficial New Game Plus by Originem.
 */
public final class EwSmog extends CharacterStatRule {

    @Override
    protected void modify(MutableCharacterStatsAPI stats, String id, float value, float sign, String source) {
        stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_MAX).modifyFlat(id, sign * value, source);
    }

    @Override
    protected void clear(MutableCharacterStatsAPI stats, String id) {
        stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_MAX).unmodify(id);
    }
}
