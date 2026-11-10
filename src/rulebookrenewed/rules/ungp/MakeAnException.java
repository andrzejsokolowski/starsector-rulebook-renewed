package rulebookrenewed.rules.ungp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.inspection.HegemonyInspectionIntel;
import com.fs.starfarer.api.impl.campaign.intel.inspection.HegemonyInspectionIntel.AntiInspectionOrders;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Helpful: each Hegemony AI inspection aimed at the player, while its sender is not hostile to the
 * player, has a chance to be handled as if the player had bribed the inspectors, at no cost. Every
 * inspection is rolled once, on the first daily check that sees it; the ones already rolled are kept in
 * the save. Adapted from Make an Exception in Unofficial New Game Plus by Originem.
 */
public final class MakeAnException implements RuleEffect {

    private static final String DAY_KEY = "rbr_exceptionDay";
    private static final String SEEN_KEY = "rbr_exceptionSeen";

    @Override
    @SuppressWarnings("unchecked")
    public void applyCampaign(RuleSpec rule, int level, float value) {
        if (Tools.daysSince(DAY_KEY) < 1f) return;
        Tools.restart(DAY_KEY);
        Map<String, Object> data = Global.getSector().getPersistentData();
        Object stored = data.get(SEEN_KEY);
        if (!(stored instanceof List)) {
            stored = new ArrayList<Object>();
            data.put(SEEN_KEY, stored);
        }
        List<Object> seen = (List<Object>) stored;
        for (IntelInfoPlugin plugin : Global.getSector().getIntelManager().getIntel(HegemonyInspectionIntel.class)) {
            HegemonyInspectionIntel inspection = (HegemonyInspectionIntel) plugin;
            if (seen.contains(inspection) || inspection.isDone() || inspection.shouldRemoveIntel()) continue;
            seen.add(inspection);
            if (inspection.getFaction() == null || inspection.getFaction().isHostileTo(Factions.PLAYER)) continue;
            if (inspection.getOrders() == AntiInspectionOrders.BRIBE || !Tools.roll(value / 100f)) continue;
            inspection.setOrders(AntiInspectionOrders.BRIBE);
            Tools.note(rule.name + ": the AI inspection will find nothing.", Misc.getPositiveHighlightColor());
        }
        Iterator<Object> it = seen.iterator();
        while (it.hasNext()) {
            Object old = it.next();
            if (!(old instanceof HegemonyInspectionIntel)) {
                it.remove();
                continue;
            }
            HegemonyInspectionIntel inspection = (HegemonyInspectionIntel) old;
            if (inspection.isDone() || inspection.shouldRemoveIntel()) it.remove();
        }
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        Tools.forget(DAY_KEY);
        Tools.forget(SEEN_KEY);
    }
}
