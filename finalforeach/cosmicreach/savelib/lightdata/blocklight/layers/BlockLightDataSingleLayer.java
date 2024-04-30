package finalforeach.cosmicreach.savelib.lightdata.blocklight.layers;

import finalforeach.cosmicreach.savelib.lightdata.blocklight.BlockLightLayeredData;

public class BlockLightDataSingleLayer implements IBlockLightDataLayer {
    private BlockLightLayeredData lightData;
    public short lightLevel;
    private int yLevel;

    public BlockLightDataSingleLayer(BlockLightLayeredData lightData, int yLevel, int lightLevelRed, int lightLevelGreen, int lightLevelBlue) {
        this.lightData = lightData;
        this.yLevel = yLevel;
        this.lightLevel = (short)((lightLevelRed << 8) + (lightLevelGreen << 4) + lightLevelBlue);
    }

    @Override
    public short getBlockLight(int localX, int localZ) {
        return this.lightLevel;
    }

    @Override
    public void setBlockLight(int lightLevelRed, int lightLevelGreen, int lightLevelBlue, int localX, int localZ) {
        short newLightLevel = (short)((lightLevelRed << 8) + (lightLevelGreen << 4) + lightLevelBlue);
        if (newLightLevel != this.lightLevel) {
            BlockLightDataShortLayer shortLayer = new BlockLightDataShortLayer(this.lightLevel);
            shortLayer.setBlockLight(lightLevelRed, lightLevelGreen, lightLevelBlue, localX, localZ);
            this.lightData.setLayer(this.yLevel, shortLayer);
        }
    }
}
