package finalforeach.cosmicreach.ui;

import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.settings.BooleanSetting;

public class ToggleButton extends UIElement {
    final String on = Lang.get("on_state");
    final String off = Lang.get("off_state");
    private final BooleanSetting setting;
    String prefix;

    public ToggleButton(String prefix, BooleanSetting setting, float x, float y, float w, float h) {
        super(x, y, w, h, false);
        this.prefix = prefix;
        this.setting = setting;
        this.onCreate();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.updateText();
    }

    @Override
    public void onClick() {
        super.onClick();
        this.setting.toggleValue();
        this.updateValue();
        this.updateText();
    }

    public void updateValue() {
    }

    public boolean getValue() {
        return this.setting.getValue();
    }

    @Override
    public void updateText() {
        this.setText(this.prefix + (this.setting.getValue() ? this.on : this.off));
    }
}