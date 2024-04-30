package finalforeach.cosmicreach.lang;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.settings.Preferences;

public class Lang implements Json.Serializable {
    public static Lang gameDefaultLang;
    public static Lang currentLang;
    private static Array<Lang> languages;
    private String langTag;
    private String name;
    private Map<String, String> mappedStrings = new HashMap<String, String>();
    private Array<Lang> fallBackLanguages;
    private String[] fallbackTags;

    public static void loadLanguages() {
        String[] defaultAssetList = Gdx.files.internal("assets.txt").readString().split("\n");
        Json json = new Json();
        for (String f : defaultAssetList) {
            try {
                String fileName;
                String rootFolder = "lang/";
                if (!f.startsWith(rootFolder) || !f.endsWith(".json") || !Gdx.files.internal(f).exists() || (fileName = f.replace(rootFolder, "")).contains("/")) continue;
                Lang lang = json.fromJson(Lang.class, GameAssetLoader.loadAsset(f));
                lang.langTag = fileName.replace(".json", "");
                if (lang.langTag.equalsIgnoreCase(Preferences.chosenLang.getValue())) {
                    currentLang = lang;
                }
                if (lang.langTag.equalsIgnoreCase("en_us")) {
                    gameDefaultLang = lang;
                }
                languages.add(lang);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        for (Lang l : languages) {
            l.calculateFallbacks(new HashSet<Lang>());
        }
        languages.sort((a, b) -> a.langTag.compareTo(b.langTag));
    }

    private void calculateFallbacks(Set<Lang> calculatingSet) {
        if (this.fallbackTags == null) {
            if (this.fallBackLanguages == null) {
                this.fallBackLanguages = new Array<Lang>(0);
            }
            return;
        }
        Array<Lang> workingFallbackLangs = new Array<Lang>();
        calculatingSet.add(this);
        for (String f : this.fallbackTags) {
            Lang l = Lang.getLangByTag(f);
            if (l == null) continue;
            if (!calculatingSet.contains(l)) {
                l.calculateFallbacks(calculatingSet);
            }
            workingFallbackLangs.add(l);
            if (l.fallBackLanguages == null) continue;
            for (Lang ff : l.fallBackLanguages) {
                workingFallbackLangs.add(ff);
            }
        }
        this.fallBackLanguages = workingFallbackLangs;
    }

    public static Lang getLangByTag(String tag) {
        for (int l = 0; l < Lang.languages.size; ++l) {
            Lang lang = languages.get(l);
            if (!lang.langTag.equalsIgnoreCase(tag)) continue;
            return lang;
        }
        return null;
    }

    public static String get(String key) {
        if (currentLang == null) {
            return key;
        }
        return currentLang.getMappedString(key, true);
    }

    public String getMappedString(String key, boolean checkFallbacks) {
        String s = this.mappedStrings.get(key);
        if (s != null) {
            return s;
        }
        if (checkFallbacks) {
            for (Lang f : this.fallBackLanguages) {
                s = f.getMappedString(key, false);
                if (s == null) continue;
                return s;
            }
            if (gameDefaultLang != null && (s = gameDefaultLang.getMappedString(key, false)) != null) {
                return s;
            }
            return key;
        }
        return null;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public void write(Json json) {
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        JsonValue value = jsonData.child;
        while (value != null) {
            if (value.name.equals("metadata")) {
                JsonValue metadataValue = value.child;
                while (metadataValue != null) {
                    switch (metadataValue.name) {
                        case "name": {
                            this.name = metadataValue.asString();
                            break;
                        }
                        case "fallbacks": {
                            this.fallbackTags = metadataValue.asStringArray();
                            break;
                        }
                    }
                    metadataValue = metadataValue.next;
                }
            } else {
                this.mappedStrings.put(value.name, value.asString());
            }
            value = value.next;
        }
    }

    public static Array<Lang> getLanguages() {
        return languages;
    }

    public boolean isCurrentLanguage() {
        return this == currentLang;
    }

    public void select() {
        currentLang = this;
        Preferences.chosenLang.setValue(this.langTag);
    }

    static {
        languages = new Array<Lang>();
    }
}