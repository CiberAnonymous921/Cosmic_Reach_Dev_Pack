package finalforeach.cosmicreach.world;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.savelib.IRegion;

public class Region implements IRegion<BlockState> {
    public static final int REGION_WIDTH = 16;
    public static final int NUM_CHUNKS_IN_REGION = 4096;
    public static final int TOTAL_BLOCK_WIDTH = 256;
    public static final int TOTAL_BLOCK_HEIGHT = 256;
    private Array<Chunk> chunks = new Array<Chunk>(Chunk.class);
    public final Zone zone;
    public final BoundingBox boundingBox;
    public final RegionOctant octant_nxnynz = new RegionOctant(0, 0, 0);
    public final RegionOctant octant_nxnypz = new RegionOctant(0, 0, 128);
    public final RegionOctant octant_nxpynz = new RegionOctant(0, 128, 0);
    public final RegionOctant octant_nxpypz = new RegionOctant(0, 128, 128);
    public final RegionOctant octant_pxnynz = new RegionOctant(128, 0, 0);
    public final RegionOctant octant_pxnypz = new RegionOctant(128, 0, 128);
    public final RegionOctant octant_pxpynz = new RegionOctant(128, 128, 0);
    public final RegionOctant octant_pxpypz = new RegionOctant(128, 128, 128);
    public final RegionOctant[] octants = new RegionOctant[]{this.octant_nxnynz, this.octant_nxnypz, this.octant_nxpynz, this.octant_nxpypz, this.octant_pxnynz, this.octant_pxnypz, this.octant_pxpynz, this.octant_pxpypz};
    public final int regionX;
    public final int regionY;
    public final int regionZ;
    public final int blockX;
    public final int blockY;
    public final int blockZ;
    public int[] fileChunkByteOffsets;
    public transient boolean[][] columnsGenerated = new boolean[16][16];
    public boolean flaggedForRemeshing;

    public Region(Zone zone, int regionX, int regionY, int regionZ) {
        this.zone = zone;
        this.regionX = regionX;
        this.regionY = regionY;
        this.regionZ = regionZ;
        this.blockX = regionX * 256;
        this.blockY = regionY * 256;
        this.blockZ = regionZ * 256;
        this.boundingBox = new BoundingBox();
        this.boundingBox.min.set(this.blockX, this.blockY, this.blockZ);
        this.boundingBox.max.set(this.blockX + 256, this.blockY + 256, this.blockZ + 256);
    }

    public Array<Chunk> getChunks() {
        return this.chunks;
    }

    public void putChunk(Chunk chunk) {
        if (chunk != null && !this.chunks.contains(chunk, true)) {
            chunk.region = this;
            this.chunks.add(chunk);
            RegionOctant octant = this.getOctant(chunk);
            octant.putChunk(chunk);
        }
    }

    public RegionOctant getOctant(Chunk chunk) {
        int midBlockX = this.blockX + 128;
        int midBlockY = this.blockY + 128;
        int midBlockZ = this.blockZ + 128;
        if (chunk.blockX < midBlockX) {
            if (chunk.blockY < midBlockY) {
                if (chunk.blockZ < midBlockZ) {
                    return this.octant_nxnynz;
                }
                return this.octant_nxnypz;
            }
            if (chunk.blockZ < midBlockZ) {
                return this.octant_nxpynz;
            }
            return this.octant_nxpypz;
        }
        if (chunk.blockY < midBlockY) {
            if (chunk.blockZ < midBlockZ) {
                return this.octant_pxnynz;
            }
            return this.octant_pxnypz;
        }
        if (chunk.blockZ < midBlockZ) {
            return this.octant_pxpynz;
        }
        return this.octant_pxpypz;
    }

    public void removeChunk(Chunk chunk) {
        this.chunks.removeValue(chunk, true);
        RegionOctant octant = this.getOctant(chunk);
        octant.removeChunk(chunk);
    }

    @Override
    public boolean isEmpty() {
        return this.chunks.isEmpty();
    }

    public int getNumberOfChunks() {
        return this.chunks.size;
    }

    public int getChunkIndex(int cx, int cy, int cz) {
        int x = cx - this.regionX * 16;
        int y = cy - this.regionY * 16;
        int z = cz - this.regionZ * 16;
        return y * 16 * 16 + x * 16 + z;
    }

    public int getChunkIndex(Chunk chunk) {
        return this.getChunkIndex(chunk.chunkX, chunk.chunkY, chunk.chunkZ);
    }

    public Chunk getChunkByIndex(Zone zone, int index) {
        int cz = index % 16 + this.regionZ * 16;
        int cy = (index /= 16) % 16 + this.regionY * 16;
        int cx = (index /= 16) + this.regionX * 16;
        return zone.getChunkAtChunkCoords(cx, cy, cz);
    }

    public boolean isColumnGeneratedForChunk(Chunk chunk) {
        int x = chunk.chunkX - this.regionX * 16;
        int z = chunk.chunkZ - this.regionZ * 16;
        return this.columnsGenerated[x][z];
    }

    public boolean isColumnGeneratedForChunkIndex(int index) {
        int z = index % 16;
        index /= 16;
        int x = index /= 16;
        return this.columnsGenerated[x][z];
    }

    public void setColumnGeneratedForChunk(Chunk chunk, boolean isGenerated) {
        int x = chunk.chunkX - this.regionX * 16;
        int z = chunk.chunkZ - this.regionZ * 16;
        this.columnsGenerated[x][z] = isGenerated;
    }

    public boolean isColumnGeneratedForChunkCol(int chunkX, int chunkZ) {
        int x = chunkX - this.regionX * 16;
        int z = chunkZ - this.regionZ * 16;
        return this.columnsGenerated[x][z];
    }

    public boolean isFlaggedForRemeshing() {
        return this.flaggedForRemeshing;
    }

    @Override
    public int getRegionX() {
        return this.regionX;
    }

    @Override
    public int getRegionY() {
        return this.regionY;
    }

    @Override
    public int getRegionZ() {
        return this.regionZ;
    }
}