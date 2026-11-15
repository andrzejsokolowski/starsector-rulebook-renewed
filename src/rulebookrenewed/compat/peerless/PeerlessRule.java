package rulebookrenewed.compat.peerless;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;

import peerlessrenewed.PeerlessData;
import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Helpful, only with Peerless Skills - Renewed: switching it on grants Peerless points (the rule's value),
 * once per campaign. The rule catalog leaves this row out when that mod is not enabled, so this class is
 * never loaded without it.
 */
public final class PeerlessRule implements RuleEffect {

    /** Sector persistent data: set once the points were given in this campaign. */
    private static final String PAID_KEY = "rbr_peerlessRulePaid";

    @Override
    public void onSwitchedOn(RuleSpec rule, int level, float value) {
        if (Global.getSector().getPersistentData().containsKey(PAID_KEY)) return;
        int points = Math.max(1, Math.round(value));
        Global.getSector().getPersistentData().put(PAID_KEY, true);
        PeerlessData.addPoints(points);
        Global.getSector().getCampaignUI().addMessage("Gained " + points + (points == 1 ? " Peerless point" : " Peerless points"),
                Misc.getHighlightColor());
    }
}
