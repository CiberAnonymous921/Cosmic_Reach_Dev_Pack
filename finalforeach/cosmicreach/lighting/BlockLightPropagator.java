package finalforeach.cosmicreach.lighting;

import com.badlogic.gdx.utils.Queue;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.constants.Direction;
import finalforeach.cosmicreach.world.Zone;

public class BlockLightPropagator {
    public static void propagateBlockDarkness(Zone zone, Queue<BlockPosition> darkQueue) {
        Queue<BlockPosition> lightQueue = new Queue<BlockPosition>();
        while (darkQueue.notEmpty()) {
            BlockPosition position = darkQueue.removeFirst();
            int lpacked = position.getBlockLight();
            int r = (lpacked & 0xF00) >> 8;
            int g = (lpacked & 0xF0) >> 4;
            int b = lpacked & 0xF;
            position.setBlockLight(0, 0, 0);
            position.flagTouchingChunksForRemeshing(zone, false);
            BlockState curBlock = position.getBlockState();
            if (curBlock != null && curBlock.isLightEmitter()) {
                lightQueue.addLast(position);
            }
            for (Direction d : Direction.ALL_DIRECTIONS) {
                BlockState nBlock;
                BlockPosition neighbourPos = position.getOffsetBlockPos(zone, d);
                if (neighbourPos == null) continue;
                int lnpacked = neighbourPos.getBlockLight();
                int nr = (lnpacked & 0xF00) >> 8;
                int ng = (lnpacked & 0xF0) >> 4;
                int nb = lnpacked & 0xF;
                if (nr != 0 && nr < r || ng != 0 && ng < g || nb != 0 && nb < b) {
                    darkQueue.addLast(neighbourPos);
                }
                if (!(nr != 0 && nr >= r || ng != 0 && ng >= g) && (nb == 0 || nb < b) || (nBlock = position.getBlockState()) != null && nBlock.isLightEmitter()) continue;
                lightQueue.addLast(neighbourPos);
            }
        }
        for (BlockPosition lightPos : lightQueue) {
            BlockState nblock = lightPos.getBlockState();
            if (nblock == null) continue;
            int lpacked = lightPos.getBlockLight();
            int r = (lpacked & 0xF00) >> 8;
            int g = (lpacked & 0xF0) >> 4;
            int b = lpacked & 0xF;
            r = Math.max(r, nblock.lightLevelRed);
            g = Math.max(g, nblock.lightLevelGreen);
            b = Math.max(b, nblock.lightLevelBlue);
            lightPos.setBlockLight(r, g, b);
        }
        if (lightQueue.notEmpty()) {
            BlockLightPropagator.propagateBlockLights(zone, lightQueue);
        }
    }

    public static void propagateBlockLights(Zone zone, Queue<BlockPosition> lightQueue) {
        while (lightQueue.notEmpty()) {
            BlockPosition position = lightQueue.removeFirst();
            int lpacked = position.getBlockLight();
            int r = (lpacked & 0xF00) >> 8;
            int g = (lpacked & 0xF0) >> 4;
            int b = lpacked & 0xF;
            BlockState blockState = position.getBlockState();
            if (blockState != null) {
                r = Math.max(r, blockState.lightLevelRed);
                g = Math.max(g, blockState.lightLevelGreen);
                b = Math.max(b, blockState.lightLevelBlue);
            }
            for (Direction d : Direction.ALL_DIRECTIONS) {
                int atten;
                BlockPosition neighbourPos = position.getOffsetBlockPos(zone, d);
                if (neighbourPos == null) continue;
                int lnpacked = neighbourPos.getBlockLight();
                int nr = (lnpacked & 0xF00) >> 8;
                int ng = (lnpacked & 0xF0) >> 4;
                int nb = lnpacked & 0xF;
                BlockState block = neighbourPos.getBlockState();
                if (block != null && block.lightAttenuation == 15 || nr >= r - (atten = Math.max(block.lightAttenuation, 1)) && ng >= g - atten && nb >= b - atten) continue;
                int ir = Math.max(nr, r - atten);
                int ig = Math.max(ng, g - atten);
                int ib = Math.max(nb, b - atten);
                neighbourPos.setBlockLight(ir, ig, ib);
                neighbourPos.chunk.flagTouchingChunksForRemeshing(zone, neighbourPos.localX, neighbourPos.localY, neighbourPos.localZ, false);
                if (ir <= 1 && ig <= 1 && ib <= 1) continue;
                lightQueue.addLast(neighbourPos);
            }
        }
    }
}