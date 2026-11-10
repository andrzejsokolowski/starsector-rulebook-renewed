package rulebookrenewed.rules.ungp;

import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.RepLevel;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Shared by Tachyon Labour and Full of Company: the first time the rule is switched on in a save, the
 * player's standing with some factions is set to a level. Nothing is taken back when it is switched off.
 */
abstract class FirstPickStanding implements RuleEffect {

    protected abstract String[] factions();

    protected abstract RepLevel standing();

    @Override
    public void onSwitchedOn(RuleSpec rule, int level, float value) {
        Map<String, Object> data = Global.getSector().getPersistentData();
        String key = RuleEffect.modId(rule) + "_done";
        if (data.containsKey(key)) return;
        data.put(key, Boolean.TRUE);
        for (String faction : factions()) {
            if (Global.getSector().getFaction(faction) != null) {
                Global.getSector().getPlayerFaction().setRelationship(faction, standing());
            }
        }
    }
}
