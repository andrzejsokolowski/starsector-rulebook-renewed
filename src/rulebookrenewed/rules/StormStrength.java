package rulebookrenewed.rules;

import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;

import rulebookrenewed.RuleSpec;

/**
 * Hyperspace storm strikes do more (hard) or less (helpful) damage. HyperspaceTerrainPlugin keeps the
 * strike strength in public static fields for the whole game session, so the originals are put back
 * whenever the rule is off.
 */
public final class StormStrength implements RuleEffect {

    private static boolean changed;
    private static float damageFraction, minStrike, maxStrike;

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        if (!changed) {
            damageFraction = HyperspaceTerrainPlugin.STORM_DAMAGE_FRACTION;
            minStrike = HyperspaceTerrainPlugin.STORM_MIN_STRIKE_DAMAGE;
            maxStrike = HyperspaceTerrainPlugin.STORM_MAX_STRIKE_DAMAGE;
            changed = true;
        }
        float mult = Scale.mult(value, RuleEffect.enemySign(rule));
        HyperspaceTerrainPlugin.STORM_DAMAGE_FRACTION = damageFraction * mult;
        HyperspaceTerrainPlugin.STORM_MIN_STRIKE_DAMAGE = Math.min(1f, minStrike * mult);
        HyperspaceTerrainPlugin.STORM_MAX_STRIKE_DAMAGE = Math.min(1f, maxStrike * mult);
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        if (!changed) return;
        HyperspaceTerrainPlugin.STORM_DAMAGE_FRACTION = damageFraction;
        HyperspaceTerrainPlugin.STORM_MIN_STRIKE_DAMAGE = minStrike;
        HyperspaceTerrainPlugin.STORM_MAX_STRIKE_DAMAGE = maxStrike;
        changed = false;
    }
}
