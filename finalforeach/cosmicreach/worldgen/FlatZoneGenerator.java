package finalforeach.cosmicreach.worldgen;

import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.savelib.blockdata.IBlockData;
import finalforeach.cosmicreach.savelib.blockdata.SingleBlockData;
import finalforeach.cosmicreach.savelib.blocks.IBlockDataFactory;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;

public class FlatZoneGenerator extends ZoneGenerator {
    public int groundLevel = 32;
    BlockState airblock = this.getBlockStateInstance("base:air[default]");
    BlockState stoneBlock = this.getBlockStateInstance("base:stone_basalt[default]");
    BlockState grassBlock = this.getBlockStateInstance("base:grass[default]");
    BlockState dirtBlock = this.getBlockStateInstance("base:dirt[default]");
    IBlockDataFactory<BlockState> chunkDataFactory = new IBlockDataFactory<BlockState>(){

        @Override
        public IBlockData<BlockState> createChunkData() {
            SingleBlockData<BlockState> chunkData = new SingleBlockData<BlockState>(FlatZoneGenerator.this.airblock);
            return chunkData;
        }
    };

    @Override
    public void create() {
    }

    @Override
    public String getSaveKey() {
        return "base:flat";
    }

    @Override
    protected String getName() {
        return "Flat world";
    }

    @Override
    public void generateForChunkColumn(Zone zone, ChunkColumn col) {
        int maxCy = 15;
        for (int cy = 0; cy <= maxCy; ++cy) {
            Chunk chunk = zone.getChunkAtChunkCoords(col.chunkX, cy, col.chunkZ);
            if (chunk == null) {
                chunk = new Chunk(col.chunkX, cy, col.chunkZ);
                chunk.initChunkData(this.chunkDataFactory);
                zone.addChunk(chunk);
                col.addChunk(chunk);
            }
            if (cy >= Math.floorDiv(this.groundLevel, CHUNK_WIDTH)) continue;
            for (int localY = 0; localY < CHUNK_WIDTH; ++localY) {
                if (chunk.getBlockY() + localY >= this.groundLevel - 1) {
                    chunk.fillLayer(this.grassBlock, localY);
                    continue;
                }
                if (chunk.getBlockY() + localY >= this.groundLevel - 3) {
                    chunk.fillLayer(this.dirtBlock, localY);
                    continue;
                }
                chunk.fillLayer(this.stoneBlock, localY);
            }
        }
    }
}