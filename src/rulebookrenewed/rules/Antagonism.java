package rulebookrenewed.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import rulebookrenewed.RuleSpec;

/**
 * Hard: enemy ships fight with extra hull mods for the battle, picked at random from a list of plain combat
 * hull mods the ship does not already have. The ship's variant is never touched: the hull mod's own
 * effect is applied through the enemy fleet buff before the ship is built (see BattlePrep), and its
 * after-creation effect on the first frame in combat, both under this rule's own ids, so everything goes
 * when the buff comes off after the battle. The same ship always draws the same mods.
 * Adapted from Antagonism in Unofficial Rule Book by DemonX3 (MIT licence, see CREDITS.txt).
 */
public final class Antagonism implements RuleEffect {

    /** Combat hull mods that only change stats and work on any hull. */
    private static final String[] POOL = {
            "heavyarmor", "reinforcedhull", "fluxdistributor", "fluxcoil", "auxiliarythrusters", "armoredweapons",
            "hardened_subsystems", "eccm", "magazines", "missleracks", "turretgyros", "insulatedengine",
            "solar_shielding", "fluxbreakers", "unstable_injector",
    };
    /** Only for ships with a front or omni shield. */
    private static final String[] SHIELD_POOL = {
            "hardenedshieldemitter", "stabilizedshieldemitter", "extendedshieldemitter",
    };

    @Override
    public boolean usesEnemyMemberBuff() {
        return true;
    }

    @Override
    public void applyToEnemyMember(RuleSpec rule, FleetMemberAPI member, int level, float value) {
        for (HullModSpecAPI spec : picks(member, (int) value, rule.id)) {
            spec.getEffect().applyEffectsBeforeShipCreation(member.getHullSpec().getHullSize(), member.getStats(),
                    modId(rule, spec));
        }
    }

    @Override
    public void applyToShip(RuleSpec rule, ShipAPI ship, boolean enemy, int level, float value) {
        if (!enemy || ship.isFighter() || ship.getFleetMember() == null) return;
        for (HullModSpecAPI spec : picks(ship.getFleetMember(), (int) value, rule.id)) {
            spec.getEffect().applyEffectsAfterShipCreation(ship, modId(rule, spec));
        }
    }

    private static String modId(RuleSpec rule, HullModSpecAPI spec) {
        return RuleEffect.modId(rule) + "_" + spec.getId();
    }

    private static List<HullModSpecAPI> picks(FleetMemberAPI member, int count, String salt) {
        List<HullModSpecAPI> out = new ArrayList<>();
        if (member == null || count <= 0 || member.getHullSpec() == null) return out;
        ShipVariantAPI variant = member.getVariant();
        List<String> candidates = new ArrayList<>();
        addCandidates(candidates, POOL, variant);
        ShieldAPI.ShieldType shield = member.getHullSpec().getShieldType();
        if (shield == ShieldAPI.ShieldType.FRONT || shield == ShieldAPI.ShieldType.OMNI) addCandidates(candidates, SHIELD_POOL, variant);
        Random random = new Random((member.getId() + salt).hashCode());
        while (out.size() < count && !candidates.isEmpty()) {
            String id = candidates.remove(random.nextInt(candidates.size()));
            HullModSpecAPI spec = Global.getSettings().getHullModSpec(id);
            if (spec != null && spec.getEffect() != null) out.add(spec);
        }
        return out;
    }

    private static void addCandidates(List<String> into, String[] ids, ShipVariantAPI variant) {
        for (String id : ids) {
            if (variant == null || !variant.hasHullMod(id)) into.add(id);
        }
    }
}
