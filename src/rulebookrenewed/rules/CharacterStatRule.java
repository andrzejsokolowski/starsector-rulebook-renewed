package rulebookrenewed.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;

import rulebookrenewed.RuleSpec;

/** A rule that changes one of the player character's stats (officers, command points, ordnance points). */
public abstract class CharacterStatRule implements RuleEffect {

    /** sign: +1 when the rule helps the player, -1 when it hurts. */
    protected abstract void modify(MutableCharacterStatsAPI stats, String id, float value, float sign, String source);

    protected abstract void clear(MutableCharacterStatsAPI stats, String id);

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        MutableCharacterStatsAPI stats = Global.getSector().getPlayerStats();
        if (stats != null) modify(stats, RuleEffect.modId(rule), value, RuleEffect.playerSign(rule), rule.name);
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        MutableCharacterStatsAPI stats = Global.getSector().getPlayerStats();
        if (stats != null) clear(stats, RuleEffect.modId(rule));
    }
}
