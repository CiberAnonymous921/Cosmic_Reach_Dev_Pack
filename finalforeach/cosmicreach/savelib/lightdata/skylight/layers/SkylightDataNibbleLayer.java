package finalforeach.cosmicreach.savelib.lightdata.skylight.layers;

import finalforeach.cosmicreach.savelib.lightdata.skylight.SkylightLayeredData;

public class SkylightDataNibbleLayer implements ISkylightDataLayer {
    private byte[] lightLevels = new byte[128];

    public SkylightDataNibbleLayer(byte lightLevel) {
        for (int i = 0; i < this.lightLevels.length; ++i) {
            this.lightLevels[i] = lightLevel;
        }
    }

    public SkylightDataNibbleLayer(byte[] bytes) {
        this.lightLevels = bytes;
    }

    @Override
    public void setSkyLight(SkylightLayeredData skylightData, int lightLevel, int localX, int localZ) {
        int idx = (localX + localZ * 16) / 2;
        byte b = this.lightLevels[idx];
        this.lightLevels[idx] = localX % 2 == 0 ? (byte)(b & 0xF0 | lightLevel) : (byte)(b & 0xF | lightLevel << 4);
    }

    @Override
    public int getSkyLight(int localX, int localZ) {
        int idx = (localX + localZ * 16) / 2;
        if (localX % 2 == 0) {
            return this.lightLevels[idx] & 0xF;
        }
        return (this.lightLevels[idx] & 0xF0) >> 4;
    }

    public byte[] getBytes() {
        return this.lightLevels;
    }

    @Override
    public int getSaveFileConstant() {
        return 2;
    }
}