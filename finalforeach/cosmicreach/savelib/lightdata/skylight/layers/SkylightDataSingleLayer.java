package finalforeach.cosmicreach.savelib.lightdata.skylight.layers;

import finalforeach.cosmicreach.savelib.lightdata.skylight.SkylightLayeredData;

public class SkylightDataSingleLayer implements ISkylightDataLayer {
    private int yLevel;
    public byte lightLevel;

    public SkylightDataSingleLayer(int yLevel, byte lightLevel) {
        this.yLevel = yLevel;
        this.lightLevel = lightLevel;
    }

    @Override
    public void setSkyLight(SkylightLayeredData skylightData, int lightLevel, int localX, int localZ) {
        if (this.lightLevel != lightLevel) {
            SkylightDataNibbleLayer nibbleLayer = new SkylightDataNibbleLayer(this.lightLevel);
            nibbleLayer.setSkyLight(skylightData, lightLevel, localX, localZ);
            skylightData.setLayer(this.yLevel, nibbleLayer);
        }
    }

    @Override
    public int getSkyLight(int localX, int localZ) {
        return this.lightLevel;
    }

    @Override
    public int getSaveFileConstant() {
        return 1;
    }
}