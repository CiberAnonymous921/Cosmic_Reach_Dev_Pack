package finalforeach.cosmicreach.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;

import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.savelib.blockdata.LayeredBlockData;
import finalforeach.cosmicreach.savelib.blockdata.SingleBlockData;
import finalforeach.cosmicreach.savelib.blockdata.layers.BlockByteLayer;
import finalforeach.cosmicreach.savelib.blockdata.layers.BlockHalfNibbleLayer;
import finalforeach.cosmicreach.savelib.blockdata.layers.BlockNibbleLayer;
import finalforeach.cosmicreach.savelib.blockdata.layers.BlockShortLayer;
import finalforeach.cosmicreach.savelib.blockdata.layers.BlockSingleLayer;
import finalforeach.cosmicreach.savelib.blockdata.layers.IBlockLayer;
import finalforeach.cosmicreach.savelib.lightdata.blocklight.BlockLightLayeredData;
import finalforeach.cosmicreach.savelib.lightdata.blocklight.layers.BlockLightDataShortLayer;
import finalforeach.cosmicreach.savelib.lightdata.blocklight.layers.BlockLightDataSingleLayer;
import finalforeach.cosmicreach.savelib.lightdata.skylight.SkylightLayeredData;
import finalforeach.cosmicreach.savelib.lightdata.skylight.SkylightSingleData;
import finalforeach.cosmicreach.savelib.lightdata.skylight.layers.ISkylightDataLayer;
import finalforeach.cosmicreach.savelib.lightdata.skylight.layers.SkylightDataNibbleLayer;
import finalforeach.cosmicreach.savelib.lightdata.skylight.layers.SkylightDataSingleLayer;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Region;
import finalforeach.cosmicreach.world.World;
import finalforeach.cosmicreach.world.Zone;
import finalforeach.cosmicreach.worldgen.ChunkColumn;

public class ChunkLoader {
    private static float readFloat(FileInputStream input) throws IOException {
        int intBits = ChunkLoader.readInt(input);
        return Float.intBitsToFloat(intBits);
    }

    public static Vector3 readVector3(FileInputStream input) throws IOException {
        if (input.read() == 1) {
            Vector3 vec = new Vector3();
            vec.x = ChunkLoader.readFloat(input);
            vec.y = ChunkLoader.readFloat(input);
            vec.z = ChunkLoader.readFloat(input);
            return vec;
        }
        return null;
    }

    private static int readInt(FileInputStream input) throws IOException {
        byte[] intBytes = input.readNBytes(4);
        if (intBytes.length != 4) {
            throw new IOException("Not enough bytes to read int.");
        }
        int i = (intBytes[0] & 0xFF) << 24 | (intBytes[1] & 0xFF) << 16 | (intBytes[2] & 0xFF) << 8 | intBytes[3] & 0xFF;
        return i;
    }

    private static String readString(RandomAccessFile raf) throws IOException {
        int byteArrLen = raf.readInt();
        if (byteArrLen == -1) {
            return null;
        }
        byte[] strBytes = new byte[byteArrLen];
        raf.readFully(strBytes);
        return new String(strBytes);
    }

    public static World loadWorld(String worldFolderName) {
        World world;
        String rootFolderName = SaveLocation.getWorldSaveFolderLocation(worldFolderName);
        String worldFileName = rootFolderName + "/worldInfo.json";
        File worldFile = new File(worldFileName);
        if (!worldFile.exists()) {
            return null;
        }
        try {
	        FileInputStream fis = new FileInputStream(worldFile);
	        try {
	            Json worldJson = new Json();
	            World world2 = worldJson.fromJson(World.class, fis);
	            world2.worldFolderName = worldFolderName;
	            world = world2;
	            fis.close();
	            return world;
	        } catch (Throwable throwable) {
	            try {
	                try {
	                    fis.close();
	                } catch (Throwable throwable2) {
	                    throwable.addSuppressed(throwable2);
	                }
	                throw throwable;
	            } catch (IOException e) {
	                e.printStackTrace();
	                return null;
	            }
	        }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } 
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SuppressWarnings("unchecked")
	public static void readChunkColumn(Zone zone, ChunkColumn cc) {
        int rx = Math.floorDiv(cc.chunkX, 16);
        int rz = Math.floorDiv(cc.chunkZ, 16);
        String regionFileName = "region_" + rx + "_0_" + rz + ".cosmicreach";
        File regionFile = new File(zone.getFullSaveFolder() + "/regions/" + regionFileName);
        if (!regionFile.exists()) {
            return;
        }
        try (RandomAccessFile raf = new RandomAccessFile(regionFile, "r");){
            Region region;
            int magic = raf.readInt();
            raf.readInt();
            raf.readInt();
            raf.readInt();
            if (magic != -1257812) {
                throw new RuntimeException("Invalid region file: " + regionFileName);
            }
            Object object = zone.regions;
            synchronized (object) {
                region = zone.getRegionAtRegionCoords(rx, 0, rz);
                if (region == null) {
                    region = new Region(zone, rx, 0, rz);
                    zone.addRegion(region);
                }
            }
            object = region;
            synchronized (object) {
                if (region.fileChunkByteOffsets == null) {
                    int[] chunkByteOffsets = new int[4096];
                    for (int i = 0; i < 4096; ++i) {
                        chunkByteOffsets[i] = raf.readInt();
                    }
                    region.fileChunkByteOffsets = chunkByteOffsets;
                }
                boolean isGenerated = false;
                for (int j = 0; j < 16; ++j) {
                    Object layer;
                    int l;
                    int chunkIndex = region.getChunkIndex(cc.chunkX, region.regionY * 16 + j, cc.chunkZ);
                    int byteOff = region.fileChunkByteOffsets[chunkIndex];
                    if (byteOff == -1) continue;
                    isGenerated = true;
                    raf.seek(byteOff);
                    raf.readInt();
                    raf.readInt();
                    int cx = raf.readInt();
                    int cy = raf.readInt();
                    int cz = raf.readInt();
                    if (chunkIndex != region.getChunkIndex(cx, cy, cz)) {
                        throw new RuntimeException("Invalid chunk X/Z coords: " + regionFileName + ", expected: (" + cc.chunkX + ", " + cc.chunkZ + ") got (" + cx + "," + cz + ")");
                    }
                    Chunk chunk = new Chunk(cx, cy, cz);
                    byte chunkDataType = raf.readByte();
                    switch (chunkDataType) {
                        case 1: {
                            String blockStateSaveKey = ChunkLoader.readString(raf);
                            BlockState b = BlockState.getInstance(blockStateSaveKey);
                            chunk.blockData = new SingleBlockData<BlockState>(b);
                            break;
                        }
                        case 2: {
                            LayeredBlockData<BlockState> chunkData = new LayeredBlockData<BlockState>();
                            chunk.blockData = chunkData;
                            int paletteSize = raf.readInt();
                            for (int i = 0; i < paletteSize; ++i) {
                                String blockStateSaveKey = ChunkLoader.readString(raf);
                                BlockState b = BlockState.getInstance(blockStateSaveKey);
                                chunkData.addToPalette(b);
                            }
                            block45: for (l = 0; l < 16; ++l) {
                                byte layerType = raf.readByte();
                                switch (layerType) {
                                    case 1: {
                                        byte blockId = raf.readByte();
                                        layer = new BlockSingleLayer<BlockState>(chunkData, chunkData.getBlockValueFromPaletteId(blockId));
                                        chunkData.setLayer(l, (IBlockLayer<BlockState>)layer);
                                        continue block45;
                                    }
                                    case 2: {
                                        int blockId = raf.readInt();
                                        layer = new BlockSingleLayer<BlockState>(chunkData, chunkData.getBlockValueFromPaletteId(blockId));
                                        chunkData.setLayer(l, (IBlockLayer<BlockState>)layer);
                                        continue block45;
                                    }
                                    case 3: {
                                        byte[] bytes = new byte[64];
                                        raf.readFully(bytes);
                                        layer = new BlockHalfNibbleLayer<Object>(bytes);
                                        chunkData.setLayer(l, (IBlockLayer<BlockState>)layer);
                                        continue block45;
                                    }
                                    case 4: {
                                        byte[] bytes = new byte[128];
                                        raf.readFully(bytes);
                                        layer = new BlockNibbleLayer<Object>(bytes);
                                        chunkData.setLayer(l, (IBlockLayer<BlockState>)layer);
                                        continue block45;
                                    }
                                    case 5: {
                                        byte[] bytes = new byte[256];
                                        raf.readFully(bytes);
                                        layer = new BlockByteLayer<Object>(bytes);
                                        chunkData.setLayer(l, (IBlockLayer<BlockState>)layer);
                                        continue block45;
                                    }
                                    case 6: {
                                        BlockShortLayer<BlockState> layer2 = BlockShortLayer.fromRandomAccessFileShortArray(raf);
                                        chunkData.setLayer(l, layer2);
                                        continue block45;
                                    }
                                    default: {
                                        throw new RuntimeException("Unknown layerType: " + layerType);
                                    }
                                }
                            }
                            break;
                        }
                        default: {
                            throw new RuntimeException("Unknown chunkDataType: " + chunkDataType);
                        }
                    }
                    byte skylightDataType = raf.readByte();
                    switch (skylightDataType) {
                        case 1: {
                            break;
                        }
                        case 3: {
                            chunk.skyLightData = new SkylightSingleData(chunk, raf.readByte());
                            break;
                        }
                        case 2: {
                            SkylightLayeredData skyLayeredData = new SkylightLayeredData();
                            chunk.skyLightData = skyLayeredData;
                            block46: for (l = 0; l < 16; ++l) {
                                byte layerType = raf.readByte();
                                switch (layerType) {
                                    case 1: {
                                        skyLayeredData.setLayer(l, new SkylightDataSingleLayer(l, raf.readByte()));
                                        continue block46;
                                    }
                                    case 2: {
                                        byte[] bytes = new byte[128];
                                        raf.readFully(bytes);
                                        layer = new SkylightDataNibbleLayer(bytes);
                                        skyLayeredData.setLayer(l, (ISkylightDataLayer)layer);
                                        continue block46;
                                    }
                                    default: {
                                        throw new RuntimeException("Unknown layerType: " + layerType);
                                    }
                                }
                            }
                            break;
                        }
                        default: {
                            throw new RuntimeException("Unknown skylightDataType: " + skylightDataType);
                        }
                    }
                    byte blockLightDataType = raf.readByte();
                    switch (blockLightDataType) {
                        case 1: {
                            break;
                        }
                        case 2: {
                            BlockLightLayeredData blockLightLayeredData = new BlockLightLayeredData();
                            chunk.blockLightData = blockLightLayeredData;
                            block47: for (int l2 = 0; l2 < 16; ++l2) {
                                byte layerType = raf.readByte();
                                switch (layerType) {
                                    case 1: {
                                        byte r = raf.readByte();
                                        byte g = raf.readByte();
                                        byte b = raf.readByte();
                                        blockLightLayeredData.setLayer(l2, new BlockLightDataSingleLayer(blockLightLayeredData, l2, r, g, b));
                                        continue block47;
                                    }
                                    case 2: {
                                        BlockLightDataShortLayer layer3 = BlockLightDataShortLayer.fromRandomAccessFileShortArray(raf);
                                        blockLightLayeredData.setLayer(l2, layer3);
                                        continue block47;
                                    }
                                    default: {
                                        throw new RuntimeException("Unknown layerType: " + layerType);
                                    }
                                }
                            }
                            break;
                        }
                        default: {
                            throw new RuntimeException("Unknown blockLightDataType: " + blockLightDataType);
                        }
                    }
                    chunk.isSaved = true;
                    chunk.isGenerated = true;
                    cc.addChunk(chunk);
                    zone.addChunk(chunk);
                    region.setColumnGeneratedForChunk(chunk, isGenerated);
                }
                cc.isGenerated = isGenerated;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}