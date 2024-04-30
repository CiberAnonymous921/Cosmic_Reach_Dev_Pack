package finalforeach.cosmicreach.world;

import finalforeach.cosmicreach.savelib.ISavedChunk;

public interface IChunkMeshGroup<T> {
    public boolean hasMesh();

    public T getAllMeshData();

    public void flagForRemeshing(boolean var1);

    public boolean isFlaggedForRemeshing();

    public boolean hasExpectedMeshCount();

    public void flushRemeshRequests();

    public T buildMeshVertices(ISavedChunk<?> var1);

    public void setMeshVertices(T var1);

    public boolean isFlaggedForImmediateRemesh();

    public void setToRemeshImmediately(boolean var1);

    public void setExpectedMeshGenCount();

    public void dispose();
}