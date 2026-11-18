package rulebookrenewed.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;

import rulebookrenewed.RuleSpec;

/**
 * Raging Storms (hard): hyperspace storm strikes do more damage. HyperspaceTerrainPlugin keeps the strike
 * strength in public static fields for the whole game session, so the originals are put back whenever
 * the rule is off. Only the rule that changed them may put them back: Calm Currents shares this class and
 * is unapplied on every refresh while Raging Storms is on.
 *
 * Calm Currents (helpful): the player's fleet takes no storm damage. The storm only strikes a fleet that
 * has no strike timeout in its memory (HyperspaceTerrainPlugin.applyStormStrikes), so the rule keeps that
 * timeout set on the player's fleet. The statics are left alone: STORM_MAX_STRIKE_DAMAGE also caps
 * campaign explosions and pre-damaged fleets, and the statics would change NPC fleets as well.
 */
public final class StormStrength implements RuleEffect {

    /** Longer than the refresh interval, so the timeout never runs out while the rule is on. */
    private static final float TIMEOUT_DAYS = 0.6f;

    private static String owner;
    private static float damageFraction, minStrike, maxStrike;

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        if (!rule.hard) {
            CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
            if (fleet != null) {
                fleet.getMemoryWithoutUpdate().set(HyperspaceTerrainPlugin.STORM_STRIKE_TIMEOUT_KEY, true, TIMEOUT_DAYS);
            }
            return;
        }
        if (owner == null) {
            damageFraction = HyperspaceTerrainPlugin.STORM_DAMAGE_FRACTION;
            minStrike = HyperspaceTerrainPlugin.STORM_MIN_STRIKE_DAMAGE;
            maxStrike = HyperspaceTerrainPlugin.STORM_MAX_STRIKE_DAMAGE;
            owner = rule.id;
        }
        float mult = Scale.mult(value, RuleEffect.enemySign(rule));
        HyperspaceTerrainPlugin.STORM_DAMAGE_FRACTION = damageFraction * mult;
        HyperspaceTerrainPlugin.STORM_MIN_STRIKE_DAMAGE = Math.min(1f, minStrike * mult);
        HyperspaceTerrainPlugin.STORM_MAX_STRIKE_DAMAGE = Math.min(1f, maxStrike * mult);
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        // Calm Currents' timeout runs out by itself; clearing it would also clear the game's own timeout.
        if (!rule.id.equals(owner)) return;
        HyperspaceTerrainPlugin.STORM_DAMAGE_FRACTION = damageFraction;
        HyperspaceTerrainPlugin.STORM_MIN_STRIKE_DAMAGE = minStrike;
        HyperspaceTerrainPlugin.STORM_MAX_STRIKE_DAMAGE = maxStrike;
        owner = null;
    }
}
