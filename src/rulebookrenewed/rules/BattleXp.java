package rulebookrenewed.rules;

/** Battles give more (helpful) or less (hard) experience. FleetEncounterContext multiplies by xpGainMult. */
public final class BattleXp extends SettingRule {

    private static final String[] KEYS = {"xpGainMult"};

    @Override
    protected String[] keys() {
        return KEYS;
    }

    @Override
    protected boolean higherHelps() {
        return true;
    }
}
