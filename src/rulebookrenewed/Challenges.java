package rulebookrenewed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;

import achievementsrenewed.api.Achievements;

/**
 * Challenges, the way Unofficial New Game Plus runs them: a set of hard rules at full level, switched on
 * while the player is still at level START_LEVEL and kept on until the player reaches the maximum level.
 * A challenge starts by itself the first time its conditions hold at that level. Once started, lowering a
 * required rule or going over its cap on positive rules fails it for the rest of the campaign. There is no
 * time limit of any kind (Andrzej, 2026-11-12: "Remove all timing from challenges"). Some challenges have a
 * goal that must hold when the maximum level is reached (five colonies).
 *
 * State is kept per save. Completion is the achievement rbr_challenge_&lt;id&gt;, which opens the reward rules.
 */
public final class Challenges {

    private static final Logger LOG = Global.getLogger(Challenges.class);
    private static final String CSV = "data/config/rulebook/challenges.csv";
    private static final String RUNS_KEY = "rbr_challengeRuns";
    /** The pre-0.10.0 progress map; dropped on load. */
    private static final String OLD_STATE_KEY = "rbr_challengeState";
    /** The player level at which a challenge can start: the rules are on from the start of the campaign. */
    public static final int START_LEVEL = 1;
    /** The campaign start day of Rule Book 0.10 and 0.11 (for the old 90-day window); dropped on load. */
    private static final String OLD_START_KEY = "rbr_campaignStart";
    private static final String RUNNING = "running", FAILED = "failed";
    private static final Map<String, ChallengeSpec> CHALLENGES = new LinkedHashMap<>();
    private static int batch;

    private Challenges() {}

    public static void load() {
        CHALLENGES.clear();
        List<ChallengeSpec> loaded = new ArrayList<>();
        try {
            JSONArray rows = Global.getSettings().getMergedSpreadsheetDataForMod("id", CSV, Ids.MOD_ID);
            for (int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.getJSONObject(i);
                String id = str(row, "id");
                if (id.isEmpty() || id.startsWith("#")) continue;
                try {
                    ChallengeSpec spec = parse(row, id);
                    if (spec != null) loaded.add(spec);
                } catch (Throwable t) {
                    LOG.error("Rule Book: challenge " + id + " could not be read and is left out", t);
                }
            }
        } catch (Throwable t) {
            LOG.error("Rule Book: could not read " + CSV, t);
        }
        loaded.sort((a, b) -> Float.compare(a.order, b.order));
        for (ChallengeSpec spec : loaded) CHALLENGES.put(spec.id, spec);
        LOG.info("Rule Book: " + CHALLENGES.size() + " challenges loaded");
    }

    private static ChallengeSpec parse(JSONObject row, String id) {
        if (!RuleCatalog.anyModEnabled(str(row, "requiredMod"))) return null;

        List<ChallengeSpec.Requirement> requirements = new ArrayList<>();
        for (String part : str(row, "rules").split("[|]")) {
            part = part.trim();
            if (part.isEmpty()) continue;
            RuleSpec rule = RuleCatalog.get(part);
            if (rule == null) return null; // its rule is not loaded (a missing mod); leave the challenge out
            requirements.add(new ChallengeSpec.Requirement(rule, rule.levels));
        }

        String goalId = null;
        int amount = 1;
        String goal = str(row, "goal");
        if (!goal.isEmpty()) {
            String[] bits = goal.split(":");
            if (!bits[0].trim().equals("achievement") || bits.length < 2) {
                throw new IllegalArgumentException("goal must be achievement:<id>[:<count>]");
            }
            goalId = bits[1].trim();
            if (bits.length > 2) amount = Integer.parseInt(bits[2].trim());
        }
        String limitText = str(row, "helpfulLimit");
        int limit = limitText.isEmpty() ? -1 : Integer.parseInt(limitText);
        String orderText = str(row, "order");
        float order = orderText.isEmpty() ? 1000f : Float.parseFloat(orderText);
        return new ChallengeSpec(id, str(row, "name"), requirements, goalId, Math.max(1, amount), limit, order);
    }

    private static String str(JSONObject row, String key) {
        if (!row.has(key) || row.isNull(key)) return "";
        String value = row.optString(key, "").trim();
        return value.equals("null") ? "" : value;
    }

    public static List<ChallengeSpec> all() {
        return Collections.unmodifiableList(new ArrayList<>(CHALLENGES.values()));
    }

    public static List<RuleSpec> rewards(ChallengeSpec spec) {
        List<RuleSpec> out = new ArrayList<>();
        for (RuleSpec rule : RuleCatalog.all()) if (spec.id.equals(rule.challengeId())) out.add(rule);
        return out;
    }

    /** Where rule levels are read from: the save, or the picker's working copy. */
    public interface Levels {
        int level(RuleSpec rule);
    }

    public static final Levels LIVE = RuleState::level;

    public static int maxLevel() {
        try {
            return Global.getSettings().getLevelupPlugin().getMaxLevel();
        } catch (Throwable t) {
            return 15;
        }
    }

    public static int positivePoints(Levels levels) {
        int sum = 0;
        for (RuleSpec rule : RuleCatalog.all()) {
            if (!rule.hard) sum += Math.max(0, rule.points(levels.level(rule)));
        }
        return sum;
    }

    /**
     * True when every required rule is at full level, positive rules are within the limit, and no rule that
     * blocks challenges (Delayed Potential) is on.
     */
    public static boolean conditionsMet(ChallengeSpec spec, Levels levels) {
        for (ChallengeSpec.Requirement r : spec.requirements) {
            if (levels.level(r.rule) < r.level) return false;
        }
        for (RuleSpec rule : RuleCatalog.all()) {
            if (rule.effect.blocksChallenges() && levels.level(rule) > 0) return false;
        }
        return spec.helpfulLimit < 0 || positivePoints(levels) <= spec.helpfulLimit;
    }

    public static boolean isComplete(ChallengeSpec spec) {
        return Achievements.isDone(spec.achievementId());
    }

    // ---- per-save state ----

    @SuppressWarnings("unchecked")
    private static Map<String, String> runs() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        Object stored = data.get(RUNS_KEY);
        if (!(stored instanceof Map)) {
            stored = new HashMap<String, String>();
            data.put(RUNS_KEY, stored);
        }
        return (Map<String, String>) stored;
    }

    /** True while a challenge may still start in this save: the player is still at the starting level. */
    public static boolean canStart() {
        return Global.getSector().getPlayerStats().getLevel() <= START_LEVEL;
    }

    public static boolean isRunning(ChallengeSpec spec) {
        return Global.getSector() != null && RUNNING.equals(runs().get(spec.id));
    }

    public static boolean isFailed(ChallengeSpec spec) {
        return Global.getSector() != null && FAILED.equals(runs().get(spec.id));
    }

    /** True when the challenge can still be won in this save. */
    public static boolean possible(ChallengeSpec spec) {
        if (Global.getSector() == null || isComplete(spec) || isFailed(spec)) return false;
        return isRunning(spec) || canStart();
    }

    /** Short state for a challenge that is not complete: "Running", "Failed in this campaign", and so on. */
    public static String statusText(ChallengeSpec spec) {
        if (Global.getSector() == null) return "";
        if (isFailed(spec)) return "Failed in this campaign";
        if (isRunning(spec)) return "Running";
        if (!canStart()) return "Only at level " + START_LEVEL;
        return "Not started";
    }

    /** Suspends the checks while the picker applies several changes; the last end runs one check. */
    public static void beginBatch() {
        batch++;
    }

    public static void endBatch() {
        batch = Math.max(0, batch - 1);
        if (batch == 0) check();
    }

    public static void dropOldState() {
        Global.getSector().getPersistentData().remove(OLD_STATE_KEY);
        Global.getSector().getPersistentData().remove(OLD_START_KEY);
        // Battle counts of Rule Book 0.10.3 and older; Achievements - Renewed counts battles now.
        Global.getSector().getPersistentData().remove("rbr_counters");
    }

    /** Starts, fails and completes challenges. Called daily, after battles and after every rule change. */
    public static void check() {
        if (Global.getSector() == null || batch > 0) return;
        for (ChallengeSpec spec : CHALLENGES.values()) {
            if (isComplete(spec)) continue;
            try {
                if (advance(spec)) Achievements.complete(spec.achievementId());
            } catch (Throwable t) {
                LOG.error("Rule Book: challenge " + spec.id + " could not be checked", t);
            }
        }
    }

    /** One evaluation step; true when the challenge is now complete. */
    private static boolean advance(ChallengeSpec spec) {
        Map<String, String> runs = runs();
        String state = runs.get(spec.id);
        if (FAILED.equals(state)) return false;
        boolean met = conditionsMet(spec, LIVE);
        if (RUNNING.equals(state)) {
            if (!met) {
                runs.put(spec.id, FAILED);
                return false;
            }
        } else {
            if (!met || !canStart()) return false;
            runs.put(spec.id, RUNNING);
        }
        if (Global.getSector().getPlayerStats().getLevel() < maxLevel()) return false;
        // The goal is counted by Achievements - Renewed in this save (rbr_colonies5: five colonies now).
        return spec.goalId == null || Achievements.progress(spec.goalId) >= spec.goalAmount;
    }
}
