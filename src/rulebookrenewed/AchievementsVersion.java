package rulebookrenewed;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModSpecAPI;

/**
 * The 0.98a launcher never counts a 0.x dependency with a version as met, so mod_info.json names
 * Achievements - Renewed without one and the lowest version is checked here instead. 0.3.0 is the first
 * that holds and checks the achievements that unlock rules (the ids rbr_*); with an older one those rules
 * could never be unlocked. Same check as Astral Ascension Renewed's OddiszCommonsVersion.
 */
final class AchievementsVersion {

    private static final String ID = "achievements_renewed";
    private static final int MIN_MAJOR = 0;
    private static final int MIN_MINOR = 3;
    private static final String TOPIC = "https://fractalsoftworks.com/forum/index.php?topic=36288";

    private AchievementsVersion() {}

    /** Stops the game at start with a clear message when Achievements - Renewed is older than 0.3.0. */
    static void require() {
        ModSpecAPI spec = Global.getSettings().getModManager().getModSpec(ID);
        if (spec == null) return; // The launcher already refuses to start without it.
        if (atLeast(spec.getVersion(), MIN_MAJOR, MIN_MINOR)) return;
        throw new RuntimeException("Rule Book - Renewed needs Achievements - Renewed " + MIN_MAJOR + "." + MIN_MINOR
                + ".0 or newer, but " + spec.getVersion() + " is installed. Update it: " + TOPIC);
    }

    /**
     * Reads the version text itself ("0.3.0"). The game's own split cannot be used: it reads a version
     * that starts with 0 as major "0.3", minor "0".
     */
    static boolean atLeast(String version, int major, int minor) {
        if (version == null) return true;
        String[] parts = version.trim().split("[.]");
        Integer haveMajor = number(parts[0]);
        Integer haveMinor = parts.length > 1 ? number(parts[1]) : Integer.valueOf(0);
        // A version this check cannot read is let through rather than blocking the game.
        if (haveMajor == null || haveMinor == null) return true;
        if (haveMajor.intValue() != major) return haveMajor.intValue() > major;
        return haveMinor.intValue() >= minor;
    }

    private static Integer number(String text) {
        int end = 0;
        while (end < text.length() && Character.isDigit(text.charAt(end))) end++;
        if (end == 0) return null;
        try {
            return Integer.valueOf(text.substring(0, end));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
