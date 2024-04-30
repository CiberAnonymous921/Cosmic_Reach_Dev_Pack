package finalforeach.cosmicreach.rendering;

import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;

public interface IMeshData {
    public FloatArray getVertices();

    public IntArray getIndices();

    public void ensureVerticesCapacity(int var1);
}