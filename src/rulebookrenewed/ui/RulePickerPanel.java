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
import rulebookrenewed.ChallengeSpec;
import rulebookrenewed.Challenges;
import rulebookrenewed.Feats;
import rulebookrenewed.RuleBookIntel;
import rulebookrenewed.RuleCatalog;
import rulebookrenewed.RuleManager;
import rulebookrenewed.RuleSpec;
import rulebookrenewed.RuleState;
import rulebookrenewed.Unlocks;

/**
 * The rule picker. Two tabs on one scrolling list. Rules: one row per mirrored pair under its section
 * heading, hard levels left of Off and helpful levels right of it, so only one side can be on; a click
 * applies at once, so switching mid-campaign is the same as picking at the start. Challenges and feats: every challenge with a button to
 * take it on or give it up, and every feat with what it unlocks. The balance meter on top only informs and
 * never blocks a pick.
 *
 * The list's labels live in a panel inside a scrolling TooltipMakerAPI, so the game clips them and
 * scrolls them with the mouse wheel. A tab switch throws that list away and builds the other one.
 * Buttons are still drawn and hit-tested by this panel, as in Gambling Den (TooltipMakerAPI buttons in a
 * custom dialog can take clicks in the wrong place): each frame the list's real screen position is read
 * back from the content panel, so drawing and clicks follow the scroll whatever direction the scroller
 * counts in.
 */
public final class RulePickerPanel extends BaseCustomUIPanelPlugin {

    public static final float PANEL_W = 1040f, PANEL_H = 720f;

    private static final float METER_X = 170f, METER_Y = 80f, METER_W = 700f, METER_H = 12f;
    private static final int METER_RANGE = 100;
    private static final float TAB_Y = 100f, TAB_H = 24f;
    private static final float DETAIL_H = 118f;
    private static final float DETAIL_TOP = PANEL_H - 56f - DETAIL_H;
    private static final float LIST_X = 20f, LIST_TOP = 132f, LIST_W = PANEL_W - 40f;
    private static final float LIST_H = DETAIL_TOP - 8f - LIST_TOP;
    private static final float PAIR_H = 46f, PAIR_GAP = 6f, HEADING_H = 30f, CHALLENGE_H = 54f, FEAT_H = 26f;
    private static final float NAME_X = 16f, NAME_W = 200f, OFF_X = 412f, OFF_W = 42f, LEVEL_W = 34f, GAP = 5f;
    private static final float HELP_NAME_X = OFF_X + OFF_W + GAP + 3f + 5f * (LEVEL_W + GAP) + 6f;
    private static final float POINTS_X = 880f;
    private static final float ACTION_W = 124f;

    private static final int TAB_RULES = 0, TAB_CHALLENGES = 1;

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

    private enum Kind { HEADING, PAIR, CHALLENGE, FEAT }

    /**
     * One list line. y is in list content coordinates (0 = top of the list). A PAIR row holds a hard rule
     * and its helpful mirror (either may be missing for a rule without a mirror): hard levels to the left of
     * Off, helpful levels to the right, so only one side can be on.
     */
    private static final class Row {
        final Kind kind;
        final float y, h;
        boolean pairStart;
        /** HEADING rows of the Rules tab carry the first rule of their section, for its name. */
        RuleSpec rule;
        RuleSpec hard, helpful;
        ChallengeSpec challenge;
        Feats.Feat feat;
        LabelAPI name, name2, points, effect, extra;
        final Btn[] hardLevels = new Btn[RuleSpec.MAX_LEVELS + 1];
        final Btn[] helpfulLevels = new Btn[RuleSpec.MAX_LEVELS + 1];
        Btn off, action;

        Row(Kind kind, float y, float h) {
            this.kind = kind;
            this.y = y;
            this.h = h;
        }
    }

    private final List<Btn> buttons = new ArrayList<>();
    private final List<Row> rows = new ArrayList<>();
    private CustomPanelAPI panel;
    private CustomPanelAPI content;
    private TooltipMakerAPI listElement;
    private DialogCallbacks callbacks;
    private PositionAPI position;
    private LabelAPI balance, multiplier, detailTitle, detailBody, detailFlavor;
    private Btn tabRules, tabChallenges;
    private int tab = TAB_RULES;
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
        multiplier = label(panel, "", DIM, 0f, 102f, PANEL_W, 18f, Fonts.DEFAULT_SMALL, Alignment.MID);

        tabRules = button(panel, "tab:" + TAB_RULES, "Rules", LIST_X, TAB_Y, 100f, TAB_H, false);
        tabChallenges = button(panel, "tab:" + TAB_CHALLENGES, "Challenges and feats", LIST_X + 106f, TAB_Y, 180f, TAB_H, false);

        detailTitle = label(panel, "", GOLD, 40f, DETAIL_TOP + 8f, PANEL_W - 80f, 20f, Fonts.DEFAULT_SMALL, Alignment.LMID);
        detailBody = label(panel, "", TEXT, 40f, DETAIL_TOP + 30f, PANEL_W - 80f, 62f, Fonts.DEFAULT_SMALL, Alignment.TL);
        detailFlavor = label(panel, "", DIM, 40f, DETAIL_TOP + DETAIL_H - 26f, PANEL_W - 80f, 20f, Fonts.DEFAULT_SMALL, Alignment.LMID);

        button(panel, "close", "Close", PANEL_W - 200f, PANEL_H - 46f, 170f, 34f, false);

        buildList();
        refresh();
    }

    // ---- building the list ----

    private void buildList() {
        if (listElement != null) panel.removeComponent(listElement);
        buttons.removeIf(b -> b.inList);
        rows.clear();
        hovered = null;

        float bottom = tab == TAB_RULES ? layoutRules() : layoutChallenges();
        float contentH = Math.max(LIST_H, bottom + 6f);

        listElement = panel.createUIElement(LIST_W, LIST_H, true);
        content = panel.createCustomPanel(LIST_W, contentH, null);
        for (int i = 0; i < rows.size(); i++) addRowParts(rows.get(i), i);
        listElement.addCustom(content, 0f);
        listElement.setHeightSoFar(contentH);
        panel.addUIElement(listElement).inTL(LIST_X, LIST_TOP);
        contentTop = LIST_TOP;
    }

    private float layoutRules() {
        float y = 6f;
        String lastSection = null;
        Row current = null;
        for (RuleSpec rule : RuleCatalog.all()) {
            if (!rule.section.equals(lastSection)) {
                Row heading = new Row(Kind.HEADING, y, HEADING_H);
                heading.rule = rule;
                rows.add(heading);
                y += HEADING_H;
                lastSection = rule.section;
                current = null;
            }
            // The catalog keeps a pair together, hard first: join the mirror to the row of its twin.
            boolean joins = current != null && current.hard != null && current.helpful == null
                    && !rule.hard && current.hard.pair.equals(rule.pair);
            if (joins) {
                current.helpful = rule;
                continue;
            }
            Row row = new Row(Kind.PAIR, y + (current != null ? PAIR_GAP : 0f), PAIR_H);
            if (current != null) y += PAIR_GAP;
            row.pairStart = current != null;
            if (rule.hard) row.hard = rule;
            else row.helpful = rule;
            rows.add(row);
            current = row;
            y += PAIR_H;
        }
        return y;
    }

    private static float hardX(int level) {
        return OFF_X - level * (LEVEL_W + GAP);
    }

    private static float helpfulX(int level) {
        return OFF_X + OFF_W + GAP + 3f + (level - 1) * (LEVEL_W + GAP);
    }

    private float layoutChallenges() {
        float y = 6f;
        rows.add(new Row(Kind.HEADING, y, HEADING_H));
        y += HEADING_H;
        for (ChallengeSpec spec : Challenges.all()) {
            Row row = new Row(Kind.CHALLENGE, y, CHALLENGE_H);
            row.challenge = spec;
            rows.add(row);
            y += CHALLENGE_H;
        }
        y += PAIR_GAP;
        rows.add(new Row(Kind.HEADING, y, HEADING_H));
        y += HEADING_H;
        for (Feats.Feat feat : Feats.all()) {
            Row row = new Row(Kind.FEAT, y, FEAT_H);
            row.feat = feat;
            rows.add(row);
            y += FEAT_H;
        }
        return y;
    }

    private void addRowParts(Row row, int index) {
        switch (row.kind) {
            case HEADING: {
                String text = row.rule != null ? row.rule.section.toUpperCase()
                        : rows.indexOf(row) == 0 ? "CHALLENGES" : "FEATS";
                row.name = label(content, text, GOLD, NAME_X, row.y + 8f, 400f, 18f, Fonts.ORBITRON_12, Alignment.LMID);
                String note = row.rule != null ? ""
                        : rows.indexOf(row) == 0 ? "Taking one on raises its rules. Lowering them, or going over its helpful limit, ends it."
                        : "Feats count in every campaign once done.";
                row.extra = label(content, note, DIM, 300f, row.y + 8f, LIST_W - 300f - 24f, 18f, Fonts.DEFAULT_SMALL, Alignment.RMID);
                break;
            }
            case PAIR: {
                float top = row.y + 3f, h = 24f;
                row.name = label(content, "", TEXT, NAME_X, top + 3f, NAME_W, 18f, Fonts.DEFAULT_SMALL, Alignment.RMID);
                row.name2 = label(content, "", TEXT, HELP_NAME_X, top + 3f, NAME_W, 18f, Fonts.DEFAULT_SMALL, Alignment.LMID);
                for (int level = 1; level <= RuleSpec.MAX_LEVELS; level++) {
                    row.hardLevels[level] = button(content, "pair:" + index + ":hard:" + level, RuleSpec.roman(level),
                            hardX(level), top, LEVEL_W, h, true);
                    row.helpfulLevels[level] = button(content, "pair:" + index + ":helpful:" + level, RuleSpec.roman(level),
                            helpfulX(level), top, LEVEL_W, h, true);
                }
                row.off = button(content, "pair:" + index + ":off:0", "Off", OFF_X, top, OFF_W, h, true);
                row.points = label(content, "", TEXT, POINTS_X, top + 3f, 60f, 18f, Fonts.DEFAULT_SMALL, Alignment.RMID);
                row.effect = label(content, "", DIM, NAME_X, row.y + 27f, LIST_W - NAME_X - 40f, 18f, Fonts.DEFAULT_SMALL, Alignment.MID);
                break;
            }
            case CHALLENGE: {
                float actionX = LIST_W - 24f - ACTION_W - 8f;
                row.name = label(content, "", GOLD, NAME_X, row.y + 5f, 360f, 18f, Fonts.DEFAULT_SMALL, Alignment.LMID);
                row.points = label(content, "", DIM, 380f, row.y + 5f, actionX - 380f - 12f, 18f, Fonts.DEFAULT_SMALL, Alignment.RMID);
                row.effect = label(content, "", DIM, NAME_X, row.y + 28f, actionX - NAME_X - 12f, 20f, Fonts.DEFAULT_SMALL, Alignment.LMID);
                row.action = button(content, "challenge:" + index, "Take it on", actionX, row.y + 14f, ACTION_W, 26f, true);
                break;
            }
            case FEAT: {
                row.name = label(content, "", TEXT, NAME_X, row.y + 4f, 380f, 18f, Fonts.DEFAULT_SMALL, Alignment.LMID);
                row.points = label(content, "", DIM, 400f, row.y + 4f, 110f, 18f, Fonts.DEFAULT_SMALL, Alignment.LMID);
                row.effect = label(content, "", DIM, 520f, row.y + 4f, LIST_W - 520f - 24f, 18f, Fonts.DEFAULT_SMALL, Alignment.LMID);
                break;
            }
            default:
                break;
        }
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

    // ---- filling in ----

    private void refresh() {
        int net = Balance.net();
        balance.setText(Balance.describe(net));
        balance.setColor(RuleBookIntel.balanceColor(net));
        multiplier.setText(Balance.ngpInstalled()
                ? "Career points " + Balance.formatMultiplier(Balance.ngpMultiplier(net)) : "");
        tabRules.checked = tab == TAB_RULES;
        tabChallenges.checked = tab == TAB_CHALLENGES;
        for (Row row : rows) {
            if (row.kind == Kind.PAIR) fillPair(row);
            else if (row.kind == Kind.CHALLENGE) fillChallenge(row);
            else if (row.kind == Kind.FEAT) fillFeat(row);
        }
        refreshDetail();
    }

    private static Color typeColor(RuleSpec rule) {
        return rule.hard ? RuleBookIntel.hardColor() : RuleBookIntel.helpfulColor();
    }

    private static boolean usable(RuleSpec rule) {
        return rule != null && (Unlocks.isUnlocked(rule) || RuleState.level(rule) > 0);
    }

    private static int level(RuleSpec rule) {
        return rule == null ? 0 : RuleState.level(rule);
    }

    /** The side that is on, or null when the row is off. */
    private static RuleSpec activeSide(Row row) {
        if (level(row.hard) > 0) return row.hard;
        if (level(row.helpful) > 0) return row.helpful;
        return null;
    }

    private void fillSideName(LabelAPI label, RuleSpec rule) {
        if (rule == null) {
            label.setText("");
            return;
        }
        boolean held = !Challenges.activeRequiring(rule).isEmpty();
        label.setText(held ? rule.name + " *" : rule.name);
        label.setColor(usable(rule) ? typeColor(rule) : DIM.darker());
    }

    private void fillSide(Btn[] buttons, RuleSpec rule) {
        boolean show = usable(rule);
        int on = level(rule);
        for (int l = 1; l <= RuleSpec.MAX_LEVELS; l++) {
            Btn b = buttons[l];
            b.visible = show && l <= rule.levels;
            b.checked = b.visible && l == on;
            b.label.setText(b.visible ? RuleSpec.roman(l) : "");
            b.label.setColor(b.checked ? Color.WHITE : TEXT);
        }
    }

    private void fillPair(Row row) {
        fillSideName(row.name, row.hard);
        fillSideName(row.name2, row.helpful);
        fillSide(row.hardLevels, row.hard);
        fillSide(row.helpfulLevels, row.helpful);
        RuleSpec active = activeSide(row);
        row.off.visible = usable(row.hard) || usable(row.helpful);
        row.off.checked = active == null;
        row.off.label.setText(row.off.visible ? "Off" : "");
        row.off.label.setColor(row.off.checked ? Color.WHITE : TEXT);

        if (active != null) {
            int points = active.points(level(active));
            row.points.setText(points > 0 ? "+" + points : String.valueOf(points));
            row.points.setColor(typeColor(active));
            row.effect.setText(active.name + " " + RuleSpec.roman(level(active)) + ":  " + active.describe(level(active)));
            row.effect.setColor(TEXT);
            return;
        }
        row.points.setText("");
        StringBuilder locked = new StringBuilder();
        for (RuleSpec side : new RuleSpec[] {row.hard, row.helpful}) {
            if (side == null || usable(side)) continue;
            if (locked.length() > 0) locked.append("   ");
            locked.append(side.name).append(" is locked.");
            if (!side.unlockHint.isEmpty()) locked.append(' ').append(side.unlockHint);
        }
        row.effect.setText(locked.toString());
        row.effect.setColor(DIM.darker());
    }

    private void fillChallenge(Row row) {
        ChallengeSpec spec = row.challenge;
        boolean complete = Challenges.isComplete(spec);
        boolean active = Challenges.isActive(spec);
        String why = active ? null : Challenges.whyNot(spec);
        row.name.setText(spec.name);
        row.name.setColor(complete ? RuleBookIntel.helpfulColor() : GOLD);
        if (complete) {
            row.points.setText("Completed");
            row.points.setColor(RuleBookIntel.helpfulColor());
        } else if (active) {
            row.points.setText("Under way: " + Challenges.progressText(spec));
            row.points.setColor(GOLD);
        } else if (why != null) {
            row.points.setText(why);
            row.points.setColor(DIM);
        } else {
            row.points.setText("Reward: " + rewardNames(spec));
            row.points.setColor(DIM);
        }
        row.effect.setText("Needs " + spec.requirementsText() + ".  Goal: " + spec.goalText() + ".  " + spec.limitText() + ".");
        row.action.visible = !complete;
        row.action.enabled = active || why == null;
        row.action.checked = active;
        row.action.label.setText(complete ? "" : active ? "Give up" : "Take it on");
        row.action.label.setColor(row.action.enabled ? Color.WHITE : DIM.darker());
    }

    private void fillFeat(Row row) {
        Feats.Feat feat = row.feat;
        boolean done = Unlocks.hasFeat(feat.id);
        row.name.setText(feat.name);
        row.name.setColor(done ? RuleBookIntel.helpfulColor() : TEXT);
        String status = done ? "Done" : "Not yet";
        if (feat.repeatable && Global.getSector() != null) {
            int count = feat.count();
            if (count > 0) status = (done ? "Done, " : "") + count + " this campaign";
        }
        row.points.setText(status);
        row.points.setColor(done ? RuleBookIntel.helpfulColor() : DIM);
        row.effect.setText(featUses(feat));
    }

    private static String rewardNames(ChallengeSpec spec) {
        StringBuilder out = new StringBuilder();
        for (RuleSpec rule : Challenges.rewards(spec)) {
            if (out.length() > 0) out.append(", ");
            out.append(rule.name);
        }
        return out.length() == 0 ? "none" : out.toString();
    }

    private static String featUses(Feats.Feat feat) {
        StringBuilder rules = new StringBuilder();
        for (RuleSpec rule : RuleCatalog.all()) {
            if (!feat.id.equals(rule.featId())) continue;
            if (rules.length() > 0) rules.append(", ");
            rules.append(rule.name);
        }
        StringBuilder challenges = new StringBuilder();
        for (ChallengeSpec spec : Challenges.all()) {
            if (spec.goalFeat != feat) continue;
            if (challenges.length() > 0) challenges.append(", ");
            challenges.append(spec.name);
        }
        StringBuilder out = new StringBuilder();
        if (rules.length() > 0) out.append("Unlocks ").append(rules);
        if (challenges.length() > 0) {
            if (out.length() > 0) out.append(".  ");
            out.append("Goal of ").append(challenges);
        }
        return out.toString();
    }

    private void refreshDetail() {
        if (hovered == null || hovered.kind == Kind.HEADING) {
            detailTitle.setText("");
            detailBody.setText(tab == TAB_RULES
                    ? "House rules for this voyage. The Sector will hold you to them."
                    : "Challenges ask you to keep hard rules on until you reach a goal. Finishing one opens new rules in every campaign.");
            detailBody.setColor(DIM);
            detailFlavor.setText("");
            return;
        }
        if (hovered.kind == Kind.PAIR) pairDetail(hovered);
        else if (hovered.kind == Kind.CHALLENGE) challengeDetail(hovered.challenge);
        else featDetail(hovered.feat);
    }

    private void pairDetail(Row row) {
        RuleSpec active = activeSide(row);
        RuleSpec first = row.hard != null ? row.hard : row.helpful;
        String title = row.hard != null && row.helpful != null ? row.hard.name + "  |  " + row.helpful.name
                : first.name + (first.hard ? "  (hard)" : "  (helpful)");
        detailTitle.setText(title);
        detailTitle.setColor(active != null ? typeColor(active) : GOLD);
        StringBuilder body = new StringBuilder();
        for (RuleSpec side : new RuleSpec[] {row.hard, row.helpful}) {
            if (side == null) continue;
            if (body.length() > 0) body.append('\n');
            body.append(side.hard ? "Hard: " : "Helpful: ").append(side.describeAllLevels())
                    .append("  Points ").append(side.pointsText()).append('.');
            if (!usable(side) && !side.unlockHint.isEmpty()) body.append("  ").append(side.unlockHint);
        }
        for (RuleSpec side : new RuleSpec[] {row.hard, row.helpful}) {
            if (side == null) continue;
            for (ChallengeSpec spec : Challenges.activeRequiring(side)) {
                body.append("\n* ").append(side.name).append(" is held by the challenge ").append(spec.name).append(" at ")
                        .append(RuleSpec.roman(spec.levelFor(side))).append(" or higher. Lowering it ends the challenge.");
            }
        }
        detailBody.setText(body.toString());
        detailBody.setColor(TEXT);
        detailFlavor.setText((active != null ? active : first).flavor);
    }

    private void challengeDetail(ChallengeSpec spec) {
        detailTitle.setText(spec.name + "  (challenge)");
        detailTitle.setColor(GOLD);
        StringBuilder body = new StringBuilder(spec.description);
        body.append("\nNeeds ").append(spec.requirementsText()).append(".  ").append(spec.limitText()).append('.');
        body.append("\nGoal: ").append(spec.goalText()).append('.');
        if (Challenges.isActive(spec)) body.append("  Progress: ").append(Challenges.progressText(spec)).append('.');
        for (RuleSpec reward : Challenges.rewards(spec)) {
            body.append("\nReward: ").append(reward.name).append(" - ").append(reward.describe(1));
            if (reward.levels > 1) body.append(" (level I)");
        }
        detailBody.setText(body.toString());
        detailBody.setColor(TEXT);
        detailFlavor.setText(spec.flavor);
    }

    private void featDetail(Feats.Feat feat) {
        boolean done = Unlocks.hasFeat(feat.id);
        detailTitle.setText(feat.name + (done ? "  (done)" : "  (feat)"));
        detailTitle.setColor(done ? RuleBookIntel.helpfulColor() : GOLD);
        String uses = featUses(feat);
        String body = (uses.isEmpty() ? "Recorded for every campaign once done." : uses + ".")
                + (feat.repeatable ? "\nCounts every battle it happens in, so challenges can ask for it more than once." : "");
        detailBody.setText(body);
        detailBody.setColor(TEXT);
        detailFlavor.setText("");
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
                tab = next;
                buildList();
                click();
                refresh();
            }
            return;
        }
        if (action.startsWith("pair:")) {
            String[] parts = action.split(":");
            Row row = rows.get(Integer.parseInt(parts[1]));
            int level = Integer.parseInt(parts[3]);
            switch (parts[2]) {
                case "hard":
                    RuleManager.setLevel(row.hard, level); // switches the helpful mirror off
                    break;
                case "helpful":
                    RuleManager.setLevel(row.helpful, level);
                    break;
                default:
                    if (row.hard != null) RuleManager.setLevel(row.hard, 0);
                    if (row.helpful != null) RuleManager.setLevel(row.helpful, 0);
                    break;
            }
            click();
            refresh();
            return;
        }
        if (action.startsWith("challenge:")) {
            Row row = rows.get(Integer.parseInt(action.substring(10)));
            if (Challenges.isActive(row.challenge)) Challenges.giveUp(row.challenge);
            else Challenges.start(row.challenge);
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
                if (my >= top && my < top + row.h) over = row;
            }
        }
        if (over != hovered) {
            hovered = over;
            refreshDetail();
        }

        if (!click || dismissed) return;
        for (Btn b : new ArrayList<>(buttons)) {
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

    // ---- drawing ----

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
                if (top + row.h < LIST_TOP || top > LIST_TOP + LIST_H) continue;
                drawRow(row, top, alpha);
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

    private void drawRow(Row row, float top, float alpha) {
        if (row.kind == Kind.HEADING) {
            rect(LIST_X + 10f, top + row.h - 4f, LIST_W - 40f, 1f, GOLD, alpha * 0.35f);
            return;
        }
        if (row.pairStart) rect(LIST_X + 10f, top - PAIR_GAP / 2f - 0.5f, LIST_W - 40f, 1f, DIM, alpha * 0.25f);
        if (row == hovered) rect(LIST_X + 4f, top, LIST_W - 28f, row.h, new Color(40, 48, 62), alpha);
        Color bar;
        if (row.kind == Kind.PAIR) {
            RuleSpec active = activeSide(row);
            bar = active != null ? typeColor(active) : DIM;
        }
        else if (row.kind == Kind.CHALLENGE) bar = Challenges.isComplete(row.challenge) ? RuleBookIntel.helpfulColor() : GOLD;
        else bar = Unlocks.hasFeat(row.feat.id) ? RuleBookIntel.helpfulColor() : DIM;
        rect(LIST_X + 4f, top + 4f, 3f, row.h - 8f, bar, alpha * 0.7f);
        if (row.kind == Kind.CHALLENGE) rect(LIST_X + 10f, top + row.h - 1f, LIST_W - 40f, 1f, DIM, alpha * 0.15f);
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
