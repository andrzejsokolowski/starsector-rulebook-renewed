package rulebookrenewed;

import lunalib.lunaSettings.LunaSettings;

/** LunaLib values, read live. A missing value falls back to the default in LunaSettings.csv. */
public final class Settings {

    private Settings() {}

    public static boolean openOnNewGame() {
        return flag("rbr_openOnNewGame", true);
    }

    public static boolean unlockAll() {
        return flag("rbr_unlockAll", false);
    }

    /** Positive rules must be paid for with negative rules. */
    public static boolean enforceBudget() {
        return flag("rbr_enforceBudget", true);
    }

    private static boolean flag(String key, boolean fallback) {
        try {
            Boolean value = LunaSettings.getBoolean(Ids.MOD_ID, key);
            return value == null ? fallback : value;
        } catch (Throwable t) {
            return fallback;
        }
    }
}
