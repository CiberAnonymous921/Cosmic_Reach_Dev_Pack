package finalforeach.cosmicreach.settings;

public class BooleanSetting extends GameSetting {
    private final String key;
    private boolean value;

    public BooleanSetting(String key, boolean defaultValue) {
        this.key = key;
        Object mapping = allSettings.getOrDefault(key, defaultValue);
        this.value = mapping instanceof Boolean ? (Boolean)mapping : defaultValue;
    }

    public boolean getValue() {
        return this.value;
    }

    private void save() {
        allSettings.put(this.key, this.value);
        BooleanSetting.saveSettings();
    }

    public void setValue(boolean newValue) {
        this.value = newValue;
        this.save();
    }

    public void toggleValue() {
        this.value = !this.value;
        this.save();
    }
}