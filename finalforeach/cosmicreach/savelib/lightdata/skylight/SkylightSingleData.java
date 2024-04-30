package finalforeach.cosmicreach.savelib.lightdata.skylight;

import finalforeach.cosmicreach.savelib.ISavedChunk;

public class SkylightSingleData implements ISkylightData {
    ISavedChunk<?> chunk;
    public byte lightValue;

    public SkylightSingleData(ISavedChunk<?> chunk, byte lightValue) {
        this.chunk = chunk;
        this.lightValue = lightValue;
    }

    @Override
    public int getSkyLight(int localX, int localY, int localZ) {
        return this.lightValue;
    }

    @Override
    public void setSkyLight(int lightLevel, int localX, int localY, int localZ) {
        if (lightLevel == this.lightValue) {
            return;
        }
        this.chunk.setSkylightData(new SkylightLayeredData(this.lightValue));
        this.chunk.setSkyLight(lightLevel, localX, localY, localZ);
    }

    @Override
    public int getSaveFileConstant() {
        return 3;
    }
}