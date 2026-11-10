package rulebookrenewed.rules;

/**
 * Ships, weapons and fighter wings cost more (hard) or less (helpful) to buy. The game reads these
 * multipliers each time it prices one (FleetMember.getBaseBuyValue reads shipBuyPriceMult).
 */
public final class ShipPrices extends SettingRule {

    private static final String[] KEYS = {"shipBuyPriceMult", "shipWeaponBuyPriceMult"};

    @Override
    protected String[] keys() {
        return KEYS;
    }

    @Override
    protected boolean higherHelps() {
        return false;
    }
}
