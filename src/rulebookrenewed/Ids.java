package rulebookrenewed;

/** Names shared by the whole mod. */
public final class Ids {

    public static final String MOD_ID = "rulebook_renewed";

    /** Read by New Game Plus - Renewed. A float: 1.0 with no rules, above 1 for hard rules, below 1 for helpful ones. */
    public static final String NGP_MULTIPLIER_KEY = "$rbr_ngpMultiplier";

    /**
     * Read by New Game Plus - Renewed. A float: career points earned from finished challenges that New Game
     * Plus - Renewed has not paid out yet. The Rule Book only adds to it; New Game Plus - Renewed pays the
     * amount and sets it back to 0.
     */
    public static final String NGP_BONUS_POINTS_KEY = "$rbr_ngpBonusPoints";

    /** Possible mod ids of New Game Plus - Renewed, used only to decide whether the picker shows the multiplier. */
    public static final String[] NGP_MOD_IDS = {"ngp_renewed"};

    /** Placeholder art until Andrzej's icons arrive. A vanilla path. */
    public static final String BOOK_ICON = "graphics/icons/intel/fleet_log.png";

    public static final String RULES_CSV = "data/config/rulebook/rules.csv";

    private Ids() {}
}
