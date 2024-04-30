package finalforeach.cosmicreach.settings;

public class FloatSetting extends GameSetting {
    private final String key;
    private float value;

    public FloatSetting(String key, int defaultValue) {
        this.key = key;
        Object mapping = allSettings.getOrDefault(key, defaultValue);
        if (mapping instanceof Number) {
            Number n = (Number)mapping;
            this.value = n.floatValue();
        } else {
            this.value = defaultValue;
        }
    }

    public float getValue() {
        return this.value;
    }

    private void save() {
        allSettings.put(this.key, Float.valueOf(this.value));
        FloatSetting.saveSettings();
    }

    public void setValue(float newValue) {
        this.value = newValue;
        this.save();
    }
}