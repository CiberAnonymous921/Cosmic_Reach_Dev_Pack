package finalforeach.cosmicreach.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.badlogic.gdx.utils.ByteArray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.savelib.IChunkByteWriter;
import finalforeach.cosmicreach.savelib.ISavedChunk;
import finalforeach.cosmicreach.savelib.blockdata.IBlockData;
import finalforeach.cosmicreach.savelib.lightdata.blocklight.BlockLightLayeredData;
import finalforeach.cosmicreach.savelib.lightdata.blocklight.IBlockLightData;
import finalforeach.cosmicreach.savelib.lightdata.blocklight.layers.BlockLightDataShortLayer;
import finalforeach.cosmicreach.savelib.lightdata.blocklight.layers.BlockLightDataSingleLayer;
import finalforeach.cosmicreach.savelib.lightdata.blocklight.layers.IBlockLightDataLayer;
import finalforeach.cosmicreach.savelib.lightdata.skylight.ISkylightData;
import finalforeach.cosmicreach.savelib.lightdata.skylight.SkylightLayeredData;
import finalforeach.cosmicreach.savelib.lightdata.skylight.SkylightSingleData;
import finalforeach.cosmicreach.savelib.lightdata.skylight.layers.ISkylightDataLayer;
import finalforeach.cosmicreach.savelib.lightdata.skylight.layers.SkylightDataNibbleLayer;
import finalforeach.cosmicreach.savelib.lightdata.skylight.layers.SkylightDataSingleLayer;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Region;
import finalforeach.cosmicreach.world.World;
import finalforeach.cosmicreach.world.Zone;

public class ChunkSaver {
    public static boolean isSaving;

    public static void writeFloat(ByteArray byteArr, float f) {
        ChunkSaver.writeInt(byteArr, Float.floatToRawIntBits(f));
    }

    private static void writeByte(ByteArray byteArr, int i) {
        byteArr.add((byte)i);
    }

    public static void writeShort(ByteArray byteArr, int i) {
        byteArr.add((byte)(i >> 8));
        byteArr.add((byte)i);
    }

    private static void writeInt(ByteArray byteArr, int i) {
        byteArr.add((byte)(i >> 24));
        byteArr.add((byte)(i >> 16));
        byteArr.add((byte)(i >> 8));
        byteArr.add((byte)i);
    }

    private static void setInt(ByteArray byteArr, int index, int val) {
        byteArr.set(index, (byte)(val >> 24));
        byteArr.set(index + 1, (byte)(val >> 16));
        byteArr.set(index + 2, (byte)(val >> 8));
        byteArr.set(index + 3, (byte)val);
    }

    private static void writeString(ByteArray byteArr, String str) {
        if (str == null) {
            ChunkSaver.writeInt(byteArr, -1);
            return;
        }
        byte[] strBytes = str.getBytes();
        ChunkSaver.writeInt(byteArr, strBytes.length);
        byteArr.addAll(strBytes);
    }

    public static void saveWorldInfo(World world, boolean overwrite) {
        String worldFolderName = world.getFullSaveFolder();
        String worldFileName = worldFolderName + "/worldInfo.json";
        File worldFile = new File(worldFileName);
        if (worldFile.exists() && !overwrite) {
            return;
        }
        new File(worldFolderName).mkdirs();
        Json worldInfoJson = new Json();
        worldInfoJson.setOutputType(JsonWriter.OutputType.json);
        try {
            worldFile.createNewFile();
            try (FileOutputStream fos = new FileOutputStream(worldFile);){
                fos.write(worldInfoJson.prettyPrint(world).getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveWorld(World world) {
        try {
            isSaving = true;
            System.out.println("Started saving world");
            ChunkSaver.saveWorldInfo(world, false);
            EntitySaveSystem.savePlayers(world);
            for (Zone z : world.getZones()) {
                ChunkSaver.saveZone(z);
            }
            System.out.println("Finished saving");
        } finally {
            isSaving = false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void saveZone(Zone zone) {
        for (Region r : zone.getRegions()) {
            if (r.isEmpty()) continue;
            Region region = r;
            synchronized (region) {
                boolean shouldSave = false;
                for (ISavedChunk<?> iSavedChunk : r.getChunks()) {
                    if (iSavedChunk.isSaved()) continue;
                    shouldSave = true;
                    break;
                }
                if (shouldSave) {
                    ChunkSaver.saveRegion(zone, r);
                }
            }
        }
    }

    public static void saveRegion(Zone zone, Region region) {
        String zoneFolderName = zone.getFullSaveFolder();
        String regionFolderName = zoneFolderName + "/regions";
        String regionFileName = regionFolderName + "/region_" + region.getRegionX() + "_" + region.getRegionY() + "_" + region.getRegionZ() + ".cosmicreach";
        new File(regionFolderName).mkdirs();
        File regionFile = new File(regionFileName);
        ByteArray regionBytes = new ByteArray();
        ChunkSaver.writeInt(regionBytes, -1257812);
        ChunkSaver.writeInt(regionBytes, 0);
        ChunkSaver.writeInt(regionBytes, 0);
        int[] chunkByteOffsets = new int[4096];
        for (int i = 0; i < chunkByteOffsets.length; ++i) {
            chunkByteOffsets[i] = -1;
        }
        final ByteArray bytesOfAllChunks = new ByteArray();
        IChunkByteWriter allChunksWriter = new IChunkByteWriter(){

            @Override
            public <T> void writeBlockValue(T blockValue) {
                if (!(blockValue instanceof BlockState)) {
                    throw new RuntimeException("writeBlockValue() not implemented for " + blockValue.getClass().getSimpleName());
                }
                BlockState blockState = (BlockState)blockValue;
                String saveKey = blockState.getSaveKey();
                ChunkSaver.writeString(bytesOfAllChunks, saveKey);
            }

            @Override
            public void writeInt(int i) {
                ChunkSaver.writeInt(bytesOfAllChunks, i);
            }

            @Override
            public void writeByte(int b) {
                ChunkSaver.writeByte(bytesOfAllChunks, b);
            }

            @Override
            public void writeBytes(byte[] bytes) {
                for (byte b : bytes) {
                    this.writeByte(b);
                }
            }

            @Override
            public void writeShorts(short[] shorts) {
                for (short s : shorts) {
                    ChunkSaver.writeShort(bytesOfAllChunks, s);
                }
            }
        };
        for (Chunk chunk : region.getChunks()) {
            int byteOff;
            IBlockData<?> blockData = chunk.getBlockData();
            if (blockData == null) continue;
            if (!chunk.isSaved()) {
                chunk.compactChunkData();
            }
            int chunkIndex = region.getChunkIndex(chunk);
            chunkByteOffsets[chunkIndex] = byteOff = bytesOfAllChunks.size;
            ChunkSaver.writeInt(bytesOfAllChunks, 0);
            ChunkSaver.writeInt(bytesOfAllChunks, 0);
            ChunkSaver.writeInt(bytesOfAllChunks, chunk.getChunkX());
            ChunkSaver.writeInt(bytesOfAllChunks, chunk.getChunkY());
            ChunkSaver.writeInt(bytesOfAllChunks, chunk.getChunkZ());
            ChunkSaver.writeByte(bytesOfAllChunks, blockData.getSaveFileConstant());
            blockData.writeTo(allChunksWriter);
            ISkylightData skylightData = chunk.skyLightData;
            if (skylightData == null) {
                ChunkSaver.writeByte(bytesOfAllChunks, 1);
            } else {
                ChunkSaver.writeByte(bytesOfAllChunks, skylightData.getSaveFileConstant());
                if (skylightData instanceof SkylightSingleData) {
                    SkylightSingleData s = (SkylightSingleData)skylightData;
                    ChunkSaver.writeByte(bytesOfAllChunks, s.lightValue);
                } else if (skylightData instanceof SkylightLayeredData) {
                    SkylightLayeredData skyLayerData = (SkylightLayeredData)skylightData;
                    for (ISkylightDataLayer iSkylightDataLayer : skyLayerData.getLayers()) {
                        ChunkSaver.writeByte(bytesOfAllChunks, iSkylightDataLayer.getSaveFileConstant());
                        if (iSkylightDataLayer instanceof SkylightDataSingleLayer) {
                            SkylightDataSingleLayer ssl = (SkylightDataSingleLayer)iSkylightDataLayer;
                            ChunkSaver.writeByte(bytesOfAllChunks, ssl.lightLevel);
                            continue;
                        }
                        if (iSkylightDataLayer instanceof SkylightDataNibbleLayer) {
                            SkylightDataNibbleLayer snl = (SkylightDataNibbleLayer)iSkylightDataLayer;
                            for (byte b : snl.getBytes()) {
                                ChunkSaver.writeByte(bytesOfAllChunks, b);
                            }
                            continue;
                        }
                        throw new RuntimeException("Unknown layer type: " + iSkylightDataLayer.getClass().getSimpleName());
                    }
                } else {
                    throw new RuntimeException("Unknown skylightData type: " + skylightData.getClass().getSimpleName());
                }
            }
            IBlockLightData blocklightData = chunk.blockLightData;
            if (blocklightData == null) {
                ChunkSaver.writeByte(bytesOfAllChunks, 1);
            } else {
                ChunkSaver.writeByte(bytesOfAllChunks, blocklightData.getSaveFileConstant());
                if (blocklightData instanceof BlockLightLayeredData) {
                    BlockLightLayeredData blockLightLayerData = (BlockLightLayeredData)blocklightData;
                    for (IBlockLightDataLayer iBlockLightDataLayer : blockLightLayerData.getLayers()) {
                        if (iBlockLightDataLayer instanceof BlockLightDataSingleLayer) {
                            BlockLightDataSingleLayer bsil = (BlockLightDataSingleLayer)iBlockLightDataLayer;
                            ChunkSaver.writeByte(bytesOfAllChunks, 1);
                            ChunkSaver.writeByte(bytesOfAllChunks, bsil.lightLevel >> 8);
                            ChunkSaver.writeByte(bytesOfAllChunks, bsil.lightLevel >> 4);
                            ChunkSaver.writeByte(bytesOfAllChunks, bsil.lightLevel);
                            continue;
                        }
                        if (iBlockLightDataLayer instanceof BlockLightDataShortLayer) {
                            BlockLightDataShortLayer bshl = (BlockLightDataShortLayer)iBlockLightDataLayer;
                            ChunkSaver.writeByte(bytesOfAllChunks, 2);
                            for (short sh : bshl.getShorts()) {
                                ChunkSaver.writeShort(bytesOfAllChunks, sh);
                            }
                            continue;
                        }
                        throw new RuntimeException("Unknown layer type: " + iBlockLightDataLayer.getClass().getSimpleName());
                    }
                } else {
                    throw new RuntimeException("Unknown blocklightData type: " + blocklightData.getClass().getSimpleName());
                }
            }
            int byteSize = bytesOfAllChunks.size - byteOff;
            ChunkSaver.setInt(bytesOfAllChunks, byteOff, byteSize);
            chunk.isSaved = true;
        }
        boolean regionFileExists = regionFile.exists();
        if (regionFileExists) {
            try (RandomAccessFile raf = new RandomAccessFile(regionFile, "r");){
                int magic = raf.readInt();
                if (magic != -1257812) {
                    throw new RuntimeException("Invalid region file: " + regionFileName);
                }
                raf.readInt();
                raf.readInt();
                raf.readInt();
                int[] fileChunkByteOffsetsOnFile = new int[4096];
                for (int i = 0; i < 4096; ++i) {
                    fileChunkByteOffsetsOnFile[i] = raf.readInt();
                }
                for (int chunkIndex = 0; chunkIndex < 4096; ++chunkIndex) {
                    int byteOff = fileChunkByteOffsetsOnFile[chunkIndex];
                    if (byteOff == -1 || chunkByteOffsets[chunkIndex] != -1) continue;
                    chunkByteOffsets[chunkIndex] = bytesOfAllChunks.size;
                    raf.seek(byteOff);
                    int byteSize = raf.readInt();
                    ChunkSaver.writeInt(bytesOfAllChunks, byteSize);
                    byte[] byArray = new byte[byteSize - 4];
                    raf.read(byArray);
                    bytesOfAllChunks.addAll(byArray);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (bytesOfAllChunks.isEmpty()) {
            return;
        }
        int numChunksWritten = 0;
        for (int i = 0; i < chunkByteOffsets.length; ++i) {
            int byteOff = chunkByteOffsets[i];
            if (byteOff == -1) continue;
            ++numChunksWritten;
        }
        ChunkSaver.writeInt(regionBytes, numChunksWritten);
        int chunkOffsetTableSize = chunkByteOffsets.length * 4;
        int chunkOffsetStart = regionBytes.size + chunkOffsetTableSize;
        region.fileChunkByteOffsets = null;
        for (int i = 0; i < chunkByteOffsets.length; ++i) {
            int byteOff = chunkByteOffsets[i];
            if (byteOff == -1) {
                ChunkSaver.writeInt(regionBytes, -1);
                continue;
            }
            ChunkSaver.writeInt(regionBytes, chunkOffsetStart + byteOff);
        }
        regionBytes.addAll(bytesOfAllChunks);
        try (FileOutputStream fos = new FileOutputStream(regionFile);){
            regionFile.createNewFile();
            fos.write(regionBytes.toArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}