package finalforeach.cosmicreach.settings;

public class CharSetting extends GameSetting {
    private final String key;
    private char value;

    public CharSetting(String key, char defaultValue) {
        this.key = key;
        Object mapping = allSettings.getOrDefault(key, Character.valueOf(defaultValue));
        if (mapping instanceof Character) {
            Character c = (Character)mapping;
            this.value = c.charValue();
        } else {
            this.value = defaultValue;
        }
    }

    public char getValue() {
        return this.value;
    }

    private void save() {
        if (this.value == '\u0000') {
            allSettings.remove(this.key);
        } else {
            allSettings.put(this.key, Character.valueOf(this.value));
        }
        CharSetting.saveSettings();
    }

    public void setValue(char newValue) {
        this.value = newValue;
        this.save();
    }
}