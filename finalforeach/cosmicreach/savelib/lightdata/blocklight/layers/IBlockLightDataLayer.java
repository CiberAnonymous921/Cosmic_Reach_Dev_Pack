package finalforeach.cosmicreach.savelib.lightdata.blocklight.layers;

public interface IBlockLightDataLayer {
    public static final int CHUNK_WIDTH = 16;

    public short getBlockLight(int var1, int var2);

    public void setBlockLight(int var1, int var2, int var3, int var4, int var5);
}