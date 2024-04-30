package finalforeach.cosmicreach.lighting;

import com.badlogic.gdx.utils.Queue;

import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.savelib.lightdata.skylight.SkylightSingleData;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;

public class LightPropagator {
    public static void calculateLightingForChunk(Zone zone, Chunk chunk, boolean isTopChunk) {
        boolean earlySkylightExit = chunk.blockData.isEntirely(b -> b.lightAttenuation == 15);
        boolean earlyBlockLightExit = chunk.blockData.isEntirely(b -> !b.isLightEmitter());
        boolean entirelySky = isTopChunk;
        boolean entirelyTransparentToSky = true;
        if (!chunk.blockData.isEntirely(b -> b.lightAttenuation == 0)) {
            entirelySky = false;
            entirelyTransparentToSky = false;
        }
        if (!entirelySky && entirelyTransparentToSky) {
            Chunk chunkAbove = zone.getChunkAtChunkCoords(chunk.chunkX, chunk.chunkY + 1, chunk.chunkZ);
            boolean chunkAboveEntirelyTransparentToSky = true;
            while (chunkAbove != null) {
                if (chunkAbove.blockData.isEntirely(b -> b.lightAttenuation > 0)) {
                    chunkAboveEntirelyTransparentToSky = false;
                    break;
                }
                chunkAbove = zone.getChunkAtChunkCoords(chunk.chunkX, chunkAbove.chunkY + 1, chunk.chunkZ);
            }
            if (chunkAboveEntirelyTransparentToSky) {
                entirelySky = true;
            }
        }
        if (entirelySky) {
            chunk.skyLightData = new SkylightSingleData(chunk, (byte) 15);
        }
        if (earlySkylightExit && earlyBlockLightExit) {
            return;
        }
        if (chunk.skyLightData != null) {
            chunk.skyLightData = null;
        }
        Queue<BlockPosition> blockLightQueue = new Queue<BlockPosition>();
        Queue<BlockPosition> skyLightQueue = new Queue<BlockPosition>();
        for (int idx = 0; idx < 4096; ++idx) {
            int nl;
            short lnpacked;
            Chunk neighbourChunk;
            int ncz = 0;
            int ncy = 0;
            int ncx = 0;
            int localZ = idx / 256;
            int localY = (idx - localZ * 256) / 16;
            int localX = (idx - localZ * 256) % 16;
            int skyLightLevel = 0;
            int borderSkyLightLevel = 0;
            int nr = 0;
            int ng = 0;
            int nb = 0;
            
            //TODO: WARNING ++
            
            if (localX == 0){} else {ncx = localX == 15 ? 1 : 0;}
            if (localY == 0){} else {ncy = localY == 15 ? 1 : 0;}
            if (localZ == 0){} else {ncz = localZ == 15 ? 1 : 0;}
            if (ncx != 0 && (neighbourChunk = zone.getChunkAtChunkCoords(chunk.chunkX + ncx, chunk.chunkY, chunk.chunkZ)) != null) {
                int nbx;
                nbx = localX == 0 ? 15 : 0;
                if (neighbourChunk.blockLightData != null) {
                    lnpacked = neighbourChunk.blockLightData.getBlockLight(nbx, localY, localZ);
                    nr = Math.max(nr, (lnpacked & 0xF00) >> 8);
                    ng = Math.max(ng, (lnpacked & 0xF0) >> 4);
                    nb = Math.max(nb, lnpacked & 0xF);
                }
                if (neighbourChunk.skyLightData != null) {
                    nl = neighbourChunk.skyLightData.getSkyLight(nbx, localY, localZ);
                    borderSkyLightLevel = Math.max(borderSkyLightLevel, nl - 1);
                }
            }
            if (ncy != 0) {
                neighbourChunk = zone.getChunkAtChunkCoords(chunk.chunkX, chunk.chunkY + ncy, chunk.chunkZ);
                if (neighbourChunk != null) {
                    int nby;
                    nby = localY == 0 ? 15 : 0;
                    if (neighbourChunk.blockLightData != null) {
                        lnpacked = neighbourChunk.blockLightData.getBlockLight(localX, nby, localZ);
                        nr = Math.max(nr, (lnpacked & 0xF00) >> 8);
                        ng = Math.max(ng, (lnpacked & 0xF0) >> 4);
                        nb = Math.max(nb, lnpacked & 0xF);
                    }
                    if (neighbourChunk.skyLightData != null) {
                        nl = neighbourChunk.skyLightData.getSkyLight(localX, nby, localZ);
                        borderSkyLightLevel = nl == 15 ? Math.max(borderSkyLightLevel, nl) : Math.max(borderSkyLightLevel, nl - 1);
                    }
                } else if (ncy == 1) {
                    borderSkyLightLevel = 15;
                }
            }
            if (ncz != 0 && (neighbourChunk = zone.getChunkAtChunkCoords(chunk.chunkX, chunk.chunkY, chunk.chunkZ + ncz)) != null) {
                int nbz;
                nbz = localZ == 0 ? 15 : 0;
                if (neighbourChunk.blockLightData != null) {
                    lnpacked = neighbourChunk.blockLightData.getBlockLight(localX, localY, nbz);
                    nr = Math.max(nr, (lnpacked & 0xF00) >> 8);
                    ng = Math.max(ng, (lnpacked & 0xF0) >> 4);
                    nb = Math.max(nb, lnpacked & 0xF);
                }
                if (neighbourChunk.skyLightData != null) {
                    nl = neighbourChunk.skyLightData.getSkyLight(localX, localY, nbz);
                    borderSkyLightLevel = Math.max(borderSkyLightLevel, nl - 1);
                }
            }
            BlockState blockState = chunk.getBlockState(localX, localY, localZ);
            if (blockState.lightAttenuation == 0) {
                if (entirelySky) {
                    if (localY == 0) {
                        skyLightLevel = 15;
                    }
                } else if (isTopChunk && localY == 15) {
                    skyLightLevel = 15;
                }
            }
            nr = Math.max(nr, blockState.lightLevelRed);
            ng = Math.max(ng, blockState.lightLevelGreen);
            nb = Math.max(nb, blockState.lightLevelBlue);
            if (nr != 0 || ng != 0 || nb != 0) {
                chunk.setBlockLight(nr, ng, nb, localX, localY, localZ);
                blockLightQueue.addLast(new BlockPosition(chunk, localX, localY, localZ));
            }
            if (skyLightLevel == 0 && borderSkyLightLevel <= skyLightLevel) continue;
            skyLightLevel = Math.max(skyLightLevel, borderSkyLightLevel);
            chunk.setSkyLight(skyLightLevel, localX, localY, localZ);
            BlockPosition blockPos = new BlockPosition(chunk, localX, localY, localZ);
            if (entirelySky && localY == 0) {
                skyLightQueue.addFirst(blockPos);
                continue;
            }
            if (isTopChunk && localY == 15) {
                skyLightQueue.addFirst(blockPos);
                continue;
            }
            skyLightQueue.addLast(blockPos);
        }
        BlockLightPropagator.propagateBlockLights(zone, blockLightQueue);
        SkyLightPropagator.propagateSkyLights(zone, skyLightQueue);
    }
}
