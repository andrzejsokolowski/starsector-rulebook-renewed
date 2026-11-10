package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Hard: while the player's marines number less than a share of the crew, each day has an 8% chance of
 * a riot: one ship (more likely one in good readiness) loses 75% combat readiness and half of its crew
 * dies. A warning names the missing marines at most every 3 days. Adapted from Work Harder! in
 * Unofficial New Game Plus by Originem.
 */
public final class WorkHarder implements RuleEffect {

    private static final String DAY_KEY = "rbr_workHarderDay";
    private static final String WARNED_KEY = "$rbr_workHarderWarned";
    private static final float RIOT_CHANCE = 0.08f;
    private static final float CR_LOSS = 0.75f;
    private static final float CREW_LOSS = 0.5f;

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        if (Tools.daysSince(DAY_KEY) < 1f) return;
        Tools.restart(DAY_KEY);
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return;
        int crew = Math.max(1, fleet.getCargo().getCrew());
        int missing = (int) Math.ceil(crew * value / 100f - fleet.getCargo().getMarines());
        if (missing <= 0) return;
        if (!fleet.getMemoryWithoutUpdate().contains(WARNED_KEY)) {
            fleet.getMemoryWithoutUpdate().set(WARNED_KEY, true, 3f);
            Tools.note(rule.name + ": " + missing + " more marines needed to keep order.", Misc.getNegativeHighlightColor());
        }
        if (!Tools.roll(RIOT_CHANCE)) return;

        WeightedRandomPicker<FleetMemberAPI> picker = new WeightedRandomPicker<>(Tools.RANDOM);
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (member.isMothballed() || member.getCrewComposition() == null) continue;
            if (member.getCrewComposition().getCrewInt() < 1) continue;
            picker.add(member, Math.max(0.01f, member.getRepairTracker().getCR()));
        }
        FleetMemberAPI ship = picker.pick();
        if (ship == null) return;
        int dead = Math.max(1, (int) (ship.getCrewComposition().getCrewInt() * CREW_LOSS));
        if (crew - dead <= 0) return;
        ship.getRepairTracker().applyCREvent(-CR_LOSS, rule.name);
        fleet.getCargo().removeCrew(dead);
        Tools.note(rule.name + ": riot on " + ship.getShipName() + ", " + dead + " crew lost.", Misc.getNegativeHighlightColor());
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        Tools.forget(DAY_KEY);
    }
}
