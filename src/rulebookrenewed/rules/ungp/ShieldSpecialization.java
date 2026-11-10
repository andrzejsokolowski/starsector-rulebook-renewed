package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

import rulebookrenewed.rules.MemberStatRule;

/**
 * Golden: shields of the player's ships take less damage, unfold and turn faster, and cost less to keep
 * up, all by the same percent. Adapted from Shield Specialization in Unofficial New Game Plus by Originem.
 */
public final class ShieldSpecialization extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        stats.getShieldDamageTakenMult().modifyMult(id, Math.max(0.05f, 1f - value / 100f));
        stats.getShieldTurnRateMult().modifyPercent(id, value);
        stats.getShieldUnfoldRateMult().modifyPercent(id, value);
        stats.getShieldUpkeepMult().modifyMult(id, Math.max(0.05f, 1f - value / 100f));
    }
}
