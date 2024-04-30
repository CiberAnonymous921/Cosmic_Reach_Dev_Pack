package finalforeach.cosmicreach.savelib.lightdata.blocklight;

import finalforeach.cosmicreach.savelib.lightdata.blocklight.layers.BlockLightDataSingleLayer;
import finalforeach.cosmicreach.savelib.lightdata.blocklight.layers.IBlockLightDataLayer;

public class BlockLightLayeredData implements IBlockLightData {
    protected static final int CHUNK_WIDTH = 16;
    private IBlockLightDataLayer[] layers = new IBlockLightDataLayer[16];

    public BlockLightLayeredData() {
        for (int i = 0; i < this.layers.length; ++i) {
            this.layers[i] = new BlockLightDataSingleLayer(this, i, 0, 0, 0);
        }
    }

    @Override
    public short getBlockLight(int localX, int localY, int localZ) {
        return this.layers[localY].getBlockLight(localX, localZ);
    }

    @Override
    public void setBlockLight(int lightLevelRed, int lightLevelGreen, int lightLevelBlue, int localX, int localY, int localZ) {
        this.layers[localY].setBlockLight(lightLevelRed, lightLevelGreen, lightLevelBlue, localX, localZ);
    }

    public void setLayer(int yLevel, IBlockLightDataLayer layer) {
        this.layers[yLevel] = layer;
    }

    public IBlockLightDataLayer[] getLayers() {
        return this.layers;
    }

    @Override
    public int getSaveFileConstant() {
        return 2;
    }
}