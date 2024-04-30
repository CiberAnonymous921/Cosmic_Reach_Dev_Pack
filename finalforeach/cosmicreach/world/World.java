package finalforeach.cosmicreach.world;

import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import finalforeach.cosmicreach.io.SaveLocation;
import finalforeach.cosmicreach.worldgen.ZoneGenerator;

public class World implements Json.Serializable {
    public String defaultZoneId;
    private transient HashMap<String, Zone> zoneMap = new HashMap<String, Zone>();
    public transient String worldFolderName;
    private String worldDisplayName;
    public long worldSeed = new Random().nextLong();

    public String getDisplayName() {
        if (this.worldDisplayName == null) {
            return this.worldFolderName;
        }
        return this.worldDisplayName;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Zone getZone(String zoneId) {
        HashMap<String, Zone> hashMap = this.zoneMap;
        synchronized (hashMap) {
            Zone zone = this.zoneMap.get(zoneId);
            if (zone == null) {
                zone = Zone.loadZone(this, zoneId);
                this.zoneMap.put(zoneId, zone);
            }
            return zone;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Collection<Zone> getZones() {
        HashMap<String, Zone> hashMap = this.zoneMap;
        synchronized (hashMap) {
            return this.zoneMap.values();
        }
    }

    public Zone getDefaultZone() {
        return this.getZone(this.defaultZoneId);
    }

    public static World createNew(String worldDisplayName, String worldSeed, String defaultZoneId, ZoneGenerator zoneGen) {
        long seed = 0L;
        if (worldSeed != null && worldSeed.length() > 0) {
            try {
                seed = Long.parseLong(worldSeed);
            } catch (Exception ex) {
                seed = worldSeed.hashCode();
            }
        } else {
            seed = new Random().nextLong();
        }
        return World.createNew(worldDisplayName, seed, defaultZoneId, zoneGen);
    }

    public static World createNew(String worldDisplayName, long worldSeed, String defaultZoneId, ZoneGenerator zoneGen) {
        World world = new World();
        world.worldDisplayName = worldDisplayName;
        world.worldFolderName = worldDisplayName.replaceAll("[^a-zA-Z0-9.-]", "_").substring(0, Math.min(255, worldDisplayName.length()));
        world.defaultZoneId = defaultZoneId;
        world.worldSeed = worldSeed;
        world.addNewZone(defaultZoneId, zoneGen);
        return world;
    }

    public void addNewZone(String zoneId, ZoneGenerator zoneGen) {
        zoneGen.seed = this.worldSeed + (long)zoneId.hashCode();
        this.zoneMap.put(zoneId, new Zone(this, zoneId, zoneGen));
    }

    private World() {
    }

    public String getFullSaveFolder() {
        return SaveLocation.getWorldSaveFolderLocation(this);
    }

    @Override
    public void write(Json json) {
        json.writeValue("latestRegionFileVersion", 0);
        json.writeValue("defaultZoneId", this.defaultZoneId);
        json.writeValue("worldDisplayName", this.worldDisplayName);
        json.writeValue("worldSeed", this.worldSeed);
        for (Zone z : this.getZones()) {
            z.saveZone(this);
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        int latestRegionFileVersion = json.readValue(Integer.class, jsonData.get("latestRegionFileVersion"));
        if (latestRegionFileVersion > 0) {
            throw new RuntimeException("Attempted to load a world with file version:" + latestRegionFileVersion + " but can only support worlds up to file version 0");
        }
        this.defaultZoneId = json.readValue(String.class, jsonData.get("defaultZoneId"));
        this.worldDisplayName = json.readValue(String.class, jsonData.get("worldDisplayName"));
        this.worldSeed = jsonData.has("worldSeed") ? json.readValue(Long.class, jsonData.get("worldSeed")) : 0L;
    }

    public String getWorldFolderName() {
        return this.worldFolderName;
    }
}