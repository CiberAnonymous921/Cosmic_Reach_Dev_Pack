package finalforeach.cosmicreach.rendering.meshes;

import java.lang.reflect.Field;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexData;

public class ShortIndexedMesh implements IGameMesh {
    private final Mesh mesh;
    private VertexData vertices;
    private static Field verticesField;

    public ShortIndexedMesh(boolean isStatic, int maxVertices, int maxIndices, VertexAttributes attributes) {
        this.mesh = new Mesh(isStatic, maxVertices, maxIndices, attributes);
    }

    @Override
    public void dispose() {
        if (this.mesh != null) {
            this.mesh.dispose();
        }
    }

    @Override
    public void setVertices(float[] array) {
        this.mesh.setVertices(array);
    }

    public void setIndices(int[] intArray) {
        short[] shortArray = new short[intArray.length];
        for (int i = 0; i < intArray.length; ++i) {
            shortArray[i] = (short)intArray[i];
        }
        this.setIndices(shortArray);
    }

    public void setIndices(short[] shortArray) {
        this.mesh.setIndices(shortArray);
    }

    @Override
    public void bind(ShaderProgram shader) {
        this.mesh.bind(shader);
    }

    @Override
    public int getNumVertices() {
        return this.mesh.getNumVertices();
    }

    @Override
    public void unbind(ShaderProgram shader) {
        this.mesh.unbind(shader);
    }

    @Override
    public void render(ShaderProgram shader, int glType) {
        this.mesh.render(shader, glType);
    }

    @Override
    public VertexData getVertices() {
        if (this.vertices == null) {
            try {
                if (verticesField == null) {
                    verticesField = Mesh.class.getDeclaredField("vertices");
                    verticesField.setAccessible(true);
                }
                this.vertices = (VertexData)verticesField.get(this.mesh);
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
                e.printStackTrace();
            }
        }
        return this.vertices;
    }

    @Override
    public void setAutoBind(boolean autoBind) {
        this.mesh.setAutoBind(autoBind);
    }

    @Override
    public int getNumMaxVertices() {
        return this.getVertices().getNumMaxVertices();
    }

    @Override
    public int getVertexSizeInFloats() {
        return this.mesh.getVertexSize() / 4;
    }
}