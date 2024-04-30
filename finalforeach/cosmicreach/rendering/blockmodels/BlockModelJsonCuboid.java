package finalforeach.cosmicreach.rendering.blockmodels;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

import finalforeach.cosmicreach.rendering.shaders.ChunkShader;

public class BlockModelJsonCuboid {
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
    public float[] localBounds;
    public OrderedMap<String, BlockModelJsonCuboidFace> faces;

    public BoundingBox getBoundingBox() {
        BoundingBox bb = new BoundingBox();
        bb.min.set(this.localBounds[0] / 16.0f, this.localBounds[1] / 16.0f, this.localBounds[2] / 16.0f);
        bb.max.set(this.localBounds[3] / 16.0f, this.localBounds[4] / 16.0f, this.localBounds[5] / 16.0f);
        bb.update();
        return bb;
    }

    private BlockModelJsonTexture getTexture(OrderedMap<String, BlockModelJsonTexture> textures, String texName) {
        BlockModelJsonTexture t = (BlockModelJsonTexture)textures.get(texName);
        if (t != null) {
            return t;
        }
        if (texName.equals("slab_top")) {
            return this.getTexture(textures, "top");
        }
        if (texName.equals("slab_bottom")) {
            return this.getTexture(textures, "bottom");
        }
        if (texName.equals("slab_side")) {
            return this.getTexture(textures, "side");
        }
        return (BlockModelJsonTexture)textures.get("all");
    }

    @SuppressWarnings("rawtypes")
    void initialize(OrderedMap<String, BlockModelJsonTexture> textures, Array<BlockModelJsonCuboidFace> allFaces) {
        for (ObjectMap.Entry entry : this.faces) {
            BlockModelJsonCuboidFace f = (BlockModelJsonCuboidFace)entry.value;
            boolean isValidFace = true;
            String faceDirection = (String)entry.key;
            float x1 = this.localBounds[0] / 16.0f;
            float y1 = this.localBounds[1] / 16.0f;
            float z1 = this.localBounds[2] / 16.0f;
            float x2 = this.localBounds[3] / 16.0f;
            float y2 = this.localBounds[4] / 16.0f;
            float z2 = this.localBounds[5] / 16.0f;
            float minX = Math.min(x1, x2);
            float minY = Math.min(y1, y2);
            float minZ = Math.min(z1, z2);
            float maxX = Math.max(x1, x2);
            float maxY = Math.max(y1, y2);
            float maxZ = Math.max(z1, z2);
            float uvScale = ChunkShader.allBlocksTexSize / 16;
            if (f.cullFace) {
                switch (faceDirection) {
                    case "localNegX": {
                        f.cullingMask = 1;
                        break;
                    }
                    case "localPosX": {
                        f.cullingMask = 2;
                        break;
                    }
                    case "localNegY": {
                        f.cullingMask = 4;
                        break;
                    }
                    case "localPosY": {
                        f.cullingMask = 8;
                        break;
                    }
                    case "localNegZ": {
                        f.cullingMask = 16;
                        break;
                    }
                    case "localPosZ": {
                        f.cullingMask = 32;
                    }
                }
            }
            BlockModelJsonTexture t = this.getTexture(textures, f.texture);
            switch (faceDirection) {
                case "localNegX": {
                    this.isNegXFaceOccluding |= minX == 0.0f && minY <= 0.0f && maxY >= 1.0f && minZ <= 0.0f && maxZ >= 1.0f;
                    this.isNegXFacePartOccluding |= minX == 0.0f;
                    f.vertexIndexA = 0;
                    f.aoBitmaskA1 = 64;
                    f.aoBitmaskA2 = 128;
                    f.aoBitmaskA3 = 512;
                    f.vertexIndexB = 1;
                    f.aoBitmaskB1 = 256;
                    f.aoBitmaskB2 = 128;
                    f.aoBitmaskB3 = 1024;
                    f.vertexIndexC = 5;
                    f.aoBitmaskC1 = 8192;
                    f.aoBitmaskC2 = 4096;
                    f.aoBitmaskC3 = 1024;
                    f.vertexIndexD = 4;
                    f.aoBitmaskD1 = 2048;
                    f.aoBitmaskD2 = 4096;
                    f.aoBitmaskD3 = 512;
                    f.x1 = x1;
                    f.y1 = y1;
                    f.z1 = z1;
                    f.x2 = x1;
                    f.y2 = y2;
                    f.z2 = z2;
                    f.midX1 = f.x1;
                    f.midY1 = f.y1;
                    f.midZ1 = f.z2;
                    f.midX2 = f.x1;
                    f.midY2 = f.y2;
                    f.midZ2 = f.z1;
                    f.uA = (t.uv[0] + f.uv[0] / 16.0f) / uvScale;
                    f.vA = (t.uv[1] + f.uv[3] / 16.0f) / uvScale;
                    f.uB = (t.uv[0] + f.uv[2] / 16.0f) / uvScale;
                    f.vB = (t.uv[1] + f.uv[3] / 16.0f) / uvScale;
                    f.uC = (t.uv[0] + f.uv[2] / 16.0f) / uvScale;
                    f.vC = (t.uv[1] + f.uv[1] / 16.0f) / uvScale;
                    f.uD = (t.uv[0] + f.uv[0] / 16.0f) / uvScale;
                    f.vD = (t.uv[1] + f.uv[1] / 16.0f) / uvScale;
                    break;
                }
                case "localPosX": {
                    this.isPosXFaceOccluding |= maxX == 1.0f && minY <= 0.0f && maxY >= 1.0f && minZ <= 0.0f && maxZ >= 1.0f;
                    this.isPosXFacePartOccluding |= maxX == 1.0f;
                    f.vertexIndexA = 2;
                    f.aoBitmaskA1 = 262144;
                    f.aoBitmaskA2 = 524288;
                    f.aoBitmaskA3 = 0x200000;
                    f.vertexIndexB = 6;
                    f.aoBitmaskB1 = 0x800000;
                    f.aoBitmaskB2 = 0x1000000;
                    f.aoBitmaskB3 = 0x200000;
                    f.vertexIndexC = 7;
                    f.aoBitmaskC1 = 0x2000000;
                    f.aoBitmaskC2 = 0x1000000;
                    f.aoBitmaskC3 = 0x400000;
                    f.vertexIndexD = 3;
                    f.aoBitmaskD1 = 0x100000;
                    f.aoBitmaskD2 = 524288;
                    f.aoBitmaskD3 = 0x400000;
                    f.x1 = x2;
                    f.y1 = y1;
                    f.z1 = z1;
                    f.x2 = x2;
                    f.y2 = y2;
                    f.z2 = z2;
                    f.midX1 = f.x1;
                    f.midY1 = f.y2;
                    f.midZ1 = f.z1;
                    f.midX2 = f.x1;
                    f.midY2 = f.y1;
                    f.midZ2 = f.z2;
                    f.uA = (t.uv[0] + f.uv[2] / 16.0f) / uvScale;
                    f.vA = (t.uv[1] + f.uv[3] / 16.0f) / uvScale;
                    f.uB = (t.uv[0] + f.uv[2] / 16.0f) / uvScale;
                    f.vB = (t.uv[1] + f.uv[1] / 16.0f) / uvScale;
                    f.uC = (t.uv[0] + f.uv[0] / 16.0f) / uvScale;
                    f.vC = (t.uv[1] + f.uv[1] / 16.0f) / uvScale;
                    f.uD = (t.uv[0] + f.uv[0] / 16.0f) / uvScale;
                    f.vD = (t.uv[1] + f.uv[3] / 16.0f) / uvScale;
                    break;
                }
                case "localNegY": {
                    this.isNegYFaceOccluding |= minX <= 0.0f && maxX >= 1.0f && minY == 0.0f && minZ <= 0.0f && maxZ >= 1.0f;
                    this.isNegYFacePartOccluding |= minY == 0.0f;
                    f.vertexIndexA = 0;
                    f.aoBitmaskA1 = 64;
                    f.aoBitmaskA2 = 128;
                    f.aoBitmaskA3 = 16384;
                    f.vertexIndexB = 2;
                    f.aoBitmaskB1 = 262144;
                    f.aoBitmaskB2 = 524288;
                    f.aoBitmaskB3 = 16384;
                    f.vertexIndexC = 3;
                    f.aoBitmaskC1 = 0x100000;
                    f.aoBitmaskC2 = 524288;
                    f.aoBitmaskC3 = 32768;
                    f.vertexIndexD = 1;
                    f.aoBitmaskD1 = 256;
                    f.aoBitmaskD2 = 128;
                    f.aoBitmaskD3 = 32768;
                    f.x1 = x1;
                    f.y1 = y1;
                    f.z1 = z1;
                    f.x2 = x2;
                    f.y2 = y1;
                    f.z2 = z2;
                    f.midX1 = f.x2;
                    f.midY1 = f.y1;
                    f.midZ1 = f.z1;
                    f.midX2 = f.x1;
                    f.midY2 = f.y1;
                    f.midZ2 = f.z2;
                    f.uA = (t.uv[0] + f.uv[0] / 16.0f) / uvScale;
                    f.vA = (t.uv[1] + f.uv[3] / 16.0f) / uvScale;
                    f.uB = (t.uv[0] + f.uv[2] / 16.0f) / uvScale;
                    f.vB = (t.uv[1] + f.uv[3] / 16.0f) / uvScale;
                    f.uC = (t.uv[0] + f.uv[2] / 16.0f) / uvScale;
                    f.vC = (t.uv[1] + f.uv[1] / 16.0f) / uvScale;
                    f.uD = (t.uv[0] + f.uv[0] / 16.0f) / uvScale;
                    f.vD = (t.uv[1] + f.uv[1] / 16.0f) / uvScale;
                    break;
                }
                case "localPosY": {
                    this.isPosYFaceOccluding |= minX <= 0.0f && maxX >= 1.0f && maxY == 1.0f && minZ <= 0.0f && maxZ >= 1.0f;
                    this.isPosYFacePartOccluding |= maxY == 1.0f;
                    f.vertexIndexA = 4;
                    f.aoBitmaskA1 = 2048;
                    f.aoBitmaskA2 = 4096;
                    f.aoBitmaskA3 = 65536;
                    f.vertexIndexB = 5;
                    f.aoBitmaskB1 = 8192;
                    f.aoBitmaskB2 = 4096;
                    f.aoBitmaskB3 = 131072;
                    f.vertexIndexC = 7;
                    f.aoBitmaskC1 = 0x2000000;
                    f.aoBitmaskC2 = 0x1000000;
                    f.aoBitmaskC3 = 131072;
                    f.vertexIndexD = 6;
                    f.aoBitmaskD1 = 0x800000;
                    f.aoBitmaskD2 = 0x1000000;
                    f.aoBitmaskD3 = 65536;
                    f.x1 = x1;
                    f.y1 = y2;
                    f.z1 = z1;
                    f.x2 = x2;
                    f.y2 = y2;
                    f.z2 = z2;
                    f.midX1 = f.x1;
                    f.midY1 = f.y1;
                    f.midZ1 = f.z2;
                    f.midX2 = f.x2;
                    f.midY2 = f.y1;
                    f.midZ2 = f.z1;
                    f.uA = (t.uv[0] + f.uv[0] / 16.0f) / uvScale;
                    f.vA = (t.uv[1] + f.uv[1] / 16.0f) / uvScale;
                    f.uB = (t.uv[0] + f.uv[0] / 16.0f) / uvScale;
                    f.vB = (t.uv[1] + f.uv[3] / 16.0f) / uvScale;
                    f.uC = (t.uv[0] + f.uv[2] / 16.0f) / uvScale;
                    f.vC = (t.uv[1] + f.uv[3] / 16.0f) / uvScale;
                    f.uD = (t.uv[0] + f.uv[2] / 16.0f) / uvScale;
                    f.vD = (t.uv[1] + f.uv[1] / 16.0f) / uvScale;
                    break;
                }
                case "localNegZ": {
                    this.isNegZFaceOccluding |= minX <= 0.0f && maxX >= 1.0f && minY <= 0.0f && maxY >= 1.0f && minZ == 0.0f;
                    this.isNegZFacePartOccluding |= minZ == 0.0f;
                    f.vertexIndexA = 0;
                    f.aoBitmaskA1 = 64;
                    f.aoBitmaskA2 = 16384;
                    f.aoBitmaskA3 = 512;
                    f.vertexIndexB = 4;
                    f.aoBitmaskB1 = 2048;
                    f.aoBitmaskB2 = 65536;
                    f.aoBitmaskB3 = 512;
                    f.vertexIndexC = 6;
                    f.aoBitmaskC1 = 0x800000;
                    f.aoBitmaskC2 = 65536;
                    f.aoBitmaskC3 = 0x200000;
                    f.vertexIndexD = 2;
                    f.aoBitmaskD1 = 262144;
                    f.aoBitmaskD2 = 16384;
                    f.aoBitmaskD3 = 0x200000;
                    f.x1 = x1;
                    f.y1 = y1;
                    f.z1 = z1;
                    f.x2 = x2;
                    f.y2 = y2;
                    f.z2 = z1;
                    f.midX1 = f.x1;
                    f.midY1 = f.y2;
                    f.midZ1 = f.z1;
                    f.midX2 = f.x2;
                    f.midY2 = f.y1;
                    f.midZ2 = f.z1;
                    f.uA = (t.uv[0] + f.uv[0] / 16.0f) / uvScale;
                    f.vA = (t.uv[1] + f.uv[3] / 16.0f) / uvScale;
                    f.uB = (t.uv[0] + f.uv[0] / 16.0f) / uvScale;
                    f.vB = (t.uv[1] + f.uv[1] / 16.0f) / uvScale;
                    f.uC = (t.uv[0] + f.uv[2] / 16.0f) / uvScale;
                    f.vC = (t.uv[1] + f.uv[1] / 16.0f) / uvScale;
                    f.uD = (t.uv[0] + f.uv[2] / 16.0f) / uvScale;
                    f.vD = (t.uv[1] + f.uv[3] / 16.0f) / uvScale;
                    break;
                }
                case "localPosZ": {
                    this.isPosZFaceOccluding |= minX <= 0.0f && maxX >= 1.0f && minY <= 0.0f && maxY >= 1.0f && maxZ == 1.0f;
                    this.isPosZFacePartOccluding |= maxZ == 1.0f;
                    f.vertexIndexA = 1;
                    f.aoBitmaskA1 = 256;
                    f.aoBitmaskA2 = 32768;
                    f.aoBitmaskA3 = 1024;
                    f.vertexIndexB = 3;
                    f.aoBitmaskB1 = 0x100000;
                    f.aoBitmaskB2 = 32768;
                    f.aoBitmaskB3 = 0x400000;
                    f.vertexIndexC = 7;
                    f.aoBitmaskC1 = 0x2000000;
                    f.aoBitmaskC2 = 131072;
                    f.aoBitmaskC3 = 0x400000;
                    f.vertexIndexD = 5;
                    f.aoBitmaskD1 = 8192;
                    f.aoBitmaskD2 = 131072;
                    f.aoBitmaskD3 = 1024;
                    f.x1 = x1;
                    f.y1 = y1;
                    f.z1 = z2;
                    f.x2 = x2;
                    f.y2 = y2;
                    f.z2 = z2;
                    f.midX1 = f.x2;
                    f.midY1 = f.y1;
                    f.midZ1 = f.z1;
                    f.midX2 = f.x1;
                    f.midY2 = f.y2;
                    f.midZ2 = f.z1;
                    f.uA = (t.uv[0] + f.uv[0] / 16.0f) / uvScale;
                    f.vA = (t.uv[1] + f.uv[3] / 16.0f) / uvScale;
                    f.uB = (t.uv[0] + f.uv[2] / 16.0f) / uvScale;
                    f.vB = (t.uv[1] + f.uv[3] / 16.0f) / uvScale;
                    f.uC = (t.uv[0] + f.uv[2] / 16.0f) / uvScale;
                    f.vC = (t.uv[1] + f.uv[1] / 16.0f) / uvScale;
                    f.uD = (t.uv[0] + f.uv[0] / 16.0f) / uvScale;
                    f.vD = (t.uv[1] + f.uv[1] / 16.0f) / uvScale;
                    break;
                }
                default: {
                    isValidFace = false;
                }
            }
            switch (f.uvRotation) {
                case 0: {
                    break;
                }
                case 270: {
                    float tmpU = f.uA;
                    float tmpV = f.vA;
                    f.uA = f.uB;
                    f.vA = f.vB;
                    f.uB = f.uC;
                    f.vB = f.vC;
                    f.uC = f.uD;
                    f.vC = f.vD;
                    f.uD = tmpU;
                    f.vD = tmpV;
                }
                case 180: {
                    float tmpU = f.uA;
                    float tmpV = f.vA;
                    f.uA = f.uB;
                    f.vA = f.vB;
                    f.uB = f.uC;
                    f.vB = f.vC;
                    f.uC = f.uD;
                    f.vC = f.vD;
                    f.uD = tmpU;
                    f.vD = tmpV;
                }
                case 90: {
                    float tmpU = f.uA;
                    float tmpV = f.vA;
                    f.uA = f.uB;
                    f.vA = f.vB;
                    f.uB = f.uC;
                    f.vB = f.vC;
                    f.uC = f.uD;
                    f.vC = f.vD;
                    f.uD = tmpU;
                    f.vD = tmpV;
                    break;
                }
                default: {
                    throw new UnsupportedOperationException("Invalid value for uvRotation, must be 0, 90, 180, or 270");
                }
            }
            f.modelUvIdxA = this.getFaceUBOFloatsIdx(f.uA, f.vA);
            f.modelUvIdxB = this.getFaceUBOFloatsIdx(f.uB, f.vB);
            f.modelUvIdxC = this.getFaceUBOFloatsIdx(f.uC, f.vC);
            f.modelUvIdxD = this.getFaceUBOFloatsIdx(f.uD, f.vD);
            if (!isValidFace) continue;
            allFaces.add(f);
        }
    }

    private int getFaceUBOFloatsIdx(float u, float v) {
        for (int i = 0; i < ChunkShader.faceTexBufFloats.size; i += 2) {
            if (ChunkShader.faceTexBufFloats.get(i) != u || ChunkShader.faceTexBufFloats.get(i + 1) != v) continue;
            return i / 2;
        }
        int fIdx = ChunkShader.faceTexBufFloats.size / 2;
        ChunkShader.faceTexBufFloats.add(u);
        ChunkShader.faceTexBufFloats.add(v);
        return fIdx;
    }
}