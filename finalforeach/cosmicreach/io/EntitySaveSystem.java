package finalforeach.cosmicreach.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import finalforeach.cosmicreach.entities.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.World;

public class EntitySaveSystem {
    public static String localPlayerFileName = "players/localPlayer.json";

    public static void savePlayers(World world) {
        if (InGame.getLocalPlayer() == null) {
            return;
        }
        String worldFolder = SaveLocation.getWorldSaveFolderLocation(world);
        File file = new File(worldFolder + "/" + localPlayerFileName);
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try (FileOutputStream fos = new FileOutputStream(file);){
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            String jsonStr = json.prettyPrint(InGame.getLocalPlayer());
            fos.write(jsonStr.getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void loadPlayers(World world) {
        String worldFolder = SaveLocation.getWorldSaveFolderLocation(world);
        File file = new File(worldFolder + "/" + localPlayerFileName);
        if (!file.exists()) {
            return;
        }
        try (FileInputStream fis = new FileInputStream(file);){
            Json json = new Json();
            json.setIgnoreUnknownFields(true);
            Player localPlayer = json.fromJson(Player.class, fis);
            InGame.setLocalPlayer(localPlayer);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}