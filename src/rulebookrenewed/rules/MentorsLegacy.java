package rulebookrenewed.rules;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

/** Helpful, unlocked by reaching level 15: the player's officers can reach more levels. */
public final class MentorsLegacy extends CharacterStatRule {

    @Override
    protected void modify(MutableCharacterStatsAPI stats, String id, float value, float sign, String source) {
        stats.getDynamic().getMod(Stats.OFFICER_MAX_LEVEL_MOD).modifyFlat(id, value, source);
    }

    @Override
    protected void clear(MutableCharacterStatsAPI stats, String id) {
        stats.getDynamic().getMod(Stats.OFFICER_MAX_LEVEL_MOD).unmodify(id);
    }
}
