package rulebookrenewed.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.VariantSource;

import rulebookrenewed.RuleSpec;

/**
 * Hard: the player's flagship carries the game's unique-signature tag (as the Ziggurat does), so other
 * fleets know who the player is even with the transponder off. Only ships this rule tagged lose the tag
 * again (a ship that has it by nature keeps it), and the ids it tagged are kept in the save. Adapted from
 * Celebrity in Unofficial Rule Book by DemonX3 (MIT licence, see CREDITS.txt), whose bookkeeping could
 * strip the tag from a ship that had it by nature.
 */
public final class Celebrity implements RuleEffect {

    private static final float CHECK_SECONDS = 0.5f;
    /** Fleet member ids this rule gave the tag to. */
    private static final String TAGGED_KEY = "rbr_celebrityTagged";

    private float sinceCheck;

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        sync(true);
    }

    @Override
    public void advanceCampaign(RuleSpec rule, float amount, int level, float value) {
        sinceCheck += amount;
        if (sinceCheck < CHECK_SECONDS) return;
        sinceCheck = 0f;
        sync(true);
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        if (!Global.getSector().getPersistentData().containsKey(TAGGED_KEY)) return;
        sync(false);
        Global.getSector().getPersistentData().remove(TAGGED_KEY);
    }

    @SuppressWarnings("unchecked")
    private static Set<String> tagged() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        Object stored = data.get(TAGGED_KEY);
        if (!(stored instanceof Set)) {
            stored = new HashSet<String>();
            data.put(TAGGED_KEY, stored);
        }
        return (Set<String>) stored;
    }

    /** on: tag the flagship and untag the others this rule tagged; off: untag every ship this rule tagged. */
    private static void sync(boolean on) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return;
        Set<String> tagged = tagged();
        List<String> seen = new ArrayList<>();
        boolean changed = false;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            seen.add(member.getId());
            ShipVariantAPI variant = member.getVariant();
            boolean ours = tagged.contains(member.getId());
            boolean want = on && member.isFlagship();
            if (want && !variant.hasTag(Tags.SHIP_UNIQUE_SIGNATURE)) {
                ownVariant(member).addTag(Tags.SHIP_UNIQUE_SIGNATURE);
                tagged.add(member.getId());
                changed = true;
            } else if (!want && ours) {
                ownVariant(member).removeTag(Tags.SHIP_UNIQUE_SIGNATURE);
                tagged.remove(member.getId());
                changed = true;
            }
        }
        tagged.retainAll(seen); // ships no longer in the fleet
        if (changed) {
            // The fleet counts its unique-signature ships when its data is synced.
            fleet.getFleetData().setSyncNeeded();
            fleet.getFleetData().syncIfNeeded();
        }
    }

    /** The member's own copy of its variant, so a tag never lands on a shared stock variant. */
    private static ShipVariantAPI ownVariant(FleetMemberAPI member) {
        ShipVariantAPI variant = member.getVariant();
        if (variant.getSource() != VariantSource.REFIT) {
            variant = variant.clone();
            variant.setSource(VariantSource.REFIT);
            member.setVariant(variant, false, true);
        }
        return variant;
    }
}
