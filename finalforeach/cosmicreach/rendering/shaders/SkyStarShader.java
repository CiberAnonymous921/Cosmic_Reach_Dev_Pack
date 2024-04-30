package finalforeach.cosmicreach.rendering.shaders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;

public class SkyStarShader extends GameShader {
    public static SkyStarShader SKY_STAR_SHADER;
    private static VertexAttribute posAttrib;
    public static VertexAttribute[] allVertexAttributes;
    PerspectiveCamera skyCam = new PerspectiveCamera();

    public SkyStarShader(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    @Override
    public void bind(Camera worldCamera) {
        super.bind(worldCamera);
        this.skyCam.up.set(worldCamera.up);
        this.skyCam.direction.set(worldCamera.direction);
        this.skyCam.fieldOfView = ((PerspectiveCamera)worldCamera).fieldOfView;
        this.skyCam.position.set(0.0f, 0.0f, 0.0f);
        this.skyCam.near = worldCamera.near;
        this.skyCam.viewportWidth = worldCamera.viewportWidth;
        this.skyCam.viewportHeight = worldCamera.viewportHeight;
        this.skyCam.update();
        this.shader.setUniformMatrix("u_projViewTrans", this.skyCam.combined);
    }

    public static void initSkyStarShader() {
        SKY_STAR_SHADER = new SkyStarShader("sky-star.vert.glsl", "sky-star.frag.glsl");
    }

    static {
        posAttrib = VertexAttribute.Position();
        allVertexAttributes = new VertexAttribute[]{posAttrib};
    }
}