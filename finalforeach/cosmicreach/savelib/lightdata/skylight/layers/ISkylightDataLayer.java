package finalforeach.cosmicreach.savelib.lightdata.skylight.layers;

import finalforeach.cosmicreach.savelib.lightdata.skylight.SkylightLayeredData;

public interface ISkylightDataLayer {
    public static final int CHUNK_WIDTH = 16;

    public void setSkyLight(SkylightLayeredData var1, int var2, int var3, int var4);

    public int getSkyLight(int var1, int var2);

    public int getSaveFileConstant();
}