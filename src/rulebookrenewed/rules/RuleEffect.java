package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import rulebookrenewed.RuleSpec;

/**
 * What a rule does. One instance per rule row, made from the class named in rules.csv. Every hook has
 * an empty default, so an effect fills in only the parts of the game it touches.
 *
 * Level counts from 1; value is the rule's number for that level from the values column, always
 * positive. Mirrored rules (a hard rule and its helpful twin) share one effect class: the rule's type
 * decides which way the number pushes, see {@link #enemySign} and {@link #playerSign}.
 */
public interface RuleEffect {

    /** Campaign side. Called on load, after every change in the picker, and every few hours of game time. */
    default void applyCampaign(RuleSpec rule, int level, float value) {}

    /** Called in the same places while the rule is off. Must be safe to call when nothing was applied. */
    default void unapplyCampaign(RuleSpec rule) {}

    /** Called once when the player switches the rule on from off. */
    default void onSwitchedOn(RuleSpec rule, int level, float value) {}

    /** Combat side. Called once for every ship and fighter in a campaign battle, on the first frame it is on the field. */
    default void applyToShip(RuleSpec rule, ShipAPI ship, boolean enemy, int level, float value) {}

    /** Called by the fleet buff that MemberStatRule keeps on every ship in the player's fleet. */
    default void applyToMember(RuleSpec rule, FleetMemberAPI member, int level, float value) {}

    /** True when the rule changes enemy ships before they are built for a battle (see BattlePrep). */
    default boolean usesEnemyMemberBuff() {
        return false;
    }

    /** Called by the buff BattlePrep puts on every enemy ship before a battle. */
    default void applyToEnemyMember(RuleSpec rule, FleetMemberAPI member, int level, float value) {}

    static String modId(RuleSpec rule) {
        return "rbr_" + rule.id;
    }

    /** For rules about the enemy: +1 when the rule makes enemies stronger (a hard rule), -1 otherwise. */
    static float enemySign(RuleSpec rule) {
        return rule.hard ? 1f : -1f;
    }

    /** For rules about the player: +1 when the rule helps the player (a helpful rule), -1 otherwise. */
    static float playerSign(RuleSpec rule) {
        return rule.hard ? -1f : 1f;
    }
}
