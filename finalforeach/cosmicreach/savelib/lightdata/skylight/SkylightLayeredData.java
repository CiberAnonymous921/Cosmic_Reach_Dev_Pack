package finalforeach.cosmicreach.savelib.lightdata.skylight;

import finalforeach.cosmicreach.savelib.lightdata.skylight.layers.ISkylightDataLayer;
import finalforeach.cosmicreach.savelib.lightdata.skylight.layers.SkylightDataSingleLayer;

public class SkylightLayeredData implements ISkylightData {
    private ISkylightDataLayer[] layers = new ISkylightDataLayer[16];

    public SkylightLayeredData() {
        this((byte) 0);
    }

    public SkylightLayeredData(byte skylightValue) {
        for (int i = 0; i < this.layers.length; ++i) {
            this.layers[i] = new SkylightDataSingleLayer(i, skylightValue);
        }
    }

    @Override
    public int getSkyLight(int localX, int localY, int localZ) {
        return this.layers[localY].getSkyLight(localX, localZ);
    }

    @Override
    public void setSkyLight(int lightLevel, int localX, int localY, int localZ) {
        this.layers[localY].setSkyLight(this, lightLevel, localX, localZ);
    }

    public void setLayer(int yLevel, ISkylightDataLayer skyLightLayer) {
        this.layers[yLevel] = skyLightLayer;
    }

    public ISkylightDataLayer getLayer(int yLevel) {
        return this.layers[yLevel];
    }

    public ISkylightDataLayer[] getLayers() {
        return this.layers;
    }

    @Override
    public int getSaveFileConstant() {
        return 2;
    }
}