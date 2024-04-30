package finalforeach.cosmicreach.settings;

public class IntSetting extends GameSetting {
    private final String key;
    private int value;

    public IntSetting(String key, int defaultValue) {
        this.key = key;
        Object mapping = allSettings.getOrDefault(key, defaultValue);
        if (mapping instanceof Number) {
            Number n = (Number)mapping;
            this.value = n.intValue();
        } else {
            this.value = defaultValue;
        }
    }

    public int getValue() {
        return this.value;
    }

    private void save() {
        allSettings.put(this.key, this.value);
        IntSetting.saveSettings();
    }

    public void setValue(int newValue) {
        this.value = newValue;
        this.save();
    }
}