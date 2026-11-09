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
import com.fs.starfarer.api.ui.UIComponentAPI;

import rulebookrenewed.Balance;
import rulebookrenewed.RuleBookIntel;
import rulebookrenewed.RuleCatalog;
import rulebookrenewed.RuleManager;
import rulebookrenewed.RuleSpec;
import rulebookrenewed.RuleState;
import rulebookrenewed.Unlocks;

/**
 * The rule picker. Every rule on one list, hard rules first, with a button per level; a click applies
 * at once, so switching mid-campaign is the same as picking at the start. The balance meter on top only
 * informs and never blocks a pick.
 *
 * Buttons are drawn and hit-tested by the panel itself, as in Gambling Den: TooltipMakerAPI buttons in
 * a custom dialog panel can render in one place and take clicks in another.
 */
public final class RulePickerPanel extends BaseCustomUIPanelPlugin {

    public static final float PANEL_W = 1040f, PANEL_H = 700f;

    private static final float ROW_TOP = 150f, ROW_H = 34f;
    private static final int ROWS_PER_PAGE = 11;
    private static final float LEVELS_X = 270f, OFF_W = 50f, LEVEL_W = 44f, GAP = 6f;
    private static final float METER_X = 170f, METER_Y = 80f, METER_W = 700f, METER_H = 12f;
    private static final int METER_RANGE = 100;
    private static final float DETAIL_TOP = PANEL_H - 166f;

    private static final Color BG = new Color(12, 15, 22);
    private static final Color PANEL = new Color(22, 27, 36);
    private static final Color GOLD = new Color(235, 185, 100);
    private static final Color DIM = new Color(150, 157, 173);
    private static final Color TEXT = new Color(220, 224, 232);

    private static final class Btn {
        final String action;
        final float x, y, w, h;
        LabelAPI label;
        boolean visible = true, enabled = true, checked, hover;

        Btn(String action, float x, float y, float w, float h) {
            this.action = action;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        boolean contains(float px, float py) {
            return visible && px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }

    /** One line of the list: a section header or a rule. */
    private static final class Entry {
        final String header;
        final boolean hard;
        final RuleSpec rule;

        Entry(String header, boolean hard, RuleSpec rule) {
            this.header = header;
            this.hard = hard;
            this.rule = rule;
        }
    }

    private static final class Slot {
        float y;
        LabelAPI name, points, effect;
        final Btn[] levels = new Btn[RuleSpec.MAX_LEVELS + 1];
        Entry entry;
    }

    private final List<Btn> buttons = new ArrayList<>();
    private final List<Entry> entries = new ArrayList<>();
    private final Slot[] slots = new Slot[ROWS_PER_PAGE];
    private CustomPanelAPI panel;
    private DialogCallbacks callbacks;
    private PositionAPI position;
    private LabelAPI balance, multiplier, pageLabel, detailTitle, detailBody, detailFlavor;
    private Btn prev, next;
    private int page, pages;
    private int hoveredSlot = -1;
    private boolean dismissed, wasMouseDown;

    public void init(CustomPanelAPI panel, DialogCallbacks callbacks) {
        this.panel = panel;
        this.callbacks = callbacks;
        wasMouseDown = Mouse.isCreated() && Mouse.isButtonDown(0);

        buildEntries();

        label("THE RULE BOOK", GOLD, 0f, 14f, PANEL_W, 26f, Fonts.ORBITRON_20AA, Alignment.MID);
        balance = label("", TEXT, 0f, 50f, PANEL_W, 20f, Fonts.DEFAULT_SMALL, Alignment.MID);
        label("Harder", RuleBookIntel.hardColor(), METER_X - 110f, METER_Y - 4f, 100f, 20f, Fonts.DEFAULT_SMALL, Alignment.RMID);
        label("Easier", RuleBookIntel.helpfulColor(), METER_X + METER_W + 10f, METER_Y - 4f, 100f, 20f, Fonts.DEFAULT_SMALL, Alignment.LMID);
        multiplier = label("", DIM, 0f, 102f, PANEL_W, 18f, Fonts.DEFAULT_SMALL, Alignment.MID);

        for (int i = 0; i < ROWS_PER_PAGE; i++) {
            Slot slot = new Slot();
            slot.y = ROW_TOP + i * ROW_H;
            slot.name = label("", TEXT, 40f, slot.y + 8f, 220f, 18f, Fonts.DEFAULT_SMALL, Alignment.LMID);
            float x = LEVELS_X;
            for (int level = 0; level <= RuleSpec.MAX_LEVELS; level++) {
                float w = level == 0 ? OFF_W : LEVEL_W;
                slot.levels[level] = button("level:" + i + ":" + level, level == 0 ? "Off" : RuleSpec.roman(level),
                        x, slot.y + 4f, w, ROW_H - 8f);
                x += w + GAP;
            }
            slot.points = label("", TEXT, 590f, slot.y + 8f, 46f, 18f, Fonts.DEFAULT_SMALL, Alignment.RMID);
            slot.effect = label("", DIM, 650f, slot.y + 2f, PANEL_W - 670f, ROW_H - 4f, Fonts.DEFAULT_SMALL, Alignment.LMID);
            slots[i] = slot;
        }

        detailTitle = label("", GOLD, 40f, DETAIL_TOP + 8f, PANEL_W - 80f, 20f, Fonts.DEFAULT_SMALL, Alignment.LMID);
        detailBody = label("", TEXT, 40f, DETAIL_TOP + 30f, PANEL_W - 80f, 60f, Fonts.DEFAULT_SMALL, Alignment.TL);
        detailFlavor = label("", DIM, 40f, DETAIL_TOP + 92f, PANEL_W - 80f, 20f, Fonts.DEFAULT_SMALL, Alignment.LMID);

        float bottom = PANEL_H - 48f;
        prev = button("prev", "Previous", 30f, bottom, 100f, 36f);
        pageLabel = label("", DIM, 135f, bottom + 9f, 90f, 18f, Fonts.DEFAULT_SMALL, Alignment.MID);
        next = button("next", "Next", 230f, bottom, 100f, 36f);
        button("close", "Close", PANEL_W - 200f, bottom, 170f, 36f);

        refresh();
    }

    private void buildEntries() {
        entries.clear();
        List<RuleSpec> hard = RuleCatalog.ofType(true);
        List<RuleSpec> helpful = RuleCatalog.ofType(false);
        if (!hard.isEmpty()) entries.add(new Entry("HARD RULES", true, null));
        for (RuleSpec r : hard) entries.add(new Entry(null, true, r));
        if (!helpful.isEmpty()) entries.add(new Entry("HELPFUL RULES", false, null));
        for (RuleSpec r : helpful) entries.add(new Entry(null, false, r));
        pages = Math.max(1, (entries.size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
        page = Math.min(page, pages - 1);
    }

    private LabelAPI label(String text, Color color, float x, float y, float w, float h, String font, Alignment align) {
        LabelAPI label = Global.getSettings().createLabel(text, font);
        label.setColor(color);
        label.setAlignment(align);
        panel.addComponent((UIComponentAPI) label).inTL(x, y).setSize(w, h);
        return label;
    }

    private Btn button(String action, String text, float x, float y, float w, float h) {
        Btn b = new Btn(action, x, y, w, h);
        b.label = label(text, Color.WHITE, x, y + (h - 18f) / 2f, w, 18f, Fonts.DEFAULT_SMALL, Alignment.MID);
        buttons.add(b);
        return b;
    }

    private void refresh() {
        int net = Balance.net();
        balance.setText(Balance.describe(net));
        balance.setColor(RuleBookIntel.balanceColor(net));
        multiplier.setText(Balance.ngpInstalled()
                ? "Career points " + Balance.formatMultiplier(Balance.ngpMultiplier(net)) : "");

        for (int i = 0; i < ROWS_PER_PAGE; i++) fillSlot(slots[i], entryAt(i));

        prev.visible = next.visible = pages > 1;
        prev.enabled = page > 0;
        next.enabled = page < pages - 1;
        prev.label.setText(prev.visible ? "Previous" : "");
        next.label.setText(next.visible ? "Next" : "");
        prev.label.setColor(prev.enabled ? Color.WHITE : DIM.darker());
        next.label.setColor(next.enabled ? Color.WHITE : DIM.darker());
        pageLabel.setText(pages > 1 ? "Page " + (page + 1) + " of " + pages : "");

        refreshDetail();
    }

    private Entry entryAt(int slotIndex) {
        int index = page * ROWS_PER_PAGE + slotIndex;
        return index < entries.size() ? entries.get(index) : null;
    }

    private void fillSlot(Slot slot, Entry entry) {
        slot.entry = entry;
        for (Btn b : slot.levels) {
            b.visible = false;
            b.label.setText("");
        }
        slot.points.setText("");
        slot.effect.setText("");
        if (entry == null) {
            slot.name.setText("");
            return;
        }
        if (entry.rule == null) {
            slot.name.setText(entry.header);
            slot.name.setColor(entry.hard ? RuleBookIntel.hardColor() : RuleBookIntel.helpfulColor());
            return;
        }

        RuleSpec rule = entry.rule;
        int level = RuleState.level(rule);
        boolean unlocked = Unlocks.isUnlocked(rule) || level > 0;
        slot.name.setText(rule.name);
        slot.name.setColor(unlocked ? TEXT : DIM.darker());
        if (!unlocked) {
            slot.effect.setText(rule.unlockHint.isEmpty() ? "Locked" : "Locked. " + rule.unlockHint);
            slot.effect.setColor(DIM.darker());
            return;
        }
        for (int l = 0; l <= rule.levels; l++) {
            Btn b = slot.levels[l];
            b.visible = true;
            b.checked = l == level;
            b.label.setText(l == 0 ? "Off" : RuleSpec.roman(l));
            b.label.setColor(b.checked ? Color.WHITE : TEXT);
        }
        int points = rule.points(level);
        slot.points.setText(level == 0 ? "" : (points > 0 ? "+" + points : String.valueOf(points)));
        slot.points.setColor(rule.hard ? RuleBookIntel.hardColor() : RuleBookIntel.helpfulColor());
        slot.effect.setText(level == 0 ? "" : rule.describe(level));
        slot.effect.setColor(TEXT);
    }

    private void refreshDetail() {
        Entry entry = hoveredSlot >= 0 ? slots[hoveredSlot].entry : null;
        if (entry == null || entry.rule == null) {
            detailTitle.setText("");
            detailBody.setText("House rules for this voyage. The Sector will hold you to them.");
            detailBody.setColor(DIM);
            detailFlavor.setText("");
            return;
        }
        RuleSpec rule = entry.rule;
        detailTitle.setText(rule.name);
        detailTitle.setColor(rule.hard ? RuleBookIntel.hardColor() : RuleBookIntel.helpfulColor());
        StringBuilder body = new StringBuilder();
        for (int l = 1; l <= rule.levels; l++) {
            if (body.length() > 0) body.append('\n');
            int points = rule.points(l);
            if (rule.levels > 1) body.append(RuleSpec.roman(l)).append(":  ");
            body.append(rule.describe(l)).append("   (").append(points > 0 ? "+" : "").append(points).append(')');
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
        if (action.equals("prev") || action.equals("next")) {
            page = Math.max(0, Math.min(pages - 1, page + (action.equals("next") ? 1 : -1)));
            refresh();
            return;
        }
        if (action.startsWith("level:")) {
            String[] parts = action.split(":");
            Slot slot = slots[Integer.parseInt(parts[1])];
            if (slot.entry == null || slot.entry.rule == null) return;
            RuleManager.setLevel(slot.entry.rule, Integer.parseInt(parts[2]));
            Global.getSoundPlayer().playUISound("ui_button_pressed", 1f, 0.6f);
            refresh();
        }
    }

    private void close() {
        if (dismissed) return;
        dismissed = true;
        if (callbacks != null) callbacks.dismissDialog();
    }

    private void pointer(float mx, float my, boolean down) {
        boolean click = down && !wasMouseDown;
        wasMouseDown = down;
        for (Btn b : buttons) b.hover = b.visible && b.enabled && b.contains(mx, my);

        int hover = -1;
        if (mx >= 20f && mx <= PANEL_W - 20f) {
            for (int i = 0; i < ROWS_PER_PAGE; i++) {
                if (my >= slots[i].y && my < slots[i].y + ROW_H) hover = i;
            }
        }
        if (hover != hoveredSlot) {
            hoveredSlot = hover;
            refreshDetail();
        }

        if (!click || dismissed) return;
        for (Btn b : buttons) {
            if (b.contains(mx, my)) {
                if (b.enabled) act(b.action);
                return;
            }
        }
    }

    @Override
    public void advance(float amount) {
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
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT | GL11.GL_TEXTURE_BIT | GL11.GL_LINE_BIT);
        try {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            rect(0f, 0f, PANEL_W, PANEL_H, BG, alpha);
            GLDraw.frame(x(0f), y(PANEL_H), PANEL_W, PANEL_H, GOLD, 2f, alpha * 0.6f);
            drawMeter(alpha);

            rect(20f, ROW_TOP - 6f, PANEL_W - 40f, ROWS_PER_PAGE * ROW_H + 12f, PANEL, alpha);
            for (int i = 0; i < ROWS_PER_PAGE; i++) {
                Slot slot = slots[i];
                if (slot == null || slot.entry == null) continue;
                if (slot.entry.rule == null) {
                    Color c = slot.entry.hard ? RuleBookIntel.hardColor() : RuleBookIntel.helpfulColor();
                    rect(30f, slot.y + ROW_H - 6f, PANEL_W - 60f, 1f, c, alpha * 0.6f);
                } else if (i == hoveredSlot) {
                    rect(24f, slot.y, PANEL_W - 48f, ROW_H, new Color(40, 48, 62), alpha);
                }
            }

            rect(20f, DETAIL_TOP, PANEL_W - 40f, 120f, PANEL, alpha);
            GLDraw.frame(x(20f), y(DETAIL_TOP + 120f), PANEL_W - 40f, 120f, DIM, 1f, alpha * 0.3f);

            for (Btn b : buttons) {
                if (!b.visible) continue;
                Color fill = !b.enabled ? new Color(22, 26, 33)
                        : b.checked ? new Color(98, 76, 38)
                        : b.hover ? new Color(60, 67, 78) : new Color(32, 40, 50);
                GLDraw.bevelledPanel(x(b.x), y(b.y + b.h), b.w, b.h, fill, 2f, alpha);
                GLDraw.frame(x(b.x), y(b.y + b.h), b.w, b.h, b.checked ? GOLD : DIM, 1f, alpha * (b.enabled ? 0.65f : 0.2f));
            }
        } finally {
            GL11.glPopAttrib();
        }
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
