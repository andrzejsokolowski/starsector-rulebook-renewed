package rulebookrenewed;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;

/**
 * Story feats, checked from the game's own story flags. The flags are the ones Quality Perks reads
 * (dal.plugins.Perks_Utils.checkMilestones); New Game Plus - Renewed keeps its own copy of this list,
 * so neither mod needs the other.
 */
public final class Feats {

    private interface Check {
        boolean done();
    }

    private static final class Feat {
        final String id;
        final Check check;

        Feat(String id, Check check) {
            this.id = id;
            this.check = check;
        }
    }

    private static final List<Feat> FEATS = new ArrayList<>();

    static {
        FEATS.add(new Feat("academy", () -> sector("$gaKA_missionCompleted")));
        FEATS.add(new Feat("ziggurat", () -> sector("$defeatedZiggurat")));
        FEATS.add(new Feat("atg", () -> sector("$gaATG_missionCompleted")));
        FEATS.add(new Feat("usurpers", () -> sector("$sdtu_missionCompleted")));
        FEATS.add(new Feat("duel", () -> character("$soe_wonDuel")));
        FEATS.add(new Feat("knight", () -> sector("$lke_missionCompleted")));
        FEATS.add(new Feat("planetkiller", () -> sector("$pk_recovered")));
        FEATS.add(new Feat("kanta", () -> character("$everHadKantaProtection")));
        FEATS.add(new Feat("level15", () -> Global.getSector().getPlayerStats().getLevel() >= 15));
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

    /** Records every feat done in this save, and names the rules that just opened up. */
    public static void check() {
        for (Feat feat : FEATS) {
            boolean done;
            try {
                done = feat.check.done();
            } catch (Throwable t) {
                continue;
            }
            if (!done || !Unlocks.addFeat(feat.id)) continue;
            for (RuleSpec rule : RuleCatalog.all()) {
                if (!feat.id.equals(rule.featId())) continue;
                try {
                    Global.getSector().getCampaignUI().addMessage(
                            "A new page in the Rule Book: " + rule.name, Misc.getHighlightColor());
                } catch (Throwable ignored) {
                    // No campaign UI yet; the unlock is saved all the same.
                }
            }
        }
    }
}
