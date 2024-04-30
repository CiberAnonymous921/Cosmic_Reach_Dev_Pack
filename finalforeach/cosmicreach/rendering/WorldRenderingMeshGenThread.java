package finalforeach.cosmicreach.rendering;

import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.PauseableThread;

import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.IChunkMeshGroup;

public class WorldRenderingMeshGenThread implements IWorldRenderingMeshGenThread {
    public PauseableThread pauseableThread;
    WorldRenderingMeshGenThreadRunnable meshGenRunnable;
    private boolean started = false;
    public Vector3 tmpChunkPos = new Vector3();

    public WorldRenderingMeshGenThread() {
        this.meshGenRunnable = new WorldRenderingMeshGenThreadRunnable(this);
        this.pauseableThread = new PauseableThread(this.meshGenRunnable);
        this.pauseableThread.setName("WorldRenderingMeshGenThread");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addChunk(Chunk chunk) {
        Set<Chunk> set = this.meshGenRunnable.chunks;
        synchronized (set) {
            if (this.meshGenRunnable.chunks.add(chunk)) {
                this.meshGenRunnable.resortingChunksNeeded = true;
                IChunkMeshGroup<?> meshGroup = chunk.meshGroup;
                if (meshGroup.isFlaggedForImmediateRemesh()) {
                    this.meshGenRunnable.immediateChunks.add(chunk);
                    meshGroup.setToRemeshImmediately(false);
                }
            }
        }
    }

    @Override
    public void meshChunks() {
        if (this.hasChunksToMesh()) {
            if (!this.started) {
                this.pauseableThread.start();
                this.started = true;
            } else {
                this.pauseableThread.onResume();
            }
        }
        for (Map.Entry<Chunk, Array<MeshData>> e : this.meshGenRunnable.meshedChunks.entrySet()) {
            Chunk chunk = e.getKey();
            ChunkMeshGroup meshGroup = (ChunkMeshGroup)chunk.meshGroup;
            meshGroup.setMeshVertices(e.getValue());
            this.meshGenRunnable.meshedChunks.remove(chunk);
        }
    }

    public boolean hasChunksToMesh() {
        return this.meshGenRunnable.chunks.size() > 0;
    }

    @Override
    public void requestImmediateResorting() {
        this.meshGenRunnable.immediateResortingRequested = true;
    }

    public boolean shouldImmediatelyResort() {
        return this.meshGenRunnable.immediateResortingRequested;
    }

    @Override
    public void stopThread() {
        this.pauseableThread.stopThread();
    }
}