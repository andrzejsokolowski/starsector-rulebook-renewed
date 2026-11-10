package rulebookrenewed.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate.DialogCallbacks;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;

import rulebookrenewed.Balance;
import rulebookrenewed.ChallengeSpec;
import rulebookrenewed.Challenges;
import rulebookrenewed.Feats;
import rulebookrenewed.Ids;
import rulebookrenewed.RuleBookIntel;
import rulebookrenewed.RuleCatalog;
import rulebookrenewed.RuleManager;
import rulebookrenewed.RuleSpec;
import rulebookrenewed.RuleState;
import rulebookrenewed.Unlocks;

/**
 * The rule picker: grids of tiles on the left, a side panel on the right.
 *
 * Rules tab: helpful rules in the top grid, hard rules in the bottom grid. Left click on a tile raises
 * its level, right click lowers it; pips on the tile show the level. A hard rule and its helpful mirror
 * are never on together (RuleManager.setLevel switches the other side off). Challenges tab: one tile per
 * challenge; a click selects it and the side panel's button takes it on or gives it up. Feats tab: one
 * tile per feat. The side panel holds the balance meter, a summary, and the hovered tile's details.
 *
 * Each tab is one container panel built once. A tab switch moves the other containers off screen, so no
 * label of another tab can stay behind. Every grid is a scrolling TooltipMakerAPI holding a content panel
 * with the tile labels; tiles and buttons are drawn and hit-tested by this panel (as in Gambling Den), and
 * each frame reads back where the scrolled content really is on screen.
 */
public final class RulePickerPanel extends BaseCustomUIPanelPlugin {

    private static final float PAD = 16f, SIDE_W = 380f, TAB_H = 28f, HEADER_H = 30f;
    private static final float TILE_W = 112f, TILE_H = 120f, TILE_GAP = 8f, GRID_PAD = 8f, SCROLLBAR_W = 22f;
    private static final float ICON = 52f, DETAIL_ICON = 64f;
    private static final float PIP_W = 14f, PIP_H = 5f, PIP_GAP = 4f;
    private static final float HIDDEN_X = -20000f;
    private static final int METER_RANGE = 100;

    private static final int TAB_RULES = 0, TAB_CHALLENGES = 1, TAB_FEATS = 2;

    private static final Color BG = new Color(10, 13, 19);
    private static final Color PANEL = new Color(20, 25, 34);
    private static final Color TILE_OFF = new Color(24, 30, 40);
    private static final Color TILE_LOCKED = new Color(15, 18, 24);
    private static final Color PIP_OFF = new Color(48, 56, 68);
    private static final Color GOLD = new Color(235, 185, 100);
    private static final Color DIM = new Color(150, 157, 173);
    private static final Color TEXT = new Color(220, 224, 232);

    private static final class Btn {
        final String action;
        final float x, y, w, h;
        /** The tab the button belongs to, or -1 for every tab. */
        final int tab;
        LabelAPI label;
        boolean visible = true, enabled = true, checked, hover;

        Btn(String action, float x, float y, float w, float h, int tab) {
            this.action = action;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.tab = tab;
        }
    }

    /** One scrolling grid of tiles. x, y, w, h: the grid's box in panel coordinates (top-down). */
    private static final class Grid {
        final int tab;
        final float x, y, w, h;
        final List<Tile> tiles = new ArrayList<>();
        CustomPanelAPI content;
        float contentLeft, contentTop;

        Grid(int tab, float x, float y, float w, float h) {
            this.tab = tab;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            contentLeft = x;
            contentTop = y;
        }
    }

    /** One tile. x, y: its top left in grid content coordinates. Exactly one of rule, challenge, feat is set. */
    private static final class Tile {
        final Grid grid;
        final float x, y;
        RuleSpec rule;
        ChallengeSpec challenge;
        Feats.Feat feat;
        String iconPath;
        LabelAPI name, status;

        Tile(Grid grid, float x, float y) {
            this.grid = grid;
            this.x = x;
            this.y = y;
        }
    }

    private final float panelW, panelH, sideX, leftW, pageTop;
    private final List<Btn> buttons = new ArrayList<>();
    private final List<Grid> grids = new ArrayList<>();
    private final CustomPanelAPI[] pages = new CustomPanelAPI[3];
    private final Map<String, SpriteAPI> sprites = new HashMap<>();
    private CustomPanelAPI panel;
    private DialogCallbacks callbacks;
    private PositionAPI position;

    private LabelAPI balance, hardSummary, helpfulSummary, career, hint;
    private LabelAPI detailTitle, detailKind, detailBody, detailFlavor;
    private LabelAPI helpfulCount, hardCount, challengeCount, featCount;
    private Btn challengeAction;
    private float detailTop, detailBottom, meterX, meterY, meterW;

    private int tab = TAB_RULES;
    private Tile hovered, shown, selectedChallenge;
    private boolean dismissed, wasLeftDown, wasRightDown;

    public RulePickerPanel() {
        float screenW = Global.getSettings().getScreenWidth();
        float screenH = Global.getSettings().getScreenHeight();
        panelW = Math.max(Math.min(1720f, screenW - 80f), Math.min(1100f, screenW - 10f));
        panelH = Math.max(Math.min(980f, screenH - 80f), Math.min(680f, screenH - 10f));
        sideX = panelW - PAD - SIDE_W;
        leftW = sideX - PAD - PAD;
        pageTop = PAD + TAB_H + 8f;
    }

    public float width() {
        return panelW;
    }

    public float height() {
        return panelH;
    }

    public void init(CustomPanelAPI panel, DialogCallbacks callbacks) {
        this.panel = panel;
        this.callbacks = callbacks;
        wasLeftDown = Mouse.isCreated() && Mouse.isButtonDown(0);
        wasRightDown = Mouse.isCreated() && Mouse.isButtonDown(1);

        button(panel, "tab:" + TAB_RULES, "Rules", PAD, PAD, 120f, TAB_H, -1);
        button(panel, "tab:" + TAB_CHALLENGES, "Challenges", PAD + 126f, PAD, 140f, TAB_H, -1);
        button(panel, "tab:" + TAB_FEATS, "Feats", PAD + 272f, PAD, 120f, TAB_H, -1);

        buildSide();
        buildRulesPage();
        buildChallengesPage();
        buildFeatsPage();
        showTab(TAB_RULES);
    }

    // ---- building ----

    private void buildSide() {
        float x = sideX + 12f, w = SIDE_W - 24f;
        label(panel, "THE RULE BOOK", GOLD, sideX, PAD + 2f, SIDE_W, 26f, Fonts.ORBITRON_20AA, Alignment.MID);
        balance = label(panel, "", TEXT, x, PAD + 34f, w, 20f, Fonts.DEFAULT_SMALL, Alignment.MID);

        meterX = sideX + 62f;
        meterW = SIDE_W - 124f;
        meterY = PAD + 64f;
        label(panel, "Harder", RuleBookIntel.hardColor(), sideX + 4f, meterY - 5f, 54f, 20f, Fonts.DEFAULT_SMALL, Alignment.RMID);
        label(panel, "Easier", RuleBookIntel.helpfulColor(), meterX + meterW + 4f, meterY - 5f, 54f, 20f, Fonts.DEFAULT_SMALL, Alignment.LMID);

        float y = meterY + 24f;
        hardSummary = label(panel, "", RuleBookIntel.hardColor(), x, y, w / 2f, 20f, Fonts.DEFAULT_SMALL, Alignment.LMID);
        helpfulSummary = label(panel, "", RuleBookIntel.helpfulColor(), x + w / 2f, y, w / 2f, 20f, Fonts.DEFAULT_SMALL, Alignment.RMID);
        career = label(panel, "", DIM, x, y + 20f, w, 20f, Fonts.DEFAULT_SMALL, Alignment.MID);

        detailTop = y + 48f;
        detailBottom = panelH - PAD - 36f - 34f;
        float textX = x + DETAIL_ICON + 12f, textW = w - DETAIL_ICON - 12f;
        detailTitle = label(panel, "", GOLD, textX, detailTop + 12f, textW, 24f, Fonts.INSIGNIA_LARGE, Alignment.LMID);
        detailKind = label(panel, "", DIM, textX, detailTop + 40f, textW, 20f, Fonts.DEFAULT_SMALL, Alignment.LMID);
        detailBody = label(panel, "", TEXT, x, detailTop + DETAIL_ICON + 22f, w, 20f, Fonts.DEFAULT_SMALL, Alignment.TL);
        detailFlavor = label(panel, "", DIM, x, detailBottom - 34f, w, 20f, Fonts.DEFAULT_SMALL, Alignment.TL);
        challengeAction = button(panel, "challenge", "Take it on", x, detailBottom - 76f, w, 32f, TAB_CHALLENGES);

        hint = label(panel, "", DIM, sideX, panelH - PAD - 36f - 26f, SIDE_W, 18f, Fonts.DEFAULT_SMALL, Alignment.MID);
        button(panel, "close", "Close", sideX, panelH - PAD - 36f, SIDE_W, 36f, -1);
    }

    private CustomPanelAPI page(int tab) {
        CustomPanelAPI page = panel.createCustomPanel(leftW, panelH - pageTop - PAD, null);
        panel.addComponent(page).inTL(PAD, pageTop);
        pages[tab] = page;
        return page;
    }

    /** A header bar over a grid: title on the left, a count next to it. Returns the count label. */
    private LabelAPI header(CustomPanelAPI page, String title, Color color, float top) {
        label(page, title, color, 10f, top + 6f, 240f, 18f, Fonts.ORBITRON_12, Alignment.LMID);
        return label(page, "", DIM, 220f, top + 6f, 300f, 18f, Fonts.DEFAULT_SMALL, Alignment.LMID);
    }

    private void buildRulesPage() {
        CustomPanelAPI page = page(TAB_RULES);
        float pageH = panelH - pageTop - PAD;
        float block = (pageH - 10f) / 2f;

        helpfulCount = header(page, "HELPFUL RULES", RuleBookIntel.helpfulColor(), 0f);
        button(page, "clear:helpful", "Clear all", leftW - 110f, 3f, 104f, HEADER_H - 6f, TAB_RULES);
        Grid helpful = grid(page, TAB_RULES, HEADER_H, block - HEADER_H);
        for (RuleSpec rule : RuleCatalog.ofType(false)) addRuleTile(helpful, rule);
        finish(page, helpful);

        float top = block + 10f;
        hardCount = header(page, "HARD RULES", RuleBookIntel.hardColor(), top);
        button(page, "clear:hard", "Clear all", leftW - 110f, top + 3f, 104f, HEADER_H - 6f, TAB_RULES);
        Grid hard = grid(page, TAB_RULES, top + HEADER_H, block - HEADER_H);
        for (RuleSpec rule : RuleCatalog.ofType(true)) addRuleTile(hard, rule);
        finish(page, hard);
    }

    private void buildChallengesPage() {
        CustomPanelAPI page = page(TAB_CHALLENGES);
        challengeCount = header(page, "CHALLENGES", GOLD, 0f);
        Grid grid = grid(page, TAB_CHALLENGES, HEADER_H, panelH - pageTop - PAD - HEADER_H);
        for (ChallengeSpec spec : Challenges.all()) {
            Tile tile = nextTile(grid);
            tile.challenge = spec;
            List<RuleSpec> rewards = Challenges.rewards(spec);
            tile.iconPath = rewards.isEmpty() ? Ids.BOOK_ICON : rewards.get(0).icon;
        }
        finish(page, grid);
    }

    private void buildFeatsPage() {
        CustomPanelAPI page = page(TAB_FEATS);
        featCount = header(page, "FEATS", GOLD, 0f);
        Grid grid = grid(page, TAB_FEATS, HEADER_H, panelH - pageTop - PAD - HEADER_H);
        for (Feats.Feat feat : Feats.all()) {
            Tile tile = nextTile(grid);
            tile.feat = feat;
            RuleSpec opens = firstRuleOpenedBy(feat);
            tile.iconPath = opens != null ? opens.icon : Ids.BOOK_ICON;
        }
        finish(page, grid);
    }

    /** A grid box at the given top inside a page; tiles are added with nextTile, then finish builds it. */
    private Grid grid(CustomPanelAPI page, int tab, float topInPage, float h) {
        Grid grid = new Grid(tab, PAD, pageTop + topInPage, leftW, h);
        grids.add(grid);
        return grid;
    }

    private int columns(Grid grid) {
        return Math.max(1, (int) ((grid.w - GRID_PAD * 2f - SCROLLBAR_W + TILE_GAP) / (TILE_W + TILE_GAP)));
    }

    private Tile nextTile(Grid grid) {
        int cols = columns(grid);
        int index = grid.tiles.size();
        float used = cols * (TILE_W + TILE_GAP) - TILE_GAP;
        float left = Math.max(GRID_PAD, (grid.w - SCROLLBAR_W - used) / 2f);
        Tile tile = new Tile(grid, left + (index % cols) * (TILE_W + TILE_GAP),
                GRID_PAD + (index / cols) * (TILE_H + TILE_GAP));
        grid.tiles.add(tile);
        return tile;
    }

    private void addRuleTile(Grid grid, RuleSpec rule) {
        Tile tile = nextTile(grid);
        tile.rule = rule;
        tile.iconPath = rule.icon;
    }

    private void finish(CustomPanelAPI page, Grid grid) {
        int cols = columns(grid);
        int rows = (grid.tiles.size() + cols - 1) / cols;
        float contentH = Math.max(grid.h, GRID_PAD * 2f + rows * (TILE_H + TILE_GAP) - TILE_GAP);
        TooltipMakerAPI scroller = page.createUIElement(grid.w, grid.h, true);
        grid.content = page.createCustomPanel(grid.w, contentH, null);
        for (Tile tile : grid.tiles) {
            tile.name = Global.getSettings().createLabel(tileName(tile), Fonts.DEFAULT_SMALL);
            tile.name.setAlignment(Alignment.MID);
            grid.content.addComponent((UIComponentAPI) tile.name);
            tile.name.autoSizeToWidth(TILE_W - 10f);
            float nameH = tile.name.getPosition().getHeight();
            tile.name.getPosition().inTL(tile.x + 5f, tile.y + Math.max(3f, 4f + (32f - nameH) / 2f));
            tile.status = label(grid.content, "", TEXT, tile.x + 4f, tile.y + TILE_H - 31f, TILE_W - 8f, 18f,
                    Fonts.DEFAULT_SMALL, Alignment.MID);
        }
        scroller.addCustom(grid.content, 0f);
        scroller.setHeightSoFar(contentH);
        page.addUIElement(scroller).inTL(grid.x - PAD, grid.y - pageTop);
    }

    private static String tileName(Tile tile) {
        if (tile.rule != null) return tile.rule.name;
        if (tile.challenge != null) return tile.challenge.name;
        return tile.feat.name;
    }

    private LabelAPI label(CustomPanelAPI parent, String text, Color color, float x, float y, float w, float h,
                           String font, Alignment align) {
        LabelAPI label = Global.getSettings().createLabel(text, font);
        label.setColor(color);
        label.setAlignment(align);
        parent.addComponent((UIComponentAPI) label).inTL(x, y).setSize(w, h);
        return label;
    }

    /**
     * A self-drawn button. x and y are panel coordinates; a button on a tab page gets its label in that page
     * (so it moves away with the page) at the matching page position.
     */
    private Btn button(CustomPanelAPI parent, String action, String text, float x, float y, float w, float h, int tab) {
        boolean onPage = parent != panel;
        float px = onPage ? PAD + x : x, py = onPage ? pageTop + y : y;
        Btn b = new Btn(action, px, py, w, h, tab);
        b.label = label(parent, text, Color.WHITE, x, y + (h - 18f) / 2f, w, 18f, Fonts.DEFAULT_SMALL, Alignment.MID);
        buttons.add(b);
        return b;
    }

    // ---- tabs ----

    private void showTab(int next) {
        tab = next;
        for (int i = 0; i < pages.length; i++) {
            if (pages[i] != null) pages[i].getPosition().inTL(i == tab ? PAD : HIDDEN_X, pageTop);
        }
        hovered = null;
        shown = tab == TAB_CHALLENGES ? selectedChallenge : null;
        hint.setText(tab == TAB_RULES ? "Left click raises a rule, right click lowers it."
                : tab == TAB_CHALLENGES ? "Click a challenge to pick it." : "");
        refresh();
    }

    // ---- filling in ----

    private void refresh() {
        int net = Balance.net();
        balance.setText(Balance.describe(net));
        balance.setColor(RuleBookIntel.balanceColor(net));

        int hardOn = 0, hardPoints = 0, helpOn = 0, helpPoints = 0;
        for (RuleSpec rule : RuleCatalog.all()) {
            int level = RuleState.level(rule);
            if (level <= 0) continue;
            if (rule.hard) {
                hardOn++;
                hardPoints += rule.points(level);
            } else {
                helpOn++;
                helpPoints += rule.points(level);
            }
        }
        hardSummary.setText("Hard: " + hardOn + " on, " + hardPoints);
        helpfulSummary.setText("Helpful: " + helpOn + " on, +" + helpPoints);
        career.setText(Balance.ngpInstalled() ? "Career points " + Balance.formatMultiplier(Balance.ngpMultiplier(net)) : "");
        helpfulCount.setText(helpOn + " on");
        hardCount.setText(hardOn + " on");

        int done = 0;
        for (ChallengeSpec spec : Challenges.all()) if (Challenges.isComplete(spec)) done++;
        challengeCount.setText(done + " of " + Challenges.all().size() + " done");
        int feats = 0;
        for (Feats.Feat feat : Feats.all()) if (Unlocks.hasFeat(feat.id)) feats++;
        featCount.setText(feats + " of " + Feats.all().size() + " done");

        for (Btn b : buttons) {
            if (b.action.startsWith("tab:")) b.checked = Integer.parseInt(b.action.substring(4)) == tab;
        }
        for (Grid grid : grids) {
            for (Tile tile : grid.tiles) fillTile(tile);
        }
        refreshDetail();
    }

    private static Color typeColor(RuleSpec rule) {
        return rule.hard ? RuleBookIntel.hardColor() : RuleBookIntel.helpfulColor();
    }

    private static boolean usable(RuleSpec rule) {
        return Unlocks.isUnlocked(rule) || RuleState.level(rule) > 0;
    }

    private static String signed(int points) {
        return points > 0 ? "+" + points : String.valueOf(points);
    }

    private void fillTile(Tile tile) {
        if (tile.rule != null) {
            RuleSpec rule = tile.rule;
            int level = RuleState.level(rule);
            if (!usable(rule)) {
                tile.name.setColor(DIM.darker());
                tile.status.setText("Locked");
                tile.status.setColor(DIM.darker());
            } else if (level > 0) {
                tile.name.setColor(Color.WHITE);
                tile.status.setText(signed(rule.points(level)));
                tile.status.setColor(typeColor(rule));
            } else {
                tile.name.setColor(TEXT);
                tile.status.setText(signed(rule.points(1)));
                tile.status.setColor(DIM);
            }
        } else if (tile.challenge != null) {
            ChallengeSpec spec = tile.challenge;
            tile.name.setColor(Challenges.isComplete(spec) ? RuleBookIntel.helpfulColor() : TEXT);
            if (Challenges.isComplete(spec)) {
                tile.status.setText("Done");
                tile.status.setColor(RuleBookIntel.helpfulColor());
            } else if (Challenges.isActive(spec)) {
                tile.status.setText("Under way");
                tile.status.setColor(GOLD);
            } else {
                boolean ready = Challenges.whyNot(spec) == null;
                tile.status.setText(ready ? "Ready" : "Not ready");
                tile.status.setColor(ready ? TEXT : DIM.darker());
            }
        } else {
            Feats.Feat feat = tile.feat;
            boolean done = Unlocks.hasFeat(feat.id);
            tile.name.setColor(done ? RuleBookIntel.helpfulColor() : TEXT);
            String status = done ? "Done" : "Not yet";
            if (feat.repeatable && Global.getSector() != null && feat.count() > 0) {
                status = feat.count() + " this campaign";
            }
            tile.status.setText(status);
            tile.status.setColor(done ? RuleBookIntel.helpfulColor() : DIM);
        }
    }

    private static RuleSpec mirror(RuleSpec rule) {
        for (RuleSpec other : RuleCatalog.all()) {
            if (other != rule && other.hard != rule.hard && other.pair.equals(rule.pair)) return other;
        }
        return null;
    }

    private static RuleSpec firstRuleOpenedBy(Feats.Feat feat) {
        for (RuleSpec rule : RuleCatalog.all()) if (feat.id.equals(rule.featId())) return rule;
        return null;
    }

    private void refreshDetail() {
        Tile tile = shown;
        challengeAction.visible = false;
        if (tile == null) {
            detailTitle.setText("");
            detailKind.setText("");
            setBody(tab == TAB_RULES ? "Point at a rule to see what it does."
                    : tab == TAB_CHALLENGES ? "Point at a challenge to see its rules and goal."
                    : "Point at a feat to see what it opens.", DIM);
            setFlavor("");
            return;
        }
        if (tile.rule != null) ruleDetail(tile.rule);
        else if (tile.challenge != null) challengeDetail(tile.challenge);
        else featDetail(tile.feat);

        ChallengeSpec selected = selectedChallenge != null ? selectedChallenge.challenge : null;
        if (tab == TAB_CHALLENGES && selected != null && !Challenges.isComplete(selected)) {
            boolean active = Challenges.isActive(selected);
            challengeAction.visible = true;
            challengeAction.enabled = active || Challenges.whyNot(selected) == null;
            challengeAction.checked = active;
            challengeAction.label.setText((active ? "Give up " : "Take on ") + selected.name);
            challengeAction.label.setColor(challengeAction.enabled ? Color.WHITE : DIM.darker());
        }
        if (!challengeAction.visible) challengeAction.label.setText("");
    }

    private void setBody(String text, Color color) {
        detailBody.setText(text);
        detailBody.setColor(color);
        detailBody.autoSizeToWidth(SIDE_W - 24f);
    }

    /** The flavor line sits at the bottom of the detail box, however many lines it wraps to. */
    private void setFlavor(String text) {
        detailFlavor.setText(text);
        detailFlavor.autoSizeToWidth(SIDE_W - 24f);
        float h = detailFlavor.getPosition().getHeight();
        detailFlavor.getPosition().inTL(sideX + 12f, detailBottom - 10f - h);
    }

    private void ruleDetail(RuleSpec rule) {
        int level = RuleState.level(rule);
        detailTitle.setText(rule.name);
        detailTitle.setColor(usable(rule) ? typeColor(rule) : DIM);
        String kind = rule.hard ? "Hard rule" : "Helpful rule";
        if (!usable(rule)) kind += ", locked";
        else if (level > 0) kind += ", level " + RuleSpec.roman(level) + (rule.levels > 1 ? " of " + RuleSpec.roman(rule.levels) : "");
        else kind += ", off";
        detailKind.setText(kind);
        detailKind.setColor(DIM);

        StringBuilder body = new StringBuilder(rule.describeAllLevels());
        if (level > 0 && rule.levels > 1) body.append("\n\nNow: ").append(rule.describe(level));
        body.append("\n\nPoints: ").append(rule.pointsText());
        RuleSpec mirror = mirror(rule);
        if (mirror != null) body.append("\nTurns off ").append(mirror.name).append('.');
        if (!usable(rule) && !rule.unlockHint.isEmpty()) body.append("\n\n").append(rule.unlockHint);
        for (ChallengeSpec spec : Challenges.activeRequiring(rule)) {
            body.append("\n\nHeld at ").append(RuleSpec.roman(spec.levelFor(rule))).append(" by ").append(spec.name)
                    .append(". Lowering it ends the challenge.");
        }
        setBody(body.toString(), TEXT);
        setFlavor(rule.flavor);
    }

    private void challengeDetail(ChallengeSpec spec) {
        detailTitle.setText(spec.name);
        detailTitle.setColor(GOLD);
        String state = Challenges.isComplete(spec) ? "Challenge, done"
                : Challenges.isActive(spec) ? "Challenge, under way: " + Challenges.progressText(spec) : "Challenge";
        detailKind.setText(state);
        detailKind.setColor(Challenges.isComplete(spec) ? RuleBookIntel.helpfulColor() : DIM);

        StringBuilder body = new StringBuilder(spec.description);
        body.append("\n\nRules: ").append(spec.requirementsText()).append('.');
        body.append("\nGoal: ").append(spec.goalText()).append('.');
        body.append('\n').append(spec.limitText()).append('.');
        for (RuleSpec reward : Challenges.rewards(spec)) body.append("\nReward: ").append(reward.name).append('.');
        String why = Challenges.whyNot(spec);
        if (why != null && !Challenges.isActive(spec) && !Challenges.isComplete(spec)) body.append("\n\n").append(why).append('.');
        setBody(body.toString(), TEXT);
        setFlavor(spec.flavor);
    }

    private void featDetail(Feats.Feat feat) {
        boolean done = Unlocks.hasFeat(feat.id);
        detailTitle.setText(feat.name);
        detailTitle.setColor(done ? RuleBookIntel.helpfulColor() : GOLD);
        detailKind.setText(done ? "Feat, done" : "Feat");
        detailKind.setColor(done ? RuleBookIntel.helpfulColor() : DIM);
        StringBuilder body = new StringBuilder();
        for (RuleSpec rule : RuleCatalog.all()) {
            if (feat.id.equals(rule.featId())) body.append(body.length() > 0 ? "\n" : "").append("Opens ").append(rule.name).append('.');
        }
        for (ChallengeSpec spec : Challenges.all()) {
            if (spec.goalFeat == feat) body.append(body.length() > 0 ? "\n" : "").append("Goal of ").append(spec.name).append('.');
        }
        if (feat.repeatable && Global.getSector() != null) {
            body.append(body.length() > 0 ? "\n\n" : "").append("This campaign: ").append(feat.count()).append('.');
        }
        setBody(body.toString(), TEXT);
        setFlavor("");
    }

    // ---- input ----

    private void act(String action) {
        if (dismissed) return;
        if (action.equals("close")) {
            close();
            return;
        }
        if (action.startsWith("tab:")) {
            int next = Integer.parseInt(action.substring(4));
            if (next != tab) {
                click();
                showTab(next);
            }
            return;
        }
        if (action.startsWith("clear:")) {
            boolean hard = action.endsWith("hard");
            for (RuleSpec rule : RuleCatalog.ofType(hard)) {
                // Rules a running challenge holds stay; lowering them would end the challenge.
                if (RuleState.level(rule) > 0 && Challenges.activeRequiring(rule).isEmpty()) RuleManager.setLevel(rule, 0);
            }
            click();
            refresh();
            return;
        }
        if (action.equals("challenge") && selectedChallenge != null) {
            ChallengeSpec spec = selectedChallenge.challenge;
            if (Challenges.isActive(spec)) Challenges.giveUp(spec);
            else Challenges.start(spec);
            click();
            refresh();
        }
    }

    private void clickTile(Tile tile, boolean right) {
        if (tile.rule != null) {
            RuleSpec rule = tile.rule;
            if (!usable(rule)) return;
            int level = RuleState.level(rule);
            int next;
            if (right) next = Math.max(0, level - 1);
            else if (level < rule.levels) next = level + 1;
            // Past the top a click turns the rule off, except when a challenge holds it.
            else next = Challenges.activeRequiring(rule).isEmpty() ? 0 : level;
            if (next == level) return;
            RuleManager.setLevel(rule, next); // switches the mirror off
            click();
            refresh();
            return;
        }
        if (tile.challenge != null && !right) {
            selectedChallenge = tile;
            shown = tile;
            click();
            refresh();
        }
    }

    private static void click() {
        Global.getSoundPlayer().playUISound("ui_button_pressed", 1f, 0.6f);
    }

    private void close() {
        if (dismissed) return;
        dismissed = true;
        if (callbacks != null) callbacks.dismissDialog();
    }

    /** Reads where each visible grid's scrolled content sits now, in this panel's top-down coordinates. */
    private void updateGrids() {
        if (position == null) return;
        for (Grid grid : grids) {
            if (grid.tab != tab || grid.content == null) continue;
            PositionAPI p = grid.content.getPosition();
            if (p == null) continue;
            grid.contentLeft = p.getX() - position.getX();
            grid.contentTop = (position.getY() + panelH) - (p.getY() + p.getHeight());
        }
    }

    private static boolean inBox(float mx, float my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private boolean buttonShown(Btn b) {
        return b.visible && (b.tab < 0 || b.tab == tab);
    }

    private Tile tileAt(float mx, float my) {
        for (Grid grid : grids) {
            if (grid.tab != tab || !inBox(mx, my, grid.x, grid.y, grid.w - SCROLLBAR_W, grid.h)) continue;
            for (Tile tile : grid.tiles) {
                if (inBox(mx, my, grid.contentLeft + tile.x, grid.contentTop + tile.y, TILE_W, TILE_H)) return tile;
            }
        }
        return null;
    }

    private void pointer(float mx, float my, boolean leftDown, boolean rightDown) {
        boolean left = leftDown && !wasLeftDown, right = rightDown && !wasRightDown;
        wasLeftDown = leftDown;
        wasRightDown = rightDown;
        for (Btn b : buttons) b.hover = buttonShown(b) && b.enabled && inBox(mx, my, b.x, b.y, b.w, b.h);

        Tile over = tileAt(mx, my);
        if (over != hovered) {
            hovered = over;
            Tile next = over != null ? over : tab == TAB_CHALLENGES ? selectedChallenge : shown;
            if (next != shown) {
                shown = next;
                refreshDetail();
            }
        }

        if (dismissed || !(left || right)) return;
        if (over != null) {
            clickTile(over, right);
            return;
        }
        if (!left) return;
        for (Btn b : new ArrayList<>(buttons)) {
            if (b.hover) {
                act(b.action);
                return;
            }
        }
    }

    @Override
    public void advance(float amount) {
        updateGrids();
        if (dismissed || position == null || !Mouse.isCreated()) return;
        float scale = Global.getSettings().getScreenScaleMult();
        if (scale <= 0f) return;
        pointer(Mouse.getX() / scale - position.getX(), position.getY() + panelH - Mouse.getY() / scale,
                Mouse.isButtonDown(0), Mouse.isButtonDown(1));
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

    // ---- drawing ----

    private float x(float n) {
        return position.getX() + n;
    }

    private float y(float topDown) {
        return position.getY() + panelH - topDown;
    }

    private void rect(float left, float top, float w, float h, Color c, float alpha) {
        GLDraw.quad(x(left), y(top + h), w, h, c, alpha);
    }

    private void frame(float left, float top, float w, float h, Color c, float thickness, float alpha) {
        GLDraw.frame(x(left), y(top + h), w, h, c, thickness, alpha);
    }

    private SpriteAPI sprite(String path) {
        if (path == null) return null;
        SpriteAPI s = sprites.get(path);
        if (s == null) {
            s = Global.getSettings().getSprite(path);
            sprites.put(path, s);
        }
        return s;
    }

    private void drawIcon(String path, float left, float top, float size, Color tint, float alpha) {
        SpriteAPI s = sprite(path);
        if (s == null || s.getTextureId() == 0) return;
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        s.setSize(size, size);
        s.setColor(tint);
        s.setAlphaMult(alpha);
        s.render(x(left), y(top + size));
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void renderBelow(float alpha) {
        if (position == null) return;
        updateGrids();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT
                | GL11.GL_TEXTURE_BIT | GL11.GL_LINE_BIT | GL11.GL_SCISSOR_BIT);
        try {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            rect(0f, 0f, panelW, panelH, BG, alpha);
            frame(0f, 0f, panelW, panelH, GOLD, 2f, alpha * 0.6f);

            // Side panel.
            rect(sideX, PAD, SIDE_W, panelH - PAD * 2f, PANEL, alpha);
            frame(sideX, PAD, SIDE_W, panelH - PAD * 2f, DIM, 1f, alpha * 0.3f);
            drawMeter(alpha);
            rect(sideX + 8f, detailTop, SIDE_W - 16f, detailBottom - detailTop, BG, alpha * 0.6f);
            frame(sideX + 8f, detailTop, SIDE_W - 16f, detailBottom - detailTop, DIM, 1f, alpha * 0.25f);

            for (Grid grid : grids) if (grid.tab == tab) drawGrid(grid, alpha);
            for (Btn b : buttons) if (buttonShown(b)) drawButton(b, alpha);

            if (shown != null) {
                Color tint = shown.rule != null && !usable(shown.rule) ? DIM : Color.WHITE;
                rect(sideX + 12f, detailTop + 8f, DETAIL_ICON, DETAIL_ICON, TILE_OFF, alpha);
                drawIcon(shown.iconPath, sideX + 12f, detailTop + 8f, DETAIL_ICON, tint, alpha);
            }
        } finally {
            GL11.glPopAttrib();
        }
    }

    private void drawGrid(Grid grid, float alpha) {
        // Header bar above the grid.
        rect(grid.x, grid.y - HEADER_H, grid.w, HEADER_H - 2f, PANEL, alpha);
        rect(grid.x, grid.y - 3f, grid.w, 1f, GOLD, alpha * 0.3f);
        rect(grid.x, grid.y, grid.w, grid.h, PANEL, alpha * 0.6f);

        float scale = Global.getSettings().getScreenScaleMult();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int) (x(grid.x) * scale), (int) (y(grid.y + grid.h) * scale),
                (int) Math.ceil(grid.w * scale), (int) Math.ceil(grid.h * scale));
        for (Tile tile : grid.tiles) {
            float left = grid.contentLeft + tile.x, top = grid.contentTop + tile.y;
            if (top + TILE_H < grid.y || top > grid.y + grid.h) continue;
            drawTile(tile, left, top, alpha);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private void drawTile(Tile tile, float left, float top, float alpha) {
        Color accent, fill;
        float iconAlpha;
        Color tint = Color.WHITE;
        boolean gold = false;
        int level = 0, levels = 0;
        if (tile.rule != null) {
            RuleSpec rule = tile.rule;
            level = RuleState.level(rule);
            levels = rule.levels;
            if (!usable(rule)) {
                accent = DIM.darker();
                fill = TILE_LOCKED;
                iconAlpha = 0.2f;
                tint = DIM;
                levels = 0;
            } else if (level > 0) {
                accent = typeColor(rule);
                fill = GLDraw.darken(accent, 0.72f);
                iconAlpha = 1f;
            } else {
                accent = DIM;
                fill = TILE_OFF;
                iconAlpha = 0.5f;
                tint = new Color(200, 200, 200);
            }
            gold = !Challenges.activeRequiring(rule).isEmpty();
        } else if (tile.challenge != null) {
            ChallengeSpec spec = tile.challenge;
            boolean complete = Challenges.isComplete(spec), active = Challenges.isActive(spec);
            accent = complete ? RuleBookIntel.helpfulColor() : active ? GOLD : DIM;
            fill = complete ? GLDraw.darken(RuleBookIntel.helpfulColor(), 0.75f)
                    : active ? GLDraw.darken(GOLD, 0.75f) : TILE_OFF;
            iconAlpha = complete || active ? 1f : 0.6f;
            gold = tile == selectedChallenge;
        } else {
            boolean done = Unlocks.hasFeat(tile.feat.id);
            accent = done ? RuleBookIntel.helpfulColor() : DIM;
            fill = done ? GLDraw.darken(accent, 0.75f) : TILE_OFF;
            iconAlpha = done ? 1f : 0.35f;
        }

        rect(left, top, TILE_W, TILE_H, fill, alpha);
        boolean hover = tile == hovered;
        Color border = gold ? GOLD : hover ? GLDraw.brighten(accent, 0.3f) : accent;
        frame(left, top, TILE_W, TILE_H, border, gold || hover ? 2f : 1f, alpha * (hover || gold ? 1f : 0.55f));

        drawIcon(tile.iconPath, left + (TILE_W - ICON) / 2f, top + 36f, ICON, tint, alpha * iconAlpha);

        if (levels > 0) {
            float total = levels * PIP_W + (levels - 1) * PIP_GAP;
            float px = left + (TILE_W - total) / 2f, py = top + TILE_H - 12f;
            for (int l = 1; l <= levels; l++) {
                rect(px, py, PIP_W, PIP_H, l <= level ? accent : PIP_OFF, alpha);
                px += PIP_W + PIP_GAP;
            }
        }
    }

    private void drawButton(Btn b, float alpha) {
        Color fill = !b.enabled ? new Color(22, 26, 33)
                : b.checked ? new Color(98, 76, 38)
                : b.hover ? new Color(60, 67, 78) : new Color(32, 40, 50);
        GLDraw.bevelledPanel(x(b.x), y(b.y + b.h), b.w, b.h, fill, 2f, alpha);
        frame(b.x, b.y, b.w, b.h, b.checked ? GOLD : DIM, 1f, alpha * (b.enabled ? 0.65f : 0.2f));
    }

    private void drawMeter(float alpha) {
        int net = Balance.net();
        float half = meterW / 2f, center = meterX + half, h = 10f;
        rect(meterX, meterY, meterW, h, new Color(30, 36, 46), alpha);
        float fraction = Math.min(1f, Math.abs(net) / (float) METER_RANGE);
        if (net > 0) rect(center, meterY, half * fraction, h, RuleBookIntel.helpfulColor(), alpha * 0.85f);
        if (net < 0) rect(center - half * fraction, meterY, half * fraction, h, RuleBookIntel.hardColor(), alpha * 0.85f);
        rect(center - 1f, meterY - 4f, 2f, h + 8f, TEXT, alpha);
        frame(meterX, meterY, meterW, h, DIM, 1f, alpha * 0.5f);
    }
}
