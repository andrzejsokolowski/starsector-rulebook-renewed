package rulebookrenewed.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate.DialogCallbacks;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;

import rulebookrenewed.Balance;
import rulebookrenewed.RuleBookIntel;
import rulebookrenewed.RuleCatalog;
import rulebookrenewed.RuleManager;
import rulebookrenewed.RuleSpec;
import rulebookrenewed.RuleState;
import rulebookrenewed.Unlocks;

/**
 * The rule picker. Every rule on one scrolling list, each hard rule next to its helpful mirror, with a
 * button per level; a click applies at once, so switching mid-campaign is the same as picking at the
 * start. The balance meter on top only informs and never blocks a pick.
 *
 * The list's labels live in a panel inside a scrolling TooltipMakerAPI, so the game clips them and
 * scrolls them with the mouse wheel. Buttons are still drawn and hit-tested by this panel, as in Gambling
 * Den (TooltipMakerAPI buttons in a custom dialog can take clicks in the wrong place): each frame the
 * list's real screen position is read back from the content panel, so drawing and clicks follow the
 * scroll whatever direction the scroller counts in.
 */
public final class RulePickerPanel extends BaseCustomUIPanelPlugin {

    public static final float PANEL_W = 1040f, PANEL_H = 720f;

    private static final float METER_X = 170f, METER_Y = 80f, METER_W = 700f, METER_H = 12f;
    private static final int METER_RANGE = 100;
    private static final float DETAIL_H = 118f;
    private static final float DETAIL_TOP = PANEL_H - 56f - DETAIL_H;
    private static final float LIST_X = 20f, LIST_TOP = 128f, LIST_W = PANEL_W - 40f;
    private static final float LIST_H = DETAIL_TOP - 8f - LIST_TOP;
    private static final float ROW_H = 30f, PAIR_GAP = 10f;
    private static final float NAME_X = 16f, LEVELS_X = 232f, OFF_W = 42f, LEVEL_W = 34f, GAP = 5f;
    private static final float POINTS_X = 480f, EFFECT_X = 532f;

    private static final Color BG = new Color(12, 15, 22);
    private static final Color PANEL = new Color(22, 27, 36);
    private static final Color GOLD = new Color(235, 185, 100);
    private static final Color DIM = new Color(150, 157, 173);
    private static final Color TEXT = new Color(220, 224, 232);

    private static final class Btn {
        final String action;
        final float x, y, w, h;
        final boolean inList;
        LabelAPI label;
        boolean visible = true, enabled = true, checked, hover;

        Btn(String action, float x, float y, float w, float h, boolean inList) {
            this.action = action;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.inList = inList;
        }
    }

    /** One rule line. y is in list content coordinates (0 = top of the list). */
    private static final class Row {
        final RuleSpec rule;
        final float y;
        final boolean pairStart;
        LabelAPI name, points, effect;
        final Btn[] levels = new Btn[RuleSpec.MAX_LEVELS + 1];

        Row(RuleSpec rule, float y, boolean pairStart) {
            this.rule = rule;
            this.y = y;
            this.pairStart = pairStart;
        }
    }

    private final List<Btn> buttons = new ArrayList<>();
    private final List<Row> rows = new ArrayList<>();
    private CustomPanelAPI panel;
    private CustomPanelAPI content;
    private DialogCallbacks callbacks;
    private PositionAPI position;
    private LabelAPI balance, multiplier, detailTitle, detailBody, detailFlavor;
    private Row hovered;
    private float contentTop = LIST_TOP;
    private boolean dismissed, wasMouseDown;

    public void init(CustomPanelAPI panel, DialogCallbacks callbacks) {
        this.panel = panel;
        this.callbacks = callbacks;
        wasMouseDown = Mouse.isCreated() && Mouse.isButtonDown(0);

        label(panel, "THE RULE BOOK", GOLD, 0f, 14f, PANEL_W, 26f, Fonts.ORBITRON_20AA, Alignment.MID);
        balance = label(panel, "", TEXT, 0f, 50f, PANEL_W, 20f, Fonts.DEFAULT_SMALL, Alignment.MID);
        label(panel, "Harder", RuleBookIntel.hardColor(), METER_X - 110f, METER_Y - 4f, 100f, 20f, Fonts.DEFAULT_SMALL, Alignment.RMID);
        label(panel, "Easier", RuleBookIntel.helpfulColor(), METER_X + METER_W + 10f, METER_Y - 4f, 100f, 20f, Fonts.DEFAULT_SMALL, Alignment.LMID);
        multiplier = label(panel, "", DIM, 0f, 100f, PANEL_W, 18f, Fonts.DEFAULT_SMALL, Alignment.MID);

        buildList();

        detailTitle = label(panel, "", GOLD, 40f, DETAIL_TOP + 8f, PANEL_W - 80f, 20f, Fonts.DEFAULT_SMALL, Alignment.LMID);
        detailBody = label(panel, "", TEXT, 40f, DETAIL_TOP + 30f, PANEL_W - 80f, 62f, Fonts.DEFAULT_SMALL, Alignment.TL);
        detailFlavor = label(panel, "", DIM, 40f, DETAIL_TOP + DETAIL_H - 26f, PANEL_W - 80f, 20f, Fonts.DEFAULT_SMALL, Alignment.LMID);

        button(panel, "close", "Close", PANEL_W - 200f, PANEL_H - 46f, 170f, 34f, false);

        refresh();
    }

    private void buildList() {
        List<RuleSpec> rules = RuleCatalog.all();
        float y = 6f;
        String lastPair = null;
        for (RuleSpec rule : rules) {
            boolean pairStart = lastPair != null && !lastPair.equals(rule.pair);
            if (pairStart) y += PAIR_GAP;
            rows.add(new Row(rule, y, pairStart));
            lastPair = rule.pair;
            y += ROW_H;
        }
        float contentH = Math.max(LIST_H, y + 6f);

        TooltipMakerAPI list = panel.createUIElement(LIST_W, LIST_H, true);
        content = panel.createCustomPanel(LIST_W, contentH, null);
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            row.name = label(content, "", TEXT, NAME_X, row.y + 6f, LEVELS_X - NAME_X - 8f, 18f, Fonts.DEFAULT_SMALL, Alignment.LMID);
            float x = LEVELS_X;
            for (int level = 0; level <= RuleSpec.MAX_LEVELS; level++) {
                float w = level == 0 ? OFF_W : LEVEL_W;
                row.levels[level] = button(content, "level:" + i + ":" + level,
                        level == 0 ? "Off" : RuleSpec.roman(level), x, row.y + 3f, w, ROW_H - 6f, true);
                x += w + GAP;
            }
            row.points = label(content, "", TEXT, POINTS_X, row.y + 6f, 40f, 18f, Fonts.DEFAULT_SMALL, Alignment.RMID);
            row.effect = label(content, "", DIM, EFFECT_X, row.y + 1f, LIST_W - EFFECT_X - 24f, ROW_H - 2f, Fonts.DEFAULT_SMALL, Alignment.LMID);
        }
        list.addCustom(content, 0f);
        list.setHeightSoFar(contentH);
        panel.addUIElement(list).inTL(LIST_X, LIST_TOP);
    }

    private LabelAPI label(CustomPanelAPI parent, String text, Color color, float x, float y, float w, float h,
                           String font, Alignment align) {
        LabelAPI label = Global.getSettings().createLabel(text, font);
        label.setColor(color);
        label.setAlignment(align);
        parent.addComponent((UIComponentAPI) label).inTL(x, y).setSize(w, h);
        return label;
    }

    private Btn button(CustomPanelAPI parent, String action, String text, float x, float y, float w, float h, boolean inList) {
        Btn b = new Btn(action, x, y, w, h, inList);
        b.label = label(parent, text, Color.WHITE, x, y + (h - 18f) / 2f, w, 18f, Fonts.DEFAULT_SMALL, Alignment.MID);
        buttons.add(b);
        return b;
    }

    private void refresh() {
        int net = Balance.net();
        balance.setText(Balance.describe(net));
        balance.setColor(RuleBookIntel.balanceColor(net));
        multiplier.setText(Balance.ngpInstalled()
                ? "Career points " + Balance.formatMultiplier(Balance.ngpMultiplier(net)) : "");
        for (Row row : rows) fillRow(row);
        refreshDetail();
    }

    private static Color typeColor(RuleSpec rule) {
        return rule.hard ? RuleBookIntel.hardColor() : RuleBookIntel.helpfulColor();
    }

    private void fillRow(Row row) {
        RuleSpec rule = row.rule;
        int level = RuleState.level(rule);
        boolean unlocked = Unlocks.isUnlocked(rule) || level > 0;
        row.name.setText(rule.name);
        row.name.setColor(unlocked ? typeColor(rule) : DIM.darker());
        for (int l = 0; l <= RuleSpec.MAX_LEVELS; l++) {
            Btn b = row.levels[l];
            b.visible = unlocked && l <= rule.levels;
            b.checked = l == level;
            b.label.setText(b.visible ? (l == 0 ? "Off" : RuleSpec.roman(l)) : "");
            b.label.setColor(b.checked ? Color.WHITE : TEXT);
        }
        if (!unlocked) {
            row.points.setText("");
            row.effect.setText(rule.unlockHint.isEmpty() ? "Locked" : "Locked. " + rule.unlockHint);
            row.effect.setColor(DIM.darker());
            return;
        }
        int points = rule.points(level);
        row.points.setText(level == 0 ? "" : (points > 0 ? "+" + points : String.valueOf(points)));
        row.points.setColor(typeColor(rule));
        row.effect.setText(level == 0 ? "" : rule.describe(level));
        row.effect.setColor(TEXT);
    }

    private void refreshDetail() {
        if (hovered == null) {
            detailTitle.setText("");
            detailBody.setText("House rules for this voyage. The Sector will hold you to them.");
            detailBody.setColor(DIM);
            detailFlavor.setText("");
            return;
        }
        RuleSpec rule = hovered.rule;
        detailTitle.setText(rule.name + (rule.hard ? "  (hard)" : "  (helpful)"));
        detailTitle.setColor(typeColor(rule));
        StringBuilder body = new StringBuilder();
        for (int l = 1; l <= rule.levels; l++) {
            if (body.length() > 0) body.append(l % 2 == 0 && rule.levels > 3 ? "     " : "\n");
            int points = rule.points(l);
            if (rule.levels > 1) body.append(RuleSpec.roman(l)).append(":  ");
            body.append(rule.describe(l)).append("  (").append(points > 0 ? "+" : "").append(points).append(')');
        }
        if (!Unlocks.isUnlocked(rule) && RuleState.level(rule) == 0 && !rule.unlockHint.isEmpty()) {
            body.append('\n').append(rule.unlockHint);
        }
        detailBody.setText(body.toString());
        detailBody.setColor(TEXT);
        detailFlavor.setText(rule.flavor);
    }

    private void act(String action) {
        if (dismissed) return;
        if (action.equals("close")) {
            close();
            return;
        }
        if (action.startsWith("level:")) {
            String[] parts = action.split(":");
            Row row = rows.get(Integer.parseInt(parts[1]));
            RuleManager.setLevel(row.rule, Integer.parseInt(parts[2]));
            Global.getSoundPlayer().playUISound("ui_button_pressed", 1f, 0.6f);
            refresh();
        }
    }

    private void close() {
        if (dismissed) return;
        dismissed = true;
        if (callbacks != null) callbacks.dismissDialog();
    }

    /** Reads where the scrolled list content sits now, in this panel's top-down coordinates. */
    private void updateContentTop() {
        if (position == null || content == null) return;
        PositionAPI p = content.getPosition();
        if (p == null) return;
        contentTop = (position.getY() + PANEL_H) - (p.getY() + p.getHeight());
    }

    private boolean inList(float my) {
        return my >= LIST_TOP && my <= LIST_TOP + LIST_H;
    }

    private float screenX(Btn b) {
        return b.inList ? LIST_X + b.x : b.x;
    }

    private float screenY(Btn b) {
        return b.inList ? contentTop + b.y : b.y;
    }

    private boolean contains(Btn b, float mx, float my) {
        if (!b.visible) return false;
        if (b.inList && !inList(my)) return false;
        float x = screenX(b), y = screenY(b);
        return mx >= x && mx <= x + b.w && my >= y && my <= y + b.h;
    }

    private void pointer(float mx, float my, boolean down) {
        boolean click = down && !wasMouseDown;
        wasMouseDown = down;
        for (Btn b : buttons) b.hover = b.enabled && contains(b, mx, my);

        Row over = null;
        if (mx >= LIST_X && mx <= LIST_X + LIST_W && inList(my)) {
            for (Row row : rows) {
                float top = contentTop + row.y;
                if (my >= top && my < top + ROW_H) over = row;
            }
        }
        if (over != hovered) {
            hovered = over;
            refreshDetail();
        }

        if (!click || dismissed) return;
        for (Btn b : buttons) {
            if (contains(b, mx, my)) {
                if (b.enabled) act(b.action);
                return;
            }
        }
    }

    @Override
    public void advance(float amount) {
        updateContentTop();
        if (dismissed || position == null || !Mouse.isCreated()) return;
        float scale = Global.getSettings().getScreenScaleMult();
        if (scale <= 0f) return;
        pointer(Mouse.getX() / scale - position.getX(), position.getY() + PANEL_H - Mouse.getY() / scale,
                Mouse.isButtonDown(0));
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        if (events == null || dismissed) return;
        for (InputEventAPI e : events) {
            if (!e.isConsumed() && e.isKeyDownEvent() && e.getEventValue() == Keyboard.KEY_ESCAPE) {
                e.consume();
                close();
                return;
            }
        }
    }

    @Override
    public void positionChanged(PositionAPI p) {
        position = p;
    }

    private float x(float n) {
        return position.getX() + n;
    }

    private float y(float topDown) {
        return position.getY() + PANEL_H - topDown;
    }

    private void rect(float left, float top, float w, float h, Color c, float alpha) {
        GLDraw.quad(x(left), y(top + h), w, h, c, alpha);
    }

    @Override
    public void renderBelow(float alpha) {
        if (position == null) return;
        updateContentTop();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT
                | GL11.GL_TEXTURE_BIT | GL11.GL_LINE_BIT | GL11.GL_SCISSOR_BIT);
        try {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            rect(0f, 0f, PANEL_W, PANEL_H, BG, alpha);
            GLDraw.frame(x(0f), y(PANEL_H), PANEL_W, PANEL_H, GOLD, 2f, alpha * 0.6f);
            drawMeter(alpha);

            rect(LIST_X, LIST_TOP, LIST_W, LIST_H, PANEL, alpha);
            GLDraw.frame(x(LIST_X), y(LIST_TOP + LIST_H), LIST_W, LIST_H, DIM, 1f, alpha * 0.3f);

            // Everything in the list is clipped to the list's box, like its labels.
            float scale = Global.getSettings().getScreenScaleMult();
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor((int) (x(LIST_X) * scale), (int) (y(LIST_TOP + LIST_H) * scale),
                    (int) Math.ceil(LIST_W * scale), (int) Math.ceil(LIST_H * scale));
            for (Row row : rows) {
                float top = contentTop + row.y;
                if (top + ROW_H < LIST_TOP || top > LIST_TOP + LIST_H) continue;
                if (row.pairStart) rect(LIST_X + 10f, top - PAIR_GAP / 2f - 0.5f, LIST_W - 40f, 1f, DIM, alpha * 0.25f);
                if (row == hovered) rect(LIST_X + 4f, top, LIST_W - 28f, ROW_H, new Color(40, 48, 62), alpha);
                rect(LIST_X + 4f, top + 4f, 3f, ROW_H - 8f, typeColor(row.rule), alpha * 0.7f);
            }
            for (Btn b : buttons) if (b.inList) drawButton(b, alpha);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            rect(20f, DETAIL_TOP, PANEL_W - 40f, DETAIL_H, PANEL, alpha);
            GLDraw.frame(x(20f), y(DETAIL_TOP + DETAIL_H), PANEL_W - 40f, DETAIL_H, DIM, 1f, alpha * 0.3f);

            for (Btn b : buttons) if (!b.inList) drawButton(b, alpha);
        } finally {
            GL11.glPopAttrib();
        }
    }

    private void drawButton(Btn b, float alpha) {
        if (!b.visible) return;
        float bx = screenX(b), by = screenY(b);
        if (b.inList && (by + b.h < LIST_TOP || by > LIST_TOP + LIST_H)) return;
        Color fill = !b.enabled ? new Color(22, 26, 33)
                : b.checked ? new Color(98, 76, 38)
                : b.hover ? new Color(60, 67, 78) : new Color(32, 40, 50);
        GLDraw.bevelledPanel(x(bx), y(by + b.h), b.w, b.h, fill, 2f, alpha);
        GLDraw.frame(x(bx), y(by + b.h), b.w, b.h, b.checked ? GOLD : DIM, 1f, alpha * (b.enabled ? 0.65f : 0.2f));
    }

    private void drawMeter(float alpha) {
        int net = Balance.net();
        float half = METER_W / 2f;
        float center = METER_X + half;
        rect(METER_X, METER_Y, METER_W, METER_H, new Color(30, 36, 46), alpha);
        float fraction = Math.min(1f, Math.abs(net) / (float) METER_RANGE);
        if (net > 0) rect(center, METER_Y, half * fraction, METER_H, RuleBookIntel.helpfulColor(), alpha * 0.85f);
        if (net < 0) rect(center - half * fraction, METER_Y, half * fraction, METER_H, RuleBookIntel.hardColor(), alpha * 0.85f);
        rect(center - 1f, METER_Y - 4f, 2f, METER_H + 8f, TEXT, alpha);
        GLDraw.frame(x(METER_X), y(METER_Y + METER_H), METER_W, METER_H, DIM, 1f, alpha * 0.5f);
    }
}
