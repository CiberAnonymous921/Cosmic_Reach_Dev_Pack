package finalforeach.cosmicreach.rendering;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.FloatArray;

import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.savelib.ISavedChunk;
import finalforeach.cosmicreach.savelib.blockdata.IBlockData;
import finalforeach.cosmicreach.savelib.lightdata.blocklight.IBlockLightData;
import finalforeach.cosmicreach.savelib.lightdata.skylight.ISkylightData;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.IChunkMeshGroup;
import finalforeach.cosmicreach.world.Zone;

public class ChunkMeshGroup implements IChunkMeshGroup<Array<MeshData>>,
Disposable {
    public static final int CHUNK_WIDTH = 16;
    public static boolean setMeshGenRecently = true;
    public transient int meshGenCount = 0;
    public transient int expectedMeshGenCount = -1;
    private transient boolean verticesSet = false;
    private transient Array<MeshData> allMeshData = new Array<MeshData>(true, 1, MeshData.class);
    public transient boolean remeshImmediately = false;
    private transient AtomicInteger remeshRequests = new AtomicInteger();

    @Override
    public Array<MeshData> buildMeshVertices(ISavedChunk<?> chunk) {
        if (chunk.getBlockData() == null) {
            return null;
        }
        return ChunkMeshGroup.getMeshData(chunk);
    }

    @SuppressWarnings("unchecked")
    private static Array<MeshData> getMeshData(ISavedChunk<?> savedChunk) {
        BlockState airBlockState = Block.AIR.getDefaultBlockState();
        Chunk chunk = (Chunk)savedChunk;
        Array<MeshData> meshDatas = new Array<MeshData>();
		IBlockData<BlockState> blockData = (IBlockData<BlockState>) chunk.getBlockData();
        if (blockData.isEntirely(airBlockState)) {
            return meshDatas;
        }
        Zone zone = chunk.region.zone;
        boolean isEntirelyOpaque = chunk.isEntirelyOpaque();
        if (isEntirelyOpaque && chunk.isCulledByAdjacentChunks(zone)) {
            return meshDatas;
        }
        boolean isEntirelyOneBlockSelfCulling = chunk.isEntirelyOneBlockSelfCulling();
        short[] blockLightLevels = new short[8];
        boolean hasNeighbouringBlockLightChunks = chunk.blockLightData != null || chunk.hasNeighbouringBlockLightChunks(zone);
        MeshData meshData = new MeshData(ChunkShader.DEFAULT_BLOCK_SHADER, RenderOrder.DEFAULT);
        MeshData transparentMeshData = new MeshData(ChunkShader.DEFAULT_BLOCK_SHADER, RenderOrder.TRANSPARENT);
        MeshData waterMeshData = new MeshData(ChunkShader.WATER_BLOCK_SHADER, RenderOrder.TRANSPARENT);
        int maxIdx = chunk.getMaxNonEmptyBlockIdxYXZ();
        int airBlockId = blockData.getBlockValueID(Block.AIR.getDefaultBlockState());
        for (int idx = 0; idx < maxIdx; ++idx) {
            int localY = idx / 256;
            int localX = (idx - localY * 256) / 16;
            int localZ = (idx - localY * 256) % 16;
            if ((isEntirelyOpaque || isEntirelyOneBlockSelfCulling) && localX != 0 && localX != 15 && localY != 0 && localY != 15 && localZ != 0 && localZ != 15) {
                idx += 13;
                continue;
            }
            int bId = blockData.getBlockValueID(localX, localY, localZ);
            if (bId == airBlockId) continue;
            BlockState b = (BlockState)blockData.getBlockValueFromPaletteId(bId);
            boolean cullsSelf = b.cullsSelf;
            int globalX = chunk.getBlockX() + localX;
            int globalY = chunk.getBlockY() + localY;
            int globalZ = chunk.getBlockZ() + localZ;
            int opaqueBitmask = 0;
            BlockState bnx = zone.getBlockState(chunk, globalX - 1, globalY, globalZ);
            BlockState bpx = zone.getBlockState(chunk, globalX + 1, globalY, globalZ);
            BlockState bny = zone.getBlockState(chunk, globalX, globalY - 1, globalZ);
            BlockState bpy = zone.getBlockState(chunk, globalX, globalY + 1, globalZ);
            BlockState bnz = zone.getBlockState(chunk, globalX, globalY, globalZ - 1);
            BlockState bpz = zone.getBlockState(chunk, globalX, globalY, globalZ + 1);
            int completeCullMask = 0;
            completeCullMask |= bnx == null || bnx.isPosXFaceOccluding ? 1 : 0;
            completeCullMask |= bpx == null || bpx.isNegXFaceOccluding ? 2 : 0;
            completeCullMask |= bny == null || bny.isPosYFaceOccluding ? 4 : 0;
            completeCullMask |= bpy == null || bpy.isNegYFaceOccluding ? 8 : 0;
            completeCullMask |= bnz == null || bnz.isPosZFaceOccluding ? 16 : 0;
            if ((completeCullMask |= bpz == null || bpz.isNegZFaceOccluding ? 32 : 0) == 63) continue;
            Block block = b.getBlock();
            opaqueBitmask |= bnx == null || bnx.isPosXFaceOccluding || cullsSelf && block == bnx.getBlock() && bnx.isSelfPosXFaceOccluding ? 1 : 0;
            opaqueBitmask |= bpx == null || bpx.isNegXFaceOccluding || cullsSelf && block == bpx.getBlock() && bpx.isSelfNegXFaceOccluding ? 2 : 0;
            opaqueBitmask |= bny == null || bny.isPosYFaceOccluding || cullsSelf && block == bny.getBlock() && bny.isSelfPosYFaceOccluding ? 4 : 0;
            opaqueBitmask |= bpy == null || bpy.isNegYFaceOccluding || cullsSelf && block == bpy.getBlock() && bpy.isSelfNegYFaceOccluding ? 8 : 0;
            opaqueBitmask |= bnz == null || bnz.isPosZFaceOccluding || cullsSelf && block == bnz.getBlock() && bnz.isSelfPosZFaceOccluding ? 16 : 0;
            opaqueBitmask |= bpz == null || bpz.isNegZFaceOccluding || cullsSelf && block == bpz.getBlock() && bpz.isSelfNegZFaceOccluding ? 32 : 0;
            BlockState bnxnynz = zone.getBlockState(chunk, globalX - 1, globalY - 1, globalZ - 1);
            BlockState bnxny0z = zone.getBlockState(chunk, globalX - 1, globalY - 1, globalZ);
            BlockState bnxnypz = zone.getBlockState(chunk, globalX - 1, globalY - 1, globalZ + 1);
            BlockState bnx0ynz = zone.getBlockState(chunk, globalX - 1, globalY, globalZ - 1);
            BlockState bnx0ypz = zone.getBlockState(chunk, globalX - 1, globalY, globalZ + 1);
            BlockState bnxpynz = zone.getBlockState(chunk, globalX - 1, globalY + 1, globalZ - 1);
            BlockState bnxpy0z = zone.getBlockState(chunk, globalX - 1, globalY + 1, globalZ);
            BlockState bnxpypz = zone.getBlockState(chunk, globalX - 1, globalY + 1, globalZ + 1);
            BlockState b0xnynz = zone.getBlockState(chunk, globalX, globalY - 1, globalZ - 1);
            BlockState b0xnypz = zone.getBlockState(chunk, globalX, globalY - 1, globalZ + 1);
            BlockState b0xpynz = zone.getBlockState(chunk, globalX, globalY + 1, globalZ - 1);
            BlockState b0xpypz = zone.getBlockState(chunk, globalX, globalY + 1, globalZ + 1);
            BlockState bpxnynz = zone.getBlockState(chunk, globalX + 1, globalY - 1, globalZ - 1);
            BlockState bpxny0z = zone.getBlockState(chunk, globalX + 1, globalY - 1, globalZ);
            BlockState bpxnypz = zone.getBlockState(chunk, globalX + 1, globalY - 1, globalZ + 1);
            BlockState bpx0ynz = zone.getBlockState(chunk, globalX + 1, globalY, globalZ - 1);
            BlockState bpx0ypz = zone.getBlockState(chunk, globalX + 1, globalY, globalZ + 1);
            BlockState bpxpynz = zone.getBlockState(chunk, globalX + 1, globalY + 1, globalZ - 1);
            BlockState bpxpy0z = zone.getBlockState(chunk, globalX + 1, globalY + 1, globalZ);
            BlockState bpxpypz = zone.getBlockState(chunk, globalX + 1, globalY + 1, globalZ + 1);
            opaqueBitmask |= bnxnynz != null && bnxnynz.isOpaque ? 64 : 0;
            opaqueBitmask |= bnxny0z != null && bnxny0z.isOpaque ? 128 : 0;
            opaqueBitmask |= bnxnypz != null && bnxnypz.isOpaque ? 256 : 0;
            opaqueBitmask |= bnx0ynz != null && bnx0ynz.isOpaque ? 512 : 0;
            opaqueBitmask |= bnx0ypz != null && bnx0ypz.isOpaque ? 1024 : 0;
            opaqueBitmask |= bnxpynz != null && bnxpynz.isOpaque ? 2048 : 0;
            opaqueBitmask |= bnxpy0z != null && bnxpy0z.isOpaque ? 4096 : 0;
            opaqueBitmask |= bnxpypz != null && bnxpypz.isOpaque ? 8192 : 0;
            opaqueBitmask |= b0xnynz != null && b0xnynz.isOpaque ? 16384 : 0;
            opaqueBitmask |= b0xnypz != null && b0xnypz.isOpaque ? 32768 : 0;
            opaqueBitmask |= b0xpynz != null && b0xpynz.isOpaque ? 65536 : 0;
            opaqueBitmask |= b0xpypz != null && b0xpypz.isOpaque ? 131072 : 0;
            opaqueBitmask |= bpxnynz != null && bpxnynz.isOpaque ? 262144 : 0;
            opaqueBitmask |= bpxny0z != null && bpxny0z.isOpaque ? 524288 : 0;
            opaqueBitmask |= bpxnypz != null && bpxnypz.isOpaque ? 0x100000 : 0;
            opaqueBitmask |= bpx0ynz != null && bpx0ynz.isOpaque ? 0x200000 : 0;
            opaqueBitmask |= bpx0ypz != null && bpx0ypz.isOpaque ? 0x400000 : 0;
            opaqueBitmask |= bpxpynz != null && bpxpynz.isOpaque ? 0x800000 : 0;
            opaqueBitmask |= bpxpy0z != null && bpxpy0z.isOpaque ? 0x1000000 : 0;
            opaqueBitmask |= bpxpypz != null && bpxpypz.isOpaque ? 0x2000000 : 0;
            MeshData md = meshData;
            if (b.isTransparent) {
                md = b.getBlock() == Block.WATER ? waterMeshData : transparentMeshData;
            }
            ChunkMeshGroup.calculateBlockLightLevels(chunk, blockLightLevels, hasNeighbouringBlockLightChunks, opaqueBitmask, localX, localY, localZ);
            int[] skyLightLevels = ChunkMeshGroup.calculateSkyLightLevels(chunk, localX, localY, localZ);
            b.addVertices(md, globalX, globalY, globalZ, opaqueBitmask, blockLightLevels, skyLightLevels);
        }
        if (meshData.getVertices().size > 0) {
            meshDatas.add(meshData);
        }
        if (transparentMeshData.getVertices().size > 0) {
            meshDatas.add(transparentMeshData);
        }
        if (waterMeshData.getVertices().size > 0) {
            meshDatas.add(waterMeshData);
        }
        return meshDatas;
    }

    private static int[] calculateSkyLightLevels(Chunk chunk, int localX, int localY, int localZ) {
        int[] skyLightLevels = new int[8];
        int skyLightLevel = 0;
        ISkylightData skyLightData = chunk.skyLightData;
        if (skyLightData == null) {
            if (localX > 0 && localY > 0 && localZ > 0 && localX < 15 && localY < 15 && localZ < 15) {
                return skyLightLevels;
            }
        } else {
            skyLightLevel = skyLightData.getSkyLight(localX, localY, localZ);
        }
        Arrays.fill(skyLightLevels, skyLightLevel);
        Zone zone = chunk.region.zone;
        int globalX = chunk.blockX + localX;
        int globalY = chunk.blockY + localY;
        int globalZ = chunk.blockZ + localZ;
        int lightNxNyNz = zone.getSkyLight(chunk, globalX - 1, globalY - 1, globalZ - 1);
        int lightNxNy0z = zone.getSkyLight(chunk, globalX - 1, globalY - 1, globalZ);
        int lightNxNyPz = zone.getSkyLight(chunk, globalX - 1, globalY - 1, globalZ + 1);
        int lightNx0yNz = zone.getSkyLight(chunk, globalX - 1, globalY, globalZ - 1);
        int lightNx0y0z = zone.getSkyLight(chunk, globalX - 1, globalY, globalZ);
        int lightNx0yPz = zone.getSkyLight(chunk, globalX - 1, globalY, globalZ + 1);
        int lightNxPyNz = zone.getSkyLight(chunk, globalX - 1, globalY + 1, globalZ - 1);
        int lightNxPy0z = zone.getSkyLight(chunk, globalX - 1, globalY + 1, globalZ);
        int lightNxPyPz = zone.getSkyLight(chunk, globalX - 1, globalY + 1, globalZ + 1);
        int light0xNyNz = zone.getSkyLight(chunk, globalX, globalY - 1, globalZ - 1);
        int light0xNy0z = zone.getSkyLight(chunk, globalX, globalY - 1, globalZ);
        int light0xNyPz = zone.getSkyLight(chunk, globalX, globalY - 1, globalZ + 1);
        int light0x0yNz = zone.getSkyLight(chunk, globalX, globalY, globalZ - 1);
        int light0x0yPz = zone.getSkyLight(chunk, globalX, globalY, globalZ + 1);
        int light0xPyNz = zone.getSkyLight(chunk, globalX, globalY + 1, globalZ - 1);
        int light0xPy0z = zone.getSkyLight(chunk, globalX, globalY + 1, globalZ);
        int light0xPyPz = zone.getSkyLight(chunk, globalX, globalY + 1, globalZ + 1);
        int lightPxNyNz = zone.getSkyLight(chunk, globalX + 1, globalY - 1, globalZ - 1);
        int lightPxNy0z = zone.getSkyLight(chunk, globalX + 1, globalY - 1, globalZ);
        int lightPxNyPz = zone.getSkyLight(chunk, globalX + 1, globalY - 1, globalZ + 1);
        int lightPx0yNz = zone.getSkyLight(chunk, globalX + 1, globalY, globalZ - 1);
        int lightPx0y0z = zone.getSkyLight(chunk, globalX + 1, globalY, globalZ);
        int lightPx0yPz = zone.getSkyLight(chunk, globalX + 1, globalY, globalZ + 1);
        int lightPxPyNz = zone.getSkyLight(chunk, globalX + 1, globalY + 1, globalZ - 1);
        int lightPxPy0z = zone.getSkyLight(chunk, globalX + 1, globalY + 1, globalZ);
        int lightPxPyPz = zone.getSkyLight(chunk, globalX + 1, globalY + 1, globalZ + 1);
        int m = skyLightLevels[0];
        m = Math.max(m, light0xNy0z);
        m = Math.max(m, light0xNyNz);
        m = Math.max(m, lightNxNy0z);
        m = Math.max(m, lightNx0y0z);
        m = Math.max(m, light0x0yNz);
        m = Math.max(m, lightNx0yNz);
        skyLightLevels[0] = m = Math.max(m, lightNxNyNz);
        m = skyLightLevels[1];
        m = Math.max(m, light0xNy0z);
        m = Math.max(m, light0xNyPz);
        m = Math.max(m, lightNxNy0z);
        m = Math.max(m, light0x0yPz);
        m = Math.max(m, lightNx0y0z);
        m = Math.max(m, lightNx0yPz);
        skyLightLevels[1] = m = Math.max(m, lightNxNyPz);
        m = skyLightLevels[2];
        m = Math.max(m, light0xNy0z);
        m = Math.max(m, light0xNyNz);
        m = Math.max(m, lightPxNy0z);
        m = Math.max(m, light0x0yNz);
        m = Math.max(m, lightPx0y0z);
        m = Math.max(m, lightPx0yNz);
        skyLightLevels[2] = m = Math.max(m, lightPxNyNz);
        m = skyLightLevels[3];
        m = Math.max(m, light0xNy0z);
        m = Math.max(m, light0xNyPz);
        m = Math.max(m, lightPxNy0z);
        m = Math.max(m, light0x0yPz);
        m = Math.max(m, lightPx0y0z);
        m = Math.max(m, lightPx0yPz);
        skyLightLevels[3] = m = Math.max(m, lightPxNyPz);
        m = skyLightLevels[4];
        m = Math.max(m, light0xPy0z);
        m = Math.max(m, light0xPyNz);
        m = Math.max(m, lightNxPy0z);
        m = Math.max(m, light0x0yNz);
        m = Math.max(m, lightNx0y0z);
        m = Math.max(m, lightNx0yNz);
        skyLightLevels[4] = m = Math.max(m, lightNxPyNz);
        m = skyLightLevels[5];
        m = Math.max(m, light0xPy0z);
        m = Math.max(m, light0xPyPz);
        m = Math.max(m, lightNxPy0z);
        m = Math.max(m, lightNx0y0z);
        m = Math.max(m, light0x0yPz);
        m = Math.max(m, lightNx0yPz);
        skyLightLevels[5] = m = Math.max(m, lightNxPyPz);
        m = skyLightLevels[6];
        m = Math.max(m, light0xPy0z);
        m = Math.max(m, light0xPyNz);
        m = Math.max(m, lightPxPy0z);
        m = Math.max(m, light0x0yNz);
        m = Math.max(m, lightPx0y0z);
        m = Math.max(m, lightPx0yNz);
        skyLightLevels[6] = m = Math.max(m, lightPxPyNz);
        m = skyLightLevels[7];
        m = Math.max(m, light0xPy0z);
        m = Math.max(m, light0xPyPz);
        m = Math.max(m, lightPxPy0z);
        m = Math.max(m, lightPx0y0z);
        m = Math.max(m, light0x0yPz);
        m = Math.max(m, lightPx0yPz);
        skyLightLevels[7] = m = Math.max(m, lightPxPyPz);
        return skyLightLevels;
    }

    public static short getMaxBlockLight(int blockLightA, int blockLightB) {
        int r = Math.max(blockLightA & 0xF00, blockLightB & 0xF00) >> 8;
        int g = Math.max(blockLightA & 0xF0, blockLightB & 0xF0) >> 4;
        int b = Math.max(blockLightA & 0xF, blockLightB & 0xF);
        return (short)((r << 8) + (g << 4) + b);
    }

    private static short[] calculateBlockLightLevels(Chunk chunk, short[] blockLightLevels, boolean hasNeighbouringBlockLightChunks, int opaqueBitmask, int localX, int localY, int localZ) {
        short lightLevel = 0;
        IBlockLightData blockLightData = chunk.blockLightData;
        Zone zone = chunk.region.zone;
        if (blockLightData == null) {
            if (!hasNeighbouringBlockLightChunks || localX > 0 && localY > 0 && localZ > 0 && localX < 15 && localY < 15 && localZ < 15) {
                return blockLightLevels;
            }
        } else {
            lightLevel = blockLightData.getBlockLight(localX, localY, localZ);
        }
        Arrays.fill(blockLightLevels, lightLevel);
        int globalX = chunk.blockX + localX;
        int globalY = chunk.blockY + localY;
        int globalZ = chunk.blockZ + localZ;
        short lightNxNyNz = zone.getBlockLight(chunk, globalX - 1, globalY - 1, globalZ - 1);
        short lightNxNy0z = zone.getBlockLight(chunk, globalX - 1, globalY - 1, globalZ);
        short lightNxNyPz = zone.getBlockLight(chunk, globalX - 1, globalY - 1, globalZ + 1);
        short lightNx0yNz = zone.getBlockLight(chunk, globalX - 1, globalY, globalZ - 1);
        short lightNx0y0z = zone.getBlockLight(chunk, globalX - 1, globalY, globalZ);
        short lightNx0yPz = zone.getBlockLight(chunk, globalX - 1, globalY, globalZ + 1);
        short lightNxPyNz = zone.getBlockLight(chunk, globalX - 1, globalY + 1, globalZ - 1);
        short lightNxPy0z = zone.getBlockLight(chunk, globalX - 1, globalY + 1, globalZ);
        short lightNxPyPz = zone.getBlockLight(chunk, globalX - 1, globalY + 1, globalZ + 1);
        short light0xNyNz = zone.getBlockLight(chunk, globalX, globalY - 1, globalZ - 1);
        short light0xNy0z = zone.getBlockLight(chunk, globalX, globalY - 1, globalZ);
        short light0xNyPz = zone.getBlockLight(chunk, globalX, globalY - 1, globalZ + 1);
        short light0x0yNz = zone.getBlockLight(chunk, globalX, globalY, globalZ - 1);
        short light0x0yPz = zone.getBlockLight(chunk, globalX, globalY, globalZ + 1);
        short light0xPyNz = zone.getBlockLight(chunk, globalX, globalY + 1, globalZ - 1);
        short light0xPy0z = zone.getBlockLight(chunk, globalX, globalY + 1, globalZ);
        short light0xPyPz = zone.getBlockLight(chunk, globalX, globalY + 1, globalZ + 1);
        short lightPxNyNz = zone.getBlockLight(chunk, globalX + 1, globalY - 1, globalZ - 1);
        short lightPxNy0z = zone.getBlockLight(chunk, globalX + 1, globalY - 1, globalZ);
        short lightPxNyPz = zone.getBlockLight(chunk, globalX + 1, globalY - 1, globalZ + 1);
        short lightPx0yNz = zone.getBlockLight(chunk, globalX + 1, globalY, globalZ - 1);
        short lightPx0y0z = zone.getBlockLight(chunk, globalX + 1, globalY, globalZ);
        short lightPx0yPz = zone.getBlockLight(chunk, globalX + 1, globalY, globalZ + 1);
        short lightPxPyNz = zone.getBlockLight(chunk, globalX + 1, globalY + 1, globalZ - 1);
        short lightPxPy0z = zone.getBlockLight(chunk, globalX + 1, globalY + 1, globalZ);
        short lightPxPyPz = zone.getBlockLight(chunk, globalX + 1, globalY + 1, globalZ + 1);
        short m = blockLightLevels[0];
        m = ChunkMeshGroup.getMaxBlockLight(m, light0xNy0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, light0xNyNz);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightNxNy0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightNx0y0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, light0x0yNz);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightNx0yNz);
        blockLightLevels[0] = m = ChunkMeshGroup.getMaxBlockLight(m, lightNxNyNz);
        m = blockLightLevels[1];
        m = ChunkMeshGroup.getMaxBlockLight(m, light0xNy0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, light0xNyPz);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightNxNy0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, light0x0yPz);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightNx0y0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightNx0yPz);
        blockLightLevels[1] = m = ChunkMeshGroup.getMaxBlockLight(m, lightNxNyPz);
        m = blockLightLevels[2];
        m = ChunkMeshGroup.getMaxBlockLight(m, light0xNy0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, light0xNyNz);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightPxNy0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, light0x0yNz);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightPx0y0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightPx0yNz);
        blockLightLevels[2] = m = ChunkMeshGroup.getMaxBlockLight(m, lightPxNyNz);
        m = blockLightLevels[3];
        m = ChunkMeshGroup.getMaxBlockLight(m, light0xNy0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, light0xNyPz);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightPxNy0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, light0x0yPz);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightPx0y0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightPx0yPz);
        blockLightLevels[3] = m = ChunkMeshGroup.getMaxBlockLight(m, lightPxNyPz);
        m = blockLightLevels[4];
        m = ChunkMeshGroup.getMaxBlockLight(m, light0xPy0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, light0xPyNz);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightNxPy0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, light0x0yNz);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightNx0y0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightNx0yNz);
        blockLightLevels[4] = m = ChunkMeshGroup.getMaxBlockLight(m, lightNxPyNz);
        m = blockLightLevels[5];
        m = ChunkMeshGroup.getMaxBlockLight(m, light0xPy0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, light0xPyPz);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightNxPy0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightNx0y0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, light0x0yPz);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightNx0yPz);
        blockLightLevels[5] = m = ChunkMeshGroup.getMaxBlockLight(m, lightNxPyPz);
        m = blockLightLevels[6];
        m = ChunkMeshGroup.getMaxBlockLight(m, light0xPy0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, light0xPyNz);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightPxPy0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, light0x0yNz);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightPx0y0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightPx0yNz);
        blockLightLevels[6] = m = ChunkMeshGroup.getMaxBlockLight(m, lightPxPyNz);
        m = blockLightLevels[7];
        m = ChunkMeshGroup.getMaxBlockLight(m, light0xPy0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, light0xPyPz);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightPxPy0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightPx0y0z);
        m = ChunkMeshGroup.getMaxBlockLight(m, light0x0yPz);
        m = ChunkMeshGroup.getMaxBlockLight(m, lightPx0yPz);
        blockLightLevels[7] = m = ChunkMeshGroup.getMaxBlockLight(m, lightPxPyPz);
        return blockLightLevels;
    }

    @Override
    public Array<MeshData> getAllMeshData() {
        return this.allMeshData;
    }

    @Override
    public void dispose() {
        this.clearMeshes();
    }

    public void clearMeshes() {
        for (int mi = 0; mi < this.allMeshData.size; ++mi) {
            this.allMeshData.get(mi).clear();
        }
        this.allMeshData.clear();
        this.verticesSet = false;
    }

    @Override
    public void setMeshVertices(Array<MeshData> meshDatas) {
        this.clearMeshes();
        this.verticesSet = true;
        for (MeshData mdata : meshDatas) {
            mdata.shrink();
            FloatArray verts = mdata.getVertices();
            if (verts.size <= 0 || mdata == null) continue;
            this.allMeshData.add(mdata);
        }
        ++this.meshGenCount;
        setMeshGenRecently = true;
    }

    @Override
    public boolean hasMesh() {
        return this.verticesSet;
    }

    @Override
    public void flagForRemeshing(boolean updateImmediately) {
        this.remeshImmediately |= updateImmediately;
        this.remeshRequests.incrementAndGet();
    }

    @Override
    public boolean isFlaggedForRemeshing() {
        return this.remeshRequests.intValue() != 0;
    }

    @Override
    public boolean isFlaggedForImmediateRemesh() {
        return this.remeshImmediately;
    }

    @Override
    public void flushRemeshRequests() {
        this.remeshRequests.set(0);
    }

    @Override
    public boolean hasExpectedMeshCount() {
        return this.expectedMeshGenCount == this.meshGenCount;
    }

    @Override
    public void setToRemeshImmediately(boolean remeshImmediately) {
        this.remeshImmediately = remeshImmediately;
    }

    @Override
    public void setExpectedMeshGenCount() {
        this.expectedMeshGenCount = this.meshGenCount;
    }
}