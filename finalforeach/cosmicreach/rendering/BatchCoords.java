package finalforeach.cosmicreach.rendering;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import finalforeach.cosmicreach.world.Chunk;

public class BatchCoords {
    public static Pool<BatchCoords> pool = Pools.get(BatchCoords.class, Integer.MAX_VALUE);
    public static final int BATCH_WIDTH = 4;
    int batchX;
    int batchY;
    int batchZ;

    public BatchCoords(Chunk chunk) {
        this.setBatchCoordsFromChunk(chunk);
    }

    public BatchCoords() {
    }

    public int hashCode() {
        int result = 1;
        result = 31 * result + this.batchX;
        result = 31 * result + this.batchY;
        result = 31 * result + this.batchZ;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        BatchCoords other = (BatchCoords)obj;
        return this.batchX == other.batchX && this.batchY == other.batchY && this.batchZ == other.batchZ;
    }

    int blockX() {
        return this.batchX * 4 * 16;
    }

    int blockY() {
        return this.batchY * 4 * 16;
    }

    int blockZ() {
        return this.batchZ * 4 * 16;
    }

    public void setBatchCoordsFromChunk(Chunk chunk) {
        this.batchX = Math.floorDiv(chunk.chunkX, 4);
        this.batchY = Math.floorDiv(chunk.chunkY, 4);
        this.batchZ = Math.floorDiv(chunk.chunkZ, 4);
    }
}