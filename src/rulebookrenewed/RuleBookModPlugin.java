package rulebookrenewed;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

/** Loads the rule list at startup and wires the Rule Book into every save. */
public final class RuleBookModPlugin extends BaseModPlugin {

    @Override
    public void onApplicationLoad() {
        RuleCatalog.load();
        loadIcon(Ids.BOOK_ICON);
        for (RuleSpec rule : RuleCatalog.all()) loadIcon(rule.icon);
    }

    private static void loadIcon(String path) {
        try {
            Global.getSettings().loadTexture(path);
        } catch (Exception ignored) {
            // A missing icon only shows as blank; the build checks every path.
        }
    }

    @Override
    public void onNewGameAfterTimePass() {
        RuleState.setPickerPending(true);
    }

    @Override
    public void onGameLoad(boolean newGame) {
        RuleBookIntel.ensure();
        Global.getSector().addTransientScript(new RuleBookScript());
        // Applies this save's rules and undoes the last save's (Ludd's Song changes a game-wide value).
        RuleManager.refreshAll();
        Feats.check();
    }
}
