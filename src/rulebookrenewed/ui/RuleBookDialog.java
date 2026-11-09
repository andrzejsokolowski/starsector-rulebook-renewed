package rulebookrenewed.ui;

import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;

/**
 * Opens the rule picker. When the picker closes, this dialog closes itself on the next frame rather
 * than from inside the picker's dismissal callback. A plain Close option stays underneath in case the
 * picker is ever closed some other way.
 */
public final class RuleBookDialog implements InteractionDialogPlugin {

    private static final String OPTION_CLOSE = "rbr_close";
    private static final String OPTION_OPEN = "rbr_open";

    private InteractionDialogAPI dialog;
    private boolean closeRequested;
    private boolean dismissed;

    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;
        dialog.getTextPanel().addPara("You open the Rule Book.");
        dialog.getOptionPanel().addOption("Open the Rule Book again", OPTION_OPEN);
        dialog.getOptionPanel().addOption("Close the book", OPTION_CLOSE);
        dialog.getOptionPanel().setShortcut(OPTION_CLOSE, org.lwjgl.input.Keyboard.KEY_ESCAPE, false, false, false, true);
        showPicker();
    }

    private void showPicker() {
        RulePickerPanel picker = new RulePickerPanel();
        dialog.showCustomVisualDialog(RulePickerPanel.PANEL_W, RulePickerPanel.PANEL_H,
                new RulePickerDelegate(picker, () -> closeRequested = true));
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (OPTION_CLOSE.equals(optionData)) dismiss();
        else if (OPTION_OPEN.equals(optionData)) showPicker();
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {
    }

    @Override
    public void advance(float amount) {
        if (closeRequested) {
            closeRequested = false;
            dismiss();
        }
    }

    private void dismiss() {
        if (dismissed || dialog == null) return;
        dismissed = true;
        dialog.dismiss();
    }

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) {
    }

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public Map<String, MemoryAPI> getMemoryMap() {
        return null;
    }
}
