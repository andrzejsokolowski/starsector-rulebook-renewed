package rulebookrenewed.rules.ungp;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.loading.ProjectileSpawnType;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Hard: each ballistic-type projectile fired by the player's side has a chance to be a dud that deals
 * half damage. Projectiles are looked at ten times a second, once each. Adapted from Wholesale Shells
 * in Unofficial New Game Plus by Originem.
 */
public final class WholesaleShells implements RuleEffect {

    private static final String KEY = "rbr_wholesale_shells";
    private static final float CHECK_SECONDS = 0.1f;
    private static final float DUD_DAMAGE = 0.5f;

    private static final class State {
        float sinceCheck;
        final Set<DamagingProjectileAPI> seen = new HashSet<>();
    }

    @Override
    public void advanceCombat(RuleSpec rule, CombatEngineAPI engine, float amount, int level, float value) {
        State state = Tools.data(engine, KEY);
        if (state == null) {
            state = new State();
            engine.getCustomData().put(KEY, state);
        }
        state.sinceCheck += amount;
        if (state.sinceCheck < CHECK_SECONDS) return;
        state.sinceCheck = 0f;
        for (DamagingProjectileAPI projectile : engine.getProjectiles()) {
            if (projectile.getOwner() != 0 || !state.seen.add(projectile)) continue;
            if (projectile.getSpawnType() == ProjectileSpawnType.BALLISTIC && Tools.roll(value / 100f)) {
                projectile.setDamageAmount(projectile.getBaseDamageAmount() * DUD_DAMAGE);
            }
        }
        Iterator<DamagingProjectileAPI> it = state.seen.iterator();
        while (it.hasNext()) {
            DamagingProjectileAPI projectile = it.next();
            if (projectile.isExpired() || !engine.isEntityInPlay(projectile)) it.remove();
        }
    }
}
