package finalforeach.cosmicreach.savelib.lightdata.skylight;

public interface ISkylightData {
    public static final int CHUNK_WIDTH = 16;

    public int getSkyLight(int var1, int var2, int var3);

    public void setSkyLight(int var1, int var2, int var3, int var4);

    public int getSaveFileConstant();
}