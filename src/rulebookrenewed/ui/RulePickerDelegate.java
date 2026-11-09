package rulebookrenewed.ui;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate;
import com.fs.starfarer.api.ui.CustomPanelAPI;

/** Hosts the rule picker panel inside an interaction dialog. */
public final class RulePickerDelegate implements CustomVisualDialogDelegate {

    private final RulePickerPanel picker;
    private final Runnable onClose;
    private boolean reported;

    public RulePickerDelegate(RulePickerPanel picker, Runnable onClose) {
        this.picker = picker;
        this.onClose = onClose;
    }

    @Override
    public CustomUIPanelPlugin getCustomPanelPlugin() {
        return picker;
    }

    @Override
    public void init(CustomPanelAPI panel, DialogCallbacks callbacks) {
        callbacks.getPanelFader().setDurationOut(0.3f);
        picker.init(panel, callbacks);
    }

    @Override
    public float getNoiseAlpha() {
        return 0.05f;
    }

    @Override
    public void advance(float amount) {
    }

    @Override
    public void reportDismissed(int option) {
        if (reported) return;
        reported = true;
        if (onClose != null) onClose.run();
    }
}
