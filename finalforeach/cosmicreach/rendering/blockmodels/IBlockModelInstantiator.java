package finalforeach.cosmicreach.rendering.blockmodels;

import finalforeach.cosmicreach.blocks.BlockState;

public interface IBlockModelInstantiator {
    public BlockModel getInstance(String var1, int var2);

    public void createGeneratedModelInstance(BlockState var1, BlockModel var2, String var3, String var4, int var5);
}