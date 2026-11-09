package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

/**
 * Helpful challenge reward: every D-mod on a ship of the player's fleet (up to five) gives it more armor and
 * makes its D-mods milder. The idea comes from Metalize in Unofficial Rule Book by DemonX3 (MIT licence,
 * see CREDITS.txt).
 */
public final class ScarredVeterans extends MemberStatRule {

    private static final int MAX_DMODS = 5;

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        ShipVariantAPI variant = stats.getVariant();
        int dmods = variant == null ? 0 : Math.min(MAX_DMODS, DModManager.getNumDMods(variant));
        if (dmods <= 0) {
            stats.getArmorBonus().unmodify(id);
            stats.getDynamic().getStat(Stats.DMOD_EFFECT_MULT).unmodify(id);
            return;
        }
        Scale.by(stats.getArmorBonus(), id, value * dmods, 1f, source);
        Scale.by(stats.getDynamic().getStat(Stats.DMOD_EFFECT_MULT), id, value * dmods, -1f, source);
    }
}
