package rulebookrenewed.rules.ungp;

import com.fs.starfarer.api.campaign.BuffManagerAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

/** A buff that changes nothing and only marks a ship for a number of days (the cooldown of Endurance). */
public final class CooldownBuff implements BuffManagerAPI.Buff {

    private final String buffId;
    private float daysLeft;

    CooldownBuff(String buffId, float days) {
        this.buffId = buffId;
        this.daysLeft = days;
    }

    @Override
    public void apply(FleetMemberAPI member) {}

    @Override
    public String getId() {
        return buffId;
    }

    @Override
    public boolean isExpired() {
        return daysLeft <= 0f;
    }

    @Override
    public void advance(float days) {
        daysLeft -= days;
    }
}
