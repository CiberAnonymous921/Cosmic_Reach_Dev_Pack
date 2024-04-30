package finalforeach.cosmicreach.rendering;

import finalforeach.cosmicreach.RuntimeInfo;
import finalforeach.cosmicreach.rendering.meshes.IntIndexData;

public class SharedQuadIndexData {
    public static IntIndexData indexData;

    public SharedQuadIndexData() {
        if (!RuntimeInfo.useSharedIndices) {
            throw new RuntimeException("RuntimeInfo.useSharedIndices is disabled, do not instantiate!");
        }
    }

    public static void bind() {
        indexData.bind();
    }

    public static void unbind() {
        indexData.unbind();
    }

    public static void allowForNumIndices(int numIndices, boolean bind) {
        numIndices = (int)Math.ceil((float)numIndices / 6.0f) * 6;
        if (indexData != null && indexData.getNumMaxIndices() < numIndices) {
            indexData.dispose();
            indexData = null;
        }
        if (indexData == null) {
            indexData = IntIndexData.createQuadIndexData(numIndices);
            if (bind) {
                indexData.bind();
            }
        }
    }
}