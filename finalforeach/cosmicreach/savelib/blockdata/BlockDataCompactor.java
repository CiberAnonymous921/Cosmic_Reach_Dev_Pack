package finalforeach.cosmicreach.savelib.blockdata;

import finalforeach.cosmicreach.savelib.blockdata.layers.BlockSingleLayer;
import finalforeach.cosmicreach.savelib.blockdata.layers.IBlockLayer;

public class BlockDataCompactor {
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> IBlockData<T> compact(IBlockData<T> blockData) {
        if (blockData instanceof LayeredBlockData) {
			LayeredBlockData layered = (LayeredBlockData)blockData;
            IBlockLayer<T>[] allLayers = layered.getLayers();
            for (int yLevel = 0; yLevel < allLayers.length; ++yLevel) {
                IBlockLayer layer = allLayers[yLevel];
                if (layer instanceof BlockSingleLayer) continue;
                Object layerBlockState = null;
                block1: for (int i = 0; i < 16; ++i) {
                    for (int k = 0; k < 16; ++k) {
                        Object curBlockState = layer.getBlockValue(layered, i, k);
                        if (layerBlockState == null) {
                            layerBlockState = curBlockState;
                            continue;
                        }
                        if (layerBlockState == curBlockState) continue;
                        layerBlockState = null;
                        break block1;
                    }
                }
                if (layerBlockState == null) continue;
                BlockSingleLayer<Object> newLayer = new BlockSingleLayer<Object>(layered, layerBlockState);
                layered.setLayer(yLevel, newLayer);
            }
            if (layered.getPaletteSize() == 1) {
                SingleBlockData newChunkData = new SingleBlockData();
                blockData = newChunkData.fill(layered.getBlockValueFromPaletteId(0));
            }
        }
        return blockData;
    }
}