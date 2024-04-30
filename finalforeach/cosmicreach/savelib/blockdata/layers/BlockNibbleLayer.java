package finalforeach.cosmicreach.savelib.blockdata.layers;

import finalforeach.cosmicreach.savelib.IChunkByteWriter;
import finalforeach.cosmicreach.savelib.blockdata.LayeredBlockData;

public class BlockNibbleLayer<T> implements IBlockLayer<T> {
    private final byte[] blockIDs;

    public BlockNibbleLayer(byte[] bytes) {
        this.blockIDs = bytes;
    }

    public BlockNibbleLayer(LayeredBlockData<T> chunkData, int localY, T blockValue) {
        this.blockIDs = new byte[128];
        int paletteID = chunkData.getBlockValueID(blockValue);
        if (paletteID != 0) {
            for (int i = 0; i < 16; ++i) {
                for (int k = 0; k < 16; ++k) {
                    this.setBlockValue(chunkData, blockValue, i, localY, k);
                }
            }
        }
    }

    public byte[] getBytes() {
        return this.blockIDs;
    }

    public BlockNibbleLayer(LayeredBlockData<T> chunkData, int localY, BlockHalfNibbleLayer<T> halfNibbleLayer) {
        this.blockIDs = new byte[128];
        for (int i = 0; i < 16; ++i) {
            for (int k = 0; k < 16; ++k) {
                this.setBlockValue(chunkData, halfNibbleLayer.getBlockValue(chunkData, i, k), i, localY, k);
            }
        }
    }

    @Override
    public T getBlockValue(LayeredBlockData<T> chunkData, int localX, int localZ) {
        return chunkData.getBlockValueFromPaletteId(this.getBlockValueID(chunkData, localX, localZ));
    }

    @Override
    public int getBlockValueID(LayeredBlockData<T> chunkData, int localX, int localZ) {
        int idx = (localX + localZ * 16) / 2;
        byte b = this.blockIDs[idx];
        int blockID = localX % 2 == 0 ? b & 0xF : (b & 0xF0) >> 4;
        return blockID;
    }

    @Override
    public void setBlockValue(LayeredBlockData<T> chunkData, T blockValue, int localX, int localY, int localZ) {
        int paletteID = chunkData.getBlockValueID(blockValue);
        if (paletteID == -1) {
            paletteID = chunkData.getPaletteSize();
            chunkData.addToPalette(blockValue);
        }
        if (paletteID > 15) {
            BlockByteLayer<T> layer = new BlockByteLayer<T>(chunkData, localY, this);
            layer.setBlockValue(chunkData, blockValue, localX, localY, localZ);
            chunkData.setLayer(localY, layer);
            return;
        }
        int oldBlockID = this.getBlockValueID(null, localX, localZ);
        if (paletteID != oldBlockID) {
            int idx = (localX + localZ * 16) / 2;
            byte b = this.blockIDs[idx];
            this.blockIDs[idx] = localX % 2 == 0 ? (byte)(b & 0xF0 | paletteID) : (byte)(b & 0xF | paletteID << 4);
        }
    }

    @Override
    public int getSaveFileConstant(LayeredBlockData<T> chunkData) {
        return 4;
    }

    @Override
    public void writeTo(LayeredBlockData<T> chunkData, IChunkByteWriter allChunksWriter) {
        allChunksWriter.writeBytes(this.getBytes());
    }
}