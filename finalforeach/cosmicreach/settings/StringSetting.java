package finalforeach.cosmicreach.settings;

public class StringSetting extends GameSetting {
    private final String key;
    private String value;

    public StringSetting(String key, String defaultValue) {
        this.key = key;
        Object mapping = allSettings.getOrDefault(key, defaultValue);
        this.value = mapping instanceof String ? ((String)mapping) : defaultValue;
    }

    public String getValue() {
        return this.value;
    }

    private void save() {
        if (this.value == null) {
            allSettings.remove(this.key);
        } else {
            allSettings.put(this.key, this.value);
        }
        StringSetting.saveSettings();
    }

    public void setValue(String newValue) {
        this.value = newValue;
        this.save();
    }
}