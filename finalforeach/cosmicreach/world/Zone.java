package finalforeach.cosmicreach.world;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blockevents.ScheduledTrigger;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.io.SaveLocation;
import finalforeach.cosmicreach.savelib.blocks.IBlockDataFactory;
import finalforeach.cosmicreach.worldgen.ZoneGenerator;

public class Zone implements Json.Serializable, Disposable {
    public PriorityQueue<ScheduledTrigger> eventQueue = new PriorityQueue<ScheduledTrigger>((a, b) -> a.triggerTick() - b.triggerTick());
    public int currentTick = 0;
    private final Map<ChunkCoords, Chunk> chunks = new ConcurrentHashMap<ChunkCoords, Chunk>();
    public final Map<RegionCoords, Region> regions = new ConcurrentHashMap<RegionCoords, Region>();
    public Array<Entity> allEntities = new Array<Entity>();
    public Vector3 spawnPoint;
    public String zoneId;
    public ZoneGenerator zoneGenerator;
    public float respawnHeight = -16.0f;
    private transient World world;

    @SuppressWarnings("unused")
    /*
     * WARNING: this void is used for load world ! (don't delete)
     */
	private Zone() {
    }
    
    public Zone(World world, String zoneId, ZoneGenerator worldGen) {
        this.world = world;
        this.zoneId = zoneId;
        this.zoneGenerator = worldGen;
        worldGen.create();
    }

    public void runScheduledTriggers() {
        while (!this.eventQueue.isEmpty() && (this.eventQueue.peek().triggerTick()) < this.currentTick) {
            this.eventQueue.poll().run();
        }
        ++this.currentTick;
    }

    @Override
    public void write(Json json) {
        json.writeValue("zoneId", this.zoneId);
        json.writeValue("worldGenSaveKey", this.zoneGenerator.getSaveKey());
        json.writeValue("seed", this.zoneGenerator.seed);
        json.writeValue("spawnPoint", this.spawnPoint);
        json.writeValue("respawnHeight", Float.valueOf(this.respawnHeight));
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        this.zoneId = json.readValue(String.class, jsonData.get("zoneId"));
        String worldGenSaveKey = json.readValue(String.class, jsonData.get("worldGenSaveKey"));
        long worldSeed = json.readValue(Long.class, jsonData.get("seed"));
        this.zoneGenerator = ZoneGenerator.getZoneGenerator(worldGenSaveKey, worldSeed);
        this.respawnHeight = json.readValue("respawnHeight", Float.class, Float.valueOf(-16.0f), jsonData).floatValue();
        this.spawnPoint = json.readValue(Vector3.class, jsonData.get("spawnPoint"));
    }

    public void saveZone(World world) {
        String path = Zone.getFullSaveFolder(world, this.zoneId) + "/zoneInfo.json";
        Json zoneJson = new Json();
        zoneJson.setOutputType(JsonWriter.OutputType.json);
        File zoneFile = new File(path);
        try {
            zoneFile.getParentFile().mkdirs();
            zoneFile.createNewFile();
            try (FileOutputStream fos = new FileOutputStream(zoneFile);){
                fos.write(zoneJson.prettyPrint(this).getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getFullSaveFolder() {
        return Zone.getFullSaveFolder(this.world, this.zoneId);
    }

    public static String getFullSaveFolder(World world, String zoneId) {
        return SaveLocation.getWorldSaveFolderLocation(world) + "/zones/" + zoneId.replace(":", "/");
    }

    public static Zone loadZone(World world, String zoneId) {
        if (world == null || zoneId == null) {
            return null;
        }
        String path = Zone.getFullSaveFolder(world, zoneId) + "/zoneInfo.json";
        Json zoneJson = new Json();
        Zone zone = zoneJson.fromJson(Zone.class, Gdx.files.absolute(path));
        zone.world = world;
        return zone;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addChunk(Chunk chunk) {
        int rx = Math.floorDiv(chunk.chunkX, 16);
        int ry = Math.floorDiv(chunk.chunkY, 16);
        int rz = Math.floorDiv(chunk.chunkZ, 16);
        Map<RegionCoords, Region> map = this.regions;
        synchronized (map) {
            Region region = this.getRegionAtRegionCoords(rx, ry, rz);
            if (region == null) {
                region = new Region(this, rx, ry, rz);
                this.regions.put(new RegionCoords(rx, ry, rz), region);
            }
            region.putChunk(chunk);
        }
        this.chunks.put(new ChunkCoords(chunk.chunkX, chunk.chunkY, chunk.chunkZ), chunk);
    }

    public void addRegion(Region region) {
        this.regions.put(new RegionCoords(region.regionX, region.regionY, region.regionZ), region);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeChunk(Chunk chunk) {
        this.chunks.remove((Object)new ChunkCoords(chunk.chunkX, chunk.chunkY, chunk.chunkZ));
        Map<RegionCoords, Region> map = this.regions;
        synchronized (map) {
            Region r = this.getRegionAtChunkCoords(chunk.chunkX, chunk.chunkY, chunk.chunkZ);
            if (r != null) {
                r.removeChunk(chunk);
                if (r.isEmpty()) {
                    this.removeRegion(r);
                }
            }
        }
        chunk.dispose();
    }

    public void removeRegion(Region r) {
        if (!r.isEmpty()) {
            Array<Chunk> toRemove = new Array<Chunk>(r.getChunks());
            for (Chunk c : toRemove) {
                this.removeChunk(c);
                r.removeChunk(c);
            }
        }
        this.regions.remove((Object)new RegionCoords(r.regionX, r.regionY, r.regionZ));
        GameSingletons.zoneRenderer.removeRegion(r);
    }

    public Region getRegionAtRegionCoords(int rx, int ry, int rz) {
        return this.regions.get((Object)new RegionCoords(rx, ry, rz));
    }

    public Region getRegionAtChunkCoords(int cx, int cy, int cz) {
        int rx = Math.floorDiv(cx, 16);
        int ry = Math.floorDiv(cy, 16);
        int rz = Math.floorDiv(cz, 16);
        return this.getRegionAtRegionCoords(rx, ry, rz);
    }

    public Chunk getChunkAtChunkCoords(int cx, int cy, int cz) {
        return this.chunks.get((Object)new ChunkCoords(cx, cy, cz));
    }

    public Chunk getChunkAtBlock(int x, int y, int z) {
        int cx = Math.floorDiv(x, 16);
        int cy = Math.floorDiv(y, 16);
        int cz = Math.floorDiv(z, 16);
        return this.getChunkAtChunkCoords(cx, cy, cz);
    }

    public BlockState getBlockState(Vector3 position) {
        return this.getBlockState(position.x, position.y, position.z);
    }

    public BlockState getBlockState(float x, float y, float z) {
        return this.getBlockState((int)x, (int)y, (int)z);
    }

    public BlockState getBlockState(int x, int y, int z) {
        Chunk c = this.getChunkAtBlock(x, y, z);
        if (c == null) {
            return null;
        }
        x -= 16 * Math.floorDiv(x, 16);
        y -= 16 * Math.floorDiv(y, 16);
        z -= 16 * Math.floorDiv(z, 16);
        return c.getBlockState(x, y, z);
    }

    public BlockState getBlockState(Chunk[] candidateChunks, int x, int y, int z) {
        int cx = Math.floorDiv(x, 16);
        int cy = Math.floorDiv(y, 16);
        int cz = Math.floorDiv(z, 16);
        Chunk c = null;
        for (int ci = 0; ci < candidateChunks.length; ++ci) {
            Chunk candidateChunk = candidateChunks[ci];
            if (candidateChunk == null || candidateChunk.chunkX != cx || candidateChunk.chunkY != cy || candidateChunk.chunkZ != cz) continue;
            c = candidateChunk;
            break;
        }
        if (c == null) {
            c = this.getChunkAtBlock(x, y, z);
        }
        if (c == null) {
            return null;
        }
        return c.getBlockState(x -= 16 * cx, y -= 16 * cy, z -= 16 * cz);
    }

    public BlockState getBlockState(Chunk candidateChunk, int x, int y, int z) {
        int cx = Math.floorDiv(x, 16);
        int cy = Math.floorDiv(y, 16);
        int cz = Math.floorDiv(z, 16);
        Chunk c = candidateChunk != null && candidateChunk.chunkX == cx && candidateChunk.chunkY == cy && candidateChunk.chunkZ == cz ? candidateChunk : this.getChunkAtBlock(x, y, z);
        if (c == null) {
            return null;
        }
        return c.getBlockState(x -= 16 * cx, y -= 16 * cy, z -= 16 * cz);
    }

    public short getBlockLight(Chunk candidateChunk, int x, int y, int z) {
        int cx = Math.floorDiv(x, 16);
        int cy = Math.floorDiv(y, 16);
        int cz = Math.floorDiv(z, 16);
        Chunk c = candidateChunk != null && candidateChunk.chunkX == cx && candidateChunk.chunkY == cy && candidateChunk.chunkZ == cz ? candidateChunk : this.getChunkAtBlock(x, y, z);
        if (c == null) {
            return 0;
        }
        return c.getBlockLight(x -= 16 * cx, y -= 16 * cy, z -= 16 * cz);
    }

    public int getSkyLight(Chunk candidateChunk, int x, int y, int z) {
        int cx = Math.floorDiv(x, 16);
        int cy = Math.floorDiv(y, 16);
        int cz = Math.floorDiv(z, 16);
        Chunk c = candidateChunk != null && candidateChunk.chunkX == cx && candidateChunk.chunkY == cy && candidateChunk.chunkZ == cz ? candidateChunk : this.getChunkAtBlock(x, y, z);
        if (c == null) {
            return 0;
        }
        return c.getSkyLight(x -= 16 * cx, y -= 16 * cy, z -= 16 * cz);
    }

    public void setBlockState(BlockState block, int x, int y, int z) {
        int cz;
        int cy;
        int cx = Math.floorDiv(x, 16);
        Chunk c = this.getChunkAtChunkCoords(cx, cy = Math.floorDiv(y, 16), cz = Math.floorDiv(z, 16));
        if (c == null) {
            c = new Chunk(cx, cy, cz);
            c.initChunkData();
            this.addChunk(c);
        }
        c.setBlockState(block, x -= 16 * cx, y -= 16 * cy, z -= 16 * cz);
    }

    public void setBlockState(BlockState block, int x, int y, int z, IBlockDataFactory<BlockState> chunkDataFactory) {
        int cz;
        int cy;
        int cx = Math.floorDiv(x, 16);
        Chunk c = this.getChunkAtChunkCoords(cx, cy = Math.floorDiv(y, 16), cz = Math.floorDiv(z, 16));
        if (c == null) {
            c = new Chunk(cx, cy, cz);
            c.initChunkData(chunkDataFactory);
            this.addChunk(c);
        }
        c.setBlockState(block, x -= 16 * cx, y -= 16 * cy, z -= 16 * cz);
    }

    public Collection<Region> getRegions() {
        return this.regions.values();
    }

    @Override
    public void dispose() {
        for (Chunk c : this.chunks.values()) {
            c.dispose();
        }
    }

    public void calculateSpawn() {
        if (this.spawnPoint == null) {
            BlockState b = null;
            int y = 5000;
            while (b == null || b.walkThrough) {
                b = this.getBlockState(0, y, 0);
                if (--y >= -5000) continue;
                y = 100;
                break;
            }
            this.spawnPoint = new Vector3(0.0f, y, 0.0f);
            this.saveZone(this.world);
        }
    }
}