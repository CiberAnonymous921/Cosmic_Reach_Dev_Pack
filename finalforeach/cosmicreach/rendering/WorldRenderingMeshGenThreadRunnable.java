package finalforeach.cosmicreach.rendering;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.savelib.ISavedChunk;
import finalforeach.cosmicreach.world.Chunk;

class WorldRenderingMeshGenThreadRunnable implements Runnable {
    private WorldRenderingMeshGenThread meshGenThread;
    public Vector3 lastChunkPos = new Vector3();
    Set<Chunk> immediateChunks = new HashSet<Chunk>();
    Set<Chunk> chunks = new HashSet<Chunk>();
    ConcurrentHashMap<Chunk, Array<MeshData>> meshedChunks = new ConcurrentHashMap<Chunk, Array<MeshData>>();
    volatile boolean resortingChunksNeeded;
    public volatile boolean immediateResortingRequested;
    Vector3 camPos = new Vector3();
    private final Comparator<Chunk> comparator = new Comparator<Chunk>(){

        @Override
        public int compare(Chunk a, Chunk b) {
            float distA = Vector3.dst2(a.blockX, a.blockY, a.blockZ, WorldRenderingMeshGenThreadRunnable.this.camPos.x, WorldRenderingMeshGenThreadRunnable.this.camPos.y, WorldRenderingMeshGenThreadRunnable.this.camPos.z);
            float distB = Vector3.dst2(b.blockX, b.blockY, b.blockZ, WorldRenderingMeshGenThreadRunnable.this.camPos.x, WorldRenderingMeshGenThreadRunnable.this.camPos.y, WorldRenderingMeshGenThreadRunnable.this.camPos.z);
            return (int)(distB - distA);
        }
    };
    long numMillisUsedGenerating;
    Array<Chunk> unMeshedChunksByDist = new Array<Chunk>(Chunk.class);

    public WorldRenderingMeshGenThreadRunnable(WorldRenderingMeshGenThread meshGenThread) {
        this.meshGenThread = meshGenThread;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void rebuildUnmeshedArray() {
        this.unMeshedChunksByDist.clear();
        Set<Chunk> set = this.chunks;
        synchronized (set) {
            if (!this.immediateChunks.isEmpty()) {
                for (Chunk chunk : this.immediateChunks) {
                    if (!chunk.isGenerated) continue;
                    this.unMeshedChunksByDist.add(chunk);
                }
                return;
            }
            for (Chunk chunk : this.chunks) {
                if (!chunk.isGenerated) continue;
                this.unMeshedChunksByDist.add(chunk);
            }
            this.resortingChunksNeeded = false;
        }
        try {
            this.camPos.set(InGame.IN_GAME.getWorldCamera().position);
            this.unMeshedChunksByDist.shrink();
            Arrays.sort((Chunk[])this.unMeshedChunksByDist.items, this.comparator);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SuppressWarnings("unchecked")
	@Override
    public void run() {
        this.immediateResortingRequested = false;
        this.rebuildUnmeshedArray();
        if (this.unMeshedChunksByDist.size > 0) {
            long start = System.currentTimeMillis();
            while (this.unMeshedChunksByDist.notEmpty()) {
                Chunk chunk;
                Set<Chunk> set = this.chunks;
                synchronized (set) {
                    chunk = this.unMeshedChunksByDist.pop();
                    this.chunks.remove(chunk);
                    if (!this.immediateChunks.isEmpty()) {
                        this.immediateChunks.remove(chunk);
                    }
                }
                ChunkMeshGroup meshGroup = (ChunkMeshGroup)chunk.meshGroup;
                Object meshData = meshGroup.buildMeshVertices((ISavedChunk<?>)chunk);
                this.meshedChunks.put(chunk, (Array<MeshData>)meshData);
                if (!this.immediateResortingRequested && this.immediateChunks.isEmpty() && (!this.resortingChunksNeeded || System.currentTimeMillis() - start <= 125L)) continue;
                break;
            }
            this.numMillisUsedGenerating += System.currentTimeMillis() - start;
        }
        if (this.chunks.isEmpty() && this.immediateChunks.isEmpty()) {
            this.meshGenThread.pauseableThread.onPause();
        }
    }
}