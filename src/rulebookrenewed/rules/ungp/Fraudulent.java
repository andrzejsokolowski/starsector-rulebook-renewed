package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyInteractionListener;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleCatalog;
import rulebookrenewed.RuleSpec;
import rulebookrenewed.RuleState;
import rulebookrenewed.rules.RuleEffect;

/**
 * Hard: every trade that earns the player credits has a chance to lose a fifth of what it earned, twice
 * the chance on a black market. A transient listener on the Sector hears the trades while the rule is
 * on. Adapted from Fraudulent in Unofficial New Game Plus by Originem.
 */
public final class Fraudulent implements RuleEffect {

    private static final float LOST_SHARE = 0.2f;

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        ListenerManagerAPI listeners = Global.getSector().getListenerManager();
        if (!listeners.hasListenerOfClass(Skimmer.class)) listeners.addListener(new Skimmer(rule.id), true);
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        Global.getSector().getListenerManager().removeListenerOfClass(Skimmer.class);
    }

    private static final class Skimmer implements ColonyInteractionListener {
        private final String ruleId;

        Skimmer(String ruleId) {
            this.ruleId = ruleId;
        }

        @Override
        public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {
            RuleSpec rule = RuleCatalog.get(ruleId);
            int level = rule == null ? 0 : RuleState.level(rule);
            if (level <= 0 || transaction == null) return;
            float earned = transaction.getCreditValue();
            if (earned <= 0f) return;
            float chance = rule.value(level) / 100f;
            if (transaction.getSubmarket() != null && transaction.getSubmarket().getPlugin() != null
                    && transaction.getSubmarket().getPlugin().isBlackMarket()) chance *= 2f;
            if (!Tools.roll(chance)) return;
            int lost = (int) (earned * LOST_SHARE);
            if (lost <= 0) return;
            Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(lost);
            Tools.note(rule.name + ": " + Misc.getDGSCredits(lost) + " of this sale lost.", Misc.getNegativeHighlightColor());
            Tools.uiSound("rbr_fraudulent");
        }

        @Override
        public void reportPlayerOpenedMarket(MarketAPI market) {}

        @Override
        public void reportPlayerClosedMarket(MarketAPI market) {}

        @Override
        public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {}
    }
}
