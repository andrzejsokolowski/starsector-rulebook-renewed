package rulebookrenewed.rules;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;

/** The player starts battles with more (helpful) or fewer (hard) command points. */
public final class PlayerCommandPoints extends CharacterStatRule {

    @Override
    protected void modify(MutableCharacterStatsAPI stats, String id, float value, float sign, String source) {
        stats.getCommandPoints().modifyFlat(id, sign * value, source);
    }

    @Override
    protected void clear(MutableCharacterStatsAPI stats, String id) {
        stats.getCommandPoints().unmodify(id);
    }
}
