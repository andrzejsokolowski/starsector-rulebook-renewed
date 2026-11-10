/*
 * Based on UNGP_RulePickItemPanelPlugin from Unofficial New Game Plus by Originem (fork 2.3.2), used
 * with permission; see CREDITS.txt. Changes for Rule Book - Renewed: rule levels with pips, locked and
 * conflicting tiles, no LazyLib, no golden-rule scan line.
 */
package rulebookrenewed.ui;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.RuleSpec;

/** One rule tile: an area checkbox with the rule's name, icon, level pips and cost. */
public final class RuleTile extends BaseCustomUIPanelPlugin {

    public static final float SIZE = 112f;
    private static final float ICON = 64f;

    public interface Listener {
        void tilePressed(RuleTile tile);
    }

    public final RuleSpec rule;
    private final Listener listener;
    private final SpriteAPI sprite;
    private PositionAPI p;
    private ButtonAPI button;
    private LabelAPI name, cost;
    private int level;
    private boolean locked;

    public RuleTile(RuleSpec rule, Listener listener) {
        this.rule = rule;
        this.listener = listener;
        this.sprite = Global.getSettings().getSprite(rule.icon);
    }

    public static Color color(RuleSpec rule) {
        return rule.hard ? RulePickerPanel.NEGATIVE : RulePickerPanel.POSITIVE;
    }

    public void init(CustomPanelAPI panel) {
        Color color = color(rule);
        TooltipMakerAPI element = panel.createUIElement(SIZE, SIZE, false);
        button = element.addAreaCheckbox("", rule.id, color, Misc.scaleColorOnly(color, 0.2f),
                Misc.getBrightPlayerColor(), SIZE, SIZE, 0f);
        button.getPosition().setXAlignOffset(-0.5f); // an area checkbox sits one pixel off
        button.setCustomData(rule);
        button.setButtonPressedSound(null);
        panel.addUIElement(element);

        TooltipMakerAPI nameElement = panel.createUIElement(SIZE, SIZE, false);
        nameElement.addSpacer(SIZE * 0.08f);
        name = nameElement.addPara(rule.name, color, 0f);
        name.setAlignment(Alignment.MID);
        panel.addUIElement(nameElement);

        TooltipMakerAPI costElement = panel.createUIElement(SIZE, 22f, false);
        costElement.setParaOrbitronLarge();
        cost = costElement.addPara("", color, 0f);
        cost.setAlignment(Alignment.MID);
        panel.addUIElement(costElement).inBL(0f, 2f);
    }

    /** The cost of a level the way the picker shows it: positive rules cost points, negative rules give them. */
    public static String costText(RuleSpec rule, int level) {
        int points = -rule.points(Math.max(1, level));
        return points > 0 ? "+" + points : String.valueOf(points);
    }

    /** Shows the tile's state. locked: the rule is hidden; blocked: its mirror is picked. */
    public void sync(int level, boolean locked, boolean blocked) {
        this.level = level;
        this.locked = locked;
        button.setChecked(level > 0);
        button.setEnabled(!locked && !blocked);
        Color color = locked || blocked ? Misc.getGrayColor() : color(rule);
        name.setText(locked ? "[ ? ]" : rule.name);
        name.setColor(color);
        cost.setText(locked ? "" : costText(rule, level));
        cost.setColor(locked || blocked ? Misc.getGrayColor()
                : rule.points(1) == 0 ? Misc.getTextColor()
                : rule.hard ? Misc.getPositiveHighlightColor() : Misc.getNegativeHighlightColor());
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isEnabled() {
        return button.isEnabled();
    }

    /** True when the point (screen coordinates, y up) is on the tile. */
    public boolean contains(float x, float y) {
        return p != null && x >= p.getX() && x <= p.getX() + p.getWidth() && y >= p.getY() && y <= p.getY() + p.getHeight();
    }

    @Override
    public void positionChanged(PositionAPI position) {
        p = position;
    }

    @Override
    public void render(float alphaMult) {
        if (p == null) return;
        float cx = p.getX() + p.getWidth() * 0.5f;
        float cy = p.getY() + p.getHeight() * 0.5f;
        if (!locked) {
            sprite.setColor(button.isEnabled() ? Color.white : Color.gray);
            sprite.setAlphaMult(alphaMult * (level > 0 || !button.isEnabled() ? 1f : 0.75f));
            sprite.setSize(ICON, ICON);
            sprite.renderAtCenter(cx, cy);
        }
        if (locked || rule.levels <= 1) return;

        // Level pips between the icon and the cost.
        float w = 12f, h = 4f, gap = 3f;
        float total = rule.levels * w + (rule.levels - 1) * gap;
        float x = cx - total / 2f, y = p.getY() + 23f;
        Color on = button.isEnabled() ? color(rule) : Color.gray;
        Color off = new Color(60, 66, 76);
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        for (int l = 1; l <= rule.levels; l++) {
            GLDraw.quad(x, y, w, h, l <= level ? on : off, alphaMult);
            x += w + gap;
        }
        GL11.glPopAttrib();
    }

    @Override
    public void buttonPressed(Object buttonId) {
        listener.tilePressed(this);
    }
}
