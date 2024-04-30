package finalforeach.cosmicreach.savelib.blockdata.layers;

import finalforeach.cosmicreach.savelib.IChunkByteWriter;
import finalforeach.cosmicreach.savelib.blockdata.LayeredBlockData;

public class BlockHalfNibbleLayer<T> implements IBlockLayer<T> {
    private final byte[] blockIDs;

    public BlockHalfNibbleLayer(byte[] bytes) {
        this.blockIDs = bytes;
    }

    public BlockHalfNibbleLayer(LayeredBlockData<T> chunkData, int localY, T blockState) {
        this.blockIDs = new byte[64];
        for (int i = 0; i < 16; ++i) {
            for (int k = 0; k < 16; ++k) {
                this.setBlockValue(chunkData, blockState, i, localY, k);
            }
        }
    }

    @Override
    public int getBlockValueID(LayeredBlockData<T> chunkData, int localX, int localZ) {
        int idx = (localX + localZ * 16) / 4;
        byte b = this.blockIDs[idx];
        int blockID = switch (localX % 4) {
            case 0 -> b & 3;
            case 1 -> (b & 0xC) >> 2;
            case 2 -> (b & 0x30) >> 4;
            case 3 -> (b & 0xC0) >> 6;
            default -> throw new IllegalArgumentException("Unexpected value: " + localX % 4);
        };
        return blockID;
    }

    @Override
    public T getBlockValue(LayeredBlockData<T> chunkData, int localX, int localZ) {
        return chunkData.getBlockValueFromPaletteId(this.getBlockValueID(chunkData, localX, localZ));
    }

    @Override
    public void setBlockValue(LayeredBlockData<T> chunkData, T blockValue, int localX, int localY, int localZ) {
        int paletteID = chunkData.getBlockValueID(blockValue);
        if (paletteID == -1) {
            paletteID = chunkData.getPaletteSize();
            chunkData.addToPalette(blockValue);
        }
        if (paletteID > 3) {
            BlockNibbleLayer<T> layer = new BlockNibbleLayer<T>(chunkData, localY, this);
            layer.setBlockValue(chunkData, blockValue, localX, localY, localZ);
            chunkData.setLayer(localY, layer);
            return;
        }
        T oldBlock = this.getBlockValue(chunkData, localX, localZ);
        if (blockValue != oldBlock) {
            int idx = (localX + localZ * 16) / 4;
            byte b = this.blockIDs[idx];
            this.blockIDs[idx] = switch (localX % 4) {
                case 0 -> (byte)(b & 0xFC | paletteID);
                case 1 -> (byte)(b & 0xF3 | paletteID << 2);
                case 2 -> (byte)(b & 0xCF | paletteID << 4);
                case 3 -> (byte)(b & 0x3F | paletteID << 6);
                default -> throw new IllegalArgumentException("Unexpected value: " + localX % 4);
            };
        }
    }

    public byte[] getBytes() {
        return this.blockIDs;
    }

    @Override
    public int getSaveFileConstant(LayeredBlockData<T> chunkData) {
        return 3;
    }

    @Override
    public void writeTo(LayeredBlockData<T> chunkData, IChunkByteWriter allChunksWriter) {
        allChunksWriter.writeBytes(this.getBytes());
    }
}