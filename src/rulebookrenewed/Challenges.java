package rulebookrenewed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;

/**
 * Challenges: hard rules to keep on, plus a goal. Nothing is "taken on": a challenge makes progress
 * whenever its conditions hold (every required rule at its level or higher, and positive rules within
 * the challenge's limit), whatever else the player does.
 *
 * Counted goals (battles won, fleets beaten) add up only what happens while the conditions hold; dropping
 * the rules pauses the count. A goal with a deadline must be reached within that many days of its first
 * counted win, or the count starts over. "Hold out" goals count unbroken days and start over when the
 * conditions break. A one-time deed counts when it happens while the conditions hold; a state goal that
 * was already true when the rules went on (five colonies, a million credits) counts after HOLD_DAYS days.
 *
 * State is kept per save. Completion is the feat challenge_<id>, which opens the reward rules.
 */
public final class Challenges {

    private static final Logger LOG = Global.getLogger(Challenges.class);
    private static final String CSV = "data/config/rulebook/challenges.csv";
    private static final String STATE_KEY = "rbr_challengeState";
    /** Days the rules must stay on before a goal that was already true when they went on counts. */
    public static final int HOLD_DAYS = 90;
    /** Goal feats that describe a state that can be true before the rules go on (not a one-time deed). */
    private static final Set<String> STATE_FEATS = new HashSet<>(Arrays.asList("colony", "colony6", "colonies5", "millionaire"));
    private static final Map<String, ChallengeSpec> CHALLENGES = new LinkedHashMap<>();

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
            String[] bits = part.split(":");
            RuleSpec rule = RuleCatalog.get(bits[0].trim());
            if (rule == null) return null; // its rule is not loaded (a missing mod); leave the challenge out
            int level = bits.length > 1 ? Integer.parseInt(bits[1].trim()) : 1;
            requirements.add(new ChallengeSpec.Requirement(rule, Math.max(1, Math.min(level, rule.levels))));
        }

        String[] goal = str(row, "goal").split(":");
        Feats.Feat feat = null;
        int amount;
        switch (goal[0].trim()) {
            case "days":
                amount = Integer.parseInt(goal[1].trim());
                break;
            case "wins":
                feat = Feats.get(Counters.WINS);
                amount = Integer.parseInt(goal[1].trim());
                break;
            case "feat":
                feat = Feats.get(goal[1].trim());
                if (feat == null) throw new IllegalArgumentException("unknown feat " + goal[1]);
                if (!feat.available()) return null; // the feat's mod is off
                amount = goal.length > 2 ? Integer.parseInt(goal[2].trim()) : 1;
                break;
            default:
                throw new IllegalArgumentException("goal must be feat:<id>[:<count>], wins:<count> or days:<count>");
        }
        String limitText = str(row, "helpfulLimit");
        int limit = limitText.isEmpty() ? -1 : Integer.parseInt(limitText);
        String orderText = str(row, "order");
        float order = orderText.isEmpty() ? 1000f : Float.parseFloat(orderText);
        return new ChallengeSpec(id, str(row, "name"), requirements, feat, Math.max(1, amount), limit,
                intOr(str(row, "deadlineDays"), 0), order);
    }

    private static int intOr(String text, int fallback) {
        return text.isEmpty() ? fallback : Integer.parseInt(text);
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

    public static int positivePoints(Levels levels) {
        int sum = 0;
        for (RuleSpec rule : RuleCatalog.all()) {
            if (!rule.hard) sum += Math.max(0, rule.points(levels.level(rule)));
        }
        return sum;
    }

    /** True when every required rule is at its level or higher and positive rules are within the limit. */
    public static boolean conditionsMet(ChallengeSpec spec, Levels levels) {
        for (ChallengeSpec.Requirement r : spec.requirements) {
            if (levels.level(r.rule) < r.level) return false;
        }
        return spec.helpfulLimit < 0 || positivePoints(levels) <= spec.helpfulLimit;
    }

    public static boolean isComplete(ChallengeSpec spec) {
        return Unlocks.hasFeat(spec.featId());
    }

    /** True when the goal can no longer happen in this save: a one-time deed done before the rules were on. */
    public static boolean spent(ChallengeSpec spec) {
        if (Global.getSector() == null || spec.goalFeat == null || spec.goalFeat.repeatable) return false;
        if (STATE_FEATS.contains(spec.goalFeat.id)) return false;
        Map<String, Object> s = state(spec);
        boolean met = Boolean.TRUE.equals(s.get("met"));
        int base = met ? intOf(s.get("base"), 0) : spec.goalFeat.count();
        return base >= spec.goalAmount;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> state(ChallengeSpec spec) {
        Map<String, Object> data = Global.getSector().getPersistentData();
        Object stored = data.get(STATE_KEY);
        if (!(stored instanceof Map)) {
            stored = new HashMap<String, Map<String, Object>>();
            data.put(STATE_KEY, stored);
        }
        Map<String, Map<String, Object>> all = (Map<String, Map<String, Object>>) stored;
        Map<String, Object> s = all.get(spec.id);
        if (s == null) {
            s = new HashMap<>();
            all.put(spec.id, s);
        }
        return s;
    }

    private static int intOf(Object value, int fallback) {
        return value instanceof Integer ? (Integer) value : fallback;
    }

    private static float daysSince(Object timestamp) {
        if (!(timestamp instanceof Long)) return 0f;
        return Global.getSector().getClock().getElapsedDaysSince((Long) timestamp);
    }

    public static int progress(ChallengeSpec spec) {
        if (Global.getSector() == null) return 0;
        Map<String, Object> s = state(spec);
        if (spec.goalFeat == null) return Boolean.TRUE.equals(s.get("met")) ? (int) daysSince(s.get("metSince")) : 0;
        return intOf(s.get("progress"), 0);
    }

    /** Short progress for a challenge that is not complete, e.g. "3 / 10, 42 days left". Empty when there is none. */
    public static String progressText(ChallengeSpec spec) {
        if (Global.getSector() == null) return "";
        Map<String, Object> s = state(spec);
        int done = Math.min(progress(spec), spec.goalAmount);
        if (spec.goalFeat == null) return done + " / " + spec.goalAmount + " days";
        if (!spec.goalFeat.repeatable) {
            if (spent(spec)) return "Already done in this campaign";
            boolean held = Boolean.TRUE.equals(s.get("met")) && spec.goalFeat.count() >= spec.goalAmount;
            return held ? Math.min(HOLD_DAYS, (int) daysSince(s.get("metSince"))) + " / " + HOLD_DAYS + " days" : "";
        }
        String text = done + " / " + spec.goalAmount;
        if (spec.deadlineDays > 0 && done > 0) {
            int left = Math.max(0, spec.deadlineDays - (int) daysSince(s.get("windowStart")));
            text += ", " + left + (left == 1 ? " day left" : " days left");
        }
        return text;
    }

    /** Counts progress and completes challenges. Called daily, after battles and after every rule change. */
    public static void check() {
        if (Global.getSector() == null) return;
        long now = Global.getSector().getClock().getTimestamp();
        for (ChallengeSpec spec : CHALLENGES.values()) {
            if (isComplete(spec)) continue;
            try {
                if (advance(spec, now)) Unlocks.addFeat(spec.featId());
            } catch (Throwable t) {
                LOG.error("Rule Book: challenge " + spec.id + " could not be checked", t);
            }
        }
    }

    /** One evaluation step; true when the challenge is now complete. */
    private static boolean advance(ChallengeSpec spec, long now) {
        Map<String, Object> s = state(spec);
        boolean before = Boolean.TRUE.equals(s.get("met"));
        boolean met = conditionsMet(spec, LIVE);
        s.put("met", met);
        int count = spec.goalFeat == null ? 0 : spec.goalFeat.count();
        if (met && !before) {
            s.put("metSince", now);
            s.put("base", count);
        }

        if (spec.goalFeat == null) {
            return met && daysSince(s.get("metSince")) >= spec.goalAmount;
        }
        if (spec.goalFeat.repeatable) {
            // Whatever was won since the last look happened under the rules as they stood then.
            int gained = Math.max(0, count - intOf(s.get("last"), count));
            s.put("last", count);
            int progress = intOf(s.get("progress"), 0);
            if (before && gained > 0) {
                if (progress == 0) s.put("windowStart", now);
                progress += gained;
            }
            if (spec.deadlineDays > 0 && progress > 0 && progress < spec.goalAmount
                    && daysSince(s.get("windowStart")) > spec.deadlineDays) {
                progress = 0;
            }
            s.put("progress", progress);
            return progress >= spec.goalAmount;
        }
        if (!met || count < spec.goalAmount) return false;
        if (intOf(s.get("base"), count) < spec.goalAmount) return true;
        return STATE_FEATS.contains(spec.goalFeat.id) && daysSince(s.get("metSince")) >= HOLD_DAYS;
    }
}
