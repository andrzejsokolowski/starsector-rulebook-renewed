package rulebookrenewed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;

import rulebookrenewed.rules.RuleEffect;

/**
 * Every rule the game can offer, read once at startup from data/config/rulebook/rules.csv (merged over
 * all mods, so another mod can add rows). Rules whose required mod is missing are left out entirely,
 * and their effect class is never loaded.
 */
public final class RuleCatalog {

    private static final Logger LOG = Global.getLogger(RuleCatalog.class);
    private static final Map<String, RuleSpec> RULES = new LinkedHashMap<>();

    private RuleCatalog() {}

    public static void load() {
        RULES.clear();
        List<RuleSpec> loaded = new ArrayList<>();
        try {
            JSONArray rows = Global.getSettings().getMergedSpreadsheetDataForMod("id", Ids.RULES_CSV, Ids.MOD_ID);
            for (int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.getJSONObject(i);
                String id = str(row, "id");
                if (id.isEmpty() || id.startsWith("#")) continue;
                try {
                    RuleSpec spec = parse(row, id);
                    if (spec != null) loaded.add(spec);
                } catch (Throwable t) {
                    LOG.error("Rule Book: rule " + id + " could not be read and is left out", t);
                }
            }
        } catch (Throwable t) {
            LOG.error("Rule Book: could not read " + Ids.RULES_CSV, t);
        }
        // Sections in the order of their first rule; inside one, mirrored rules sit together: pairs in the
        // order of their first rule, hard before helpful.
        Map<String, Float> sectionOrder = new LinkedHashMap<>();
        Map<String, Float> pairOrder = new LinkedHashMap<>();
        for (RuleSpec r : loaded) {
            sectionOrder.merge(r.section, r.order, Math::min);
            pairOrder.merge(r.pair, r.order, Math::min);
        }
        loaded.sort(Comparator.comparingDouble((RuleSpec r) -> sectionOrder.get(r.section))
                .thenComparing(r -> r.section)
                .thenComparingDouble(r -> pairOrder.get(r.pair))
                .thenComparing(r -> r.pair)
                .thenComparing(r -> !r.hard)
                .thenComparingDouble(r -> r.order)
                .thenComparing(r -> r.name));
        for (RuleSpec spec : loaded) RULES.put(spec.id, spec);
        LOG.info("Rule Book: " + RULES.size() + " rules loaded");
    }

    private static RuleSpec parse(JSONObject row, String id) {
        String requiredMod = str(row, "requiredMod");
        if (!anyModEnabled(requiredMod)) return null;

        String type = str(row, "type").toLowerCase();
        boolean hard;
        if (type.equals("negative") || type.equals("hard")) hard = true;
        else if (type.equals("positive") || type.equals("helpful")) hard = false;
        else throw new IllegalArgumentException("type must be positive or negative");
        String[] pointTexts = str(row, "points").split("[|]");
        String[] valueTexts = str(row, "values").split("[|]");
        int levels = Integer.parseInt(str(row, "levels"));
        if (levels < 1 || levels > RuleSpec.MAX_LEVELS) throw new IllegalArgumentException("levels must be 1 to " + RuleSpec.MAX_LEVELS);
        if (pointTexts.length != levels || valueTexts.length != levels)
            throw new IllegalArgumentException("points and values need one entry per level");
        int[] points = new int[levels];
        for (int i = 0; i < levels; i++) {
            valueTexts[i] = valueTexts[i].trim();
            points[i] = Integer.parseInt(pointTexts[i].trim());
        }

        String effectClass = str(row, "effect");
        Object effect = Global.getSettings().getInstanceOfScript(effectClass);
        if (!(effect instanceof RuleEffect)) throw new IllegalArgumentException(effectClass + " is not a rule effect");

        String icon = str(row, "icon");
        if (icon.isEmpty()) icon = Ids.BOOK_ICON;
        String orderText = str(row, "order");
        float order = orderText.isEmpty() ? 1000f : Float.parseFloat(orderText);

        String pair = str(row, "pair");
        return new RuleSpec(id, str(row, "name"), hard, points, valueTexts, requiredMod, str(row, "unlock"), icon,
                str(row, "description"), order, pair.isEmpty() ? id : pair,
                str(row, "section").isEmpty() ? "Other" : str(row, "section"), (RuleEffect) effect);
    }


    /** True when the column is empty or names (with | between them) at least one enabled mod. */
    public static boolean anyModEnabled(String mods) {
        if (mods == null || mods.trim().isEmpty()) return true;
        for (String mod : mods.split("[|]")) {
            if (!mod.trim().isEmpty() && Global.getSettings().getModManager().isModEnabled(mod.trim())) return true;
        }
        return false;
    }

    /** The bundled org.json returns the string "null" for a JSON null, so check isNull first. */
    private static String str(JSONObject row, String key) {
        if (!row.has(key) || row.isNull(key)) return "";
        String value = row.optString(key, "").trim();
        return value.equals("null") ? "" : value;
    }

    public static RuleSpec get(String id) {
        return RULES.get(id);
    }

    public static List<RuleSpec> all() {
        return Collections.unmodifiableList(new ArrayList<>(RULES.values()));
    }

    public static List<RuleSpec> ofType(boolean hard) {
        List<RuleSpec> out = new ArrayList<>();
        for (RuleSpec r : RULES.values()) if (r.hard == hard) out.add(r);
        return out;
    }
}
