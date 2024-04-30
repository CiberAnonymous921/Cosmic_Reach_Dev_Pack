package finalforeach.cosmicreach.rendering.shaders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;

import finalforeach.cosmicreach.GameAssetLoader;

public class BlockSelectionShader extends GameShader {
    public static BlockSelectionShader SELECTION_SHADER;
    public static Texture blockSelectionTex;
    private static VertexAttribute posAttrib;
    private static VertexAttribute texCoordsAttrib;

    public BlockSelectionShader(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        this.allVertexAttributes = new VertexAttribute[]{posAttrib, texCoordsAttrib};
    }

    @Override
    public void bind(Camera worldCamera) {
        super.bind(worldCamera);
        this.shader.setUniformMatrix("u_projViewTrans", worldCamera.combined);
        int texNum = 0;
        texNum = this.bindOptionalTexture("texDiffuse", blockSelectionTex, texNum);
    }

    public static void initBlockSelectionShaders() {
        SELECTION_SHADER = new BlockSelectionShader("block-selection.vert.glsl", "block-selection.frag.glsl");
        blockSelectionTex = new Texture(GameAssetLoader.loadAsset("textures/block-selection.png"), true);
    }

    static {
        posAttrib = VertexAttribute.Position();
        texCoordsAttrib = VertexAttribute.TexCoords(0);
    }
}