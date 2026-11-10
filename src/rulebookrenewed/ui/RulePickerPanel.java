/*
 * The layout, tile grids, summary lists and Confirm/Cancel flow follow UNGP_RulePickPanelPlugin from
 * Unofficial New Game Plus by Originem (fork 2.3.2), used with permission; see CREDITS.txt.
 */
package rulebookrenewed.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate.DialogCallbacks;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.CutStyle;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;

import rulebookrenewed.Balance;
import rulebookrenewed.ChallengeSpec;
import rulebookrenewed.Presets;
import rulebookrenewed.Challenges;
import rulebookrenewed.RuleCatalog;
import rulebookrenewed.RuleManager;
import rulebookrenewed.RuleSpec;
import rulebookrenewed.RuleState;
import rulebookrenewed.Settings;
import rulebookrenewed.Unlocks;

/**
 * The rule picker. Left: a grid of positive rules over a grid of negative rules, or the challenge list.
 * Right: the rule under the mouse, the picked rules, challenges whose rules are met, the point total,
 * and Confirm / Cancel.
 *
 * Picks are made on a working copy and applied on Confirm. A left click picks a rule and then raises
 * its level (past the top it is dropped); a right click lowers it. Four preset slots, kept across saves,
 * save and load whole rule sets. A rule and its mirror are never on
 * together: picking one disables the other tile. With the point budget on (LunaLib), a rule set that
 * costs more than it gives cannot be confirmed.
 */
public final class RulePickerPanel extends BaseCustomUIPanelPlugin implements RuleTile.Listener, ChallengeRow.Listener {

    public static final Color POSITIVE = new Color(60, 235, 140);
    public static final Color NEGATIVE = new Color(255, 80, 100);
    public static final Color GOLD = new Color(255, 200, 80);

    private static final String POSITIVE_ICON = "graphics/rulebook/icons/positive_icon.png";
    private static final String NEGATIVE_ICON = "graphics/rulebook/icons/negative_icon.png";
    private static final float HEADING_H = 25f, HIDDEN_X = -30000f;
    private static final int TAB_RULES = 0, TAB_CHALLENGES = 1;

    /** Forwards button presses from a page to the picker. */
    private final class PagePlugin extends BaseCustomUIPanelPlugin {
        @Override
        public void buttonPressed(Object buttonId) {
            RulePickerPanel.this.buttonPressed(buttonId);
        }
    }

    /** A dark box behind the warning text. */
    private final class BoxPlugin extends BaseCustomUIPanelPlugin {
        private PositionAPI p;

        @Override
        public void positionChanged(PositionAPI position) {
            p = position;
        }

        @Override
        public void renderBelow(float alphaMult) {
            if (p == null) return;
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GLDraw.quad(p.getX(), p.getY(), p.getWidth(), p.getHeight(), new Color(8, 10, 14), alphaMult * 0.97f);
            GLDraw.frame(p.getX(), p.getY(), p.getWidth(), p.getHeight(), Misc.getNegativeHighlightColor(), 2f, alphaMult);
            GL11.glPopAttrib();
        }

        @Override
        public void buttonPressed(Object buttonId) {
            RulePickerPanel.this.buttonPressed(buttonId);
        }
    }

    private final Map<RuleSpec, Integer> working = new HashMap<>();
    private final Challenges.Levels levels = this::level;
    private final List<RuleTile> tiles = new ArrayList<>();
    /** The scrolling box each tile lives in. Tiles scrolled out of one box still sit, unseen, over the other. */
    private final Map<RuleTile, UIRect> tileGrid = new HashMap<>();
    private final List<ChallengeRow> rows = new ArrayList<>();
    private final Set<String> warned = new HashSet<>();
    private final float width, height;

    private CustomPanelAPI panel;
    private DialogCallbacks callbacks;
    private PositionAPI pos;
    private final CustomPanelAPI[] pages = new CustomPanelAPI[2];
    private UIRect pageRect, positiveGrid, negativeGrid, challengeList;
    private UIRect detailRect, positiveSummary, negativeSummary, challengeSummary, reasonRect, warningRect;
    private LabelAPI positiveCount, negativeCount, pointsLabel, countLabel, careerLabel;
    private ButtonAPI confirmBtn, rulesTab, challengesTab, presetLoad;
    private final ButtonAPI[] presetButtons = new ButtonAPI[Presets.SLOTS];
    private int presetSlot;
    private ChallengeSpec tracked;
    private RuleSpec hovered;
    private int hoveredLevel = -1, tab = TAB_RULES;
    private boolean wasRightDown, closed;

    public RulePickerPanel() {
        width = Global.getSettings().getScreenWidth();
        height = Global.getSettings().getScreenHeight();
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public void init(CustomPanelAPI panel, DialogCallbacks callbacks) {
        this.panel = panel;
        this.callbacks = callbacks;
        wasRightDown = Mouse.isCreated() && Mouse.isButtonDown(1);
        for (RuleSpec rule : RuleCatalog.all()) {
            int level = RuleState.level(rule);
            if (level > 0) working.put(rule, level);
        }
        createContent();
        panel.addComponent(panel.createCustomPanel(width, height, new BackgroundUI(0.8f)));
    }

    private int level(RuleSpec rule) {
        Integer level = working.get(rule);
        return level == null ? 0 : level;
    }

    private boolean usable(RuleSpec rule) {
        return Unlocks.isUnlocked(rule) || level(rule) > 0;
    }

    /** The golden rule that is picked, when this is a different golden rule; otherwise null. */
    private RuleSpec otherGolden(RuleSpec rule) {
        if (!rule.golden) return null;
        for (RuleSpec other : working.keySet()) if (other != rule && other.golden) return other;
        return null;
    }

    private static RuleSpec mirror(RuleSpec rule) {
        for (RuleSpec other : RuleCatalog.all()) {
            if (other != rule && other.hard != rule.hard && other.pair.equals(rule.pair)) return other;
        }
        return null;
    }

    // ---- building ----

    private void createContent() {
        Color border = Misc.getDarkPlayerColor();
        UIRect[] temp;
        UIRect all = new UIRect(0, 0, width, height).shrink(20f);
        temp = all.splitHorizontallyReverse(Math.min(512f, width * 0.36f));
        UIRect left = temp[0];
        UIRect right = temp[1];

        // Left: the pages; under them the tab buttons and, as in UNGP, the presets.
        temp = left.splitVertically(0.92f, 0.08f);
        pageRect = temp[0];
        UIRect tabRect = temp[1].shrink(5f);
        tabRect.addBoarder(panel, border, 2f);
        temp = tabRect.shrink(10f).splitHorizontally(1.2f, 1.2f, 0.4f, 1f, 1f, 1f, 1f, 1f, 1f);
        rulesTab = tabButton(temp[0], "Rules", "tab_rules");
        challengesTab = tabButton(temp[1], "Challenges", "tab_challenges");
        for (int i = 0; i < Presets.SLOTS; i++) {
            presetButtons[i] = tabButton(temp[3 + i], "Preset " + (i + 1), "preset_" + i);
        }
        presetButtons[0].setChecked(true);
        presetLoad = plainButton(temp[8], "Load", "preset_load");
        plainButton(temp[7], "Save", "preset_save");
        presetLoad.setEnabled(Presets.exists(presetSlot));

        buildRulesPage();
        buildChallengesPage();

        // Right: detail, picked rules and challenges, totals, confirm.
        temp = right.splitVertically(0.3f, 0.55f, 0.15f);
        UIRect detail = temp[0].shrink(10f);
        UIRect middle = temp[1];
        UIRect confirm = temp[2];

        detail.addBoarder(panel, border, 2f);
        detailRect = detail.shrink(10f);

        temp = middle.splitVerticallyReverse(100f);
        UIRect summary = temp[0].shrink(10f);
        UIRect totals = temp[1].shrink(0f, 0f, 10f, 10f);
        summary.addBoarder(panel, border, 2f);
        temp = summary.splitVertically(0.6f, 0.4f);
        UIRect picked = temp[0];
        UIRect challenges = temp[1];
        temp = picked.splitHorizontally(0.5f, 0.5f);
        positiveSummary = heading(temp[0], "Positive rules", POSITIVE, border).shrink(2f, 0f);
        negativeSummary = heading(temp[1], "Negative rules", NEGATIVE, border).shrink(2f, 0f);
        challengeSummary = heading(challenges, "Challenges", GOLD, border).shrink(2f, 0f);

        totals.addBoarder(panel, border, 2f);
        UIRect totalsContent = heading(totals, "Summary", Misc.getBasePlayerColor(), border).shrink(10f, 4f);
        TooltipMakerAPI totalsTip = totalsContent.beginTooltip(panel, false);
        totalsTip.setParaOrbitronLarge();
        pointsLabel = totalsTip.addPara("", Misc.getBasePlayerColor(), 3f);
        countLabel = totalsTip.addPara("", Misc.getBasePlayerColor(), 3f);
        totalsTip.setParaFontDefault();
        careerLabel = totalsTip.addPara("", Misc.getGrayColor(), 3f);
        totalsContent.addTooltip();

        temp = confirm.splitHorizontally(0.6f, 0.4f);
        UIRect reasons = temp[0].shrink(10f);
        UIRect buttons = temp[1].shrink(10f);
        reasons.addBoarder(panel, border, 2f);
        reasonRect = heading(reasons, "Problems", Misc.getBasePlayerColor(), border).shrink(10f, 4f);
        temp = buttons.splitVertically(0.5f, 0.5f);
        UIRect confirmRect = temp[0].shrink(0f, 5f);
        TooltipMakerAPI confirmTip = confirmRect.beginTooltip(panel, false);
        confirmTip.setButtonFontOrbitron20Bold();
        confirmBtn = confirmTip.addButton("Confirm", "confirm", confirmRect.getWidth(), confirmRect.getHeight(), 0f);
        confirmRect.addTooltip();
        UIRect cancelRect = temp[1].shrink(0f, 5f);
        TooltipMakerAPI cancelTip = cancelRect.beginTooltip(panel, false);
        cancelTip.setButtonFontOrbitron20Bold();
        ButtonAPI cancel = cancelTip.addButton("Cancel", "cancel", cancelRect.getWidth(), cancelRect.getHeight(), 0f);
        cancel.setShortcut(Keyboard.KEY_ESCAPE, true);
        cancelRect.addTooltip();

        float boxW = Math.min(520f, width - 80f);
        warningRect = new UIRect((width - boxW) / 2f, height / 2f - 70f, boxW, 140f);

        showTab(TAB_RULES);
        refresh();
    }

    private ButtonAPI tabButton(UIRect rect, String text, String id) {
        rect = rect.shrink(4f, 0f);
        TooltipMakerAPI tip = rect.beginTooltip(panel, false);
        ButtonAPI button = tip.addAreaCheckbox(text, id, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(),
                Misc.getBrightPlayerColor(), rect.getWidth(), rect.getHeight(), 0f);
        rect.addTooltip();
        return button;
    }

    private ButtonAPI plainButton(UIRect rect, String text, String id) {
        rect = rect.shrink(4f, 0f);
        TooltipMakerAPI tip = rect.beginTooltip(panel, false);
        ButtonAPI button = tip.addButton(text, id, rect.getWidth(), rect.getHeight(), 0f);
        rect.addTooltip();
        return button;
    }

    private void selectPreset(int slot) {
        presetSlot = slot;
        for (int i = 0; i < presetButtons.length; i++) presetButtons[i].setChecked(i == slot);
        presetLoad.setEnabled(Presets.exists(slot));
    }

    private void savePreset() {
        Map<String, Integer> out = new HashMap<>();
        for (Map.Entry<RuleSpec, Integer> e : working.entrySet()) out.put(e.getKey().id, e.getValue());
        Presets.save(presetSlot, out);
        presetLoad.setEnabled(Presets.exists(presetSlot));
    }

    /** Replaces the working rules with a preset; rules that are locked or no longer exist are left out. */
    private void loadPreset() {
        Map<String, Integer> preset = Presets.load(presetSlot);
        if (preset.isEmpty()) return;
        working.clear();
        for (RuleSpec rule : RuleCatalog.all()) {
            Integer level = preset.get(rule.id);
            if (level == null || level <= 0 || !Unlocks.isUnlocked(rule)) continue;
            RuleSpec mirror = mirror(rule);
            if (mirror != null && working.containsKey(mirror)) continue;
            if (otherGolden(rule) != null) continue;
            working.put(rule, Math.min(level, rule.levels));
        }
        tracked = null;
        refresh();
    }

    /** Puts a section heading at the top of a box and returns the space under it. */
    private UIRect heading(UIRect box, String text, Color color, Color background) {
        UIRect[] temp = box.splitVertically(HEADING_H);
        TooltipMakerAPI tip = temp[0].beginTooltip(panel, false);
        temp[0].syncPositionSize(tip.addSectionHeading(text, color, background, Alignment.MID, 0f).getPosition());
        temp[0].addTooltip();
        return temp[1];
    }

    private CustomPanelAPI page(int index) {
        CustomPanelAPI page = panel.createCustomPanel(pageRect.getWidth(), pageRect.getHeight(), new PagePlugin());
        panel.addComponent(page).inTL(pageRect.getX(), pageRect.getY());
        pages[index] = page;
        return page;
    }

    private void buildRulesPage() {
        CustomPanelAPI page = page(TAB_RULES);
        List<RuleSpec> positive = new ArrayList<>(), negative = new ArrayList<>();
        for (RuleSpec rule : RuleCatalog.all()) (rule.hard ? negative : positive).add(rule);
        // Dearest first: the highest cost among positive rules, the most points given among negative ones.
        java.util.Comparator<RuleSpec> byCost = (a, b) -> a.golden != b.golden ? (a.golden ? -1 : 1)
                : Integer.compare(Math.abs(b.points(1)), Math.abs(a.points(1)));
        positive.sort(byCost);
        negative.sort(byCost);

        UIRect[] temp = new UIRect(0, 0, pageRect.getWidth(), pageRect.getHeight()).splitVertically(0.5f, 0.5f);
        positiveGrid = grid(page, temp[0].shrink(5f), "Positive rules", POSITIVE_ICON, POSITIVE, "clear_positive", positive, true);
        negativeGrid = grid(page, temp[1].shrink(5f), "Negative rules", NEGATIVE_ICON, NEGATIVE, "clear_negative", negative, false);
    }

    /** One titled grid of rule tiles; returns the rect of its scrolling part, in page coordinates. */
    private UIRect grid(CustomPanelAPI page, UIRect rect, String title, String icon, Color color, String clearId,
                        List<RuleSpec> rules, boolean positive) {
        UIRect[] temp = rect.splitVertically(32f);
        UIRect titleRect = temp[0];
        UIRect content = temp[1];

        TooltipMakerAPI titleTip = titleRect.beginTooltip(page, false);
        TooltipMakerAPI image = titleTip.beginImageWithText(icon, 16f);
        image.setParaOrbitronLarge();
        image.addPara(title, color, 0f);
        titleTip.addImageWithText(0f);
        LabelAPI count = titleTip.createLabel("", Color.white);
        count.setAlignment(Alignment.RMID);
        titleTip.addCustomDoNotSetPosition((UIComponentAPI) count).getPosition()
                .setSize(titleRect.getWidth() - 110f, titleRect.getHeight()).inTL(0f, 0f);
        titleTip.addButton("Clear all", clearId, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID,
                CutStyle.BL_TR, 100f, titleRect.getHeight() * 0.8f, 0f).getPosition().inTL(titleRect.getWidth() - 100f, 0f);
        titleRect.addTooltip();
        if (positive) positiveCount = count;
        else negativeCount = count;

        float pad = 10f, rowPad = pad * 2.5f, headingH = 26f;
        int perRow = Math.max(1, (int) (content.getWidth() / (RuleTile.SIZE + pad)));
        TooltipMakerAPI tip = content.beginTooltip(page, true);
        float y = 0f;
        for (Map.Entry<String, List<RuleSpec>> group : groups(rules).entrySet()) {
            tip.setParaOrbitronLarge();
            LabelAPI heading = tip.createLabel(group.getKey(), color);
            tip.setParaFontDefault();
            tip.addCustomDoNotSetPosition((UIComponentAPI) heading).getPosition()
                    .inTL(0f, y).setSize(content.getWidth() - 20f, headingH - 4f);
            y += headingH;
            List<RuleSpec> members = group.getValue();
            for (int i = 0; i < members.size(); i++) {
                RuleTile tile = new RuleTile(members.get(i), this);
                CustomPanelAPI custom = Global.getSettings().createCustom(RuleTile.SIZE, RuleTile.SIZE, tile);
                tile.init(custom);
                float x = (i % perRow) * (RuleTile.SIZE + pad);
                tip.addCustomDoNotSetPosition(custom).getPosition().inTL(x, y + (i / perRow) * (RuleTile.SIZE + rowPad));
                tiles.add(tile);
                tileGrid.put(tile, content);
            }
            y += ((members.size() + perRow - 1) / perRow) * (RuleTile.SIZE + rowPad);
        }
        // Tiles and headings take no room in the tooltip, so one spacer gives the scroller its height.
        tip.addSpacer(y + 10f);
        content.addTooltip();
        return content;
    }

    /** The section order of the picker; a section not named here comes after these, by name. */
    private static final List<String> GROUP_ORDER = java.util.Arrays.asList("Combat", "Fleet", "Colonies", "Economy", "Exploration");

    /** Rules by their section column, in GROUP_ORDER, keeping the order they were given in. */
    private static Map<String, List<RuleSpec>> groups(List<RuleSpec> rules) {
        Map<String, List<RuleSpec>> bySection = new java.util.TreeMap<>((a, b) -> {
            int ia = GROUP_ORDER.indexOf(a), ib = GROUP_ORDER.indexOf(b);
            if (ia < 0) ia = GROUP_ORDER.size();
            if (ib < 0) ib = GROUP_ORDER.size();
            return ia != ib ? Integer.compare(ia, ib) : a.compareTo(b);
        });
        for (RuleSpec rule : rules) {
            String section = rule.section == null || rule.section.isEmpty() ? "Other" : rule.section;
            bySection.computeIfAbsent(section, k -> new ArrayList<>()).add(rule);
        }
        return bySection;
    }

    private void buildChallengesPage() {
        CustomPanelAPI page = page(TAB_CHALLENGES);
        challengeList = new UIRect(0, 0, pageRect.getWidth(), pageRect.getHeight()).shrink(5f);
        float rowWidth = challengeList.getWidth() - 16f;
        TooltipMakerAPI tip = challengeList.beginTooltip(page, true);
        tip.addPara("Have a challenge's rules at full level by day %s of the campaign and keep them until you reach level %s.",
                4f, Misc.getTextColor(), Misc.getHighlightColor(),
                String.valueOf(Challenges.START_DAYS), String.valueOf(Challenges.maxLevel()));
        tip.addSpacer(8f);
        for (ChallengeSpec spec : Challenges.all()) {
            ChallengeRow row = new ChallengeRow(spec, this);
            CustomPanelAPI custom = page.createCustomPanel(rowWidth, ChallengeRow.HEIGHT, row);
            row.init(custom, rowWidth);
            tip.addCustom(custom, 0f);
            rows.add(row);
        }
        challengeList.addTooltip();
    }

    private void showTab(int next) {
        tab = next;
        for (int i = 0; i < pages.length; i++) {
            pages[i].getPosition().inTL(i == tab ? pageRect.getX() : HIDDEN_X, pageRect.getY());
        }
        rulesTab.setChecked(tab == TAB_RULES);
        challengesTab.setChecked(tab == TAB_CHALLENGES);
        setHovered(null, -1);
    }

    // ---- state ----

    private int points() {
        int sum = 0;
        for (Map.Entry<RuleSpec, Integer> e : working.entrySet()) sum -= e.getKey().points(e.getValue());
        return sum;
    }

    private void refresh() {
        int positive = 0, negative = 0;
        for (RuleTile tile : tiles) {
            RuleSpec rule = tile.rule;
            RuleSpec mirror = mirror(rule);
            tile.sync(level(rule), !usable(rule), (mirror != null && level(mirror) > 0) || otherGolden(rule) != null);
            if (level(rule) > 0) {
                if (rule.hard) negative++;
                else positive++;
            }
        }
        for (ChallengeRow row : rows) {
            boolean unlocked = true;
            for (ChallengeSpec.Requirement r : row.spec.requirements) unlocked &= usable(r.rule);
            row.sync(levels, unlocked);
        }
        setCount(positiveCount, positive);
        setCount(negativeCount, negative);

        int points = points();
        pointsLabel.setText("Points: " + points);
        pointsLabel.setHighlight(String.valueOf(points));
        pointsLabel.setHighlightColor(points >= 0 ? Misc.getPositiveHighlightColor() : Misc.getNegativeHighlightColor());
        String total = String.valueOf(positive + negative);
        countLabel.setText("Rules: " + total);
        countLabel.setHighlight(total);
        countLabel.setHighlightColor(Misc.getHighlightColor());
        careerLabel.setText(Balance.ngpInstalled()
                ? "Career points " + Balance.formatMultiplier(Balance.ngpMultiplier(-points)) : "");

        fillSummary(positiveSummary, false);
        fillSummary(negativeSummary, true);
        fillChallengeSummary();

        boolean ok = true;
        reasonRect.removeLatestSubPanel();
        CustomPanelAPI reasonPanel = reasonRect.createSubPanel(panel, null).getPanel();
        TooltipMakerAPI reasons = reasonPanel.createUIElement(reasonRect.getWidth(), reasonRect.getHeight(), true);
        if (Settings.enforceBudget() && points < 0) {
            ok = false;
            reasons.addPara("Over budget by %s. Add negative rules or drop positive ones.", 3f,
                    Misc.getNegativeHighlightColor(), Misc.getHighlightColor(), String.valueOf(-points));
        }
        if (ok) reasons.addPara("None", Misc.getPositiveHighlightColor(), 3f);
        reasonPanel.addUIElement(reasons);
        confirmBtn.setEnabled(ok);

        updateDetail();
    }

    private void setCount(LabelAPI label, int count) {
        label.setText(count + " picked");
        label.setHighlight(String.valueOf(count));
        label.setHighlightColor(Misc.getHighlightColor());
    }

    private void fillSummary(UIRect rect, boolean hard) {
        rect.removeLatestSubPanel();
        CustomPanelAPI sub = rect.createSubPanel(panel, null).getPanel();
        TooltipMakerAPI tip = sub.createUIElement(rect.getWidth(), rect.getHeight(), true);
        tip.addSpacer(5f);
        for (RuleSpec rule : RuleCatalog.all()) {
            int level = level(rule);
            if (level <= 0 || rule.hard != hard) continue;
            TooltipMakerAPI image = tip.beginImageWithText(rule.icon, 32f);
            image.addPara(rule.name + (rule.levels > 1 ? " " + RuleSpec.roman(level) : ""), RuleTile.color(rule), 0f);
            image.addPara(rule.describe(level), Misc.getGrayColor(), 0f);
            tip.addImageWithText(2f);
        }
        sub.addUIElement(tip);
    }

    private void fillChallengeSummary() {
        challengeSummary.removeLatestSubPanel();
        CustomPanelAPI sub = challengeSummary.createSubPanel(panel, null).getPanel();
        TooltipMakerAPI tip = sub.createUIElement(challengeSummary.getWidth(), challengeSummary.getHeight(), true);
        tip.addSpacer(5f);
        for (ChallengeSpec spec : Challenges.all()) {
            if (!Challenges.possible(spec) || !Challenges.conditionsMet(spec, levels)) continue;
            tip.addPara(spec.name + "   " + Challenges.statusText(spec), GOLD, 3f);
            tip.addPara(spec.goalText() + ".", Misc.getGrayColor(), 0f);
        }
        sub.addUIElement(tip);
    }

    // ---- the detail box ----

    private void setHovered(RuleSpec rule, int levelToShow) {
        if (rule == hovered && levelToShow == hoveredLevel) return;
        hovered = rule;
        hoveredLevel = levelToShow;
        updateDetail();
    }

    /** What opens a rule, for the detail box; null when it needs nothing. */
    private static String unlockSource(RuleSpec rule) {
        if (rule.unlock.startsWith("challenge:")) {
            String id = rule.unlock.substring(10);
            for (ChallengeSpec spec : Challenges.all()) if (spec.id.equals(id)) return "the challenge " + spec.name;
            return "a challenge";
        }
        if (rule.achievementId() != null) return Unlocks.label(rule.achievementId());
        if (!rule.requiredMod.isEmpty()) {
            for (String id : rule.requiredMod.split("[|]")) {
                if (Global.getSettings().getModManager().isModEnabled(id.trim())) {
                    return "\"" + Global.getSettings().getModManager().getModSpec(id.trim()).getName() + "\" mod compatibility";
                }
            }
        }
        return null;
    }

    private void updateDetail() {
        detailRect.removeLatestSubPanel();
        CustomPanelAPI sub = detailRect.createSubPanel(panel, null).getPanel();
        TooltipMakerAPI tip = sub.createUIElement(detailRect.getWidth(), detailRect.getHeight(), false);
        RuleSpec rule = hovered;
        if (rule == null) {
            tip.addPara("Left click - Increase rule intensity", Misc.getGrayColor(), 0f);
            tip.addPara("Right click - Diminish rule intensity", Misc.getGrayColor(), 3f);
            sub.addUIElement(tip);
            return;
        }
        String source = unlockSource(rule);
        if (!usable(rule)) {
            tip.setParaOrbitronLarge();
            tip.addPara("[ ? ]", Misc.getGrayColor(), 0f);
            tip.setParaFontDefault();
            if (source != null) tip.addPara("Unlocked by: " + source, Misc.getTextColor(), 10f);
            sub.addUIElement(tip);
            return;
        }

        int picked = level(rule);
        int shown = hoveredLevel > 0 ? hoveredLevel : Math.max(1, picked);
        boolean on = picked > 0 || hoveredLevel > 0;
        Color color = RuleTile.color(rule);
        TooltipMakerAPI image = tip.beginImageWithText(rule.icon, 64f);
        image.setParaOrbitronLarge();
        image.addPara(rule.name, color, 0f);
        image.setParaFontDefault();
        String state = picked <= 0 ? "Off" : rule.levels > 1 ? "Level " + RuleSpec.roman(picked) + " / " + RuleSpec.roman(rule.levels) : "On";
        if (hoveredLevel > 0) state = rule.levels > 1 ? "Needed: level " + RuleSpec.roman(hoveredLevel) : "Needed";
        image.addPara(state, Misc.getGrayColor(), 4f);
        int cost = -rule.points(shown);
        if (cost != 0) {
            image.addPara((cost < 0 ? "Costs %s" : "Gives %s") + (cost == 1 || cost == -1 ? " point" : " points"), 2f,
                    Misc.getGrayColor(), cost < 0 ? Misc.getNegativeHighlightColor() : Misc.getPositiveHighlightColor(),
                    String.valueOf(Math.abs(cost)));
        }
        tip.addImageWithText(0f);

        tip.setParaInsigniaLarge();
        tip.addPara(rule.describe(shown), on ? color : Misc.getGrayColor(), 12f);
        tip.setParaFontDefault();

        RuleSpec mirror = mirror(rule);
        if (mirror != null && level(mirror) > 0) {
            tip.addPara("Conflict with " + mirror.name, Misc.getNegativeHighlightColor(), 10f);
        }
        RuleSpec golden = otherGolden(rule);
        if (golden != null) tip.addPara("Only one golden rule. Picked: " + golden.name, Misc.getNegativeHighlightColor(), 10f);
        else if (rule.golden) tip.addPara("Golden rule: only one can be picked.", GOLD, 10f);
        if (source != null) tip.addPara("Unlocked by: " + source, Misc.getGrayColor(), 10f);
        sub.addUIElement(tip);
    }

    // ---- input ----

    private void setLevel(RuleSpec rule, int next) {
        Set<ChallengeSpec> before = holding();
        if (next <= 0) working.remove(rule);
        else {
            working.put(rule, next);
            RuleSpec mirror = mirror(rule);
            if (mirror != null) working.remove(mirror);
        }
        warnAboutLost(before, rule, next > 0);
    }

    /** Challenges the working rules now satisfy that the player has a stake in. */
    private Set<ChallengeSpec> holding() {
        Set<ChallengeSpec> out = new HashSet<>();
        for (ChallengeSpec spec : Challenges.all()) {
            if (!Challenges.possible(spec) || !Challenges.conditionsMet(spec, levels)) continue;
            if (spec == tracked || Challenges.isRunning(spec)) out.add(spec);
        }
        return out;
    }

    private void warnAboutLost(Set<ChallengeSpec> before, RuleSpec rule, boolean enabling) {
        for (ChallengeSpec spec : before) {
            if (Challenges.conditionsMet(spec, levels) || !warned.add(spec.id)) continue;
            showWarning(Challenges.isRunning(spec)
                            ? "Warning: confirming this fails the challenge for this campaign"
                            : "Warning: " + (enabling ? "Enabling" : "Lowering") + " this rule will not permit you to achieve this challenge",
                    spec.name + "  -  " + rule.name);
            return;
        }
    }

    private void showWarning(String text, String detail) {
        warningRect.removeLatestSubPanel();
        CustomPanelAPI box = warningRect.createSubPanel(panel, new BoxPlugin()).getPanel();
        TooltipMakerAPI tip = box.createUIElement(warningRect.getWidth() - 30f, warningRect.getHeight() - 20f, false);
        tip.addPara(text, Misc.getNegativeHighlightColor(), 0f);
        tip.addPara(detail, Misc.getTextColor(), 8f);
        tip.addButton("OK", "warning_ok", 100f, 26f, 14f);
        box.addUIElement(tip).inTL(15f, 12f);
    }

    @Override
    public void tilePressed(RuleTile tile) {
        RuleSpec rule = tile.rule;
        int level = level(rule);
        int next = level < rule.levels ? level + 1 : 0;
        setLevel(rule, next);
        sound(rule, next > 0);
        refresh();
    }

    private void lower(RuleTile tile) {
        int level = level(tile.rule);
        if (level <= 0 || !tile.isEnabled()) return;
        setLevel(tile.rule, level - 1);
        sound(tile.rule, false);
        refresh();
    }

    private static void sound(RuleSpec rule, boolean pickup) {
        Global.getSoundPlayer().playUISound((rule.hard ? "rbr_negative_" : "rbr_positive_") + (pickup ? "pickup" : "drop"), 1f, 1f);
    }

    @Override
    public void enableRequired(ChallengeSpec spec) {
        working.clear();
        for (ChallengeSpec.Requirement r : spec.requirements) working.put(r.rule, r.level);
        tracked = spec;
        warned.remove(spec.id);
        showTab(TAB_RULES);
        refresh();
    }

    @Override
    public void buttonPressed(Object buttonId) {
        if (closed) return;
        String id = String.valueOf(buttonId);
        if (id.startsWith("preset_") && id.length() == 8 && Character.isDigit(id.charAt(7))) {
            selectPreset(id.charAt(7) - '0');
            return;
        }
        switch (id) {
            case "preset_save":
                savePreset();
                break;
            case "preset_load":
                loadPreset();
                break;
            case "tab_rules":
                showTab(TAB_RULES);
                break;
            case "tab_challenges":
                showTab(TAB_CHALLENGES);
                break;
            case "clear_positive":
            case "clear_negative": {
                boolean hard = id.equals("clear_negative");
                Set<ChallengeSpec> before = holding();
                RuleSpec last = null;
                for (RuleSpec rule : new ArrayList<>(working.keySet())) {
                    if (rule.hard != hard) continue;
                    working.remove(rule);
                    last = rule;
                }
                if (last != null) warnAboutLost(before, last, false);
                refresh();
                break;
            }
            case "warning_ok":
                warningRect.removeLatestSubPanel();
                break;
            case "confirm":
                apply();
                exit();
                break;
            case "cancel":
                exit();
                break;
            default:
                break;
        }
    }

    private void apply() {
        // One challenge check for the final set, so an in-between state cannot fail a running challenge.
        Challenges.beginBatch();
        try {
            for (RuleSpec rule : RuleCatalog.all()) {
                if (level(rule) == 0 && RuleState.level(rule) > 0) RuleManager.setLevel(rule, 0);
            }
            for (RuleSpec rule : RuleCatalog.all()) {
                int level = level(rule);
                if (level > 0 && RuleState.level(rule) != level) RuleManager.setLevel(rule, level);
            }
        } finally {
            Challenges.endBatch();
        }
    }

    private void exit() {
        if (closed) return;
        closed = true;
        callbacks.dismissDialog();
    }

    private boolean inside(UIRect rect, float x, float y) {
        float left = pos.getX() + pageRect.getX() + rect.getX();
        float top = pos.getY() + pos.getHeight() - pageRect.getY() - rect.getY();
        return x >= left && x <= left + rect.getWidth() && y <= top && y >= top - rect.getHeight();
    }

    @Override
    public void advance(float amount) {
        if (closed || pos == null || !Mouse.isCreated()) return;
        float scale = Global.getSettings().getScreenScaleMult();
        if (scale <= 0f) return;
        float x = Mouse.getX() / scale, y = Mouse.getY() / scale;
        boolean rightDown = Mouse.isButtonDown(1);
        boolean rightClick = rightDown && !wasRightDown;
        wasRightDown = rightDown;

        if (tab == TAB_RULES) {
            RuleTile over = null;
            UIRect grid = inside(positiveGrid, x, y) ? positiveGrid : inside(negativeGrid, x, y) ? negativeGrid : null;
            if (grid != null) {
                for (RuleTile tile : tiles) {
                    if (tileGrid.get(tile) == grid && tile.contains(x, y)) {
                        over = tile;
                        break;
                    }
                }
            }
            if (over != null && rightClick) lower(over);
            setHovered(over == null ? null : over.rule, -1);
        } else {
            ChallengeSpec.Requirement over = null;
            if (inside(challengeList, x, y)) {
                for (ChallengeRow row : rows) {
                    over = row.requirementAt(x, y);
                    if (over != null) break;
                }
            }
            if (over == null) setHovered(null, -1);
            else setHovered(over.rule, over.level);
        }
    }

    @Override
    public void positionChanged(PositionAPI position) {
        pos = position;
    }

    @Override
    public void renderBelow(float alphaMult) {
        if (pos == null) return;
        // The plain backdrop under UNGP's animated one: dark teal at the top, black at the bottom.
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(0.02f, 0.09f, 0.1f, alphaMult);
        GL11.glVertex2f(pos.getX(), pos.getY() + pos.getHeight());
        GL11.glVertex2f(pos.getX() + pos.getWidth(), pos.getY() + pos.getHeight());
        GL11.glColor4f(0f, 0f, 0f, alphaMult);
        GL11.glVertex2f(pos.getX() + pos.getWidth(), pos.getY());
        GL11.glVertex2f(pos.getX(), pos.getY());
        GL11.glEnd();
        GL11.glPopAttrib();
    }
}
