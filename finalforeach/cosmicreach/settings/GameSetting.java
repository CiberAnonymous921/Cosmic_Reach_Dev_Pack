package finalforeach.cosmicreach.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import finalforeach.cosmicreach.io.SaveLocation;

public class GameSetting {
    public static final String SETTINGS_FILE_NAME = "gameSettings.json";
    static SettingsDictionary allSettings = new SettingsDictionary();

    public static void setSetting(String key, Object value) {
        allSettings.put(key, value);
    }

    public static Object getSetting(String key, Object value, Object defaultValue) {
        return allSettings.getOrDefault(key, defaultValue);
    }

    public static void loadSettings() {
        File f = new File(SaveLocation.getSaveFolderLocation() + "/gameSettings.json");
        if (!f.exists()) {
            return;
        }
        try (FileInputStream fis = new FileInputStream(f);){
            Json json = new Json();
            allSettings = (SettingsDictionary)json.fromJson(allSettings.getClass(), fis);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void saveSettings() {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        String jsonStr = json.prettyPrint(allSettings);
        File f = new File(SaveLocation.getSaveFolderLocation() + "/gameSettings.json");
        try (FileOutputStream fos = new FileOutputStream(f);){
            fos.write(jsonStr.getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static {
        GameSetting.loadSettings();
    }
}
