package finalforeach.cosmicreach.world;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.PauseableThread;

import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.io.ChunkLoader;
import finalforeach.cosmicreach.io.ChunkSaver;
import finalforeach.cosmicreach.lighting.LightPropagator;
import finalforeach.cosmicreach.settings.GraphicsSettings;
import finalforeach.cosmicreach.worldgen.ChunkColumn;
import finalforeach.cosmicreach.worldgen.ChunkColumnCoords;

public class WorldLoader {
    public static WorldLoader worldLoader = new WorldLoader();
    public long autoSaveIntervalSeconds = 180L;
    public long lastSaveMillis;
    public boolean requestSave;
    PauseableThread worldGenThread;
    HashMap<ChunkColumnCoords, ChunkColumn> loadedChunkColumns = new HashMap<ChunkColumnCoords, ChunkColumn>();
    Array<ChunkColumn> chunkColumnsToGenerate = new Array<ChunkColumn>(ChunkColumn.class);
    Array<ChunkColumnCoords> chunkColumnsToRemove = new Array<ChunkColumnCoords>();
    int lastPlayerChunkX = Integer.MAX_VALUE;
    int lastPlayerChunkZ = Integer.MAX_VALUE;
    int lastRenderDist = GraphicsSettings.renderDistanceInChunks.getValue();
    Zone lastZone = null;
    public boolean readyToPlay;
    public float loadProgress = 0.0f;
    public static Object worldGenLock = new Object();
    GameState lastGameState;

    public WorldLoader() {
        this.lastSaveMillis = System.currentTimeMillis();
        this.worldGenThread = new PauseableThread(() -> {
            World world = InGame.world;
            if (world == null || GameState.currentGameState != GameState.IN_GAME && GameState.currentGameState != GameState.LOADING_GAME) {
                this.lastGameState = GameState.currentGameState;
                return;
            }
            Zone zone = world.getDefaultZone();
            if (zone != null) {
                Object object = worldGenLock;
                synchronized (object) {
                    this.generateWorld(world, zone);
                }
            }
        });
        this.worldGenThread.setName("WorldLoaderThread");
        this.worldGenThread.start();
    }

    public ChunkColumn getChunkColumn(Zone zone, int chunkX, int chunkZ, boolean createIfNoneFound) {
        ChunkColumnCoords colCoords = new ChunkColumnCoords(chunkX, chunkZ);
        ChunkColumn cc = this.loadedChunkColumns.get((Object)colCoords);
        if (cc == null && createIfNoneFound) {
            cc = new ChunkColumn(chunkX, chunkZ);
            ChunkLoader.readChunkColumn(zone, cc);
            Array<Chunk> chunks = new Array<Chunk>();
            cc.getChunks(zone, chunks);
            chunks.forEach(c -> c.flagHorizontalTouchingChunksForRemeshing(zone, false));
            this.loadedChunkColumns.put(colCoords, cc);
        }
        return cc;
    }

    private boolean needToGenDifferentChunks(Zone zone, int localGenRadiusInChunks, int playerChunkX, int playerChunkZ) {
        return localGenRadiusInChunks != this.lastRenderDist || this.lastPlayerChunkX != playerChunkX || this.lastPlayerChunkZ != playerChunkZ || this.lastZone != zone || this.requestSave || this.lastGameState != GameState.currentGameState;
    }

    private void unloadFarAwayChunks(Zone zone, int chunkRadius, int playerChunkX, int playerChunkZ, Array<Chunk> tmpColChunks) {
        this.chunkColumnsToRemove.clear();
        for (Map.Entry<ChunkColumnCoords, ChunkColumn> c : this.loadedChunkColumns.entrySet()) {
            ChunkColumn cc = c.getValue();
            float chunkDistSq = Vector2.dst2(cc.chunkX, cc.chunkZ, playerChunkX, playerChunkZ);
            if (!(chunkDistSq > (float)(1 + chunkRadius * chunkRadius))) continue;
            Array<Chunk> colChunks = cc.getChunks(zone, tmpColChunks);
            for (Chunk chunk : colChunks) {
                if (!chunk.isSaved) {
                    ChunkSaver.saveRegion(zone, chunk.region);
                }
                zone.removeChunk(chunk);
            }
            this.chunkColumnsToRemove.add(c.getKey());
        }
        this.chunkColumnsToRemove.forEach(coords -> this.loadedChunkColumns.remove(coords));
    }

    public void generateWorld(World world, Zone zone) {
        int playerChunkZ;
        int localRenderRadiusInChunks;
        int localGenRadiusInChunks = localRenderRadiusInChunks = GraphicsSettings.renderDistanceInChunks.getValue();
        int lesserRadius = MathUtils.clamp(localGenRadiusInChunks, 6, 12);
        if (GameState.currentGameState == GameState.LOADING_GAME) {
            localGenRadiusInChunks = lesserRadius;
        }
        long curTimeMillis = System.currentTimeMillis();
        if (this.requestSave || curTimeMillis - this.lastSaveMillis > this.autoSaveIntervalSeconds * 1000L) {
            ChunkSaver.saveWorld(world);
            this.lastSaveMillis = curTimeMillis;
            this.requestSave = false;
        }
        Camera worldCamera = InGame.IN_GAME.getWorldCamera();
        int playerChunkX = Math.floorDiv((int)worldCamera.position.x, 16);
        if (!this.needToGenDifferentChunks(zone, localGenRadiusInChunks, playerChunkX, playerChunkZ = Math.floorDiv((int)worldCamera.position.z, 16)) && this.chunkColumnsToGenerate.isEmpty()) {
            if (!this.readyToPlay) {
                this.loadProgress = 1.0f;
                ChunkSaver.saveWorld(world);
                this.lastSaveMillis = curTimeMillis;
            }
            this.readyToPlay = true;
            this.lastGameState = GameState.currentGameState;
            return;
        }
        if (this.lastZone != zone) {
            this.loadedChunkColumns.clear();
        }
        this.chunkColumnsToGenerate.clear();
        this.lastPlayerChunkX = playerChunkX;
        this.lastPlayerChunkZ = playerChunkZ;
        this.lastZone = zone;
        this.lastRenderDist = localGenRadiusInChunks;
        for (int i = -localGenRadiusInChunks; i <= localGenRadiusInChunks && (GameState.currentGameState != GameState.LOADING_GAME || localGenRadiusInChunks == lesserRadius); ++i) {
            for (int k = -localGenRadiusInChunks; k <= localGenRadiusInChunks; ++k) {
                int chunkZ;
                int chunkX;
                float chunkDistSq = Vector2.dst2(chunkX = playerChunkX + i, chunkZ = playerChunkZ + k, playerChunkX, playerChunkZ);
                boolean shouldGenerate = chunkDistSq <= (float)(1 + localGenRadiusInChunks * localGenRadiusInChunks);
                ChunkColumn cc = this.getChunkColumn(zone, chunkX, chunkZ, shouldGenerate);
                if (cc == null || cc.isGenerated) continue;
                this.chunkColumnsToGenerate.add(cc);
            }
        }
        int numToGen = this.chunkColumnsToGenerate.size;
        Array<Chunk> tmpColChunks = new Array<Chunk>();
        this.unloadFarAwayChunks(zone, localRenderRadiusInChunks, playerChunkX, playerChunkZ, tmpColChunks);
        try {
            this.chunkColumnsToGenerate.sort((cA, cB) -> {
                float dASq = Vector2.dst2(playerChunkX, playerChunkZ, cA.chunkX, cA.chunkZ);
                float dBSq = Vector2.dst2(playerChunkX, playerChunkZ, cB.chunkX, cB.chunkZ);
                return (int)(dBSq - dASq);
            });
        } catch (Exception chunkX) {
            // empty catch block
        }
        while (this.chunkColumnsToGenerate.notEmpty() && !this.requestSave) {
            ChunkColumn col = this.chunkColumnsToGenerate.pop();
            this.loadProgress = (float)(numToGen - this.chunkColumnsToGenerate.size) / (float)numToGen;
            if (this.chunkColumnsToRemove.contains(new ChunkColumnCoords(col.chunkX, col.chunkZ), false)) continue;
            zone.zoneGenerator.generateForChunkColumn(zone, col);
            Array<Chunk> colChunks = col.getChunks(zone, tmpColChunks);
            for (Chunk chunk : colChunks) {
                LightPropagator.calculateLightingForChunk(zone, chunk, chunk.chunkY == col.topChunkY);
            }
            col.isGenerated = true;
            for (Chunk chunk : colChunks) {
                if (chunk.isEntirelyTransparentToSky()) {
                    chunk.isGenerated = true;
                    continue;
                }
                for (int i = -1; i <= 1; ++i) {
                    for (int k = -1; k <= 1; ++k) {
                        if (i == 0 && k == 0) continue;
                        ChunkColumn cc = this.getChunkColumn(zone, chunk.chunkX + i, chunk.chunkZ + k, false);
                        if (cc == null) {
                            cc = new ChunkColumn(chunk.chunkX + i, chunk.chunkZ + k);
                            cc.minChunkY = Math.min(cc.minChunkY, chunk.chunkY);
                            cc.topChunkY = Math.max(cc.topChunkY, chunk.chunkY + k);
                            this.loadedChunkColumns.put(new ChunkColumnCoords(cc.chunkX, cc.chunkZ), cc);
                            continue;
                        }
                        if (cc.isGenerated) {
                            for (int j = -1; j <= 1; ++j) {
                                Chunk neighbour = zone.getChunkAtChunkCoords(chunk.chunkX + i, chunk.chunkY + j, chunk.chunkZ + k);
                                if (neighbour != null) continue;
                                neighbour = new Chunk(chunk.chunkX + i, chunk.chunkY + j, chunk.chunkZ + k);
                                neighbour.initChunkData();
                                LightPropagator.calculateLightingForChunk(zone, neighbour, neighbour.chunkY >= cc.topChunkY);
                                zone.addChunk(neighbour);
                            }
                            continue;
                        }
                        cc.minChunkY = Math.min(cc.minChunkY, chunk.chunkY);
                        cc.topChunkY = Math.max(cc.topChunkY, chunk.chunkY + k);
                    }
                }
                chunk.isGenerated = true;
            }
            for (Chunk chunk : colChunks) {
                Region region = zone.getRegionAtChunkCoords(chunk.chunkX, chunk.chunkY, chunk.chunkZ);
                region.setColumnGeneratedForChunk(chunk, true);
            }
            for (Chunk chunk : colChunks) {
                chunk.flagTouchingChunksForRemeshing(zone, false);
            }
            int nextPlayerChunkX = Math.floorDiv((int)worldCamera.position.x, 16);
            if (!this.needToGenDifferentChunks(zone, localGenRadiusInChunks, nextPlayerChunkX, Math.floorDiv((int)worldCamera.position.z, 16))) continue;
            break;
        }
        this.lastGameState = GameState.currentGameState;
    }

    public void requestSave() {
        this.requestSave = true;
    }
}