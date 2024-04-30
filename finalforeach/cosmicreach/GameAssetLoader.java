package finalforeach.cosmicreach;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import finalforeach.cosmicreach.io.SaveLocation;

public class GameAssetLoader {
    public static final HashMap<String, FileHandle> ALL_ASSETS = new HashMap<String, FileHandle>();
    public static final HashMap<String, Sound> ALL_SOUNDS = new HashMap<String, Sound>();
    public static AssetManager assetManager = new AssetManager();

    public static FileHandle loadAsset(String fileName) {
        return GameAssetLoader.loadAsset(fileName, false);
    }

    public static FileHandle loadAsset(String fileName, boolean forceReload) {
        if (!forceReload && ALL_ASSETS.containsKey(fileName)) {
            return ALL_ASSETS.get(fileName);
        }
        System.out.print("Loading " + fileName);
        FileHandle moddedFile = Gdx.files.absolute(SaveLocation.getSaveFolderLocation() + "/mods/assets/" + fileName);
        if (moddedFile.exists()) {
            System.out.println(" from mods folder");
            ALL_ASSETS.put(fileName, moddedFile);
            return moddedFile;
        }
        System.out.println(" from jar");
        FileHandle fileFromJar = Gdx.files.internal(fileName);
        ALL_ASSETS.put(fileName, fileFromJar);
        return fileFromJar;
    }

    public static Sound getSound(String soundFileName) {
        if (ALL_SOUNDS.containsKey(soundFileName)) {
            return ALL_SOUNDS.get(soundFileName);
        }
        Sound sound = Gdx.audio.newSound(GameAssetLoader.loadAsset(soundFileName));
        ALL_SOUNDS.put(soundFileName, sound);
        return sound;
    }
}