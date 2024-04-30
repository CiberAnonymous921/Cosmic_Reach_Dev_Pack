package finalforeach.cosmicreach.rendering;

import java.nio.FloatBuffer;
import java.util.HashMap;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

import finalforeach.cosmicreach.RuntimeInfo;
import finalforeach.cosmicreach.rendering.meshes.IGameMesh;
import finalforeach.cosmicreach.rendering.meshes.IntIndexData;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.IChunkMeshGroup;
import finalforeach.cosmicreach.world.Zone;

public class ChunkBatch {
    public static GameShader lastBoundShader = null;
    public final Array<ChunkBatch> layer;
    HashMap<MeshData, MeshDataMetadata> meshDataMetadatas = new HashMap<MeshData, MeshDataMetadata>();
    GameShader shader;
    Array<MeshData> meshDatas = new Array<MeshData>(4);
    Array<MeshData> meshDatasToAdd = new Array<MeshData>(4);
    Array<Chunk> chunks = new Array<Chunk>(4);
    Array<Chunk> visibleChunks = new Array<Chunk>(4);
    IGameMesh mesh;
    BoundingBox boundingBox;
    MeshData combined;
    boolean seen;
    boolean disposed;
    boolean needToRebuild = true;
    private static transient Vector3 tmp = new Vector3();

    public ChunkBatch(BatchMaterial mat, Array<ChunkBatch> layer) {
        BatchCoords batchCoords = mat.batchCoords;
        this.layer = layer;
        this.boundingBox = new BoundingBox();
        this.boundingBox.min.set(batchCoords.blockX(), batchCoords.blockY(), batchCoords.blockZ());
        this.boundingBox.max.set(this.boundingBox.min);
        this.boundingBox.max.add(64.0f);
        this.boundingBox.update();
        layer.add(this);
        this.shader = mat.shader;
        this.combined = new MeshData(this.shader, mat.renderOrder);
        this.seen = true;
    }

    public void dispose(boolean forceUnload) {
        if (this.mesh != null) {
            if (!forceUnload) {
                FloatBuffer meshVerts = this.mesh.getVertices().getBuffer(false);
                for (MeshData mData : this.meshDataMetadatas.keySet()) {
                    MeshDataMetadata meta = this.meshDataMetadatas.get(mData);
                    if (!mData.vertices.isEmpty()) continue;
                    mData.clear();
                    float[] vertArray = new float[meta.numVertices];
                    meshVerts.slice(meta.vertexPosition, meta.numVertices).get(vertArray);
                    mData.getVertices().addAll(vertArray);
                }
            }
            this.meshDatas.clear();
            this.meshDataMetadatas.clear();
            this.mesh.dispose();
        }
        this.disposed = true;
        this.chunks.clear();
        this.visibleChunks.clear();
        this.layer.removeValue(this, true);
    }

    public void addMeshData(Chunk chunk, MeshData mdata) {
        this.visibleChunks.add(chunk);
        IChunkMeshGroup<?> meshGroup = chunk.meshGroup;
        if (!meshGroup.hasExpectedMeshCount()) {
            meshGroup.setExpectedMeshGenCount();
            this.needToRebuild = true;
        }
        this.meshDatasToAdd.add(mdata);
        if (!this.needToRebuild) {
            this.needToRebuild = !this.meshDatas.contains(mdata, true);
        }
        this.seen = true;
    }

    @SuppressWarnings("unused")
	private boolean boundsSeenByCamera(Camera worldCamera) {
        Frustum frustum = worldCamera.frustum;
        BoundingBox bb = this.boundingBox;
        return frustum.boundsInFrustum(bb.getCenterX(), bb.getCenterY(), bb.getCenterZ(), bb.getWidth(), bb.getHeight(), bb.getDepth());
    }

    public void render(Zone zone, Camera worldCamera) {
        this.seen = false;
        if (this.needToRebuild) {
            this.rebuildMesh();
        }
        this.needToRebuild = false;
        this.meshDatasToAdd.size = 0;
        this.visibleChunks.size = 0;
        if (this.mesh != null) {
            this.mesh.setAutoBind(false);
            if (lastBoundShader != this.shader) {
                lastBoundShader = this.shader;
                lastBoundShader.bind(worldCamera);
            }
            this.mesh.bind(this.shader.shader);
            this.mesh.render(this.shader.shader, 4);
            this.mesh.unbind(this.shader.shader);
        }
    }

    private void rebuildMesh() {
        for (Chunk chunk : this.visibleChunks) {
            if (this.chunks.contains(chunk, true)) continue;
            this.chunks.add(chunk);
        }
        int numVert = 0;
        for (MeshData meshData : this.meshDatasToAdd) {
            if (meshData.getVertices().isEmpty()) continue;
            numVert += meshData.getVertices().size;
        }
        this.combined.getVertices().ensureCapacity(numVert);
        if (this.mesh != null) {
            MeshDataMetadata meta;
            FloatBuffer floatBuffer = this.mesh.getVertices().getBuffer(false);
            for (MeshData mData : this.meshDataMetadatas.keySet()) {
                meta = this.meshDataMetadatas.get(mData);
                if (this.meshDatasToAdd.contains(mData, true)) continue;
                mData.clear();
                float[] vertArray = new float[meta.numVertices];
                floatBuffer.slice(meta.vertexPosition, meta.numVertices).get(vertArray);
                mData.getVertices().addAll(vertArray);
            }
            for (MeshData mData : this.meshDatasToAdd) {
                meta = this.meshDataMetadatas.get(mData);
                if (meta == null) continue;
                int vertPos = this.combined.getVertices().size;
                float[] vertArray = new float[meta.numVertices];
                floatBuffer.slice(meta.vertexPosition, meta.numVertices).get(vertArray);
                this.combined.getVertices().addAll(vertArray);
                if (vertPos == meta.vertexPosition) continue;
                this.meshDataMetadatas.put(mData, new MeshDataMetadata(vertPos, meta.numVertices));
            }
        }
        for (MeshData meshData : this.meshDatasToAdd) {
            if (this.meshDataMetadatas.containsKey(meshData) || meshData.getVertices().isEmpty()) continue;
            int vertPos = this.combined.getVertices().size;
            this.combined.getVertices().addAll(meshData.getVertices());
            this.meshDataMetadatas.put(meshData, new MeshDataMetadata(vertPos, meshData.getVertices().size));
            meshData.clear();
            meshData.shrink();
        }
        this.meshDataMetadatas.keySet().removeIf(key -> !this.meshDatasToAdd.contains((MeshData)key, true));
        this.setMeshFromCombined(this.combined);
        this.combined.clear();
        this.combined.shrink();
        this.meshDatas.clear();
        this.meshDatasToAdd.forEach(m -> this.meshDatas.add((MeshData)m));
    }

    private void setMeshFromCombined(MeshData combined) {
        if (this.mesh != null && this.mesh.getNumMaxVertices() < combined.getVertices().size / this.mesh.getVertexSizeInFloats()) {
            this.mesh.dispose();
            this.mesh = null;
        }
        if (this.mesh == null) {
            if (RuntimeInfo.useSharedIndices) {
                this.mesh = combined.toSharedIndexMesh(true);
            } else {
                VertexAttributes a = new VertexAttributes(this.shader.allVertexAttributes);
                if (combined.indices.isEmpty()) {
                    int numIndices = combined.getVertices().size / (a.vertexSize / 4) * 6 / 4;
                    combined.indices.addAll(IntIndexData.createQuadIndices(numIndices));
                }
                this.mesh = combined.toIntIndexedMesh(true);
            }
            if (this.mesh == null) {
                return;
            }
        } else {
            this.mesh.setVertices(combined.getVertices().toArray());
        }
        if (RuntimeInfo.useSharedIndices) {
            int numIndices = this.mesh.getNumVertices() * 6 / 4;
            SharedQuadIndexData.allowForNumIndices(numIndices, true);
        }
    }

    public static Vector3 getTmp() {
		return tmp;
	}

	public static void setTmp(Vector3 tmp) {
		ChunkBatch.tmp = tmp;
	}

	record MeshDataMetadata(int vertexPosition, int numVertices) {
    }
}