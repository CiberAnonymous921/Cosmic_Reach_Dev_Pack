package finalforeach.cosmicreach.worldgen;

import com.badlogic.gdx.math.Vector2;

import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.savelib.blockdata.IBlockData;
import finalforeach.cosmicreach.savelib.blockdata.SingleBlockData;
import finalforeach.cosmicreach.savelib.blocks.IBlockDataFactory;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;
import finalforeach.cosmicreach.worldgen.noise.SimplexNoise;

public class NostalgicIslandZoneGenerator extends ZoneGenerator {
    public int groundLevel = 32;
    BlockState airblock = this.getBlockStateInstance("base:air[default]");
    BlockState waterblock = this.getBlockStateInstance("base:water[default]");
    BlockState stoneBasaltBlock = this.getBlockStateInstance("base:stone_basalt[default]");
    BlockState grassBlock = this.getBlockStateInstance("base:grass[default]");
    BlockState sandBlock = this.getBlockStateInstance("base:sand[default]");
    BlockState dirtBlock = this.getBlockStateInstance("base:dirt[default]");
    private SimplexNoise simplexNoise;
    IBlockDataFactory<BlockState> chunkDataFactory = new IBlockDataFactory<BlockState>(){

        @Override
        public IBlockData<BlockState> createChunkData() {
            SingleBlockData<BlockState> chunkData = new SingleBlockData<BlockState>(NostalgicIslandZoneGenerator.this.airblock);
            return chunkData;
        }
    };

    @Override
    public void create() {
        this.simplexNoise = new SimplexNoise(this.seed);
    }

    @Override
    public String getSaveKey() {
        return "base:nostalgic_island";
    }

    @Override
    protected String getName() {
        return "Nostalgic Islands";
    }

    @Override
    public void generateForChunkColumn(Zone zone, ChunkColumn col) {
        int maxCy = 15;
        for (int cy = 0; cy <= maxCy; ++cy) {
            Chunk chunk = zone.getChunkAtChunkCoords(col.chunkX, cy, col.chunkZ);
            if (chunk == null) {
                chunk = new Chunk(col.chunkX, cy, col.chunkZ);
                chunk.initChunkData(this.chunkDataFactory);
                zone.addChunk(chunk);
                col.addChunk(chunk);
            }
            this.generateForChunk(chunk);
        }
    }

    private void generateForChunk(Chunk chunk) {
        Zone zone = chunk.region.zone;
        for (int localY = 0; localY < CHUNK_WIDTH; ++localY) {
            int globalY = chunk.blockY + localY;
            if (globalY == 0) {
                chunk.fillLayer(this.stoneBasaltBlock, localY);
            } else if (globalY < this.groundLevel) {
                chunk.fillLayer(this.waterblock, localY);
            }
            if (globalY <= 0) continue;
            for (int localX = 0; localX < CHUNK_WIDTH; ++localX) {
                int globalX = chunk.blockX + localX;
                for (int localZ = 0; localZ < CHUNK_WIDTH; ++localZ) {
                    int globalZ = chunk.blockZ + localZ;
                    float freq = 0.005f;
                    float freq2 = 0.01f;
                    float freq3 = 0.02f;
                    float freq4 = 0.05f;
                    int currentGround = (int)((float)this.groundLevel + 8.0f * this.simplexNoise.noise2((float)globalX * freq, (float)globalZ * freq) + 14.0f * this.simplexNoise.noise2((float)globalX * freq2, (float)globalZ * freq2) + 2.0f * this.simplexNoise.noise2((float)globalX * freq3, (float)globalZ * freq3) + 1.0f * this.simplexNoise.noise2((float)globalX * freq4, (float)globalZ * freq4));
                    float distFromCent = Vector2.len(globalX, globalZ);
                    float d = distFromCent / 3.0f;
                    if (globalY < currentGround && (float)currentGround - d > (float)(globalY - currentGround)) {
                        if (globalY <= this.groundLevel + 3) {
                            chunk.setBlockState(this.sandBlock, localX, localY, localZ);
                            if (zone.getBlockState(globalX, globalY - 2, globalZ) != this.sandBlock || globalY <= 0) continue;
                            zone.setBlockState(this.stoneBasaltBlock, globalX, globalY - 2, globalZ);
                            continue;
                        }
                        chunk.setBlockState(this.dirtBlock, localX, localY, localZ);
                        if (zone.getBlockState(globalX, globalY - 2, globalZ) != this.dirtBlock || globalY <= 0) continue;
                        zone.setBlockState(this.stoneBasaltBlock, globalX, globalY - 2, globalZ);
                        continue;
                    }
                    if (chunk.getBlockState(localX, localY, localZ) != this.airblock || globalY <= 0 || zone.getBlockState(globalX, globalY - 1, globalZ) != this.dirtBlock || globalY <= 0) continue;
                    zone.setBlockState(this.grassBlock, globalX, globalY - 1, globalZ);
                }
            }
        }
    }
}