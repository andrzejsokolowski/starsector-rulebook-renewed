package rulebookrenewed.rules.ungp;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;

import rulebookrenewed.RuleSpec;
import rulebookrenewed.rules.RuleEffect;

/**
 * Helpful: when the ship the player flies destroys its third enemy ship within 8 seconds, and with
 * every further one while the streak lasts, a share of its maximum flux is removed. Fighters, drones,
 * stations and station modules do not count. A battle-wide listener notes each hit by the player's
 * ship that is about to finish a ship; the kill counts on the next frame if the ship is dead. Adapted
 * from Symbol Perseus in Unofficial New Game Plus by Originem.
 */
public final class SymbolPerseus implements RuleEffect {

    private static final String KEY = "rbr_symbol_perseus";
    private static final float WINDOW = 8f;
    private static final int KILLS_BEFORE_BONUS = 2;

    private static final class Watcher implements HullDamageAboutToBeTakenListener {
        ShipAPI dying;
        final List<Float> kills = new ArrayList<>();

        @Override
        public boolean notifyAboutToTakeHullDamage(Object param, ShipAPI ship, Vector2f point, float damageAmount) {
            CombatEngineAPI engine = Global.getCombatEngine();
            ShipAPI player = engine == null ? null : engine.getPlayerShip();
            if (player == null || ship == null || ship.getOwner() == 0 || !ship.isAlive()) return false;
            if (ship.isFighter() || ship.isDrone() || ship.isStation() || ship.isStationModule()) return false;
            Object source = param;
            if (param instanceof DamagingProjectileAPI) source = ((DamagingProjectileAPI) param).getSource();
            else if (param instanceof BeamAPI) source = ((BeamAPI) param).getSource();
            else if (!(param instanceof CombatEntityAPI)) return false;
            if (source == player && damageAmount >= ship.getHitpoints()) dying = ship;
            return false;
        }
    }

    @Override
    public void advanceCombat(RuleSpec rule, CombatEngineAPI engine, float amount, int level, float value) {
        Watcher watcher = Tools.data(engine, KEY);
        if (watcher == null) {
            watcher = new Watcher();
            engine.getCustomData().put(KEY, watcher);
            engine.getListenerManager().addListener(watcher);
        }
        if (watcher.dying == null) return;
        boolean killed = !watcher.dying.isAlive();
        watcher.dying = null;
        if (!killed) return;
        float now = engine.getTotalElapsedTime(false);
        Iterator<Float> it = watcher.kills.iterator();
        while (it.hasNext()) if (now - it.next() > WINDOW) it.remove();
        watcher.kills.add(now);
        ShipAPI player = engine.getPlayerShip();
        if (watcher.kills.size() <= KILLS_BEFORE_BONUS || player == null) return;
        player.getFluxTracker().decreaseFlux(player.getMaxFlux() * value / 100f);
        player.setJitterUnder(KEY, Color.CYAN, 2f, 5, 10f);
        Global.getSoundPlayer().playSound("system_emp_emitter_impact", 1f, 1f, player.getLocation(), new Vector2f());
    }
}
