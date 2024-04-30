package finalforeach.cosmicreach.rendering;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Array;

import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.RuntimeInfo;
import finalforeach.cosmicreach.rendering.meshes.IGameMesh;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Region;
import finalforeach.cosmicreach.world.RegionOctant;
import finalforeach.cosmicreach.world.Zone;

public class ExperimentalNaiveZoneRenderer implements IZoneRenderer {
    public static GameShader lastBoundShader = null;

    @Override
    public void dispose() {
    }

    private void requestMeshes(Zone zone) {
        IWorldRenderingMeshGenThread meshGenThread = GameSingletons.meshGenThread;
        for (Region r : zone.regions.values()) {
            for (RegionOctant ro : r.octants) {
                for (Chunk chunk : ro.getChunks()) {
                    if (chunk == null) continue;
                    ChunkMeshGroup meshGroup = (ChunkMeshGroup) chunk.meshGroup;
                    if (meshGroup == null) {
                        meshGroup = (ChunkMeshGroup) (chunk.meshGroup = new ChunkMeshGroup());
                    }
                    if (!chunk.isGenerated || meshGroup.hasMesh() && !meshGroup.isFlaggedForRemeshing()) continue;
                    meshGroup.flushRemeshRequests();
                    meshGenThread.addChunk(chunk);
                }
            }
            meshGenThread.meshChunks();
        }
    }

    @Override
    public void render(Zone zone, Camera worldCamera) {
        Gdx.gl.glEnable(2929);
        Gdx.gl.glDepthFunc(513);
        Gdx.gl.glEnable(2884);
        Gdx.gl.glCullFace(1029);
        Gdx.gl.glEnable(3042);
        Gdx.gl.glBlendFunc(770, 771);
        this.requestMeshes(zone);
        lastBoundShader = null;
        if (SharedQuadIndexData.indexData != null && RuntimeInfo.useSharedIndices) {
            SharedQuadIndexData.indexData.bind();
        }
        for (Region r : zone.regions.values()) {
            if (!worldCamera.frustum.boundsInFrustum(r.boundingBox)) continue;
            for (RegionOctant ro : r.octants) {
                for (Chunk c : ro.getChunks()) {
                    if (c == null) continue;
                    ChunkMeshGroup meshGroup = (ChunkMeshGroup)c.meshGroup;
                    if (meshGroup == null) {
                        meshGroup = (ChunkMeshGroup) (c.meshGroup = new ChunkMeshGroup());
                    }
                    Object allMeshData = meshGroup.getAllMeshData();
                    Iterator<?> iterator = ((Array<?>)allMeshData).iterator();
                    while (iterator.hasNext()) {
                        MeshData m = (MeshData)iterator.next();
                        if (m.mesh == null || m.meshDirty) {
                            if (m.mesh != null && m.meshDirty) {
                                m.mesh.dispose();
                            }
                            if (RuntimeInfo.useSharedIndices) {
                                m.mesh = m.toSharedIndexMesh(true);
                                int numIndices = m.mesh.getNumVertices() * 6 / 4;
                                SharedQuadIndexData.allowForNumIndices(numIndices, true);
                            } else {
                                m.mesh = m.toIntIndexedMesh(true);
                            }
                        }
                        IGameMesh mesh = m.mesh;
                        GameShader shader = m.shader;
                        mesh.setAutoBind(false);
                        if (lastBoundShader != shader) {
                            lastBoundShader = shader;
                            lastBoundShader.bind(worldCamera);
                        }
                        mesh.bind(shader.shader);
                        mesh.render(shader.shader, 4);
                        mesh.unbind(shader.shader);
                    }
                }
            }
        }
        if (SharedQuadIndexData.indexData != null && RuntimeInfo.useSharedIndices) {
            SharedQuadIndexData.indexData.unbind();
        }
        Gdx.gl.glActiveTexture(33984);
        Gdx.gl.glBindTexture(3553, 0);
    }

    @Override
    public void removeRegion(Region r) {
    }

    @Override
    public void unload() {
    }
}