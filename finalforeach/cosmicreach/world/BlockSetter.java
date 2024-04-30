package finalforeach.cosmicreach.world;

import com.badlogic.gdx.utils.Queue;

import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.constants.Direction;
import finalforeach.cosmicreach.lighting.BlockLightPropagator;
import finalforeach.cosmicreach.lighting.SkyLightPropagator;

public class BlockSetter {
    public static void replaceBlocks(Zone zone, BlockState targetBlockState, Queue<BlockPosition> allBlockPosToReplace, Queue<BlockPosition> tmpQueue) {
        for (BlockPosition bp : allBlockPosToReplace) {
            BlockSetter.replaceBlock(zone, targetBlockState, bp, tmpQueue);
        }
    }

    public static void replaceBlock(Zone zone, BlockState targetBlockState, BlockPosition blockPos, Queue<BlockPosition> tmpQueue) {
        BlockState oldBlockState = blockPos.getBlockState();
        if (targetBlockState != oldBlockState) {
            boolean propagateSkylight;
            int oldSkylightAttenuation = 0;
            int skylightAttenuation = targetBlockState.lightAttenuation;
            if (oldBlockState != null) {
                oldSkylightAttenuation = oldBlockState.lightAttenuation;
            }
            blockPos.setBlockState(targetBlockState);
            int currentSkylight = blockPos.getSkyLight();
            tmpQueue.clear();
            tmpQueue.addFirst(blockPos);
            BlockLightPropagator.propagateBlockDarkness(zone, tmpQueue);
            tmpQueue.clear();
            tmpQueue.addFirst(blockPos);
            BlockLightPropagator.propagateBlockLights(zone, tmpQueue);
            boolean propagateShade = currentSkylight > 0 && skylightAttenuation > oldSkylightAttenuation;
            propagateSkylight = currentSkylight != 15 && skylightAttenuation < oldSkylightAttenuation;
            if (propagateShade || propagateSkylight) {
                tmpQueue.clear();
                if (propagateShade) {
                    tmpQueue.addFirst(blockPos);
                    SkyLightPropagator.propagateShade(zone, tmpQueue);
                } else {
                    tmpQueue.addFirst(blockPos.getOffsetBlockPos(zone, Direction.POS_X));
                    tmpQueue.addFirst(blockPos.getOffsetBlockPos(zone, Direction.POS_Y));
                    tmpQueue.addFirst(blockPos.getOffsetBlockPos(zone, Direction.POS_Z));
                    tmpQueue.addFirst(blockPos.getOffsetBlockPos(zone, Direction.NEG_X));
                    tmpQueue.addFirst(blockPos.getOffsetBlockPos(zone, Direction.NEG_Y));
                    tmpQueue.addFirst(blockPos.getOffsetBlockPos(zone, Direction.NEG_Z));
                    SkyLightPropagator.propagateSkyLights(zone, tmpQueue);
                }
            }
            blockPos.chunk().flagTouchingChunksForRemeshing(zone, blockPos.localX(), blockPos.localY(), blockPos.localZ(), true);
            GameSingletons.meshGenThread.requestImmediateResorting();
        }
    }
}