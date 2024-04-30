package finalforeach.cosmicreach.savelib.blockdata;

import finalforeach.cosmicreach.savelib.IChunkByteWriter;
import java.util.function.Predicate;

public interface IBlockData<T> {
    public static final int CHUNK_WIDTH = 16;

    public T getBlockValue(int var1, int var2, int var3);

    public int getBlockValueID(int var1, int var2, int var3);

    public IBlockData<T> setBlockValue(T var1, int var2, int var3, int var4);

    public IBlockData<T> fill(T var1);

    public IBlockData<T> fillLayer(T var1, int var2);

    public int getBlockValueID(T var1);

    public T getBlockValueFromPaletteId(int var1);

    public boolean isEntirely(T var1);

    public boolean isEntirely(Predicate<T> var1);

    public int getUniqueBlockValuesCount();

    public int getSaveFileConstant();

    public void writeTo(IChunkByteWriter var1);
}