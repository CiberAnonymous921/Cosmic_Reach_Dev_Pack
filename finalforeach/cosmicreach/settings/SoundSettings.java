package finalforeach.cosmicreach.settings;

public class SoundSettings {
    public static final BooleanSetting soundEnabled = new BooleanSetting("soundEnabled", true);

    public static boolean isSoundEnabled() {
        return soundEnabled.getValue();
    }

    public static void toggleSound() {
        soundEnabled.toggleValue();
    }
}