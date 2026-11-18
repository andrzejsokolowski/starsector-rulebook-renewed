package rulebookrenewed.rules;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;

/**
 * Makes a change to Stats.DMOD_EFFECT_MULT reach the ship's D-mods.
 *
 * FleetMember.updateStats (0.98.5a) builds a ship's stats in this order: the captain's and the fleet
 * commander's skills, then every hull mod's applyEffectsBeforeShipCreation, and only later, when the fleet
 * syncs, the buffs. D-mods read DMOD_EFFECT_MULT inside applyEffectsBeforeShipCreation, so a buff that
 * changes it comes too late. So after changing it, the rule runs the ship's D-mods once more: each D-mod
 * sets its own modifiers under its own id, so running it again replaces them with values that use the
 * new multiplier (checked for the vanilla D-mods and CompromisedStructure.modifyCost).
 */
final class DModRefresh {

    private static final Logger LOG = Global.getLogger(DModRefresh.class);

    private DModRefresh() {}

    static void reapply(MutableShipStatsAPI stats) {
        ShipVariantAPI variant = stats.getVariant();
        if (variant == null || variant.getHullSpec() == null) return;
        Set<String> ids = new LinkedHashSet<>(variant.getHullSpec().getBuiltInMods());
        ids.addAll(variant.getHullMods());
        for (String id : ids) {
            HullModSpecAPI spec;
            try {
                spec = Global.getSettings().getHullModSpec(id);
            } catch (Throwable t) {
                continue;
            }
            if (spec == null || !spec.hasTag(Tags.HULLMOD_DMOD) || spec.getEffect() == null) continue;
            try {
                spec.getEffect().applyEffectsBeforeShipCreation(variant.getHullSize(), stats, id);
            } catch (Throwable t) {
                LOG.error("Rule Book: could not re-apply D-mod " + id, t);
            }
        }
    }
}
