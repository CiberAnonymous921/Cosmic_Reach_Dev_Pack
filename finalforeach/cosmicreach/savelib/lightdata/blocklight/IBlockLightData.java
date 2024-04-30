package finalforeach.cosmicreach.savelib.lightdata.blocklight;

public interface IBlockLightData {
    public short getBlockLight(int var1, int var2, int var3);

    public void setBlockLight(int var1, int var2, int var3, int var4, int var5, int var6);

    public int getSaveFileConstant();
}