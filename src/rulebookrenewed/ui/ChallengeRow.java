package rulebookrenewed.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.ChallengeSpec;
import rulebookrenewed.Challenges;
import rulebookrenewed.RuleSpec;

/**
 * One line of the challenge list: name, goal and reward, the icons of the rules it needs with their
 * levels underneath, and the button that switches exactly those rules on.
 */
public final class ChallengeRow extends BaseCustomUIPanelPlugin {

    public static final float HEIGHT = 128f;
    private static final float ICON = 44f, ICON_X = 14f, ICON_TOP = 72f, ICON_STEP = 86f;

    public interface Listener {
        void enableRequired(ChallengeSpec spec);
    }

    public final ChallengeSpec spec;
    private final Listener listener;
    private final List<SpriteAPI> sprites = new ArrayList<>();
    private PositionAPI p;
    private LabelAPI title, status;
    private ButtonAPI enable;

    public ChallengeRow(ChallengeSpec spec, Listener listener) {
        this.spec = spec;
        this.listener = listener;
        for (ChallengeSpec.Requirement r : spec.requirements) sprites.add(Global.getSettings().getSprite(r.rule.icon));
    }

    public void init(CustomPanelAPI panel, float width) {
        TooltipMakerAPI el = panel.createUIElement(width, HEIGHT, false);
        UIComponentAPI border = el.createRect(Misc.getDarkPlayerColor(), 1f);
        el.addCustomDoNotSetPosition(border).getPosition().inTL(0f, 0f).setSize(width, HEIGHT - 6f);

        el.setParaOrbitronLarge();
        title = el.addPara(spec.name, RulePickerPanel.GOLD, 8f);
        title.getPosition().setXAlignOffset(ICON_X);
        el.setParaFontDefault();
        String extra = spec.extraText();
        el.addPara(extra.isEmpty() ? "Positive rules allowed." : extra, Misc.getTextColor(), 6f);
        StringBuilder reward = new StringBuilder();
        for (RuleSpec rule : Challenges.rewards(spec)) reward.append(reward.length() > 0 ? ", " : "").append(rule.name);
        if (reward.length() > 0) el.addPara("Reward: " + reward, Misc.getGrayColor(), 3f);

        status = el.createLabel("", Misc.getGrayColor());
        status.setAlignment(Alignment.RMID);
        el.addCustomDoNotSetPosition((UIComponentAPI) status).getPosition().inTL(width - 420f, 8f).setSize(400f, 20f);

        el.setParaOrbitronLarge();
        for (int i = 0; i < spec.requirements.size(); i++) {
            ChallengeSpec.Requirement r = spec.requirements.get(i);
            LabelAPI level = el.createLabel(r.rule.levels > 1 ? RuleSpec.roman(r.level) : "", RuleTile.color(r.rule));
            el.addCustomDoNotSetPosition((UIComponentAPI) level).getPosition()
                    .inTL(ICON_X + i * ICON_STEP + ICON + 6f, ICON_TOP + 12f).setSize(34f, 20f);
        }
        el.setParaFontDefault();

        enable = el.addButton("Enable required rules", "enable", 190f, 26f, 0f);
        enable.getPosition().inTL(width - 210f, HEIGHT - 6f - 36f);
        panel.addUIElement(el).inTL(0f, 0f);
    }

    /** Shows progress against the picker's working rule levels. unlocked: every required rule can be picked. */
    public void sync(Challenges.Levels working, boolean unlocked) {
        boolean complete = Challenges.isComplete(spec);
        boolean possible = Challenges.possible(spec);
        Color color = Misc.getGrayColor();
        String text;
        if (complete) {
            text = "Done";
            color = RulePickerPanel.POSITIVE;
        } else if (!possible) {
            text = Challenges.statusText(spec);
            color = Misc.getNegativeHighlightColor();
        } else if (Challenges.conditionsMet(spec, working)) {
            text = Challenges.isRunning(spec) ? Challenges.statusText(spec) : "Rules met   " + Challenges.statusText(spec);
            color = RulePickerPanel.GOLD;
        } else {
            text = Challenges.isRunning(spec) ? "Fails on Confirm" : Challenges.statusText(spec);
            if (Challenges.isRunning(spec)) color = Misc.getNegativeHighlightColor();
        }
        status.setText(text);
        status.setColor(color);
        title.setColor(complete ? RulePickerPanel.POSITIVE : RulePickerPanel.GOLD);
        enable.setEnabled(!complete && possible && unlocked);
    }

    /** The required rule whose icon is under the point (screen coordinates, y up), or null. */
    public ChallengeSpec.Requirement requirementAt(float x, float y) {
        if (p == null) return null;
        float top = p.getY() + p.getHeight() - ICON_TOP;
        if (y > top || y < top - ICON) return null;
        for (int i = 0; i < spec.requirements.size(); i++) {
            float left = p.getX() + ICON_X + i * ICON_STEP;
            if (x >= left && x <= left + ICON) return spec.requirements.get(i);
        }
        return null;
    }

    @Override
    public void positionChanged(PositionAPI position) {
        p = position;
    }

    @Override
    public void render(float alphaMult) {
        if (p == null) return;
        float y = p.getY() + p.getHeight() - ICON_TOP - ICON / 2f;
        for (int i = 0; i < sprites.size(); i++) {
            SpriteAPI sprite = sprites.get(i);
            sprite.setColor(Color.white);
            sprite.setAlphaMult(alphaMult);
            sprite.setSize(ICON, ICON);
            sprite.renderAtCenter(p.getX() + ICON_X + i * ICON_STEP + ICON / 2f, y);
        }
    }

    @Override
    public void buttonPressed(Object buttonId) {
        listener.enableRequired(spec);
    }
}
