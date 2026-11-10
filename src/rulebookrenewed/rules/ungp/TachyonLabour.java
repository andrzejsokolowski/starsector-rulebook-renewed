package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

/**
 * Helpful: the first time the rule is switched on in a save, the player's standing with the Remnants
 * becomes cooperative. Adapted from Tachyon Labour in Unofficial New Game Plus by Originem.
 */
public final class TachyonLabour extends FirstPickStanding {

    @Override
    protected String[] factions() {
        return new String[] {Factions.REMNANTS};
    }

    @Override
    protected RepLevel standing() {
        return RepLevel.COOPERATIVE;
    }
}
