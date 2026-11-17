package rulebookrenewed.rules;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;

/**
 * Multiplies values from the game's settings.json that the game reads each time it uses them (bounty
 * rewards, battle XP, ship and weapon prices, crew and marine wages). Settings live for the whole game
 * session, not per save, so the original value is kept the first time a key is touched and put back when
 * the rule goes off, including after another save is loaded (RuleManager.refreshAll unapplies every rule
 * that is off).
 *
 * A hard rule and its helpful mirror scale the same keys, and the one that is off is unapplied on every
 * refresh. So each key remembers which rule scaled it, and only that rule can put it back; otherwise the
 * off mirror would undo the active rule a moment after it applied.
 */
final class SettingScale {

    private static final Map<String, Float> ORIGINAL = new HashMap<>();
    private static final Map<String, String> OWNER = new HashMap<>();

    private SettingScale() {}

    static void apply(String[] keys, float mult, String owner) {
        for (String key : keys) {
            Float original = ORIGINAL.get(key);
            if (original == null) {
                original = Global.getSettings().getFloat(key);
                ORIGINAL.put(key, original);
            }
            OWNER.put(key, owner);
            Global.getSettings().setFloat(key, original * mult);
        }
    }

    static void restore(String[] keys, String owner) {
        for (String key : keys) {
            if (!owner.equals(OWNER.get(key))) continue;
            OWNER.remove(key);
            Float original = ORIGINAL.remove(key);
            if (original != null) Global.getSettings().setFloat(key, original);
        }
    }
}
