package finalforeach.cosmicreach.rendering;

import finalforeach.cosmicreach.world.Chunk;

public interface IWorldRenderingMeshGenThread {
    public void requestImmediateResorting();

    public void addChunk(Chunk var1);

    public void meshChunks();

    public void stopThread();
}