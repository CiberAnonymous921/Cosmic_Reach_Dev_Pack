package finalforeach.cosmicreach.savelib.blockdata;

import java.util.Arrays;
import java.util.function.Predicate;

import finalforeach.cosmicreach.savelib.IChunkByteWriter;
import finalforeach.cosmicreach.savelib.blockdata.layers.BlockSingleLayer;
import finalforeach.cosmicreach.savelib.blockdata.layers.IBlockLayer;

public class LayeredBlockData<T> implements IBlockData<T> {
    @SuppressWarnings("unchecked")
	private IBlockLayer<T>[] layers = new IBlockLayer[16];
	@SuppressWarnings("unchecked")
	private T[] blockStatePalette = (T[]) new Object[8];
    private int paletteSize = 0;
    private boolean allowCleaning = true;

    public LayeredBlockData() {
    }

    public LayeredBlockData(T defaultBlockState) {
        this.addToPalette(defaultBlockState);
        for (int j = 0; j < 16; ++j) {
            this.fillLayer(defaultBlockState, j);
        }
    }

    public IBlockLayer<T>[] getLayers() {
        return this.layers;
    }

    @Override
    public T getBlockValue(int localX, int localY, int localZ) {
        return this.layers[localY].getBlockValue(this, localX, localZ);
    }

    @Override
    public int getBlockValueID(int localX, int localY, int localZ) {
        return this.layers[localY].getBlockValueID(this, localX, localZ);
    }

    @Override
    public IBlockData<T> setBlockValue(T blockState, int localX, int localY, int localZ) {
        this.layers[localY].setBlockValue(this, blockState, localX, localY, localZ);
        return this;
    }

    @Override
    public IBlockData<T> fill(T blockState) {
        return new SingleBlockData<T>(blockState);
    }

    @Override
    public IBlockData<T> fillLayer(T blockState, int localY) {
        IBlockLayer<T> iBlockLayer = this.layers[localY];
        if (iBlockLayer instanceof BlockSingleLayer) {
            BlockSingleLayer<T> s = (BlockSingleLayer<T>)iBlockLayer;
            s.fill(this, blockState);
        } else {
            this.layers[localY] = new BlockSingleLayer<T>(this, blockState);
        }
        for (int i = 0; i < 16; ++i) {
            IBlockLayer<T> iBlockLayer2 = this.layers[i];
            if (iBlockLayer2 instanceof BlockSingleLayer) {
                BlockSingleLayer<T> s = (BlockSingleLayer<T>)iBlockLayer2;
                if (s.blockValue == blockState) continue;
            }
            return this;
        }
        return this.fill(blockState);
    }

    @Override
    public boolean isEntirely(Predicate<T> predicate) {
        T[] palette = this.blockStatePalette;
        int paletteSize = this.getPaletteSize();
        for (int i = 0; i < paletteSize; ++i) {
            T b = palette[i];
            if (predicate.test(b)) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isEntirely(T blockValue) {
        return this.getPaletteSize() == 1 && this.getBlockValueFromPaletteId(0) == blockValue;
    }

    @Override
    public int getUniqueBlockValuesCount() {
        return this.getPaletteSize();
    }

    public int getPaletteSize() {
        return this.paletteSize;
    }

    public void setLayer(int yLevel, IBlockLayer<T> layer) {
        this.layers[yLevel] = layer;
    }

    public IBlockLayer<T> getLayer(int yLevel) {
        return this.layers[yLevel];
    }

    @Override
    public int getBlockValueID(T blockValue) {
        int paletteSize = this.getPaletteSize();
        for (int i = 0; i < paletteSize; ++i) {
            if (this.blockStatePalette[i] != blockValue) continue;
            return i;
        }
        return -1;
    }

    @Override
    public T getBlockValueFromPaletteId(int bId) {
        return this.blockStatePalette[bId];
    }

    public void addToPalette(T blockValue) {
        if (this.paletteSize == this.blockStatePalette.length) {
            int newSize = (int)((float)this.paletteSize * 1.75f);
            newSize = Math.max(newSize, this.paletteSize + 1);
            this.blockStatePalette = Arrays.copyOf(this.blockStatePalette, newSize);
        }
        this.blockStatePalette[this.paletteSize] = blockValue;
        ++this.paletteSize;
    }

    public boolean paletteHasValue(T blockValue) {
        return this.getBlockValueID(blockValue) != -1;
    }

    @Override
    public int getSaveFileConstant() {
        return 2;
    }

    @Override
    public void writeTo(IChunkByteWriter allChunksWriter) {
        int paletteSize = this.getPaletteSize();
        allChunksWriter.writeInt(paletteSize);
        for (int i = 0; i < paletteSize; ++i) {
            allChunksWriter.writeBlockValue(this.getBlockValueFromPaletteId(i));
        }
        for (IBlockLayer<T> layer : this.getLayers()) {
            allChunksWriter.writeByte(layer.getSaveFileConstant(this));
            layer.writeTo(this, allChunksWriter);
        }
    }

    public void cleanPalette() {
        if (!this.allowCleaning) {
            return;
        }
        int currentPaletteSize = this.getPaletteSize();
        LayeredBlockData<T> tempBlockData = new LayeredBlockData<T>(this.getBlockValue(0, 0, 0));
        tempBlockData.allowCleaning = false;
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    tempBlockData.setBlockValue(this.getBlockValue(i, j, k), i, j, k);
                }
            }
        }
        this.paletteSize = tempBlockData.paletteSize;
        this.blockStatePalette = tempBlockData.blockStatePalette;
        this.layers = tempBlockData.layers;
        int numRemoved = currentPaletteSize - tempBlockData.paletteSize;
        System.out.println("Cleaned up " + numRemoved + " blockstates from palette.");
        if (this.getPaletteSize() > 4096) {
            throw new RuntimeException("Failed to clean palette: This should never happen.");
        }
    }
}