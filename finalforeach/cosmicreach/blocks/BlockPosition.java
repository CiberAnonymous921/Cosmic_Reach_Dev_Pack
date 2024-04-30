package finalforeach.cosmicreach.blocks;

import java.util.Objects;

import finalforeach.cosmicreach.constants.Direction;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;

public class BlockPosition {
    static final int CHUNK_WIDTH = 16;
    public final Chunk chunk;
    public final int localX;
    public final int localY;
    public final int localZ;

    public int hashCode() {
        int result = 1;
        result = 31 * result + (this.chunk == null ? 0 : this.chunk.hashCode());
        result = 31 * result + this.localX;
        result = 31 * result + this.localY;
        result = 31 * result + this.localZ;
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
        BlockPosition other = (BlockPosition)obj;
        return Objects.equals(this.chunk, other.chunk) && this.localX == other.localX && this.localY == other.localY && this.localZ == other.localZ;
    }

    public BlockPosition(Chunk chunk, int localX, int localY, int localZ) {
        this.chunk = chunk;
        this.localX = localX;
        this.localY = localY;
        this.localZ = localZ;
    }

    public BlockPosition getOffsetBlockPos(Zone zone, int offsetX, int offsetY, int offsetZ) {
        if (offsetX == 0 && offsetY == 0 && offsetZ == 0) {
            return this;
        }
        int nLocalX = this.localX + offsetX;
        int nLocalY = this.localY + offsetY;
        int nLocalZ = this.localZ + offsetZ;
        Chunk c = this.chunk;
        if (nLocalX < 0 || nLocalX >= 16 || nLocalY < 0 || nLocalY >= 16 || nLocalZ < 0 || nLocalZ >= 16) {
            int nGlobalX = this.chunk.blockX + nLocalX;
            int nGlobalY = this.chunk.blockY + nLocalY;
            int nGlobalZ = this.chunk.blockZ + nLocalZ;
            c = zone.getChunkAtBlock(nGlobalX, nGlobalY, nGlobalZ);
            if (c == null) {
                return null;
            }
            nLocalX = nGlobalX - c.blockX;
            nLocalY = nGlobalY - c.blockY;
            nLocalZ = nGlobalZ - c.blockZ;
        }
        return new BlockPosition(c, nLocalX, nLocalY, nLocalZ);
    }

    public BlockPosition getOffsetBlockPos(Zone zone, Direction d) {
        return this.getOffsetBlockPos(zone, d.getXOffset(), d.getYOffset(), d.getZOffset());
    }

    public int getGlobalX() {
        return this.chunk.blockX + this.localX;
    }

    public int getGlobalY() {
        return this.chunk.blockY + this.localY;
    }

    public int getGlobalZ() {
        return this.chunk.blockZ + this.localZ;
    }

    public BlockState getBlockState() {
        return this.chunk.getBlockState(this.localX, this.localY, this.localZ);
    }

    public void setBlockState(BlockState targetBlockState) {
        this.chunk.setBlockState(targetBlockState, this.localX, this.localY, this.localZ);
    }

    public int getSkyLight() {
        return this.chunk.getSkyLight(this.localX, this.localY, this.localZ);
    }

    public String toString() {
        return "[" + this.chunk.toString() + ":" + this.localX + ", " + this.localY + ", " + this.localZ + "]";
    }

    public boolean isAt(int chunkX, int chunkY, int chunkZ, int localX, int localY, int localZ) {
        if (this.localX != localX || this.localY != localY || this.localZ != localZ) {
            return false;
        }
        return this.chunk.chunkX == chunkX && this.chunk.chunkY == chunkY && this.chunk.chunkZ == chunkZ;
    }

    public Chunk chunk() {
        return this.chunk;
    }

    public int localX() {
        return this.localX;
    }

    public int localY() {
        return this.localY;
    }

    public int localZ() {
        return this.localZ;
    }

    public int getBlockLight() {
        return this.chunk.getBlockLight(this.localX, this.localY, this.localZ);
    }

    public void setBlockLight(int r, int g, int b) {
        this.chunk.setBlockLight(r, g, b, this.localX, this.localY, this.localZ);
    }

    public void flagTouchingChunksForRemeshing(Zone zone, boolean updateImmediately) {
        this.chunk.flagTouchingChunksForRemeshing(zone, this.localX, this.localY, this.localZ, updateImmediately);
    }
}