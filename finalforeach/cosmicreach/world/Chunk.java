package finalforeach.cosmicreach.world;

import com.badlogic.gdx.utils.Array;

import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.savelib.ISavedChunk;
import finalforeach.cosmicreach.savelib.blockdata.BlockDataCompactor;
import finalforeach.cosmicreach.savelib.blockdata.IBlockData;
import finalforeach.cosmicreach.savelib.blockdata.LayeredBlockData;
import finalforeach.cosmicreach.savelib.blockdata.SingleBlockData;
import finalforeach.cosmicreach.savelib.blockdata.layers.BlockSingleLayer;
import finalforeach.cosmicreach.savelib.blockdata.layers.IBlockLayer;
import finalforeach.cosmicreach.savelib.blocks.IBlockDataFactory;
import finalforeach.cosmicreach.savelib.lightdata.blocklight.BlockLightLayeredData;
import finalforeach.cosmicreach.savelib.lightdata.blocklight.IBlockLightData;
import finalforeach.cosmicreach.savelib.lightdata.skylight.ISkylightData;
import finalforeach.cosmicreach.savelib.lightdata.skylight.SkylightLayeredData;
import finalforeach.cosmicreach.savelib.lightdata.skylight.layers.ISkylightDataLayer;
import finalforeach.cosmicreach.savelib.lightdata.skylight.layers.SkylightDataSingleLayer;

public class Chunk implements ISavedChunk<BlockState> {
    public static final int CHUNK_WIDTH = 16;
    public static final int NUM_BLOCKS_IN_CHUNK = 4096;
    public Region region;
    public IChunkMeshGroup<?> meshGroup;
    public int chunkX;
    public int chunkY;
    public int chunkZ;
    public int blockX;
    public int blockY;
    public int blockZ;
    public boolean isGenerated;
    public IBlockData<BlockState> blockData;
    public IBlockLightData blockLightData;
    public ISkylightData skyLightData;
    public transient boolean isSaved = false;

    public Chunk(int chunkX, int chunkY, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
        this.blockX = chunkX * 16;
        this.blockY = chunkY * 16;
        this.blockZ = chunkZ * 16;
    }

    public void initChunkData() {
        this.blockData = new SingleBlockData<BlockState>(Block.AIR.getDefaultBlockState());
    }

    @Override
    public void initChunkData(IBlockDataFactory<BlockState> chunkDataFactory) {
        this.blockData = chunkDataFactory.createChunkData();
    }

    @Override
    public int getMaxNonEmptyBlockIdxYXZ() {
        IBlockData<BlockState> iBlockData = this.blockData;
        if (iBlockData instanceof LayeredBlockData) {
            LayeredBlockData<?> layered = (LayeredBlockData<?>)iBlockData;
            int max = 4096;
            for (int l = 15; l >= 0; --l) {
                IBlockLayer<?> layer = layered.getLayer(l);
                if (layer instanceof BlockSingleLayer) {
                    BlockSingleLayer<?> s = (BlockSingleLayer<?>)layer;
                    BlockState block = (BlockState)s.blockValue;
                    if (!block.hasEmptyModel()) break;
                    max -= 256;
                    continue;
                }
                if (layer != null) break;
                max -= 256;
            }
            return max;
        }
        return 4096;
    }

    public boolean hasNeighbouringBlockLightChunks(Zone zone) {
        Chunk cny = zone.getChunkAtChunkCoords(this.chunkX, this.chunkY - 1, this.chunkZ);
        if (cny == null || cny.blockLightData != null) {
            return true;
        }
        Chunk cpy = zone.getChunkAtChunkCoords(this.chunkX, this.chunkY + 1, this.chunkZ);
        if (cpy == null || cpy.blockLightData != null) {
            return true;
        }
        Chunk cnx = zone.getChunkAtChunkCoords(this.chunkX - 1, this.chunkY, this.chunkZ);
        if (cnx == null || cnx.blockLightData != null) {
            return true;
        }
        Chunk cpx = zone.getChunkAtChunkCoords(this.chunkX + 1, this.chunkY, this.chunkZ);
        if (cpx == null || cpx.blockLightData != null) {
            return true;
        }
        Chunk cnz = zone.getChunkAtChunkCoords(this.chunkX, this.chunkY, this.chunkZ - 1);
        if (cnz == null || cnz.blockLightData != null) {
            return true;
        }
        Chunk cpz = zone.getChunkAtChunkCoords(this.chunkX, this.chunkY, this.chunkZ + 1);
        return cpz == null || cpz.blockLightData != null;
    }

    public boolean isCulledByAdjacentChunks(Zone zone) {
        Chunk cny = zone.getChunkAtChunkCoords(this.chunkX, this.chunkY - 1, this.chunkZ);
        if (cny == null || !cny.isEntirelyOpaque()) {
            return false;
        }
        Chunk cpy = zone.getChunkAtChunkCoords(this.chunkX, this.chunkY + 1, this.chunkZ);
        if (cpy == null || !cpy.isEntirelyOpaque()) {
            return false;
        }
        Chunk cnx = zone.getChunkAtChunkCoords(this.chunkX - 1, this.chunkY, this.chunkZ);
        if (cnx == null || !cnx.isEntirelyOpaque()) {
            return false;
        }
        Chunk cpx = zone.getChunkAtChunkCoords(this.chunkX + 1, this.chunkY, this.chunkZ);
        if (cpx == null || !cpx.isEntirelyOpaque()) {
            return false;
        }
        Chunk cnz = zone.getChunkAtChunkCoords(this.chunkX, this.chunkY, this.chunkZ - 1);
        if (cnz == null || !cnz.isEntirelyOpaque()) {
            return false;
        }
        Chunk cpz = zone.getChunkAtChunkCoords(this.chunkX, this.chunkY, this.chunkZ + 1);
        return cpz != null && cpz.isEntirelyOpaque();
    }

    @Override
    public boolean isEntirelyOpaque() {
        return this.blockData.isEntirely(b -> b != null && b.isOpaque);
    }

    @Override
    public boolean isEntirelyOneBlockSelfCulling() {
        return this.blockData.getUniqueBlockValuesCount() == 1 && this.blockData.isEntirely(b -> b != null && b.cullsSelf);
    }

    public boolean isEntirelyTransparentToSky() {
        return this.blockData.isEntirely(b -> b.lightAttenuation <= 1);
    }

    public BlockState getBlockState(int x, int y, int z) {
        if (this.blockData == null) {
            return null;
        }
        if (x >= 0 && y >= 0 && z >= 0 && x < 16 && y < 16 && z < 16) {
            return this.blockData.getBlockValue(x, y, z);
        }
        return null;
    }

    @Override
    public void setBlockState(BlockState block, int x, int y, int z) {
        this.blockData = this.blockData.setBlockValue(block, x, y, z);
        this.isSaved = false;
    }

    @Override
    public void fillLayer(BlockState blockState, int localY) {
        this.blockData = this.blockData.fillLayer(blockState, localY);
    }

    @Override
    public void fill(BlockState blockState) {
        this.blockData = this.blockData.fill(blockState);
    }

    public Array<Chunk> getSurroundingChunks(Zone zone) {
        Array<Chunk> surroundingChunks = new Array<Chunk>();
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                for (int k = -1; k <= 1; ++k) {
                    Chunk s;
                    if (i == 0 && j == 0 && k == 0 || (s = zone.getChunkAtChunkCoords(this.chunkX + i, this.chunkY + j, this.chunkZ + k)) == null) continue;
                    surroundingChunks.add(s);
                }
            }
        }
        return surroundingChunks;
    }

    public short getBlockLight(int localX, int localY, int localZ) {
        if (this.blockLightData != null) {
            return this.blockLightData.getBlockLight(localX, localY, localZ);
        }
        return 0;
    }

    public int getSkyLight(int localX, int localY, int localZ) {
        if (this.skyLightData != null) {
            return this.skyLightData.getSkyLight(localX, localY, localZ);
        }
        return 0;
    }

    public void setBlockLight(int r, int g, int b, int localX, int localY, int localZ) {
        if (this.blockLightData == null) {
            this.blockLightData = new BlockLightLayeredData();
        }
        this.blockLightData.setBlockLight(r, g, b, localX, localY, localZ);
        this.isSaved = false;
    }

    @Override
    public void setSkyLight(int skylight, int localX, int localY, int localZ) {
        if (this.skyLightData == null) {
            this.skyLightData = new SkylightLayeredData();
        }
        this.skyLightData.setSkyLight(skylight, localX, localY, localZ);
        this.isSaved = false;
    }

    public String toString() {
        return "(" + this.chunkX + ", " + this.chunkY + ", " + this.chunkZ + ")";
    }

    @Override
    public void compactChunkData() {
        ISkylightData iSkylightData;
        this.blockData = BlockDataCompactor.compact(this.blockData);
        if (this.skyLightData != null && (iSkylightData = this.skyLightData) instanceof SkylightLayeredData) {
            SkylightLayeredData layered = (SkylightLayeredData)iSkylightData;
            ISkylightDataLayer[] allLayers = layered.getLayers();
            for (int yLevel = 0; yLevel < allLayers.length; ++yLevel) {
                ISkylightDataLayer layer = allLayers[yLevel];
                if (layer instanceof SkylightDataSingleLayer) continue;
                int layerLightLevel = -1;
                block1: for (int i = 0; i < 16; ++i) {
                    for (int k = 0; k < 16; ++k) {
                        int curSkyLightLevel = layer.getSkyLight(i, k);
                        if (layerLightLevel == -1) {
                            layerLightLevel = curSkyLightLevel;
                            continue;
                        }
                        if (layerLightLevel == curSkyLightLevel) continue;
                        layerLightLevel = -1;
                        break block1;
                    }
                }
                if (layerLightLevel == -1) continue;
                SkylightDataSingleLayer newLayer = new SkylightDataSingleLayer(yLevel, (byte)layerLightLevel);
                layered.setLayer(yLevel, newLayer);
            }
        }
    }

    public void flagTouchingChunksForRemeshing(Zone zone, boolean updateImmediately) {
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                for (int k = -1; k <= 1; ++k) {
                    Chunk nc;
                    if (i == j && j == k && k == 0 || (nc = zone.getChunkAtChunkCoords(this.chunkX + i, this.chunkY + j, this.chunkZ + k)) == null) continue;
                    nc.flagForRemeshing(updateImmediately);
                }
            }
        }
    }

    public void flagHorizontalTouchingChunksForRemeshing(Zone zone, boolean updateImmediately) {
        for (int i = -1; i <= 1; ++i) {
            for (int k = -1; k <= 1; ++k) {
                if (i == 0 && k == 0) continue;
                for (int j = -1; j <= 1; ++j) {
                    Chunk nc = zone.getChunkAtChunkCoords(this.chunkX + i, this.chunkY + j, this.chunkZ + k);
                    if (nc == null) continue;
                    nc.flagForRemeshing(updateImmediately);
                }
            }
        }
    }

    public void flagForRemeshing(boolean updateImmediately) {
        if (this.meshGroup != null) {
            this.meshGroup.flagForRemeshing(updateImmediately);
        }
        if (this.region != null) {
            this.region.flaggedForRemeshing = true;
        }
    }

    public void flagTouchingChunksForRemeshing(Zone zone, int localX, int localY, int localZ, boolean updateImmediately) {
        this.flagForRemeshing(updateImmediately);
        if (localX == 0 || localY == 0 || localZ == 0 || localX == 15 || localY == 15 || localZ == 15) {
            Chunk nc;
            int dz = 0;
            int dy = 0;
            int dx = 0;
            
            //TODO: WARNING ++
            if (localX == 0){} else {dx = localX == 15 ? 1 : 0;}
            if (localY == 0){} else {dy = localY == 15 ? 1 : 0;}
            if (localZ == 0){} else {dz = localZ == 15 ? 1 : 0;}
            if (dx != 0 && (nc = zone.getChunkAtChunkCoords(this.chunkX + dx, this.chunkY, this.chunkZ)) != null) {
                nc.flagForRemeshing(updateImmediately);
            }
            if (dy != 0 && (nc = zone.getChunkAtChunkCoords(this.chunkX, this.chunkY + dy, this.chunkZ)) != null) {
                nc.flagForRemeshing(updateImmediately);
            }
            if (dz != 0 && (nc = zone.getChunkAtChunkCoords(this.chunkX, this.chunkY, this.chunkZ + dz)) != null) {
                nc.flagForRemeshing(updateImmediately);
            }
            if (dx != 0 && dy != 0 && (nc = zone.getChunkAtChunkCoords(this.chunkX + dx, this.chunkY + dy, this.chunkZ)) != null) {
                nc.flagForRemeshing(updateImmediately);
            }
            if (dx != 0 && dz != 0 && (nc = zone.getChunkAtChunkCoords(this.chunkX + dx, this.chunkY, this.chunkZ + dz)) != null) {
                nc.flagForRemeshing(updateImmediately);
            }
            if (dy != 0 && dz != 0 && (nc = zone.getChunkAtChunkCoords(this.chunkX, this.chunkY + dy, this.chunkZ + dz)) != null) {
                nc.flagForRemeshing(updateImmediately);
            }
            if (dx != 0 && dy != 0 && dz != 0 && (nc = zone.getChunkAtChunkCoords(this.chunkX + dx, this.chunkY + dy, this.chunkZ + dz)) != null) {
                nc.flagForRemeshing(updateImmediately);
            }
        }
    }

    public void dispose() {
        if (this.meshGroup != null) {
            this.meshGroup.dispose();
        }
    }

    @Override
    public int getBlockX() {
        return this.blockX;
    }

    @Override
    public int getBlockY() {
        return this.blockY;
    }

    @Override
    public int getBlockZ() {
        return this.blockZ;
    }

    @Override
    public IBlockData<?> getBlockData() {
        return this.blockData;
    }

    @Override
    public int getChunkY() {
        return this.chunkY;
    }

    @Override
    public boolean isSaved() {
        return this.isSaved;
    }

    @Override
    public int getChunkX() {
        return this.chunkX;
    }

    @Override
    public int getChunkZ() {
        return this.chunkZ;
    }

    @Override
    public void setSkylightData(ISkylightData skyLightData) {
        this.skyLightData = skyLightData;
    }
}