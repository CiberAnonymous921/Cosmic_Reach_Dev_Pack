package finalforeach.cosmicreach.settings;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

public class SettingsDictionary implements Json.Serializable {
    private HashMap<String, Object> dict = new HashMap<String, Object>();

    @Override
    public void write(Json json) {
        for (Map.Entry<String, Object> e : this.dict.entrySet()) {
            json.writeValue(e.getKey(), e.getValue());
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        jsonData.forEach(c -> {
            String jsonVal = c.toJson(JsonWriter.OutputType.minimal);
            this.dict.put(c.name, json.fromJson(null, jsonVal));
        });
    }

    public Object getOrDefault(String key, Object defaultValue) {
        return this.dict.getOrDefault(key, defaultValue);
    }

    public Object put(String key, Object value) {
        return this.dict.put(key, value);
    }

    public Object remove(String key) {
        return this.dict.remove(key);
    }
}