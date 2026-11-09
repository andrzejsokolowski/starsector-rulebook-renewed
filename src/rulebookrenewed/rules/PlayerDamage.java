package rulebookrenewed.rules;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * Weapons on the player's ships deal more (helpful) or less (hard) damage.
 */
public final class PlayerDamage extends MemberStatRule {

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        float percent = Math.max(-95f, sign * value);
        stats.getBallisticWeaponDamageMult().modifyPercent(id, percent, source);
        stats.getEnergyWeaponDamageMult().modifyPercent(id, percent, source);
        stats.getMissileWeaponDamageMult().modifyPercent(id, percent, source);
    }
}
