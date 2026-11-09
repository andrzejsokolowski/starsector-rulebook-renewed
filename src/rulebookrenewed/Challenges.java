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
import com.fs.starfarer.api.util.Misc;

/**
 * Challenges: a set of required rules plus a goal. Taking one on raises its rules to the required levels.
 * While it runs, lowering a required rule or going over its helpful-rule cap ends it. Reaching the goal
 * records the challenge in saves/common and opens its reward rules in every campaign.
 *
 * The list is read once at startup from data/config/rulebook/challenges.csv, merged over all mods. A run
 * in progress is kept in the save: when it started, and the goal feat's count at that moment, so only
 * progress made during the challenge counts.
 */
public final class Challenges {

    private static final Logger LOG = Global.getLogger(Challenges.class);
    private static final String CSV = "data/config/rulebook/challenges.csv";
    private static final String RUNS_KEY = "rbr_challengeRuns";
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
        String requiredMod = str(row, "requiredMod");
        if (!requiredMod.isEmpty() && !Global.getSettings().getModManager().isModEnabled(requiredMod)) return null;

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
                str(row, "description"), str(row, "flavor"), order);
    }

    private static String str(JSONObject row, String key) {
        if (!row.has(key) || row.isNull(key)) return "";
        String value = row.optString(key, "").trim();
        return value.equals("null") ? "" : value;
    }

    public static List<ChallengeSpec> all() {
        return Collections.unmodifiableList(new ArrayList<>(CHALLENGES.values()));
    }

    /** The rules this challenge unlocks (unlock column challenge:&lt;id&gt;). */
    public static List<RuleSpec> rewards(ChallengeSpec spec) {
        List<RuleSpec> out = new ArrayList<>();
        for (RuleSpec rule : RuleCatalog.all()) if (spec.featId().equals(rule.featId())) out.add(rule);
        return out;
    }

    // ---- runs in this save ----

    @SuppressWarnings("unchecked")
    private static Map<String, Map<String, Object>> runs() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        Object stored = data.get(RUNS_KEY);
        if (!(stored instanceof Map)) {
            stored = new HashMap<String, Map<String, Object>>();
            data.put(RUNS_KEY, stored);
        }
        return (Map<String, Map<String, Object>>) stored;
    }

    public static boolean isActive(ChallengeSpec spec) {
        return Global.getSector() != null && runs().containsKey(spec.id);
    }

    public static boolean isComplete(ChallengeSpec spec) {
        return Unlocks.hasFeat(spec.featId());
    }

    /** Active challenges that need this rule, with the level each needs. */
    public static List<ChallengeSpec> activeRequiring(RuleSpec rule) {
        List<ChallengeSpec> out = new ArrayList<>();
        if (Global.getSector() == null) return out;
        for (ChallengeSpec spec : CHALLENGES.values()) {
            if (spec.levelFor(rule) > 0 && isActive(spec)) out.add(spec);
        }
        return out;
    }

    public static int helpfulPoints() {
        int sum = 0;
        for (RuleSpec rule : RuleCatalog.all()) {
            if (!rule.hard) sum += Math.max(0, rule.points(RuleState.level(rule)));
        }
        return sum;
    }

    /** Null when the challenge can be taken on now, otherwise why not. */
    public static String whyNot(ChallengeSpec spec) {
        if (isComplete(spec)) return "Completed";
        if (isActive(spec)) return "Under way";
        for (ChallengeSpec.Requirement r : spec.requirements) {
            if (RuleState.level(r.rule) == 0 && !Unlocks.isUnlocked(r.rule)) return r.rule.name + " is still locked";
        }
        if (spec.goalFeat != null && !spec.goalFeat.repeatable && spec.goalFeat.count() > 0) {
            return "Already done in this campaign";
        }
        if (spec.helpfulLimit >= 0) {
            int helpful = helpfulPointsAfterStart(spec);
            if (helpful > spec.helpfulLimit) {
                return "Lower your helpful rules to " + spec.helpfulLimit + " points first (now " + helpful + ")";
            }
        }
        return null;
    }

    /** Helpful points once the challenge's rules are raised; raising a required helpful rule counts too. */
    private static int helpfulPointsAfterStart(ChallengeSpec spec) {
        int sum = 0;
        for (RuleSpec rule : RuleCatalog.all()) {
            if (rule.hard) continue;
            int level = Math.max(RuleState.level(rule), spec.levelFor(rule));
            sum += Math.max(0, rule.points(level));
        }
        return sum;
    }

    public static void start(ChallengeSpec spec) {
        if (whyNot(spec) != null) return;
        for (ChallengeSpec.Requirement r : spec.requirements) {
            if (RuleState.level(r.rule) < r.level) RuleManager.setLevel(r.rule, r.level);
        }
        Map<String, Object> run = new HashMap<>();
        run.put("start", Global.getSector().getClock().getTimestamp());
        run.put("base", spec.goalFeat == null ? 0 : spec.goalFeat.count());
        runs().put(spec.id, run);
        Feats.message("Challenge taken on: " + spec.name + ". " + spec.goalText() + ".");
    }

    public static void giveUp(ChallengeSpec spec) {
        if (runs().remove(spec.id) != null) message("Challenge given up: " + spec.name);
    }

    /** Ends every active challenge whose rules no longer hold. Called after each change of rules. */
    public static void validate() {
        if (Global.getSector() == null) return;
        for (ChallengeSpec spec : CHALLENGES.values()) {
            if (!isActive(spec)) continue;
            String broken = broken(spec);
            if (broken == null) continue;
            runs().remove(spec.id);
            message("Challenge failed: " + spec.name + ". " + broken);
        }
    }

    private static String broken(ChallengeSpec spec) {
        for (ChallengeSpec.Requirement r : spec.requirements) {
            if (RuleState.level(r.rule) < r.level) {
                return r.rule.name + " went below " + RuleSpec.roman(r.level) + ".";
            }
        }
        if (spec.helpfulLimit >= 0 && helpfulPoints() > spec.helpfulLimit) {
            return "Helpful rules went over " + spec.helpfulLimit + " points.";
        }
        return null;
    }

    /** Progress toward the goal in this save, 0 when the challenge is not running. */
    public static int progress(ChallengeSpec spec) {
        if (!isActive(spec)) return 0;
        Map<String, Object> run = runs().get(spec.id);
        if (spec.goalFeat == null) {
            Object start = run.get("start");
            if (!(start instanceof Long)) return 0;
            return (int) Global.getSector().getClock().getElapsedDaysSince((Long) start);
        }
        Object base = run.get("base");
        int baseCount = base instanceof Integer ? (Integer) base : 0;
        return Math.max(0, spec.goalFeat.count() - baseCount);
    }

    public static String progressText(ChallengeSpec spec) {
        int done = Math.min(progress(spec), spec.goalAmount);
        if (spec.goalFeat == null) return done + " of " + spec.goalAmount + " days";
        if (spec.goalAmount == 1) return "Not yet";
        return done + " of " + spec.goalAmount;
    }

    /** Completes every active challenge whose goal is met. Called daily and after battles. */
    public static void check() {
        if (Global.getSector() == null) return;
        validate();
        for (ChallengeSpec spec : CHALLENGES.values()) {
            if (!isActive(spec) || progress(spec) < spec.goalAmount) continue;
            runs().remove(spec.id);
            Unlocks.addFeat(spec.featId());
            Feats.message("Challenge complete: " + spec.name + "!");
            for (RuleSpec reward : rewards(spec)) Feats.message("A new page in the Rule Book: " + reward.name);
        }
    }

    private static void message(String text) {
        try {
            Global.getSector().getCampaignUI().addMessage(text, Misc.getNegativeHighlightColor());
        } catch (Throwable ignored) {
            // No campaign UI yet.
        }
    }
}
