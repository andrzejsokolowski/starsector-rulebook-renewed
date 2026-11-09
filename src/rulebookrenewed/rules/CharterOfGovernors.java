package rulebookrenewed.rules;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;

/** Helpful, unlocked by growing a colony to size 6: the player can employ more administrators. */
public final class CharterOfGovernors extends CharacterStatRule {

    @Override
    protected void modify(MutableCharacterStatsAPI stats, String id, float value, float sign, String source) {
        stats.getAdminNumber().modifyFlat(id, value, source);
    }

    @Override
    protected void clear(MutableCharacterStatsAPI stats, String id) {
        stats.getAdminNumber().unmodify(id);
    }
}
