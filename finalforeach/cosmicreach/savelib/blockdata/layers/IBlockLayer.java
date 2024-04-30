package finalforeach.cosmicreach.savelib.blockdata.layers;

import finalforeach.cosmicreach.savelib.IChunkByteWriter;
import finalforeach.cosmicreach.savelib.blockdata.LayeredBlockData;

public interface IBlockLayer<T> {
    public static final int CHUNK_WIDTH = 16;
    public static final int NUM_BLOCKS_IN_LAYER = 256;

    public T getBlockValue(LayeredBlockData<T> var1, int var2, int var3);

    public void setBlockValue(LayeredBlockData<T> var1, T var2, int var3, int var4, int var5);

    public int getBlockValueID(LayeredBlockData<T> var1, int var2, int var3);

    public int getSaveFileConstant(LayeredBlockData<T> var1);

    public void writeTo(LayeredBlockData<T> var1, IChunkByteWriter var2);
}