package finalforeach.cosmicreach.world;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

public class RegionOctant {
    private Array<Chunk> chunks = new Array<Chunk>(Chunk.class);
    private int localBlockX;
    private int localBlockY;
    private int localBlockZ;

    public RegionOctant(int localBlockX, int localBlockY, int localBlockZ) {
        this.localBlockX = localBlockX;
        this.localBlockY = localBlockY;
        this.localBlockZ = localBlockZ;
    }

    public void putChunk(Chunk chunk) {
        if (chunk != null && !this.chunks.contains(chunk, true)) {
            this.chunks.add(chunk);
        }
    }

    public void removeChunk(Chunk chunk) {
        this.chunks.removeValue(chunk, true);
    }

    public Array<Chunk> getChunks() {
        return this.chunks;
    }

    public void getBounds(Region region, BoundingBox bounds) {
        bounds.min.set(region.blockX + this.localBlockX, region.blockY + this.localBlockY, region.blockZ + this.localBlockZ);
        bounds.max.set(bounds.min);
        bounds.max.add(128.0f, 128.0f, 128.0f);
        bounds.update();
    }
}