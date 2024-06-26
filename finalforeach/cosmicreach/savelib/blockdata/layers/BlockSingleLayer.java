package finalforeach.cosmicreach.savelib.blockdata.layers;

import finalforeach.cosmicreach.savelib.IChunkByteWriter;
import finalforeach.cosmicreach.savelib.blockdata.LayeredBlockData;

public class BlockSingleLayer<T> implements IBlockLayer<T> {
    public T blockValue;

    public BlockSingleLayer(LayeredBlockData<T> chunkData, T blockState) {
        this.blockValue = blockState;
        if (!chunkData.paletteHasValue(this.blockValue)) {
            chunkData.addToPalette(blockState);
        }
    }

    public T getBlockState() {
        return this.blockValue;
    }

    @Override
    public T getBlockValue(LayeredBlockData<T> chunkData, int localX, int localZ) {
        return this.blockValue;
    }

    @Override
    public int getBlockValueID(LayeredBlockData<T> chunkData, int localX, int localZ) {
        return chunkData.getBlockValueID(this.blockValue);
    }

    @Override
    public void setBlockValue(LayeredBlockData<T> chunkData, T blockState, int localX, int localY, int localZ) {
        if (this.blockValue == blockState) {
            return;
        }
        BlockNibbleLayer<T> layer = new BlockNibbleLayer<T>(chunkData, localY, this.blockValue);
        layer.setBlockValue(chunkData, blockState, localX, localY, localZ);
        chunkData.setLayer(localY, layer);
    }

    public void fill(LayeredBlockData<T> chunkData, T blockValue) {
        this.blockValue = blockValue;
        if (!chunkData.paletteHasValue(blockValue)) {
            chunkData.addToPalette(blockValue);
        }
    }

    @Override
    public int getSaveFileConstant(LayeredBlockData<T> chunkData) {
        int paletteId = chunkData.getBlockValueID(this.blockValue);
        if (paletteId <= 255) {
            return 1;
        }
        return 2;
    }

    @Override
    public void writeTo(LayeredBlockData<T> chunkData, IChunkByteWriter allChunksWriter) {
        int paletteId = chunkData.getBlockValueID(this.blockValue);
        if (paletteId <= 255) {
            allChunksWriter.writeByte(paletteId);
        } else {
            allChunksWriter.writeInt(paletteId);
        }
    }
}