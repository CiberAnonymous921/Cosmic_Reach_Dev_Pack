package finalforeach.cosmicreach.savelib.blockdata;

import java.util.function.Predicate;

import finalforeach.cosmicreach.savelib.IChunkByteWriter;

public class SingleBlockData<T> implements IBlockData<T> {
    private T blockValue;

    public SingleBlockData() {
    }

    public SingleBlockData(T blockValue) {
        this.blockValue = blockValue;
    }

    public T getBlockState() {
        return this.blockValue;
    }

    @Override
    public T getBlockValue(int localX, int localY, int localZ) {
        return this.blockValue;
    }

    @Override
    public int getBlockValueID(int localX, int localY, int localZ) {
        return 0;
    }

    @Override
    public IBlockData<T> setBlockValue(T blockState, int localX, int localY, int localZ) {
        if (this.blockValue != blockState) {
            LayeredBlockData<T> chunkData = new LayeredBlockData<T>(this.blockValue);
            return chunkData.setBlockValue(blockState, localX, localY, localZ);
        }
        return this;
    }

    @Override
    public IBlockData<T> fill(T blockState) {
        this.blockValue = blockState;
        return this;
    }

    @Override
    public IBlockData<T> fillLayer(T blockState, int localY) {
        if (this.blockValue != blockState) {
            LayeredBlockData<T> chunkData = new LayeredBlockData<T>(this.blockValue);
            return chunkData.fillLayer(blockState, localY);
        }
        return this;
    }

    @Override
    public boolean isEntirely(T blockValue) {
        return this.blockValue == blockValue;
    }

    @Override
    public boolean isEntirely(Predicate<T> predicate) {
        return predicate.test(this.blockValue);
    }

    @Override
    public int getBlockValueID(T blockState) {
        return blockState == this.blockValue ? 0 : -1;
    }

    @Override
    public T getBlockValueFromPaletteId(int bId) {
        return bId == 0 ? (T)this.blockValue : null;
    }

    @Override
    public int getUniqueBlockValuesCount() {
        return 1;
    }

    @Override
    public int getSaveFileConstant() {
        return 1;
    }

    @Override
    public void writeTo(IChunkByteWriter allChunksWriter) {
        allChunksWriter.writeBlockValue(this.blockValue);
    }
}