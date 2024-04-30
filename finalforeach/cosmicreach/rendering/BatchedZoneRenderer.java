package finalforeach.cosmicreach.rendering;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectSet;

import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.RuntimeInfo;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.IChunkMeshGroup;
import finalforeach.cosmicreach.world.Region;
import finalforeach.cosmicreach.world.RegionCoords;
import finalforeach.cosmicreach.world.RegionOctant;
import finalforeach.cosmicreach.world.Zone;

public class BatchedZoneRenderer implements IZoneRenderer {
    private ConcurrentHashMap<Region, Array<Chunk>> regionChunksToRender = new ConcurrentHashMap<Region, Array<Chunk>>();
    private final IntMap<Array<ChunkBatch>> layers = new IntMap<Array<ChunkBatch>>();
    public HashMap<BatchMaterial, ChunkBatch> batchMap = new HashMap<BatchMaterial, ChunkBatch>();
    private final IntArray layerNums = new IntArray();
    private BatchMaterial tmpMat = new BatchMaterial();
    private final BoundingBox tmpBounds = new BoundingBox();
    private Vector3 lastCameraPosition;
    private Vector3 lastCameraDirection;
    private ObjectIntMap<Region> numChunksConsideredPerRegion = new ObjectIntMap<Region>();
    private ObjectIntMap<Region> numChunksPerRegion = new ObjectIntMap<Region>();
    private boolean gotNewChunksToRender;
    private ShapeRenderer batchDebuggingShapeRenderer;
    public boolean drawDebugLines = false;
    private Vector3 tmpVec = new Vector3();

    @Override
    public void unload() {
        this.disposeUnusedBatches(true);
    }

    @Override
    public void removeRegion(Region r) {
        this.numChunksConsideredPerRegion.remove(r, 0);
        this.numChunksPerRegion.remove(r, 0);
        this.regionChunksToRender.remove(r);
    }

    private boolean regionInBounds(Camera worldCamera, Region r) {
        return r.boundingBox.contains(worldCamera.position) || worldCamera.frustum.boundsInFrustum(r.boundingBox);
    }

    private boolean octantInBounds(Camera worldCamera, Region r, RegionOctant octant, BoundingBox tmpBounds) {
        octant.getBounds(r, tmpBounds);
        return worldCamera.frustum.boundsInFrustum(tmpBounds);
    }

    private boolean chunkInBounds(Camera worldCamera, Chunk chunk, BoundingBox tmpBounds) {
        Frustum frustum = worldCamera.frustum;
        boolean inBounds = frustum.pointInFrustum(chunk.blockX + 8, chunk.blockY + 8, chunk.blockZ + 8);
        if (!inBounds && (frustum.sphereInFrustum(chunk.blockX + 8, chunk.blockY + 8, chunk.blockZ + 8, (float)(16.0 * Math.sqrt(2.0))))) {
            tmpBounds.min.set(chunk.blockX, chunk.blockY, chunk.blockZ);
            tmpBounds.max.set(tmpBounds.min).add(16.0f, 16.0f, 16.0f);
            tmpBounds.update();
            inBounds = frustum.boundsInFrustum(tmpBounds);
        }
        return inBounds;
    }

    private boolean boundsContainedInView(Camera worldCamera, BoundingBox box) {
        Frustum frustum = worldCamera.frustum;
        this.tmpVec.set(box.min);
        if (!frustum.pointInFrustum(this.tmpVec)) {
            return false;
        }
        this.tmpVec.add(box.max.x - box.min.x, 0.0f, 0.0f);
        if (!frustum.pointInFrustum(this.tmpVec)) {
            return false;
        }
        this.tmpVec.set(box.min).add(0.0f, box.max.y - box.min.y, 0.0f);
        if (!frustum.pointInFrustum(this.tmpVec)) {
            return false;
        }
        this.tmpVec.set(box.min).add(0.0f, 0.0f, box.max.z - box.min.z);
        if (!frustum.pointInFrustum(this.tmpVec)) {
            return false;
        }
        this.tmpVec.set(box.max);
        if (!frustum.pointInFrustum(this.tmpVec)) {
            return false;
        }
        this.tmpVec.add(box.min.x - box.max.x, 0.0f, 0.0f);
        if (!frustum.pointInFrustum(this.tmpVec)) {
            return false;
        }
        this.tmpVec.set(box.max).add(0.0f, box.min.y - box.max.y, 0.0f);
        if (!frustum.pointInFrustum(this.tmpVec)) {
            return false;
        }
        this.tmpVec.set(box.max).add(0.0f, 0.0f, box.min.z - box.max.z);
        return frustum.pointInFrustum(this.tmpVec);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void getChunksToRender(Zone zone, Camera worldCamera) {
        this.gotNewChunksToRender = false;
        if (this.lastCameraPosition == null) {
            this.lastCameraPosition = new Vector3(worldCamera.position);
            this.lastCameraDirection = new Vector3(worldCamera.direction);
        } else if (this.lastCameraPosition.epsilonEquals(worldCamera.position) && this.lastCameraDirection.epsilonEquals(worldCamera.direction)) {
            boolean earlyExit = true;
            Map<RegionCoords, Region> map = zone.regions;
            synchronized (map) {
                for (Region r : zone.regions.values()) {
                    if (!this.regionInBounds(worldCamera, r) || this.numChunksPerRegion.get(r, 0) == r.getNumberOfChunks()) continue;
                    earlyExit = false;
                    break;
                }
            }
            if (earlyExit) {
                return;
            }
        }
        this.lastCameraPosition.set(worldCamera.position);
        this.lastCameraDirection.set(worldCamera.direction);
        for (Region r : zone.regions.values()) {
            if (!this.regionInBounds(worldCamera, r)) {
                this.regionChunksToRender.remove(r);
                continue;
            }
            Array<Chunk> chunksToRender = this.regionChunksToRender.get(r);
            if (chunksToRender == null) {
                chunksToRender = new Array<Chunk>(Chunk.class);
                this.regionChunksToRender.put(r, chunksToRender);
            }
            int prevChunksToRenderSize = chunksToRender.size;
            ObjectSet<Chunk> chunksToNoLongerRender = new ObjectSet<Chunk>(chunksToRender.size);
            chunksToNoLongerRender.addAll(chunksToRender);
            chunksToRender.size = 0;
            boolean regionContained = this.boundsContainedInView(worldCamera, r.boundingBox);
            this.numChunksPerRegion.put(r, r.getNumberOfChunks());
            int numConsidered = 0;
            for (RegionOctant octant : r.octants) {
                if (!this.octantInBounds(worldCamera, r, octant, this.tmpBounds)) continue;
                boolean octantContained = regionContained;
                if (!regionContained) {
                    octant.getBounds(r, this.tmpBounds);
                    octantContained = this.boundsContainedInView(worldCamera, this.tmpBounds);
                }
                for (Chunk chunk : (Chunk[])octant.getChunks().items) {
                    if (chunk == null) break;
                    ChunkMeshGroup meshGroup = (ChunkMeshGroup)chunk.meshGroup;
                    if (meshGroup == null) {
                        meshGroup = (ChunkMeshGroup) (chunk.meshGroup = new ChunkMeshGroup());
                    }
                    ++numConsidered;
                    if (((Array<?>)meshGroup.getAllMeshData()).isEmpty() && meshGroup.hasMesh() && !meshGroup.isFlaggedForRemeshing() || !octantContained && !this.chunkInBounds(worldCamera, chunk, this.tmpBounds)) continue;
                    chunksToRender.add(chunk);
                }
            }
            this.numChunksConsideredPerRegion.put(r, numConsidered);
            if (prevChunksToRenderSize > chunksToRender.size) {
                this.gotNewChunksToRender = true;
            }
            if (!this.gotNewChunksToRender) {
                for (Chunk chunk : chunksToRender) {
                    this.gotNewChunksToRender = !chunksToNoLongerRender.contains(chunk);
                    if (!this.gotNewChunksToRender) continue;
                    break;
                }
            }
            r.flaggedForRemeshing = false;
        }
    }

    private void requestMeshes() {
        IWorldRenderingMeshGenThread meshGenThread = GameSingletons.meshGenThread;
        for (Map.Entry<Region, Array<Chunk>> regionChunks : this.regionChunksToRender.entrySet()) {
            Array<Chunk> chunksToRender = regionChunks.getValue();
            int numChunks = chunksToRender.size;
            for (int ci = 0; ci < numChunks; ++ci) {
                IChunkMeshGroup<?> meshGroup;
                Chunk chunk = ((Chunk[])chunksToRender.items)[ci];
                if (!chunk.isGenerated || (meshGroup = chunk.meshGroup).hasMesh() && !meshGroup.isFlaggedForRemeshing()) continue;
                meshGroup.flushRemeshRequests();
                meshGenThread.addChunk(chunk);
            }
        }
        meshGenThread.meshChunks();
    }

    private void addMeshDatasToChunkBatches() {
        if (!this.gotNewChunksToRender && !ChunkMeshGroup.setMeshGenRecently) {
            return;
        }
        ChunkMeshGroup.setMeshGenRecently = false;
        BatchCoords batchCoords = BatchCoords.pool.obtain();
        for (Map.Entry<Region, Array<Chunk>> regionChunks : this.regionChunksToRender.entrySet()) {
            Array<Chunk> chunksToRender = regionChunks.getValue();
            for (int ci = 0; ci < chunksToRender.size; ++ci) {
                Chunk chunk = ((Chunk[])chunksToRender.items)[ci];
                ChunkMeshGroup meshGroup = (ChunkMeshGroup)chunk.meshGroup;
                Object chunkAllMeshData = meshGroup.getAllMeshData();
                batchCoords.setBatchCoordsFromChunk(chunk);
                for (int mi = 0; mi < ((Array<?>)chunkAllMeshData).size; ++mi) {
                    MeshData m = ((MeshData[])((Array<?>)chunkAllMeshData).items)[mi];
                    if (m == null) continue;
                    this.tmpMat.set(batchCoords, m.getShader(), m.getRenderOrder());
                    ChunkBatch batch = this.batchMap.get(this.tmpMat);
                    if (batch == null) {
                        Array<ChunkBatch> layer = this.layers.get(m.getRenderOrder().order);
                        if (layer == null) {
                            layer = new Array<ChunkBatch>();
                            this.layers.put(m.getRenderOrder().order, layer);
                            this.layerNums.add(m.getRenderOrder().order);
                            this.layerNums.shrink();
                            this.layerNums.sort();
                        }
                        batch = new ChunkBatch(this.tmpMat, layer);
                        this.batchMap.put(new BatchMaterial(this.tmpMat), batch);
                    }
                    batch.addMeshData(chunk, m);
                }
            }
        }
        BatchCoords.pool.free(batchCoords);
    }

    private void disposeUnusedBatches(boolean unloadAll) {
        if (!(this.gotNewChunksToRender || ChunkMeshGroup.setMeshGenRecently || unloadAll)) {
            return;
        }
        block0: for (ChunkBatch batch : this.batchMap.values()) {
            if (batch.seen && !unloadAll) continue;
            for (Chunk c : batch.chunks) {
                ChunkMeshGroup meshGroup = (ChunkMeshGroup)c.meshGroup;
                if (meshGroup.hasMesh() && !((Array<?>)meshGroup.getAllMeshData()).isEmpty() && !meshGroup.isFlaggedForRemeshing() && meshGroup.hasExpectedMeshCount() && !unloadAll) continue;
                batch.dispose(unloadAll);
                batch.layer.removeValue(batch, true);
                continue block0;
            }
        }
        this.batchMap.entrySet().removeIf(e -> ((ChunkBatch)e.getValue()).disposed);
    }

    @Override
    public void render(Zone zone, Camera worldCamera) {
        Gdx.gl.glEnable(2929);
        Gdx.gl.glDepthFunc(513);
        Gdx.gl.glEnable(2884);
        Gdx.gl.glCullFace(1029);
        Gdx.gl.glEnable(3042);
        Gdx.gl.glBlendFunc(770, 771);
        this.getChunksToRender(zone, worldCamera);
        this.requestMeshes();
        this.disposeUnusedBatches(false);
        this.addMeshDatasToChunkBatches();
        ChunkBatch.lastBoundShader = null;
        if (SharedQuadIndexData.indexData != null && RuntimeInfo.useSharedIndices) {
            SharedQuadIndexData.indexData.bind();
        }
        for (int layerNum : this.layerNums.items) {
            Array<ChunkBatch> layer = this.layers.get(layerNum);
            if (layer == null) continue;
            for (ChunkBatch batch : layer) {
                batch.render(zone, worldCamera);
            }
        }
        if (ChunkBatch.lastBoundShader != null) {
            ChunkBatch.lastBoundShader.unbind();
        }
        if (SharedQuadIndexData.indexData != null && RuntimeInfo.useSharedIndices) {
            SharedQuadIndexData.indexData.unbind();
        }
        if (this.drawDebugLines) {
            if (this.batchDebuggingShapeRenderer == null) {
                this.batchDebuggingShapeRenderer = new ShapeRenderer();
            }
            this.batchDebuggingShapeRenderer.setProjectionMatrix(worldCamera.combined);
            this.batchDebuggingShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            Random rand = new Random();
            for (int layerNum : this.layerNums.items) {
                Array<ChunkBatch> layer = this.layers.get(layerNum);
                if (layer == null) continue;
                for (ChunkBatch batch : layer) {
                    if (worldCamera.frustum.boundsInFrustum(batch.boundingBox)) continue;
                    int h = batch.hashCode();
                    rand.setSeed(h);
                    float r = rand.nextFloat();
                    float g = rand.nextFloat();
                    float b = rand.nextFloat();
                    this.batchDebuggingShapeRenderer.setColor(r, g, b, 1.0f);
                    BoundingBox bb = batch.boundingBox;
                    this.batchDebuggingShapeRenderer.box(bb.min.x, bb.min.y, bb.min.z, bb.getWidth(), bb.getHeight(), -bb.getDepth());
                }
            }
            this.batchDebuggingShapeRenderer.end();
        }
        Gdx.gl.glActiveTexture(33984);
        Gdx.gl.glBindTexture(3553, 0);
    }

    @Override
    public void dispose() {
        IWorldRenderingMeshGenThread meshGenThread = GameSingletons.meshGenThread;
        meshGenThread.stopThread();
    }
}