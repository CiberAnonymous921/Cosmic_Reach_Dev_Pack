package finalforeach.cosmicreach.rendering.blockmodels;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import finalforeach.cosmicreach.rendering.IMeshData;

public abstract class BlockModel {
    public boolean isPosXFaceOccluding;
    public boolean isNegXFaceOccluding;
    public boolean isPosYFaceOccluding;
    public boolean isNegYFaceOccluding;
    public boolean isPosZFaceOccluding;
    public boolean isNegZFaceOccluding;
    public boolean isPosXFacePartOccluding;
    public boolean isNegXFacePartOccluding;
    public boolean isPosYFacePartOccluding;
    public boolean isNegYFacePartOccluding;
    public boolean isPosZFacePartOccluding;
    public boolean isNegZFacePartOccluding;
    public BoundingBox boundingBox = new BoundingBox();

    public abstract void addVertices(IMeshData var1, int var2, int var3, int var4, int var5, short[] var6, int[] var7);

    public abstract boolean isGreedyCube();

    public abstract boolean canGreedyCombine();

    public abstract boolean isEmpty();

    public abstract void getAllBoundingBoxes(Array<BoundingBox> var1, int var2, int var3, int var4);
}