package rulebookrenewed.rules.ungp;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Hard: every enemy ship (not fighters, drones or stations) keeps laying mines next to the player's
 * ships: the Doom's heavy mine, in a clear spot 400 to 600 units from a ship picked by size. A frigate
 * lays one every 30 seconds, a destroyer every 20, a cruiser every 14, a capital ship every 10, at
 * level I; the rule's value is the frigate's interval and the others shrink with it. No more than 10 mines come in a burst. Adapted from Danger Zone in Unofficial
 * New Game Plus by Originem.
 */
public final class DangerZone implements RuleEffect {

    private static final String KEY = "rbr_danger_zone";
    private static final String MINE_WEAPON = "minelayer2";
    private static final float BURST_LIMIT = 10f;
    private static final float FRIGATE_SECONDS = 30f;

    @Override
    public void advanceCombat(RuleSpec rule, CombatEngineAPI engine, float amount, int level, float value) {
        float[] burst = Tools.data(engine, KEY);
        if (burst == null) {
            burst = new float[1];
            engine.getCustomData().put(KEY, burst);
        }
        float rate = FRIGATE_SECONDS / Math.max(1f, value);
        for (ShipAPI ship : engine.getShips()) {
            if (ship.getOwner() != 1 || !ship.isAlive() || !canFight(ship)) continue;
            burst[0] = Math.max(0f, burst[0] - amount * rate);
            float[] timer = (float[]) ship.getCustomData().get(KEY);
            if (timer == null) {
                timer = new float[1];
                ship.setCustomData(KEY, timer);
            }
            if (burst[0] >= BURST_LIMIT) continue;
            timer[0] += amount;
            if (timer[0] < interval(ship) / rate) continue;
            timer[0] = 0f;
            ShipAPI victim = pickVictim(engine);
            Vector2f spot = victim == null ? null : clearSpot(engine, victim);
            if (spot == null) continue;
            layMine(engine, ship, spot);
            burst[0] += 1f;
        }
    }

    private static boolean canFight(ShipAPI ship) {
        return !ship.isFighter() && !ship.isDrone() && !ship.isStation() && !ship.isStationModule();
    }

    private static float interval(ShipAPI ship) {
        if (ship.getHullSize() == null) return 30f;
        switch (ship.getHullSize()) {
            case CAPITAL_SHIP: return 10f;
            case CRUISER: return 14f;
            case DESTROYER: return 20f;
            default: return 30f;
        }
    }

    private static ShipAPI pickVictim(CombatEngineAPI engine) {
        WeightedRandomPicker<ShipAPI> picker = new WeightedRandomPicker<>(Tools.RANDOM);
        for (ShipAPI ship : engine.getShips()) {
            if (ship.getOwner() != 0 || !ship.isAlive() || !canFight(ship) || ship.getHullSize() == null) continue;
            if (ship.getTravelDrive() != null && ship.getTravelDrive().isActive()) continue;
            picker.add(ship, ship.getHullSize().ordinal());
        }
        return picker.pick();
    }

    private static Vector2f clearSpot(CombatEngineAPI engine, ShipAPI victim) {
        List<Vector2f> clear = new ArrayList<>();
        float keepOff = 400f + victim.getCollisionRadius();
        for (int i = 0; i < 36; i++) {
            Vector2f spot = Tools.randomPointOnCircle(victim.getLocation(),
                    victim.getCollisionRadius() + 400f + 200f * Tools.RANDOM.nextFloat());
            if (isClear(engine, spot, keepOff)) clear.add(spot);
        }
        return clear.isEmpty() ? null : clear.get(Tools.RANDOM.nextInt(clear.size()));
    }

    private static boolean isClear(CombatEngineAPI engine, Vector2f spot, float range) {
        for (ShipAPI other : engine.getShips()) {
            if (other.isFighter() || other.isDrone()) continue;
            if (Misc.getDistance(spot, other.getLocation()) < range) return false;
        }
        for (CombatEntityAPI asteroid : engine.getAsteroids()) {
            if (Misc.getDistance(spot, asteroid.getLocation()) < asteroid.getCollisionRadius() + 100f) return false;
        }
        return true;
    }

    private static void layMine(CombatEngineAPI engine, ShipAPI source, Vector2f spot) {
        Vector2f at = Misc.getPointAtRadius(spot, 40f + Tools.RANDOM.nextFloat() * 80f);
        CombatEntityAPI spawned = engine.spawnProjectile(source, null, MINE_WEAPON, at, Tools.RANDOM.nextFloat() * 360f, null);
        if (!(spawned instanceof MissileAPI)) return;
        MissileAPI mine = (MissileAPI) spawned;
        engine.applyDamageModifiersToSpawnedProjectileWithNullWeapon(source, WeaponAPI.WeaponType.MISSILE, false, mine.getDamage());
        mine.setFlightTime(Tools.RANDOM.nextFloat());
        mine.fadeOutThenIn(1f);
        Global.getSoundPlayer().playSound("mine_spawn", 1f, 1f, mine.getLocation(), mine.getVelocity());
    }
}
