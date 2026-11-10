package rulebookrenewed.rules;

/**
 * Bounties pay more (helpful) or less (hard): person bounties, system and commerce bounties, and pirate and
 * Pather base bounties. The game reads these values when a bounty is posted, so bounties already on
 * the board keep their old reward.
 */
public final class BountyPay extends SettingRule {

    private static final String[] KEYS = {
            "basePersonBounty", "personBountyPerLevel", "baseSystemBounty", "baseCommerceSystemBounty",
            "pirateBaseBounty1", "pirateBaseBounty2", "pirateBaseBounty3", "pirateBaseBounty4", "pirateBaseBounty5",
            "luddicPathBaseBountySmall", "luddicPathBaseBountyLarge",
    };

    @Override
    protected String[] keys() {
        return KEYS;
    }

    @Override
    protected boolean higherHelps() {
        return true;
    }
}
