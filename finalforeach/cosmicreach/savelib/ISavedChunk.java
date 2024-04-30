package finalforeach.cosmicreach.savelib;

import finalforeach.cosmicreach.savelib.blockdata.IBlockData;
import finalforeach.cosmicreach.savelib.blocks.IBlockDataFactory;
import finalforeach.cosmicreach.savelib.blocks.IBlockState;
import finalforeach.cosmicreach.savelib.lightdata.skylight.ISkylightData;

public interface ISavedChunk<B extends IBlockState> {
    public static final int CHUNK_WIDTH = 16;
    public static final int NUM_BLOCKS_IN_CHUNK = 4096;

    public boolean isEntirelyOpaque();

    public boolean isEntirelyOneBlockSelfCulling();

    public int getMaxNonEmptyBlockIdxYXZ();

    public int getBlockX();

    public int getBlockY();

    public int getBlockZ();

    public IBlockData<?> getBlockData();

    public void initChunkData(IBlockDataFactory<B> var1);

    public void fillLayer(B var1, int var2);

    public void fill(B var1);

    public void setBlockState(B var1, int var2, int var3, int var4);

    public boolean isSaved();

    public void compactChunkData();

    public int getChunkX();

    public int getChunkY();

    public int getChunkZ();

    public void setSkyLight(int var1, int var2, int var3, int var4);

    public void setSkylightData(ISkylightData var1);
}