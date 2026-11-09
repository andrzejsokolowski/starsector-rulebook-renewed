package rulebookrenewed.rules;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;

/** Ships the player commands have more (helpful) or fewer (hard) ordnance points. */
public final class PlayerOrdnance extends CharacterStatRule {

    @Override
    protected void modify(MutableCharacterStatsAPI stats, String id, float value, float sign, String source) {
        Scale.by(stats.getShipOrdnancePointBonus(), id, value, sign, source);
    }

    @Override
    protected void clear(MutableCharacterStatsAPI stats, String id) {
        stats.getShipOrdnancePointBonus().unmodify(id);
    }
}
