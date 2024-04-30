package finalforeach.cosmicreach.audio;

import com.badlogic.gdx.audio.Sound;
import finalforeach.cosmicreach.settings.SoundSettings;

public class SoundManager {
    public static long playSound(Sound sound, float volume, float pitch, float pan) {
        if (!SoundSettings.isSoundEnabled()) {
            return -1L;
        }
        return sound.play(volume, pitch, pan);
    }

    public static long playSound(Sound sound, float volume, float pitch) {
        if (!SoundSettings.isSoundEnabled()) {
            return -1L;
        }
        return sound.play(volume, pitch, 0.0f);
    }

    public static long playSound(Sound sound) {
        return SoundManager.playSound(sound, 1.0f, 1.0f, 0.0f);
    }
}