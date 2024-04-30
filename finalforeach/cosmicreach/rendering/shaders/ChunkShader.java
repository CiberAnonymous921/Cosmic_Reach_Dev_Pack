package finalforeach.cosmicreach.rendering.shaders;

import java.util.HashMap;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.utils.FloatArray;

import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.RuntimeInfo;
import finalforeach.cosmicreach.rendering.TextureBuffer;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJson;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJsonTexture;
import finalforeach.cosmicreach.world.Sky;

public class ChunkShader extends GameShader {
    public static Texture chunkTerrainTex;
    public static Texture noiseTex;
    public static ChunkShader DEFAULT_BLOCK_SHADER;
    public static ChunkShader WATER_BLOCK_SHADER;
    private static VertexAttribute posAttrib;
    private static VertexAttribute uvAttrib;
    private static VertexAttribute uvIdxAttrib;
    private static VertexAttribute lightingAttrib;
    public static int allBlocksTexSize;
    private static Pixmap allBlocksPix;
    private static int terrainPixCurX;
    private static int terrainPixCurY;
    private static HashMap<String, BlockModelJsonTexture> storedTexs;
    public static final int NUM_FLOATS_PER_FACE_UVTEXBUFF = 2;
    public static FloatArray faceTexBufFloats;
    private TextureBuffer uvTexBuf;

    public ChunkShader(String vertFile, String fragFile) {
        super(vertFile, fragFile);
        this.allVertexAttributes = new VertexAttribute[]{posAttrib, lightingAttrib, RuntimeInfo.isMac ? uvAttrib : uvIdxAttrib};
    }

    public static void initChunkShaders() {
        DEFAULT_BLOCK_SHADER = new ChunkShader("chunk.vert.glsl", "chunk.frag.glsl");
        WATER_BLOCK_SHADER = new ChunkShader("chunk-water.vert.glsl", "chunk-water.frag.glsl");
        noiseTex = new Texture(GameAssetLoader.loadAsset("textures/noise.png"), true);
        noiseTex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
    }

    public static float[] addToAllBlocksTexture(BlockModelJson blockModelJson, BlockModelJsonTexture t) {
        BlockModelJsonTexture storedTex = storedTexs.get(t.fileName);
        if (storedTex != null) {
            t.uv = storedTex.uv;
            return storedTex.uv;
        }
        Texture blockTex = new Texture(GameAssetLoader.loadAsset("textures/blocks/" + t.fileName));
        if (blockTex.getWidth() != blockTex.getHeight()) {
            throw new RuntimeException("Width and height of " + t.fileName + " must be the same!");
        }
        TextureData texData = blockTex.getTextureData();
        texData.prepare();
        Pixmap blockPix = texData.consumePixmap();
        float[] uv = new float[]{terrainPixCurX / blockPix.getWidth(), terrainPixCurY / blockPix.getHeight()};
        allBlocksPix.drawPixmap(blockPix, terrainPixCurX, terrainPixCurY);
        terrainPixCurX += blockPix.getWidth();
        if ((float)terrainPixCurX > (float)(allBlocksPix.getWidth() * 15) / 16.0f) {
            terrainPixCurX = 0;
            terrainPixCurY += blockPix.getHeight();
        }
        if (texData.disposePixmap()) {
            blockPix.dispose();
        }
        blockTex.dispose();
        if (chunkTerrainTex != null) {
            chunkTerrainTex.dispose();
            chunkTerrainTex = null;
        }
        storedTexs.put(t.fileName, t);
        return uv;
    }

    @Override
    public void bind(Camera worldCamera) {
        if (chunkTerrainTex == null) {
            chunkTerrainTex = new Texture(allBlocksPix);
            if (this.uvTexBuf != null) {
                this.uvTexBuf.dispose();
                this.uvTexBuf = null;
            }
            this.reload();
        }
        super.bind(worldCamera);
        if (this.uvTexBuf == null) {
            faceTexBufFloats.shrink();
            this.uvTexBuf = TextureBuffer.fromFloats(faceTexBufFloats.toArray());
        }
        this.shader.setUniformMatrix("u_projViewTrans", worldCamera.combined);
        int texNum = 0;
        texNum = this.bindOptionalTextureBuffer("texBuffer", this.uvTexBuf, texNum);
        texNum = this.bindOptionalTexture("texDiffuse", chunkTerrainTex, texNum);
        texNum = this.bindOptionalTexture("noiseTex", noiseTex, texNum);
        this.bindOptionalUniform3f("skyAmbientColor", Sky.ambientColor);
        this.bindOptionalUniform3f("skyColor", Sky.skyColor);
    }

    @Override
    public void unbind() {
        super.unbind();
        if (this.uvTexBuf != null) {
            this.uvTexBuf.unbind();
        }
    }

    static {
        posAttrib = VertexAttribute.Position();
        uvAttrib = new VertexAttribute(16, 2, 5126, false, "a_uv");
        uvIdxAttrib = new VertexAttribute(32, 1, 5126, false, "a_uvIdx");
        lightingAttrib = new VertexAttribute(4, 4, "a_lighting");
        allBlocksTexSize = 1024;
        allBlocksPix = new Pixmap(allBlocksTexSize, allBlocksTexSize, Pixmap.Format.RGBA8888);
        terrainPixCurX = 0;
        terrainPixCurY = 0;
        storedTexs = new HashMap<String, BlockModelJsonTexture>();
        faceTexBufFloats = new FloatArray();
    }
}