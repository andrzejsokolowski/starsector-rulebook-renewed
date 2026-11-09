package rulebookrenewed.compat.peerless;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;

import peerless.api.PeerlessSkills;
import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Helpful, only with Peerless Skills: switching it on grants a Peerless point, once per campaign.
 * Peerless Skills keeps the "once" itself: awardOnce pays each memory key a single time.
 * The rule catalog leaves this row out when Peerless Skills is not enabled, so this class is never
 * loaded without it.
 */
public final class PeerlessRule implements RuleEffect {

    private static final String AWARD_KEY = "$rbr_peerlessRulePoint";

    @Override
    public void onSwitchedOn(RuleSpec rule, int level, float value) {
        if (PeerlessSkills.awardOnce(AWARD_KEY)) {
            Global.getSector().getCampaignUI().addMessage("Gained a Peerless point", Misc.getHighlightColor());
        }
    }
}
