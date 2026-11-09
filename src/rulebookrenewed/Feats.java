package rulebookrenewed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;

/**
 * Feats: things done in a campaign. A feat done once in any save is kept in saves/common (Unlocks) and
 * opens the rules that name it. Story feats read the game's own story flags, the ones Quality Perks reads
 * (dal.plugins.Perks_Utils.checkMilestones); New Game Plus - Renewed keeps its own copy of that list, so
 * neither mod needs the other. Battle feats count wins in this save (Counters, filled by
 * RuleBattleListener). Every feat gives a count, so a challenge can ask for it to rise by some amount.
 */
public final class Feats {

    private interface Count {
        int get();
    }

    public static final class Feat {
        public final String id;
        /** What the player has to do, as a short instruction. */
        public final String name;
        /** True when the count can keep rising (battles won); false for one-time story flags. */
        public final boolean repeatable;
        private final Count count;

        Feat(String id, String name, boolean repeatable, Count count) {
            this.id = id;
            this.name = name;
            this.repeatable = repeatable;
            this.count = count;
        }

        /** How many times it is done in this save; 0 or 1 for one-time feats. */
        public int count() {
            try {
                return Global.getSector() == null ? 0 : count.get();
            } catch (Throwable t) {
                return 0;
            }
        }
    }

    private static final List<Feat> FEATS = new ArrayList<>();

    static {
        add("academy", "Finish the Galatia Academy story", () -> sector("$gaKA_missionCompleted"));
        add("ziggurat", "Defeat the Ziggurat", () -> sector("$defeatedZiggurat"));
        add("atg", "Finish At the Gates", () -> sector("$gaATG_missionCompleted"));
        add("usurpers", "Finish the Usurpers story", () -> sector("$sdtu_missionCompleted"));
        add("duel", "Win the duel of honour", () -> character("$soe_wonDuel"));
        add("knight", "Finish the Knight Errant story", () -> sector("$lke_missionCompleted"));
        add("planetkiller", "Recover the planetkiller", () -> sector("$pk_recovered"));
        add("kanta", "Earn Kanta's protection", () -> character("$everHadKantaProtection"));
        add("level15", "Reach level 15", () -> Global.getSector().getPlayerStats().getLevel() >= 15);
        add("colony", "Found a colony", () -> colonyOfSize(1));
        add("colony6", "Grow a colony to size 6", () -> colonyOfSize(6));
        add("millionaire", "Hold a million credits", () -> Global.getSector().getPlayerFleet() != null
                && Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= 1000000f);
        counter(Counters.WINS, "Win a battle");
        counter(Counters.REMNANT, "Defeat a Remnant fleet");
        counter(Counters.ABYSS, "Defeat Omega, the Threat or the Dwellers");
        counter(Counters.STATION, "Win a battle against an orbital station");
    }

    private interface Flag {
        boolean done();
    }

    private static void add(String id, String name, Flag flag) {
        FEATS.add(new Feat(id, name, false, () -> flag.done() ? 1 : 0));
    }

    private static void counter(String id, String name) {
        FEATS.add(new Feat(id, name, true, () -> Counters.get(id)));
    }

    private Feats() {}

    private static boolean sector(String key) {
        MemoryAPI memory = Global.getSector().getMemoryWithoutUpdate();
        return memory != null && memory.contains(key);
    }

    private static boolean character(String key) {
        MemoryAPI memory = Global.getSector().getCharacterData().getMemoryWithoutUpdate();
        return memory != null && memory.contains(key);
    }

    private static boolean colonyOfSize(int size) {
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market.isPlayerOwned() && market.getSize() >= size) return true;
        }
        return false;
    }

    public static List<Feat> all() {
        return Collections.unmodifiableList(FEATS);
    }

    public static Feat get(String id) {
        for (Feat feat : FEATS) if (feat.id.equals(id)) return feat;
        return null;
    }

    /** Records every feat done in this save, and names the rules that just opened up. */
    public static void check() {
        for (Feat feat : FEATS) {
            if (feat.count() <= 0 || !Unlocks.addFeat(feat.id)) continue;
            for (RuleSpec rule : RuleCatalog.all()) {
                if (feat.id.equals(rule.featId())) message("A new page in the Rule Book: " + rule.name);
            }
        }
    }

    static void message(String text) {
        try {
            Global.getSector().getCampaignUI().addMessage(text, Misc.getHighlightColor());
        } catch (Throwable ignored) {
            // No campaign UI yet; the unlock is saved all the same.
        }
    }
}
