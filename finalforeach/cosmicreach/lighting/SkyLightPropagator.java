package finalforeach.cosmicreach.lighting;

import com.badlogic.gdx.utils.Queue;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.constants.Direction;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;

public class SkyLightPropagator {
	@SuppressWarnings("unused")
    public static void propagateSkyLights(Zone zone, Queue<BlockPosition> lightQueue) {
        while (lightQueue.notEmpty()) {
            BlockPosition position = lightQueue.removeFirst();
            if (position == null) continue;
            Chunk posChunk = position.chunk();
            int l = posChunk.getSkyLight(position.localX(), position.localY(), position.localZ());
            for (Direction d : Direction.ALL_DIRECTIONS) {
                BlockState b;
                boolean copyDown;
                int nLocalX = position.localX() + d.getXOffset();
                int nLocalY = position.localY() + d.getYOffset();
                int nLocalZ = position.localZ() + d.getZOffset();
                Chunk neighbourChunk = posChunk;
                if (nLocalX < 0 || nLocalX >= 16 || nLocalY < 0 || nLocalY >= 16 || nLocalZ < 0 || nLocalZ >= 16) {
                    int nGlobalX = posChunk.blockX + nLocalX;
                    int nGlobalY = posChunk.blockY + nLocalY;
                    int nGlobalZ = posChunk.blockZ + nLocalZ;
                    neighbourChunk = zone.getChunkAtBlock(nGlobalX, nGlobalY, nGlobalZ);
                    if (neighbourChunk == null) continue;
                    nLocalX = nGlobalX - neighbourChunk.blockX;
                    nLocalY = nGlobalY - neighbourChunk.blockY;
                    nLocalZ = nGlobalZ - neighbourChunk.blockZ;
                }
                int ln = neighbourChunk.getSkyLight(nLocalX, nLocalY, nLocalZ);
				boolean bl = copyDown = l == 15 && ln < l && d == Direction.NEG_Y;
                if (!copyDown && ln >= l - 1 || (b = neighbourChunk.getBlockState(nLocalX, nLocalY, nLocalZ)) != null && b.lightAttenuation == 15) continue;
                int nextL = copyDown && b.lightAttenuation == 0 ? l : Math.max(0, l - Math.max(b.lightAttenuation, 1));
                neighbourChunk.setSkyLight(nextL, nLocalX, nLocalY, nLocalZ);
                neighbourChunk.flagTouchingChunksForRemeshing(zone, nLocalX, nLocalY, nLocalZ, false);
                if (nextL <= 1) continue;
                BlockPosition blockPos = new BlockPosition(neighbourChunk, nLocalX, nLocalY, nLocalZ);
                lightQueue.addLast(blockPos);
            }
        }
    }

    public static void propagateShade(Zone zone, Queue<BlockPosition> shadeQueue) {
        Queue<BlockPosition> lightQueue = new Queue<BlockPosition>();
        while (shadeQueue.notEmpty()) {
            BlockPosition position = shadeQueue.removeFirst();
            int l = position.chunk().getSkyLight(position.localX(), position.localY(), position.localZ());
            position.chunk().setSkyLight(0, position.localX(), position.localY(), position.localZ());
            position.chunk().flagTouchingChunksForRemeshing(zone, position.localX(), position.localY(), position.localZ(), false);
            for (Direction d : Direction.ALL_DIRECTIONS) {
                int ln;
                int nLocalX = position.localX() + d.getXOffset();
                int nLocalY = position.localY() + d.getYOffset();
                int nLocalZ = position.localZ() + d.getZOffset();
                Chunk neighbourChunk = position.chunk();
                if (nLocalX < 0 || nLocalX >= 16 || nLocalY < 0 || nLocalY >= 16 || nLocalZ < 0 || nLocalZ >= 16) {
                    int nGlobalX = position.chunk().blockX + nLocalX;
                    int nGlobalY = position.chunk().blockY + nLocalY;
                    int nGlobalZ = position.chunk().blockZ + nLocalZ;
                    neighbourChunk = zone.getChunkAtBlock(nGlobalX, nGlobalY, nGlobalZ);
                    if (neighbourChunk == null) continue;
                    nLocalX = nGlobalX - neighbourChunk.blockX;
                    nLocalY = nGlobalY - neighbourChunk.blockY;
                    nLocalZ = nGlobalZ - neighbourChunk.blockZ;
                }
                if ((ln = neighbourChunk.getSkyLight(nLocalX, nLocalY, nLocalZ)) != 0 && ln < l || d == Direction.NEG_Y && ln == 15) {
                    BlockPosition blockPos = new BlockPosition(neighbourChunk, nLocalX, nLocalY, nLocalZ);
                    shadeQueue.addLast(blockPos);
                    continue;
                }
                if (ln == 0) continue;
                BlockPosition blockPos = new BlockPosition(neighbourChunk, nLocalX, nLocalY, nLocalZ);
                lightQueue.addLast(blockPos);
            }
        }
        if (lightQueue.notEmpty()) {
            SkyLightPropagator.propagateSkyLights(zone, lightQueue);
        }
    }
}