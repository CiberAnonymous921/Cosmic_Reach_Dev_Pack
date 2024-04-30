package finalforeach.cosmicreach.worldgen;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.savelib.blockdata.IBlockData;
import finalforeach.cosmicreach.savelib.blockdata.LayeredBlockData;
import finalforeach.cosmicreach.savelib.blocks.IBlockDataFactory;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;
import finalforeach.cosmicreach.worldgen.noise.SimplexNoise;
import finalforeach.cosmicreach.worldgen.noise.WhiteNoise;

public class MoonZoneGenerator extends ZoneGenerator {
    private SimplexNoise simplexNoise;
    private SimplexNoise simplexNoiseVariance;
    public int groundLevel = 128;
    public int minGroundLevel = 32;
    public int maxGroundLevel = 192;
    BlockState airblock = this.getBlockStateInstance("base:air[default]");
    BlockState groundBlock = this.getBlockStateInstance("base:lunar_soil[default]");
    BlockState craterImpactBlock = this.getBlockStateInstance("base:stone_basalt[default]");
    private WhiteNoise whiteNoiseCratersX;
    private WhiteNoise whiteNoiseCratersY;
    private WhiteNoise whiteNoiseCratersZ;
    private WhiteNoise whiteNoiseCratersH;
    private WhiteNoise whiteNoiseCratersV;
    IBlockDataFactory<BlockState> layeredChunkDataFactory = new IBlockDataFactory<BlockState>(){

        @Override
        public IBlockData<BlockState> createChunkData() {
            LayeredBlockData<BlockState> chunkData = new LayeredBlockData<BlockState>(MoonZoneGenerator.this.airblock);
            return chunkData;
        }
    };

    @Override
    public void create() {
        this.simplexNoise = new SimplexNoise(this.seed);
        this.simplexNoiseVariance = new SimplexNoise(this.seed + 100L);
        this.whiteNoiseCratersX = new WhiteNoise(this.seed + 100L);
        this.whiteNoiseCratersY = new WhiteNoise(this.seed + 200L);
        this.whiteNoiseCratersZ = new WhiteNoise(this.seed + 300L);
        this.whiteNoiseCratersH = new WhiteNoise(this.seed + 400L);
        this.whiteNoiseCratersV = new WhiteNoise(this.seed + 500L);
    }

    @Override
    public void generateForChunkColumn(Zone zone, ChunkColumn col) {
        int[][] maxHeights = new int[CHUNK_WIDTH][CHUNK_WIDTH];
        float[][] localHeightOffset = new float[CHUNK_WIDTH][CHUNK_WIDTH];
        this.maxGroundLevel = 256;
        float offsetRange = this.maxGroundLevel - this.minGroundLevel;
        float maxLocalHeightOffset = 0.0f;
        for (int localX = 0; localX < CHUNK_WIDTH; ++localX) {
            int globalX = localX + col.getBlockX();
            for (int localZ = 0; localZ < CHUNK_WIDTH; ++localZ) {
                int globalZ = localZ + col.getBlockZ();
                float freq = 5.0E-4f;
                float nSum = 0.0f;
                int totalOctaves = 3;
                float octaveWeight = 0.5f;
                float octaveFreq = 1.5f;
                for (int octave = 0; octave < totalOctaves; ++octave) {
                    float noise = this.simplexNoise.noise2(freq * (float)globalX * octaveFreq, freq * (float)globalZ * octaveFreq);
                    float noise2 = this.simplexNoise.noise2(2.0f * freq * (float)globalX * octaveFreq, 2.0f * freq * (float)globalZ * octaveFreq);
                    noise = Math.min(noise, noise2);
                    noise = (noise + 1.0f) / 2.0f;
                    nSum += noise * octaveWeight;
                    octaveWeight /= 2.0f;
                    octaveFreq *= 2.0f;
                }
                float offsetVarianceFreq = 1.0f;
                float offsetVariance = this.simplexNoiseVariance.noise2(freq * (float)globalX * offsetVarianceFreq, freq * (float)globalZ * offsetVarianceFreq);
                offsetVariance = MathUtils.map(-1.0f, 1.0f, 0.1f, 1.0f, offsetVariance);
                offsetVariance = (float)(1.0 - Math.pow(offsetVariance, 2.0));
                offsetVariance = (offsetVariance + 2.0f) / 5.0f;
                localHeightOffset[localX][localZ] = nSum * offsetRange * offsetVariance + (float)this.minGroundLevel;
                maxLocalHeightOffset = Math.max(maxLocalHeightOffset, nSum * offsetRange);
            }
        }
        int maxCraterSize = 64;
        int maxCraterSizeTimes2 = maxCraterSize * 2;
        float[][] craterOffsets = new float[CHUNK_WIDTH][CHUNK_WIDTH];
        float gridX = Math.floorDiv(col.getBlockX(), maxCraterSizeTimes2) * maxCraterSizeTimes2;
        float gridZ = Math.floorDiv(col.getBlockZ(), maxCraterSizeTimes2) * maxCraterSizeTimes2;
        this.generateCraters(col, craterOffsets, maxCraterSize, gridX - (float)maxCraterSizeTimes2, gridZ);
        this.generateCraters(col, craterOffsets, maxCraterSize, gridX, gridZ - (float)maxCraterSizeTimes2);
        this.generateCraters(col, craterOffsets, maxCraterSize, gridX - (float)maxCraterSizeTimes2, gridZ - (float)maxCraterSizeTimes2);
        this.generateCraters(col, craterOffsets, maxCraterSize, gridX, gridZ);
        this.generateCraters(col, craterOffsets, maxCraterSize, gridX + (float)maxCraterSizeTimes2, gridZ);
        this.generateCraters(col, craterOffsets, maxCraterSize, gridX, gridZ + (float)maxCraterSizeTimes2);
        this.generateCraters(col, craterOffsets, maxCraterSize, gridX + (float)maxCraterSizeTimes2, gridZ + (float)maxCraterSizeTimes2);
        for (int cy = 0; cy <= Math.floorDiv(this.maxGroundLevel, CHUNK_WIDTH); ++cy) {
            Chunk chunk = zone.getChunkAtChunkCoords(col.chunkX, cy, col.chunkZ);
            if (chunk != null) continue;
            chunk = new Chunk(col.chunkX, cy, col.chunkZ);
            chunk.initChunkData(this.layeredChunkDataFactory);
            zone.addChunk(chunk);
            col.addChunk(chunk);
            this.generateForChunk(col, chunk, maxHeights, localHeightOffset, craterOffsets);
        }
    }

    private void generateForChunk(ChunkColumn col, Chunk chunk, int[][] maxHeights, float[][] localHeightOffset, float[][] craterOffsets) {
        if (chunk.getBlockY() + CHUNK_WIDTH <= this.groundLevel) {
            chunk.fill(this.groundBlock);
        }
        for (int i = 0; i < CHUNK_WIDTH; ++i) {
            for (int k = 0; k < CHUNK_WIDTH; ++k) {
                float craterOffset = craterOffsets[i][k];
                float offset = craterOffset + localHeightOffset[i][k];
                for (int j = 0; j < CHUNK_WIDTH; ++j) {
                    if (offset < (float)(chunk.getBlockY() + j)) {
                        chunk.setBlockState(this.airblock, i, j, k);
                        continue;
                    }
                    if (craterOffset < 0.0f && Math.abs((float)(chunk.getBlockY() + j) - offset) < 1.0f) {
                        chunk.setBlockState(this.craterImpactBlock, i, j, k);
                        continue;
                    }
                    if (!(offset > (float)(chunk.getBlockY() + j)) || craterOffset == 0.0f) continue;
                    chunk.setBlockState(this.groundBlock, i, j, k);
                }
            }
        }
    }

    private void generateCraters(ChunkColumn col, float[][] craterOffsets, float maxCraterSize, float gridX, float gridZ) {
        float numCraters = 10.0f;
        float sizeMod = 1.0f;
        int n = 0;
        while ((float)n < numCraters) {
            float nFreq = n * 100;
            float xFreq = gridX * 100.0f;
            float zFreq = gridZ * 100.0f;
            float craterHorizRadius = MathUtils.map(-1.0f, 1.0f, 0.0f, maxCraterSize * (sizeMod *= 0.8f), this.whiteNoiseCratersH.noise3D(xFreq, nFreq, zFreq));
            float craterVertRadius = MathUtils.map(-1.0f, 1.0f, 0.0f, 0.25f * maxCraterSize * sizeMod, this.whiteNoiseCratersV.noise3D(xFreq, nFreq, zFreq));
            float craterX = gridX + this.whiteNoiseCratersX.noise3D(xFreq, nFreq, zFreq) * maxCraterSize;
            float craterZ = gridZ + this.whiteNoiseCratersZ.noise3D(xFreq, nFreq, zFreq) * maxCraterSize;
            float craterY = (float)this.groundLevel - MathUtils.map(-1.0f, 1.0f, 0.0f, craterVertRadius / 2.0f, this.whiteNoiseCratersY.noise3D(xFreq, n, zFreq));
            this.generateCrater(col, craterOffsets, craterX, craterY, craterZ, craterHorizRadius, craterVertRadius);
            ++n;
        }
    }

    private void generateCrater(ChunkColumn col, float[][] craterOffsets, float craterX, float craterY, float craterZ, float craterHorizRadius, float craterVertRadius) {
        float edgeSize = Math.min(craterHorizRadius, craterVertRadius) / 2.0f;
        float edgeRadius = craterHorizRadius + edgeSize;
        float edgeRadiusSq = edgeRadius * edgeRadius;
        float craterRadiusSq = craterHorizRadius * craterHorizRadius;
        float dyFactor = craterHorizRadius / craterVertRadius;
        for (int i = 0; i < CHUNK_WIDTH; ++i) {
            float globalX = col.getBlockX() + i;
            for (int k = 0; k < CHUNK_WIDTH; ++k) {
                float globalZ = col.getBlockZ() + k;
                for (float j = craterY; j >= craterY - craterVertRadius; j -= 1.0f) {
                    float dy = (craterY - j) * dyFactor;
                    float distSq = Vector3.dst2(globalX, 0.0f, globalZ, craterX, dy, craterZ);
                    if (distSq < craterRadiusSq) {
                        craterOffsets[i][k] = Math.min(craterOffsets[i][k] - 1.0f, -1.0f);
                        continue;
                    }
                    if (!(distSq < edgeRadiusSq) || !(dy * dy < dyFactor * (edgeRadiusSq - distSq) / (edgeSize * edgeSize)) || !(craterOffsets[i][k] >= 0.0f)) continue;
                    float[] fArray = craterOffsets[i];
                    int n = k;
                    fArray[n] = fArray[n] + 1.0f;
                }
            }
        }
    }

    @Override
    public String getSaveKey() {
        return "base:moon";
    }

    @Override
    protected String getName() {
        return "Moon";
    }
}