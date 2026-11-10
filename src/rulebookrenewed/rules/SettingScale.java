package rulebookrenewed.rules;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;

/**
 * Multiplies values from the game's settings.json that the game reads each time it uses them (bounty
 * rewards, battle XP, ship and weapon prices). Settings live for the whole game session, not per save, so
 * the original value is kept the first time a key is touched and put back when the rule goes off,
 * including after another save is loaded (RuleManager.refreshAll unapplies every rule that is off).
 */
final class SettingScale {

    private static final Map<String, Float> ORIGINAL = new HashMap<>();

    private SettingScale() {}

    static void apply(String[] keys, float mult) {
        for (String key : keys) {
            Float original = ORIGINAL.get(key);
            if (original == null) {
                original = Global.getSettings().getFloat(key);
                ORIGINAL.put(key, original);
            }
            Global.getSettings().setFloat(key, original * mult);
        }
    }

    static void restore(String[] keys) {
        for (String key : keys) {
            Float original = ORIGINAL.remove(key);
            if (original != null) Global.getSettings().setFloat(key, original);
        }
    }
}
