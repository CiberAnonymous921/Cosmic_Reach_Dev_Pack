package finalforeach.cosmicreach.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.rendering.shaders.SkyStarShader;

public class Sky {
    public static Color skyColor = new Color(0.0f, 0.0f, 0.0f, 1.0f);
    public static Color ambientColor = new Color(0.75f, 0.75f, 0.75f, 1.0f);
    public static boolean shouldDrawStars = true;
    public static Mesh starMesh;
    private static GameShader starShader;

    public static void drawStars(Camera worldCamera) {
        if (!shouldDrawStars) {
            return;
        }
        Gdx.gl.glDepthMask(false);
        if (starMesh == null) {
            starShader = SkyStarShader.SKY_STAR_SHADER;
            VertexAttribute[] attribs = new VertexAttribute[]{VertexAttribute.Position()};
            FloatArray verts = new FloatArray();
            int numStars = 1000;
            Vector3 pointOff = new Vector3();
            Vector3 pointA = new Vector3();
            Vector3 pointB = new Vector3();
            Vector3 pointC = new Vector3();
            Vector3 pointD = new Vector3();
            for (int i = 0; i < numStars; ++i) {
                float s = MathUtils.random(0.01f, 0.05f) / 2.0f;
                float ax = MathUtils.random(360.0f);
                float ay = MathUtils.random(360.0f);
                float az = MathUtils.random(360.0f);
                pointA.set(0.0f, 0.0f, 0.0f);
                pointA.rotate(ax, 1.0f, 0.0f, 0.0f);
                pointA.rotate(ay, 0.0f, 1.0f, 0.0f);
                pointA.rotate(az, 0.0f, 0.0f, 1.0f);
                pointB.set(s, 0.0f, 0.0f);
                pointB.rotate(ax, 1.0f, 0.0f, 0.0f);
                pointB.rotate(ay, 0.0f, 1.0f, 0.0f);
                pointB.rotate(az, 0.0f, 0.0f, 1.0f);
                pointC.set(0.0f, s, 0.0f);
                pointC.rotate(ax, 1.0f, 0.0f, 0.0f);
                pointC.rotate(ay, 0.0f, 1.0f, 0.0f);
                pointC.rotate(az, 0.0f, 0.0f, 1.0f);
                pointD.set(s, s, 0.0f);
                pointD.rotate(ax, 1.0f, 0.0f, 0.0f);
                pointD.rotate(ay, 0.0f, 1.0f, 0.0f);
                pointD.rotate(az, 0.0f, 0.0f, 1.0f);
                pointOff.set(0.0f, 0.0f, 5.0f);
                pointOff.rotate(ax, 1.0f, 0.0f, 0.0f);
                pointOff.rotate(ay, 0.0f, 1.0f, 0.0f);
                pointOff.rotate(az, 0.0f, 0.0f, 1.0f);
                pointA.add(pointOff);
                pointB.add(pointOff);
                pointC.add(pointOff);
                pointD.add(pointOff);
                verts.add(pointC.x, pointC.y, pointC.z);
                verts.add(pointB.x, pointB.y, pointB.z);
                verts.add(pointA.x, pointA.y, pointA.z);
                verts.add(pointD.x, pointD.y, pointD.z);
                verts.add(pointB.x, pointB.y, pointB.z);
                verts.add(pointC.x, pointC.y, pointC.z);
            }
            int maxVert = verts.size / attribs.length;
            starMesh = new Mesh(true, maxVert, 0, attribs);
            starMesh.setVertices(verts.toArray());
        }
        starShader.bind(worldCamera);
        starMesh.bind(Sky.starShader.shader);
        starMesh.render(Sky.starShader.shader, 4);
        starShader.unbind();
        Gdx.gl.glDepthMask(true);
    }
}