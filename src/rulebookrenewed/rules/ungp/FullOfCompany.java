package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

/**
 * Helpful: the first time the rule is switched on in a save, the player's standing with the
 * independents and the pirates becomes friendly. Adapted from Full of Company in Unofficial New Game
 * Plus by Originem.
 */
public final class FullOfCompany extends FirstPickStanding {

    @Override
    protected String[] factions() {
        return new String[] {Factions.INDEPENDENT, Factions.PIRATES};
    }

    @Override
    protected RepLevel standing() {
        return RepLevel.FRIENDLY;
    }
}
