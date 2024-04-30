package finalforeach.cosmicreach.worldgen;

import com.badlogic.gdx.math.MathUtils;

import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.savelib.blockdata.IBlockData;
import finalforeach.cosmicreach.savelib.blockdata.SingleBlockData;
import finalforeach.cosmicreach.savelib.blocks.IBlockDataFactory;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;
import finalforeach.cosmicreach.worldgen.noise.SimplexNoise;
import finalforeach.cosmicreach.worldgen.noise.WhiteNoise;

public class InfiniteBasicZoneGenerator extends ZoneGenerator {
    private SimplexNoise simplexNoise;
    private SimplexNoise simplexNoise2;
    private WhiteNoise whiteNoiseTrees;
    private WhiteNoise whiteNoiseTreeLights;
    private WhiteNoise whiteNoise;
    public int seaLevel = 256;
    public int minGroundLevel = 32;
    public int maxGroundLevel = 128;
    BlockState airblock = Block.AIR.getDefaultBlockState();
    BlockState stoneBlock = Block.STONE_BASALT.getDefaultBlockState();
    BlockState grassBlock = Block.GRASS.getDefaultBlockState();
    BlockState grassFullBlock = Block.getBlockStateInstance("base:grass[type=full]");
    BlockState dirtBlock = Block.DIRT.getDefaultBlockState();
    BlockState snowBlock = Block.SNOW.getDefaultBlockState();
    BlockState waterBlock = Block.WATER.getDefaultBlockState();
    BlockState sandBlock = Block.SAND.getDefaultBlockState();
    BlockState oakLeavesBlock = Block.SNOW.getDefaultBlockState();
    BlockState oakLogBlock = Block.TREELOG.getDefaultBlockState();
    IBlockDataFactory<BlockState> layeredChunkDataFactory = new IBlockDataFactory<BlockState>(){

        @Override
        public IBlockData<BlockState> createChunkData() {
            SingleBlockData<BlockState> chunkData = new SingleBlockData<BlockState>(InfiniteBasicZoneGenerator.this.airblock);
            return chunkData;
        }
    };

    @Override
    public String getSaveKey() {
        return "base:basic";
    }

    @Override
    protected String getName() {
        return "Basic Earthlike";
    }

    @Override
    public void create() {
        this.simplexNoise = new SimplexNoise(this.seed);
        this.simplexNoise2 = new SimplexNoise(this.seed + 100L);
        this.whiteNoise = new WhiteNoise(this.seed);
        this.setWhiteNoiseTrees(new WhiteNoise(this.seed + 100L));
        this.setWhiteNoiseTreeLights(new WhiteNoise(this.seed + 200L));
    }

    private static float lerp2D(float dx, float dy, float x1y1, float x1y2, float x2y1, float x2y2) {
        float a = MathUtils.lerp(x1y1, x1y2, dy);
        float b = MathUtils.lerp(x2y1, x2y2, dy);
        return MathUtils.lerp(a, b, dx);
    }

    private static float lerp3D(float dx, float dy, float dz, float x1y1z1, float x1y1z2, float x1y2z1, float x1y2z2, float x2y1z1, float x2y1z2, float x2y2z1, float x2y2z2) {
        float a = InfiniteBasicZoneGenerator.lerp2D(dx, dy, x1y1z1, x1y2z1, x2y1z1, x2y2z1);
        float b = InfiniteBasicZoneGenerator.lerp2D(dx, dy, x1y1z2, x1y2z2, x2y1z2, x2y2z2);
        return MathUtils.lerp(a, b, dz);
    }

    private static float getInterpolatedNoise(int div, float[][][] noises, int localX, int localY, int localZ) {
        int floorX = Math.floorDiv(localX, div) * div;
        int floorY = Math.floorDiv(localY, div) * div;
        int floorZ = Math.floorDiv(localZ, div) * div;
        int ceilX = floorX + div;
        int ceilY = floorY + div;
        int ceilZ = floorZ + div;
        float n1 = noises[floorX / div][floorY / div][floorZ / div];
        float n2 = noises[ceilX / div][floorY / div][floorZ / div];
        float n3 = noises[floorX / div][ceilY / div][floorZ / div];
        float n4 = noises[ceilX / div][ceilY / div][floorZ / div];
        float n5 = noises[floorX / div][floorY / div][ceilZ / div];
        float n6 = noises[ceilX / div][floorY / div][ceilZ / div];
        float n7 = noises[floorX / div][ceilY / div][ceilZ / div];
        float n8 = noises[ceilX / div][ceilY / div][ceilZ / div];
        float dx = (float)(localX - floorX) / (float)div;
        float dy = (float)(localY - floorY) / (float)div;
        float dz = (float)(localZ - floorZ) / (float)div;
        return InfiniteBasicZoneGenerator.lerp3D(dx, dy, dz, n1, n5, n3, n7, n2, n6, n4, n8);
    }

    private void generateForChunk(ChunkColumn col, Chunk chunk, int[][] maxHeights, float[][] localHeightOffset) {
        if (chunk.blockY + 16 <= this.minGroundLevel) {
            chunk.fill(this.stoneBlock);
        }
        float freq = 0.01f;
        int div = 8;
        float[][][] noises = new float[1 + 16 / div][1 + 16 / div][1 + 16 / div];
        for (int i = 0; i < 1 + 16 / div; ++i) {
            for (int j = 0; j < 1 + 16 / div; ++j) {
                for (int k = 0; k < 1 + 16 / div; ++k) {
                    noises[i][j][k] = this.simplexNoise.noise3_XZBeforeY(freq * (float)(chunk.blockX + i * div), freq * (float)(chunk.blockY + j * div), freq * (float)(chunk.blockZ + k * div));
                }
            }
        }
        for (int localY = 0; localY < 16; ++localY) {
            int globalY = chunk.blockY + localY;
            if (globalY <= this.minGroundLevel) {
                chunk.fillLayer(this.stoneBlock, localY);
                continue;
            }
            if (globalY <= this.seaLevel) {
                chunk.fillLayer(this.waterBlock, localY);
            }
            for (int localX = 0; localX < 16; ++localX) {
                for (int localZ = 0; localZ < 16; ++localZ) {
                    float hDiff = localHeightOffset[localX][localZ] + (float)this.maxGroundLevel - (float)globalY;
                    float hRange = this.maxGroundLevel - this.minGroundLevel;
                    float hDensity = hDiff / hRange;
                    float hDensityWeight = 2.0f;
                    float n = InfiniteBasicZoneGenerator.getInterpolatedNoise(div, noises, localX, localY, localZ);
                    n = (n + 1.0f) / 2.0f;
                    n *= n;
                    float nDensity = (n + hDensity * hDensityWeight) / (hDensityWeight + 1.0f);
                    boolean isCave = false;
                    if (!(nDensity > 0.5f) || isCave) continue;
                    chunk.setBlockState(this.stoneBlock, localX, localY, localZ);
                    maxHeights[localX][localZ] = globalY;
                }
            }
        }
    }

    @Override
    public void generateForChunkColumn(Zone zone, ChunkColumn col) {
        float[][] localHeightOffset = new float[16][16];
        float offsetRange = 768 - this.maxGroundLevel;
        float maxLocalHeightOffset = 0.0f;
        for (int localX = 0; localX < 16; ++localX) {
            int globalX = localX + col.getBlockX();
            for (int localZ = 0; localZ < 16; ++localZ) {
                int globalZ = localZ + col.getBlockZ();
                float freq = 0.001f;
                float n = this.simplexNoise.noise2(freq * (float)globalX, freq * (float)globalZ);
                float n2 = this.simplexNoise.noise2(2.0f * freq * (float)globalX, 2.0f * freq * (float)globalZ);
                n = Math.min(n, n2);
                n = (n + 1.0f) / 2.0f;
                localHeightOffset[localX][localZ] = n * offsetRange;
                maxLocalHeightOffset = Math.max(maxLocalHeightOffset, n * offsetRange);
            }
        }
        int[][] maxHeights = new int[16][16];
        int maxPossibleY = Math.max(this.seaLevel, this.maxGroundLevel + (int)maxLocalHeightOffset);
        int topCy = Math.max(col.topChunkY, Math.floorDiv(maxPossibleY, 16) + 1);
        for (int cy = col.minChunkY; cy < topCy; ++cy) {
            Chunk chunk = zone.getChunkAtChunkCoords(col.chunkX, cy, col.chunkZ);
            if (chunk == null) {
                chunk = new Chunk(col.chunkX, cy, col.chunkZ);
                chunk.initChunkData(this.layeredChunkDataFactory);
                zone.addChunk(chunk);
            }
            col.addChunk(chunk);
            this.generateForChunk(col, chunk, maxHeights, localHeightOffset);
        }
        for (int localX = 0; localX < 16; ++localX) {
            int globalX = localX + col.getBlockX();
            for (int localZ = 0; localZ < 16; ++localZ) {
                int globalZ = localZ + col.getBlockZ();
                boolean hasSnow = (double)this.simplexNoise.noise2((float)globalX / 50.0f, (float)globalZ / 50.0f) < 0.2 + (double)(this.whiteNoise.noise2D(globalX, globalZ) / 3.0f);
                int maxH = maxHeights[localX][localZ];
                double w = this.simplexNoise2.noise2((float)globalX / 4.0f, (float)globalZ / 4.0f);
                w = (w + 1.0) / 2.0;
                int numDown = 2 + (int)(w * 3.0);
                for (int globalY = maxH; globalY > maxH - numDown; --globalY) {
                    Chunk chunk = zone.getChunkAtChunkCoords(col.chunkX, Math.floorDiv(globalY, 16), col.chunkZ);
                    int localY = globalY - chunk.blockY;
                    BlockState blockStateAbove = zone.getBlockState(chunk, globalX, globalY + 1, globalZ);
                    Block blockAbove = blockStateAbove.getBlock();
                    if (globalY <= this.seaLevel + numDown || blockAbove == Block.WATER || blockAbove == Block.SAND) {
                        chunk.setBlockState(this.sandBlock, localX, localY, localZ);
                        continue;
                    }
                    if ((double)globalY < 504.0 + w * 80.0) {
                        if (globalY == maxH) {
                            if (hasSnow) {
                                chunk.setBlockState(Block.SNOW.getDefaultBlockState(), localX, localY, localZ);
                                continue;
                            }
                            chunk.setBlockState(this.grassFullBlock, localX, localY, localZ);
                            continue;
                        }
                        if (blockStateAbove == this.grassFullBlock) {
                            chunk.setBlockState(this.grassBlock, localX, localY, localZ);
                            continue;
                        }
                        chunk.setBlockState(this.dirtBlock, localX, localY, localZ);
                        continue;
                    }
                    if (!((double)globalY > 584.0 + w * 40.0)) continue;
                    chunk.setBlockState(this.snowBlock, localX, localY, localZ);
                }
            }
        }
        if (col.chunkZ == 0) {
            int wallWidth = 7;
            BlockState slab = Block.getBlockStateInstance("base:lunar_soil[default,slab_type=bottom]");
            BlockState vnz = Block.getBlockStateInstance("base:lunar_soil[default,slab_type=verticalNegZ]");
            BlockState vpz = Block.getBlockStateInstance("base:lunar_soil[default,slab_type=verticalPosZ]");
            for (int x = 0; x < 16; ++x) {
                int globalX = x + col.chunkX * 16;
                int minY = maxHeights[x][0] - 7;
                int maxWh = Math.max(25, 25 + this.seaLevel - minY);
                col.topChunkY = Math.max(col.topChunkY, Math.floorDiv(minY + maxWh + 1, 16));
                for (int wh = 0; wh < maxWh; ++wh) {
                    BlockState topBlock = Block.STONE_BASALT.getDefaultBlockState();
                    BlockState topSideBlock = Block.STONE_BASALT.getDefaultBlockState();
                    if (wh == maxWh - 1) {
                        topBlock = Block.AIR.getDefaultBlockState();
                        topSideBlock = slab;
                    } else if (wh == maxWh - 2) {
                        topBlock = Block.WOODPLANKS.getDefaultBlockState();
                    }
                    zone.setBlockState(topSideBlock, globalX, minY + wh, 1);
                    for (int ww = 1; ww < wallWidth - 1; ++ww) {
                        if (wh == maxWh - 2 && 3.0f * ((this.whiteNoise.noise2D(globalX, ww + 1) + 1.0f) / 2.0f) > 2.0f) {
                            zone.setBlockState(Block.SNOW.getDefaultBlockState(), globalX, minY + wh, ww + 1);
                            continue;
                        }
                        zone.setBlockState(topBlock, globalX, minY + wh, ww + 1);
                    }
                    zone.setBlockState(topSideBlock, globalX, minY + wh, wallWidth);
                }
                int spacing = 4;
                for (int wh = 0; wh < maxWh + 1; ++wh) {
                    if (Math.abs(globalX) % spacing != 0) continue;
                    if (wh == maxWh - 3) {
                        // empty if block
                    }
                    zone.setBlockState(vpz, globalX, minY + wh, 0);
                    zone.setBlockState(vnz, globalX, minY + wh, wallWidth + 1);
                    zone.setBlockState(vpz, globalX + 1, minY + wh, 0);
                    zone.setBlockState(vnz, globalX + 1, minY + wh, wallWidth + 1);
                }
            }
        }
    }

    public WhiteNoise getWhiteNoiseTrees() {
		return whiteNoiseTrees;
	}

	public void setWhiteNoiseTrees(WhiteNoise whiteNoiseTrees) {
		this.whiteNoiseTrees = whiteNoiseTrees;
	}

	public WhiteNoise getWhiteNoiseTreeLights() {
		return whiteNoiseTreeLights;
	}

	public void setWhiteNoiseTreeLights(WhiteNoise whiteNoiseTreeLights) {
		this.whiteNoiseTreeLights = whiteNoiseTreeLights;
	}
}