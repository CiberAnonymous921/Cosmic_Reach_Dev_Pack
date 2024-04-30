package finalforeach.cosmicreach.savelib.blockdata.layers;

import finalforeach.cosmicreach.savelib.IChunkByteWriter;
import finalforeach.cosmicreach.savelib.blockdata.LayeredBlockData;

public class BlockByteLayer<T> implements IBlockLayer<T> {
    private final byte[] blockIDs;

    public BlockByteLayer(byte[] bytes) {
        this.blockIDs = bytes;
    }

    public BlockByteLayer(LayeredBlockData<T> chunkData, int localY, T blockValue) {
        this.blockIDs = new byte[256];
        for (int i = 0; i < 16; ++i) {
            for (int k = 0; k < 16; ++k) {
                this.setBlockValue(chunkData, blockValue, i, localY, k);
            }
        }
    }

    public BlockByteLayer(LayeredBlockData<T> chunkData, int localY, BlockNibbleLayer<T> nibbleLayer) {
        this.blockIDs = new byte[256];
        for (int i = 0; i < 16; ++i) {
            for (int k = 0; k < 16; ++k) {
                this.setBlockValue(chunkData, nibbleLayer.getBlockValue(chunkData, i, k), i, localY, k);
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
        byte blockID = this.blockIDs[idx];
        return blockID & 0xFF;
    }

    @Override
    public void setBlockValue(LayeredBlockData<T> chunkData, T blockValue, int localX, int localY, int localZ) {
        int fullPaletteID;
        if (!chunkData.paletteHasValue(blockValue)) {
            chunkData.addToPalette(blockValue);
        }
        if ((fullPaletteID = chunkData.getBlockValueID(blockValue)) > 255) {
            chunkData.cleanPalette();
            if (chunkData.getPaletteSize() <= 255) {
                chunkData.setBlockValue(blockValue, localX, localY, localZ);
                return;
            }
            BlockShortLayer<T> layer = new BlockShortLayer<T>(chunkData, localY, this);
            layer.setBlockValue(chunkData, blockValue, localX, localY, localZ);
            chunkData.setLayer(localY, layer);
            return;
        }
        T oldBlock = this.getBlockValue(chunkData, localX, localZ);
        if (blockValue != oldBlock) {
            int idx = localX + localZ * 16;
            this.blockIDs[idx] = (byte)fullPaletteID;
        }
    }

    public byte[] getBytes() {
        return this.blockIDs;
    }

    @Override
    public int getSaveFileConstant(LayeredBlockData<T> chunkData) {
        return 5;
    }

    @Override
    public void writeTo(LayeredBlockData<T> chunkData, IChunkByteWriter allChunksWriter) {
        allChunksWriter.writeBytes(this.getBytes());
    }
}