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

/**
 * Challenges, the way Unofficial New Game Plus runs them: a set of hard rules at full level, switched on
 * within START_DAYS days of the campaign start and kept on until the player reaches the maximum level.
 * A challenge starts by itself the first time its conditions hold inside that window. Once started,
 * lowering a required rule or going over its cap on positive rules fails it for the rest of the campaign.
 * Some challenges also have a time limit (reach the maximum level by a day of the campaign) or a goal
 * that must hold when the maximum level is reached (five colonies).
 *
 * State is kept per save. Completion is the feat challenge_&lt;id&gt;, which opens the reward rules.
 */
public final class Challenges {

    private static final Logger LOG = Global.getLogger(Challenges.class);
    private static final String CSV = "data/config/rulebook/challenges.csv";
    private static final String RUNS_KEY = "rbr_challengeRuns";
    /** The pre-0.10.0 progress map; dropped on load. */
    private static final String OLD_STATE_KEY = "rbr_challengeState";
    /** Days from the campaign start in which a challenge can start. */
    public static final int START_DAYS = 90;
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

        Feats.Feat feat = null;
        int amount = 1;
        String goal = str(row, "goal");
        if (!goal.isEmpty()) {
            String[] bits = goal.split(":");
            if (!bits[0].trim().equals("feat") || bits.length < 2) throw new IllegalArgumentException("goal must be feat:<id>[:<count>]");
            feat = Feats.get(bits[1].trim());
            if (feat == null) throw new IllegalArgumentException("unknown feat " + bits[1]);
            if (!feat.available()) return null; // the feat's mod is off
            if (bits.length > 2) amount = Integer.parseInt(bits[2].trim());
        }
        String limitText = str(row, "helpfulLimit");
        int limit = limitText.isEmpty() ? -1 : Integer.parseInt(limitText);
        String orderText = str(row, "order");
        float order = orderText.isEmpty() ? 1000f : Float.parseFloat(orderText);
        String deadline = str(row, "deadlineDays");
        return new ChallengeSpec(id, str(row, "name"), requirements, feat, Math.max(1, amount), limit,
                deadline.isEmpty() ? 0 : Integer.parseInt(deadline), order);
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
        for (RuleSpec rule : RuleCatalog.all()) if (spec.featId().equals(rule.featId())) out.add(rule);
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

    /** True when every required rule is at full level and positive rules are within the limit. */
    public static boolean conditionsMet(ChallengeSpec spec, Levels levels) {
        for (ChallengeSpec.Requirement r : spec.requirements) {
            if (levels.level(r.rule) < r.level) return false;
        }
        return spec.helpfulLimit < 0 || positivePoints(levels) <= spec.helpfulLimit;
    }

    public static boolean isComplete(ChallengeSpec spec) {
        return Unlocks.hasFeat(spec.featId());
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

    /** Days since the campaign started, or -1 when the save is older than the start record. */
    public static float campaignDays() {
        long start = RuleState.campaignStart();
        if (start < 0) return -1f;
        return Global.getSector().getClock().getElapsedDaysSince(start);
    }

    /** True while a challenge may still start in this save. */
    public static boolean startWindowOpen() {
        float days = campaignDays();
        return days >= 0f && days <= START_DAYS;
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
        return isRunning(spec) || startWindowOpen();
    }

    /** Short state for a challenge that is not complete, e.g. "Running, 120 days left". */
    public static String statusText(ChallengeSpec spec) {
        if (Global.getSector() == null) return "";
        if (isFailed(spec)) return "Failed in this campaign";
        float days = campaignDays();
        if (isRunning(spec)) {
            if (spec.deadlineDays <= 0) return "Running";
            int left = Math.max(0, spec.deadlineDays - (int) days);
            return "Running, " + left + (left == 1 ? " day left" : " days left");
        }
        if (days < 0f) return "Only in a campaign started with Rule Book 0.10.0 or later";
        if (days > START_DAYS) return "Only in the first " + START_DAYS + " days of a campaign";
        int left = Math.max(0, START_DAYS - (int) days);
        return left + (left == 1 ? " day left to start" : " days left to start");
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
    }

    /** Starts, fails and completes challenges. Called daily, after battles and after every rule change. */
    public static void check() {
        if (Global.getSector() == null || batch > 0) return;
        for (ChallengeSpec spec : CHALLENGES.values()) {
            if (isComplete(spec)) continue;
            try {
                if (advance(spec)) Unlocks.addFeat(spec.featId());
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
            if (!met || (spec.deadlineDays > 0 && campaignDays() > spec.deadlineDays)) {
                runs.put(spec.id, FAILED);
                return false;
            }
        } else {
            if (!met || !startWindowOpen()) return false;
            runs.put(spec.id, RUNNING);
        }
        if (Global.getSector().getPlayerStats().getLevel() < maxLevel()) return false;
        return spec.goalFeat == null || spec.goalFeat.count() >= spec.goalAmount;
    }
}
