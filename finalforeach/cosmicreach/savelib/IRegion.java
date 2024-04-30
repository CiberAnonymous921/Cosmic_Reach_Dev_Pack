package finalforeach.cosmicreach.savelib;

import finalforeach.cosmicreach.savelib.blocks.IBlockState;

public interface IRegion<B extends IBlockState> {
    public boolean isEmpty();

    public int getRegionX();

    public int getRegionY();

    public int getRegionZ();
}
