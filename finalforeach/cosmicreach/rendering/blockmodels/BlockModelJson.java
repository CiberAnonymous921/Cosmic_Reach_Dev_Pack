package finalforeach.cosmicreach.rendering.blockmodels;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.OrderedMap;

import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.RuntimeInfo;
import finalforeach.cosmicreach.rendering.IMeshData;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;

public class BlockModelJson extends BlockModel {
    private static final Map<BlockModelJsonInstanceKey, BlockModelJson> models = new HashMap<BlockModelJsonInstanceKey, BlockModelJson>();
    private String parent;
    private OrderedMap<String, BlockModelJsonTexture> textures;
    private BlockModelJsonCuboid[] cuboids;
    private transient BlockModelJsonCuboidFace[] allFaces;
    public static final boolean useIndices = !RuntimeInfo.useSharedIndices;
    Boolean canGreedyCombine;
    public int uvUBOIndex;

    public static BlockModelJson getInstance(String modelName, int rotXZ) {
        BlockModelJsonInstanceKey key = new BlockModelJsonInstanceKey(modelName, rotXZ);
        if (!models.containsKey((Object)key)) {
            String jsonStr = GameAssetLoader.loadAsset("models/blocks/" + modelName + ".json").readString();
            BlockModelJson b = BlockModelJson.fromJson(jsonStr, rotXZ);
            models.put(key, b);
        }
        return models.get((Object)key);
    }

    public static BlockModelJson getInstanceFromJsonStr(String modelName, String modelJson, int rotXZ) {
        BlockModelJsonInstanceKey key = new BlockModelJsonInstanceKey(modelName, rotXZ);
        if (!models.containsKey((Object)key)) {
            BlockModelJson b = BlockModelJson.fromJson(modelJson, rotXZ);
            models.put(key, b);
        }
        return models.get((Object)key);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static BlockModelJson fromJson(String modelJson, int rotXZ) {
        Json json = new Json();
        BlockModelJson b = json.fromJson(BlockModelJson.class, modelJson);
        if (b.parent != null && !b.parent.isEmpty()) {
            BlockModelJson parent = BlockModelJson.getInstance(b.parent, 0);
            if (b.cuboids == null && parent.cuboids != null) {
                b.cuboids = (BlockModelJsonCuboid[])json.fromJson(parent.cuboids.getClass(), json.toJson(parent.cuboids));
            }
            if (b.textures == null && parent.textures != null) {
                b.textures = (OrderedMap)json.fromJson(parent.textures.getClass(), json.toJson(parent.textures));
            }
        }
        b.initialize(rotXZ);
        return b;
    }

    private BlockModelJson() {
    }

    public OrderedMap<String, BlockModelJsonTexture> getTextures() {
        return this.textures;
    }

    private void initialize(int rotXZ) {
        if (this.textures != null) {
            for (BlockModelJsonTexture t : this.textures.values()) {
                if (t.fileName == null) continue;
                t.uv = ChunkShader.addToAllBlocksTexture(this, t);
            }
        }
        if (this.cuboids != null && this.textures != null) {
            Array<BlockModelJsonCuboidFace> faces = new Array<BlockModelJsonCuboidFace>(BlockModelJsonCuboidFace.class);
            for (BlockModelJsonCuboid c : this.cuboids) {
                float boundsX1 = c.localBounds[0];
                float boundsZ1 = c.localBounds[2];
                float boundsX2 = c.localBounds[3];
                float boundsZ2 = c.localBounds[5];
                BlockModelJsonCuboidFace tmpNegX = (BlockModelJsonCuboidFace)c.faces.get("localNegX");
                BlockModelJsonCuboidFace tmpPosX = (BlockModelJsonCuboidFace)c.faces.get("localPosX");
                BlockModelJsonCuboidFace tmpNegY = (BlockModelJsonCuboidFace)c.faces.get("localNegY");
                BlockModelJsonCuboidFace tmpPosY = (BlockModelJsonCuboidFace)c.faces.get("localPosY");
                BlockModelJsonCuboidFace tmpNegZ = (BlockModelJsonCuboidFace)c.faces.get("localNegZ");
                BlockModelJsonCuboidFace tmpPosZ = (BlockModelJsonCuboidFace)c.faces.get("localPosZ");
                switch (rotXZ) {
                    case 0: {
                        break;
                    }
                    case 90: {
                        c.localBounds[0] = boundsZ1;
                        c.localBounds[2] = boundsX1;
                        c.localBounds[3] = boundsZ2;
                        c.localBounds[5] = boundsX2;
                        c.faces.clear();
                        if (tmpPosX != null) {
                            c.faces.put("localPosZ", tmpPosX);
                        }
                        if (tmpNegX != null) {
                            c.faces.put("localNegZ", tmpNegX);
                        }
                        if (tmpNegY != null) {
                            tmpNegY.uvRotation = (tmpNegY.uvRotation - 90 + 360) % 360;
                            c.faces.put("localNegY", tmpNegY);
                        }
                        if (tmpPosY != null) {
                            tmpPosY.uvRotation = (tmpPosY.uvRotation + 90 + 360) % 360;
                            c.faces.put("localPosY", tmpPosY);
                        }
                        if (tmpNegZ != null) {
                            float tmpU = tmpPosZ.uv[0];
                            tmpNegZ.uv[0] = tmpNegZ.uv[2];
                            tmpNegZ.uv[2] = tmpU;
                            c.faces.put("localPosX", tmpNegZ);
                        }
                        if (tmpPosZ == null) break;
                        c.faces.put("localNegX", tmpPosZ);
                        break;
                    }
                    case 180: {
                        float tmpU;
                        float fxa = 16.0f - boundsX1;
                        float fxb = 16.0f - boundsX2;
                        float fza = 16.0f - boundsZ1;
                        float fzb = 16.0f - boundsZ2;
                        c.localBounds[0] = Math.min(fxa, fxb);
                        c.localBounds[2] = Math.min(fza, fzb);
                        c.localBounds[3] = Math.max(fxa, fxb);
                        c.localBounds[5] = Math.max(fza, fzb);
                        c.faces.clear();
                        if (tmpNegX != null) {
                            c.faces.put("localPosX", tmpNegX);
                        }
                        if (tmpPosX != null) {
                            c.faces.put("localNegX", tmpPosX);
                        }
                        if (tmpNegY != null) {
                            c.faces.put("localNegY", tmpNegY);
                        }
                        if (tmpPosY != null) {
                            c.faces.put("localPosY", tmpPosY);
                        }
                        if (tmpPosZ != null) {
                            tmpU = tmpPosZ.uv[0];
                            tmpPosZ.uv[0] = tmpPosZ.uv[2];
                            tmpPosZ.uv[2] = tmpU;
                            c.faces.put("localNegZ", tmpPosZ);
                        }
                        if (tmpNegZ == null) break;
                        tmpU = tmpNegZ.uv[0];
                        tmpNegZ.uv[0] = tmpNegZ.uv[2];
                        tmpNegZ.uv[2] = tmpU;
                        c.faces.put("localPosZ", tmpNegZ);
                        break;
                    }
                    case 270: {
                        float fxa = 16.0f - boundsX1;
                        float fxb = 16.0f - boundsX2;
                        float fza = 16.0f - boundsZ1;
                        float fzb = 16.0f - boundsZ2;
                        c.localBounds[0] = Math.min(fza, fzb);
                        c.localBounds[2] = Math.min(fxa, fxb);
                        c.localBounds[3] = Math.max(fza, fzb);
                        c.localBounds[5] = Math.max(fxa, fxb);
                        c.faces.clear();
                        if (tmpNegX != null) {
                            c.faces.put("localPosZ", tmpNegX);
                        }
                        if (tmpPosX != null) {
                            c.faces.put("localNegZ", tmpPosX);
                        }
                        if (tmpNegY != null) {
                            tmpNegY.uvRotation = (tmpNegY.uvRotation - 90 + 360) % 360;
                            c.faces.put("localNegY", tmpNegY);
                        }
                        if (tmpPosY != null) {
                            tmpPosY.uvRotation = (tmpPosY.uvRotation + 90 + 360) % 360;
                            c.faces.put("localPosY", tmpPosY);
                        }
                        if (tmpPosZ != null) {
                            c.faces.put("localPosX", tmpPosZ);
                        }
                        if (tmpNegZ == null) break;
                        float tmpU = tmpPosZ.uv[0];
                        tmpNegZ.uv[0] = tmpNegZ.uv[2];
                        tmpNegZ.uv[2] = tmpU;
                        c.faces.put("localNegX", tmpNegZ);
                        break;
                    }
                }
                c.initialize(this.textures, faces);
                this.isNegXFaceOccluding |= c.isNegXFaceOccluding;
                this.isPosXFaceOccluding |= c.isPosXFaceOccluding;
                this.isNegYFaceOccluding |= c.isNegYFaceOccluding;
                this.isPosYFaceOccluding |= c.isPosYFaceOccluding;
                this.isNegZFaceOccluding |= c.isNegZFaceOccluding;
                this.isPosZFaceOccluding |= c.isPosZFaceOccluding;
                this.isNegXFacePartOccluding |= c.isNegXFacePartOccluding;
                this.isPosXFacePartOccluding |= c.isPosXFacePartOccluding;
                this.isNegYFacePartOccluding |= c.isNegYFacePartOccluding;
                this.isPosYFacePartOccluding |= c.isPosYFacePartOccluding;
                this.isNegZFacePartOccluding |= c.isNegZFacePartOccluding;
                this.isPosZFacePartOccluding |= c.isPosZFacePartOccluding;
            }
            for (BlockModelJsonCuboid c : this.cuboids) {
                if (this.boundingBox.max.epsilonEquals(this.boundingBox.min)) {
                    this.boundingBox = c.getBoundingBox();
                    continue;
                }
                this.boundingBox.ext(c.getBoundingBox());
            }
            this.allFaces = (BlockModelJsonCuboidFace[])faces.toArray();
        } else {
            this.allFaces = new BlockModelJsonCuboidFace[0];
        }
        if (this.cuboids == null || this.cuboids.length == 0 || this.boundingBox.max.epsilonEquals(this.boundingBox.min)) {
            this.boundingBox.min.set(0.0f, 0.0f, 0.0f);
            this.boundingBox.max.set(1.0f, 1.0f, 1.0f);
        }
        this.boundingBox.update();
    }

    @Override
    public void addVertices(IMeshData meshData, int bx, int by, int bz, int opaqueBitmask, short[] blockLightLevels, int[] skyLightLevels) {
        IntArray indices = meshData.getIndices();
        meshData.ensureVerticesCapacity(6 * this.allFaces.length * 7);
        for (int fi = 0; fi < this.allFaces.length; ++fi) {
            int aoIdD;
            int aoIdC;
            int aoIdB;
            int aoIdA;
            BlockModelJsonCuboidFace f = this.allFaces[fi];
            if ((opaqueBitmask & f.cullingMask) != 0) continue;
            float x1 = (float)bx + f.x1;
            float y1 = (float)by + f.y1;
            float z1 = (float)bz + f.z1;
            float x2 = (float)bx + f.x2;
            float y2 = (float)by + f.y2;
            float z2 = (float)bz + f.z2;
            float midX1 = (float)bx + f.midX1;
            float midY1 = (float)by + f.midY1;
            float midZ1 = (float)bz + f.midZ1;
            float midX2 = (float)bx + f.midX2;
            float midY2 = (float)by + f.midY2;
            float midZ2 = (float)bz + f.midZ2;
            if (f.ambientocclusion) {
                aoIdA = ((opaqueBitmask & f.aoBitmaskA1) == 0 ? 1 : 0) + ((opaqueBitmask & f.aoBitmaskA2) == 0 ? 1 : 0) + ((opaqueBitmask & f.aoBitmaskA3) == 0 ? 1 : 0);
                aoIdB = ((opaqueBitmask & f.aoBitmaskB1) == 0 ? 1 : 0) + ((opaqueBitmask & f.aoBitmaskB2) == 0 ? 1 : 0) + ((opaqueBitmask & f.aoBitmaskB3) == 0 ? 1 : 0);
                aoIdC = ((opaqueBitmask & f.aoBitmaskC1) == 0 ? 1 : 0) + ((opaqueBitmask & f.aoBitmaskC2) == 0 ? 1 : 0) + ((opaqueBitmask & f.aoBitmaskC3) == 0 ? 1 : 0);
                aoIdD = ((opaqueBitmask & f.aoBitmaskD1) == 0 ? 1 : 0) + ((opaqueBitmask & f.aoBitmaskD2) == 0 ? 1 : 0) + ((opaqueBitmask & f.aoBitmaskD3) == 0 ? 1 : 0);
            } else {
                aoIdA = 3;
                aoIdB = 3;
                aoIdC = 3;
                aoIdD = 3;
            }
            int viA = f.vertexIndexA;
            int viB = f.vertexIndexB;
            int viC = f.vertexIndexC;
            int viD = f.vertexIndexD;
            int i1 = this.addVert(meshData, x1, y1, z1, f.uA, f.vA, aoIdA, blockLightLevels[viA], skyLightLevels[viA], f.modelUvIdxA);
            int i2 = this.addVert(meshData, midX1, midY1, midZ1, f.uB, f.vB, aoIdB, blockLightLevels[viB], skyLightLevels[viB], f.modelUvIdxB);
            int i3 = this.addVert(meshData, x2, y2, z2, f.uC, f.vC, aoIdC, blockLightLevels[viC], skyLightLevels[viC], f.modelUvIdxC);
            int i4 = this.addVert(meshData, midX2, midY2, midZ2, f.uD, f.vD, aoIdD, blockLightLevels[viD], skyLightLevels[viD], f.modelUvIdxD);
            if (!useIndices) continue;
            indices.add(i1);
            indices.add(i2);
            indices.add(i3);
            indices.add(i3);
            indices.add(i4);
            indices.add(i1);
        }
    }

    public int addVert(IMeshData meshData, float x, float y, float z, float u, float v, int aoId, short blockLight, int skyLight, int uvIdx) {
        FloatArray verts = meshData.getVertices();
        float[] items = verts.items;
        int size = verts.size;
        int numComponents = 5;
        if (RuntimeInfo.isMac) {
            ++numComponents;
        }
        int indexOfCurVertex = size / numComponents;
        int r = 17 * ((blockLight & 0xF00) >> 8);
        int g = 17 * ((blockLight & 0xF0) >> 4);
        int b = 17 * (blockLight & 0xF);
        float subAO = (float)aoId / 4.0f + 0.25f;
        r = (int)((float)r * subAO);
        g = (int)((float)g * subAO);
        b = (int)((float)b * subAO);
        skyLight = (int)((float)skyLight * (subAO * 17.0f));
        items[size] = x;
        items[size + 1] = y;
        items[size + 2] = z;
        items[size + 3] = Color.toFloatBits(r, g, b, skyLight);
        if (RuntimeInfo.isMac) {
            items[size + 4] = u;
            items[size + 5] = v;
        } else {
            items[size + 4] = Float.intBitsToFloat(uvIdx);
        }
        verts.size += numComponents;
        return indexOfCurVertex;
    }

    @Override
    public boolean isGreedyCube() {
        if (this.cuboids.length == 1) {
            BlockModelJsonCuboid c = this.cuboids[0];
            if (c.faces.size == 6 && c.localBounds[0] == 0.0f && c.localBounds[1] == 0.0f && c.localBounds[2] == 0.0f && c.localBounds[3] == 16.0f && c.localBounds[4] == 16.0f && c.localBounds[5] == 16.0f) {
                for (BlockModelJsonCuboidFace f : c.faces.values()) {
                    boolean expectedUVs = true;
                    expectedUVs &= f.uv[0] == 0.0f;
                    expectedUVs &= f.uv[1] == 0.0f;
                    expectedUVs &= f.uv[2] == 16.0f;
                    if (expectedUVs &= f.uv[3] == 16.0f) continue;
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canGreedyCombine() {
        if (this.canGreedyCombine == null) {
            return this.isGreedyCube();
        }
        return this.canGreedyCombine;
    }

    @Override
    public boolean isEmpty() {
        return this.cuboids == null || this.cuboids.length == 0;
    }

    @Override
    public void getAllBoundingBoxes(Array<BoundingBox> boundingBoxes, int bx, int by, int bz) {
        boundingBoxes.clear();
        if (this.cuboids == null) {
            return;
        }
        for (BlockModelJsonCuboid c : this.cuboids) {
            BoundingBox bb = new BoundingBox();
            bb.min.set(c.localBounds[0] / 16.0f, c.localBounds[1] / 16.0f, c.localBounds[2] / 16.0f);
            bb.max.set(c.localBounds[3] / 16.0f, c.localBounds[4] / 16.0f, c.localBounds[5] / 16.0f);
            bb.min.add(bx, by, bz);
            bb.max.add(bx, by, bz);
            bb.update();
            boundingBoxes.add(bb);
        }
    }

    record BlockModelJsonInstanceKey(String modelName, int rotXZ) {
    }
}