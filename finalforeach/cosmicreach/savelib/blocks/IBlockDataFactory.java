package finalforeach.cosmicreach.savelib.blocks;

import finalforeach.cosmicreach.savelib.blockdata.IBlockData;

public interface IBlockDataFactory<B extends IBlockState> {
    public IBlockData<B> createChunkData();
}