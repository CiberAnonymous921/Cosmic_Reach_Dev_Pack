package finalforeach.cosmicreach.worldgen;

import com.badlogic.gdx.utils.Array;
import finalforeach.cosmicreach.savelib.ISavedChunk;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;

public class ChunkColumn {
    public int chunkX;
    public int chunkZ;
    public int minChunkY;
    public int topChunkY;
    public boolean isGenerated;

    public ChunkColumn(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public int getBlockX() {
        return this.chunkX * 16;
    }

    public int getBlockZ() {
        return this.chunkZ * 16;
    }

    public void addChunk(ISavedChunk<?> chunk) {
        this.minChunkY = Math.min(this.minChunkY, chunk.getChunkY());
        this.topChunkY = Math.max(this.topChunkY, chunk.getChunkY());
    }

    public Array<Chunk> getChunks(Zone zone, Array<Chunk> colChunks) {
        colChunks.clear();
        for (int chunkY = this.minChunkY; chunkY <= this.topChunkY; ++chunkY) {
            Chunk c = zone.getChunkAtChunkCoords(this.chunkX, chunkY, this.chunkZ);
            if (c == null) continue;
            colChunks.add(c);
        }
        return colChunks;
    }
}