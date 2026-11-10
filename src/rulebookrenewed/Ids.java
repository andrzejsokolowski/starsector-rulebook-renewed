package rulebookrenewed;

/** Names shared by the whole mod. */
public final class Ids {

    public static final String MOD_ID = "rulebook_renewed";

    /** Read by New Game Plus - Renewed. A float: 1.0 with no rules, above 1 for hard rules, below 1 for helpful ones. */
    public static final String NGP_MULTIPLIER_KEY = "$rbr_ngpMultiplier";

    /** Possible mod ids of New Game Plus - Renewed, used only to decide whether the picker shows the multiplier. */
    public static final String[] NGP_MOD_IDS = {"ngp_renewed"};

    /** Placeholder art until Andrzej's icons arrive. A vanilla path. */
    public static final String BOOK_ICON = "graphics/icons/intel/fleet_log.png";

    public static final String RULES_CSV = "data/config/rulebook/rules.csv";

    private Ids() {}
}
