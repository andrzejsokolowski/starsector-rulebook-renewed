package rulebookrenewed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;

/**
 * Feats: things done in a campaign. A feat done once in any save is kept in saves/common (Unlocks) and
 * opens the rules that name it. Story feats read the game's own mission flags (the _missionCompleted keys
 * the vanilla missions set when they end; the first few follow the list Quality Perks reads in
 * dal.plugins.Perks_Utils.checkMilestones). New Game Plus - Renewed keeps its own copy of the story list,
 * so neither mod needs the other. Battle feats count wins in this save (Counters, filled by
 * RuleBattleListener). Every feat gives a count, so a challenge can ask for it to rise by some amount.
 *
 * Feats from Oddisz's other mods read only state those mods leave in the save (memory keys, persistent
 * data, industries, hull mods, intel class names), never their classes, and exist only while one of the
 * mods named is enabled. Coalesced Little Mods bundles several of them with the same keys.
 */
public final class Feats {

    private interface Count {
        int get();
    }

    private interface Flag {
        boolean done();
    }

    public static final class Feat {
        public final String id;
        /** What the player has to do, as a short instruction. */
        public final String name;
        /** True when the count can keep rising (battles won); false for one-time feats. */
        public final boolean repeatable;
        /** Mod ids of which one must be enabled; empty for vanilla feats. */
        private final String[] mods;
        private final Count count;

        Feat(String id, String name, boolean repeatable, String[] mods, Count count) {
            this.id = id;
            this.name = name;
            this.repeatable = repeatable;
            this.mods = mods;
            this.count = count;
        }

        public boolean available() {
            if (mods.length == 0) return true;
            for (String mod : mods) {
                if (Global.getSettings().getModManager().isModEnabled(mod)) return true;
            }
            return false;
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

    private static final String[] NONE = {};
    private static final String[] SOL = {"sol_renewed"};
    private static final String[] GAMBLING = {"gambling_den", "coalesced_little_mods"};
    private static final String[] CLONING = {"oddisz_cloning", "coalesced_little_mods"};
    private static final String[] ARSENAL = {"re_private_arsenal", "coalesced_little_mods"};
    private static final String[] STARTER = {"starterpack", "coalesced_little_mods"};
    private static final String[] MISSIONS = {"missions_renewed"};
    private static final String[] ASCENSION = {"astral_ascension_renewed"};
    private static final String[] OMEGA_CORE = {"omega_core_everywhere"};
    private static final String[] CHICKEN = {"chickentechshop"};
    private static final String[] SUBMARKETS = {"MM_ESM_Vanilla"};
    private static final String[] NGP = {"ngp_renewed"};

    private static final List<Feat> FEATS = new ArrayList<>();

    static {
        // Vanilla story missions.
        add("academy", "Finish The Kallichore Archive", () -> sector("$gaKA_missionCompleted"));
        add("hamatsu", "Return the ISS Hamatsu", () -> sector("$gaRH_missionCompleted"));
        add("coureuse", "Find Coureuse", () -> sector("$gaFC_missionCompleted"));
        add("project_ziggurat", "Finish Project Ziggurat", () -> sector("$gaPZ_missionCompleted"));
        add("ziggurat", "Defeat the Ziggurat", () -> sector("$defeatedZiggurat"));
        add("atg", "Finish At the Gates", () -> sector("$gaATG_missionCompleted"));
        add("oddity", "Find the hyperspace oddity", () -> sector("$gaDHO_missionCompleted"));
        add("usurpers", "Finish The Usurpers", () -> sector("$sdtu_missionCompleted"));
        add("persea", "Finish Princess of Persea", () -> sector("$soe_completed"));
        add("duel", "Win the duel in Princess of Persea", () -> character("$soe_wonDuel"));
        add("knight", "Finish Knight Errant", () -> sector("$lke_missionCompleted"));
        add("pilgrim", "Walk the Pilgrim's Path", () -> sector("$lpp_missionCompleted"));
        add("false_idols", "Finish False Idols", () -> sector("$bffi_missionCompleted"));
        add("stray_flock", "Bring back The Stray Flock", () -> sector("$locrlf_completed"));
        add("volturn", "Finish Passage To Volturn", () -> sector("$anh_missionCompleted"));
        add("planetkiller", "Recover the planetkiller", () -> sector("$pk_recovered"));
        add("kanta", "Earn Kanta's protection", () -> character("$everHadKantaProtection"));
        // The campaign at large.
        add("level15", "Reach level 15", () -> Global.getSector().getPlayerStats().getLevel() >= 15);
        add("colony", "Found a colony", () -> colonies(1) >= 1);
        add("colony6", "Grow a colony to size 6", () -> colonies(6) >= 1);
        add("colonies5", "Own five colonies", () -> colonies(1) >= 5);
        add("millionaire", "Hold a million credits", () -> Global.getSector().getPlayerFleet() != null
                && Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= 1000000f);
        counter(Counters.WINS, "Win a battle");
        counter(Counters.REMNANT, "Defeat a Remnant fleet");
        counter(Counters.ABYSS, "Defeat Omega, the Threat or the Dwellers");
        counter(Counters.STATION, "Win a battle against an orbital station");
        counter(Counters.GIANT, "Beat a fleet twice the size of your side");

        // Oddisz's other mods.
        modFlag("sol_reach", "Reach Sol", SOL, Feats::inSol);
        modFlag("sol_quest", "Finish the Translocator quest in Sol", SOL,
                () -> "DONE".equals(sectorString("$sr_quest_stage")));
        modFlag("sol_shroud", "Defeat the Shroud in Sol", SOL, () -> sector("$sr_quest_shroud_killed"));
        modFlag("sol_crisis", "Deter the Sol crisis", SOL, () -> sector("$sr_crisis_deterred"));
        modCount("gd_wins", "Win a spin at the Gambling Den", GAMBLING, () -> persistentInt("gd_wins"));
        modCount("gd_pulls", "Pull a Gambling Den slot lever", GAMBLING, () -> persistentInt("gd_pulls"));
        modFlag("cloning", "Build a Cloning Megafactory", CLONING, () -> playerIndustry("oddisz_cloning_megafactory"));
        modFlag("arsenal_weapons", "Reverse-engineer 10 weapons", ARSENAL, () -> sectorSetSize("$RePA_ProducedWeapons") >= 10);
        modFlag("arsenal_ship", "Reverse-engineer a ship", ARSENAL, () -> sectorSetSize("$RePA_ProducedShips") >= 1);
        modFlag("starterpack", "Start a campaign with a Starter Pack", STARTER,
                () -> Global.getSector().getPersistentData().get("$starterpack_applied") != null);
        modFlag("contract", "Sign a Missions - Renewed contract", MISSIONS,
                () -> hasIntelOf("missionsrenewed.missions.ContractIntel"));
        modFlag("aar_ascended", "Ascend a colony past size 10", ASCENSION, () -> playerMarketMemory("$aar_ascended"));
        modFlag("aar_ecumenopolis", "Build an Ecumenopolis", ASCENSION, () -> playerCondition("aar_ecumenopolis"));
        modFlag("oce_tutelage", "Train an officer under an Omega core", OMEGA_CORE, () -> officerMemory("$oce_omegaTutelage"));
        modFlag("chicken", "Meet Chicken", CHICKEN, () -> sector("$CTS_ChickenIntroComplete"));
        modFlag("chicken_market", "Max out Chicken's Tech Market", CHICKEN, () -> sectorNumber("$cts_techMarketLevel") >= 5f);
        modFlag("great_mall", "Visit the Great Mall", SUBMARKETS, () -> {
            StarSystemAPI system = Global.getSector().getStarSystem("Gilgamesh Nebula");
            return system != null && system.isEnteredByPlayer();
        });
        modFlag("ngp_ledger", "Refit a ship at the New Game Plus Ledger", NGP, () -> fleetHasPermaMod("ngp_ledger_refit"));
    }

    private static void add(String id, String name, Flag flag) {
        FEATS.add(new Feat(id, name, false, NONE, () -> flag.done() ? 1 : 0));
    }

    private static void counter(String id, String name) {
        FEATS.add(new Feat(id, name, true, NONE, () -> Counters.get(id)));
    }

    private static void modFlag(String id, String name, String[] mods, Flag flag) {
        FEATS.add(new Feat(id, name, false, mods, () -> flag.done() ? 1 : 0));
    }

    private static void modCount(String id, String name, String[] mods, Count count) {
        FEATS.add(new Feat(id, name, true, mods, count));
    }

    private Feats() {}

    // ---- reading the save ----

    private static boolean sector(String key) {
        MemoryAPI memory = Global.getSector().getMemoryWithoutUpdate();
        return memory != null && memory.contains(key) && !Boolean.FALSE.equals(memory.get(key));
    }

    private static String sectorString(String key) {
        Object value = Global.getSector().getMemoryWithoutUpdate().get(key);
        return value == null ? null : value.toString();
    }

    private static float sectorNumber(String key) {
        Object value = Global.getSector().getMemoryWithoutUpdate().get(key);
        return value instanceof Number ? ((Number) value).floatValue() : 0f;
    }

    private static int sectorSetSize(String key) {
        Object value = Global.getSector().getMemoryWithoutUpdate().get(key);
        return value instanceof Collection ? ((Collection<?>) value).size() : 0;
    }

    private static int persistentInt(String key) {
        Map<String, Object> data = Global.getSector().getPersistentData();
        Object value = data.get(key);
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private static boolean character(String key) {
        MemoryAPI memory = Global.getSector().getCharacterData().getMemoryWithoutUpdate();
        return memory != null && memory.contains(key);
    }

    private static int colonies(int minSize) {
        int count = 0;
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market.isPlayerOwned() && market.getSize() >= minSize) count++;
        }
        return count;
    }

    private static boolean playerIndustry(String industryId) {
        for (MarketAPI market : Misc.getPlayerMarkets(false)) {
            if (market.hasIndustry(industryId)) return true;
        }
        return false;
    }

    private static boolean playerCondition(String conditionId) {
        for (MarketAPI market : Misc.getPlayerMarkets(false)) {
            if (market.hasCondition(conditionId)) return true;
        }
        return false;
    }

    private static boolean playerMarketMemory(String key) {
        for (MarketAPI market : Misc.getPlayerMarkets(false)) {
            if (market.getMemoryWithoutUpdate().contains(key)) return true;
        }
        return false;
    }

    private static boolean officerMemory(String key) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return false;
        for (OfficerDataAPI officer : fleet.getFleetData().getOfficersCopy()) {
            if (officer.getPerson() != null && officer.getPerson().getMemoryWithoutUpdate().contains(key)) return true;
        }
        return false;
    }

    private static boolean fleetHasPermaMod(String hullModId) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return false;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (member.getVariant() != null && member.getVariant().getPermaMods().contains(hullModId)) return true;
        }
        return false;
    }

    private static boolean inSol() {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        StarSystemAPI system = fleet == null ? null : fleet.getStarSystem();
        return system != null && system.getMemoryWithoutUpdate().contains("$sr_sol");
    }

    /** True when an intel of this class, or of a class built on it, is in the intel screen. */
    private static boolean hasIntelOf(String className) {
        for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel()) {
            for (Class<?> c = intel.getClass(); c != null; c = c.getSuperclass()) {
                if (c.getName().equals(className)) return true;
            }
        }
        return false;
    }

    // ---- the list ----

    /** Feats whose mod is enabled (every vanilla feat). */
    public static List<Feat> all() {
        List<Feat> out = new ArrayList<>();
        for (Feat feat : FEATS) if (feat.available()) out.add(feat);
        return Collections.unmodifiableList(out);
    }

    public static Feat get(String id) {
        for (Feat feat : FEATS) if (feat.id.equals(id)) return feat;
        return null;
    }

    /** Records every feat done in this save, and names the rules that just opened up. */
    public static void check() {
        for (Feat feat : all()) {
            if (feat.count() <= 0 || !Unlocks.addFeat(feat.id)) continue;
            Feats.message("Feat done: " + feat.name + ".");
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
