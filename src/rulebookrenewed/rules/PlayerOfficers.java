package rulebookrenewed.rules;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;

/** The player can command more (helpful) or fewer (hard) officers. */
public final class PlayerOfficers extends CharacterStatRule {

    @Override
    protected void modify(MutableCharacterStatsAPI stats, String id, float value, float sign, String source) {
        stats.getOfficerNumber().modifyFlat(id, sign * value, source);
    }

    @Override
    protected void clear(MutableCharacterStatsAPI stats, String id) {
        stats.getOfficerNumber().unmodify(id);
    }
}
