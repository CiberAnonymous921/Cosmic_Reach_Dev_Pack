package finalforeach.cosmicreach.savelib.blockdata.layers;

import java.io.IOException;
import java.io.RandomAccessFile;

import finalforeach.cosmicreach.savelib.IChunkByteWriter;
import finalforeach.cosmicreach.savelib.blockdata.LayeredBlockData;

public class BlockShortLayer<T> implements IBlockLayer<T> {
    private final short[] blockIDs;

    public BlockShortLayer(short[] shorts) {
        this.blockIDs = shorts;
    }

    public BlockShortLayer(LayeredBlockData<T> chunkData, int localY, T blockValue) {
        this.blockIDs = new short[256];
        for (int i = 0; i < 16; ++i) {
            for (int k = 0; k < 16; ++k) {
                this.setBlockValue(chunkData, blockValue, i, localY, k);
            }
        }
    }

    public BlockShortLayer(LayeredBlockData<T> chunkData, int localY, BlockByteLayer<T> blockByteLayer) {
        this.blockIDs = new short[256];
        for (int i = 0; i < 16; ++i) {
            for (int k = 0; k < 16; ++k) {
                this.setBlockValue(chunkData, blockByteLayer.getBlockValue(chunkData, i, k), i, localY, k);
            }
        }
    }

    @Override
    public T getBlockValue(LayeredBlockData<T> chunkData, int localX, int localZ) {
        return chunkData.getBlockValueFromPaletteId(this.getBlockValueID(chunkData, localX, localZ));
    }

    @Override
    public int getBlockValueID(LayeredBlockData<T> chunkData, int localX, int localZ) {
        int idx = localX + localZ * 16;
        short blockID = this.blockIDs[idx];
        return blockID;
    }

    @Override
    public void setBlockValue(LayeredBlockData<T> chunkData, T blockValue, int localX, int localY, int localZ) {
        int fullPaletteID;
        if (!chunkData.paletteHasValue(blockValue)) {
            chunkData.addToPalette(blockValue);
        }
        if ((fullPaletteID = chunkData.getBlockValueID(blockValue)) > 4095) {
            chunkData.cleanPalette();
            chunkData.setBlockValue(blockValue, localX, localY, localZ);
            return;
        }
        T oldBlock = this.getBlockValue(chunkData, localX, localZ);
        if (blockValue != oldBlock) {
            int idx = localX + localZ * 16;
            this.blockIDs[idx] = (short)fullPaletteID;
        }
    }

    @Override
    public int getSaveFileConstant(LayeredBlockData<T> chunkData) {
        return 6;
    }

    public static <T> BlockShortLayer<T> fromRandomAccessFileShortArray(RandomAccessFile raf) throws IOException {
        BlockShortLayer<T> layer = new BlockShortLayer<T>(new short[256]);
        int l = layer.blockIDs.length;
        for (int i = 0; i < l; ++i) {
            layer.blockIDs[i] = raf.readShort();
        }
        return layer;
    }

    @Override
    public void writeTo(LayeredBlockData<T> chunkData, IChunkByteWriter allChunksWriter) {
        allChunksWriter.writeShorts(this.blockIDs);
    }
}