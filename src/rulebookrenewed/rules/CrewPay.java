package rulebookrenewed.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import rulebookrenewed.RuleSpec;

/**
 * Crew and marines want more pay (hard) or settle for less (helpful), through the game's own wages, so the
 * fleet screen, the income report and the money taken all agree.
 *
 * The game pays active-duty crew per ship (the hull's crew salary times the ship's crew salary multiplier),
 * crew beyond what the ships need at the crewSalary setting, and marines at the marineSalary setting
 * (CoreScript.reportEconomyTick in 0.98.5a). So the rule puts a multiplier on every player ship's crew
 * salary and scales those two settings.
 */
public class CrewPay extends MemberStatRule {

    private static final String[] KEYS = {"crewSalary", "marineSalary"};

    /** Left in saves by 1.0.0 and older, which paid the difference once a month. */
    private static final String OLD_MONTH_KEY = "rbr_crewPayMonth";

    @Override
    public void applyCampaign(RuleSpec rule, int level, float value) {
        Global.getSector().getPersistentData().remove(OLD_MONTH_KEY);
        super.applyCampaign(rule, level, value);
        SettingScale.apply(KEYS, wageMult(value, RuleEffect.playerSign(rule)), rule.id);
    }

    @Override
    public void unapplyCampaign(RuleSpec rule) {
        super.unapplyCampaign(rule);
        SettingScale.restore(KEYS, rule.id);
    }

    @Override
    protected void modify(MutableShipStatsAPI stats, String id, float value, float sign, String source) {
        stats.getDynamic().getStat(Stats.SHIP_CREW_SALARY_MULT).modifyMult(id, wageMult(value, sign), source);
    }

    /** sign -1 (hard) raises wages by value percent, +1 (helpful) cuts them; 100 percent off is no pay. */
    private static float wageMult(float value, float sign) {
        return sign < 0f ? 1f + value / 100f : Math.max(0f, 1f - value / 100f);
    }
}
