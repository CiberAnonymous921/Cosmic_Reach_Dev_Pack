package finalforeach.cosmicreach.rendering.meshes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectWithVAO;
import com.badlogic.gdx.graphics.glutils.VertexData;

public abstract class GameMesh implements IGameMesh {
    protected static final int BYTES_PER_INDEX = 4;
    public final VertexData vertices;
    final int usage;

    public GameMesh(boolean isStatic, int maxVertices, VertexAttributes attributes) {
        this.vertices = this.makeVertexBuffer(isStatic, maxVertices, attributes);
        this.usage = isStatic ? 35044 : 35048;
    }

    protected VertexData makeVertexBuffer(boolean isStatic, int maxVertices, VertexAttributes vertexAttributes) {
        if (Gdx.gl30 != null) {
            return new VertexBufferObjectWithVAO(isStatic, maxVertices, vertexAttributes);
        }
        return new VertexBufferObject(isStatic, maxVertices, vertexAttributes);
    }

    @Override
    public int getNumVertices() {
        return this.vertices.getNumVertices();
    }

    @Override
    public int getNumMaxVertices() {
        return this.vertices.getNumMaxVertices();
    }

    @Override
    public void setVertices(float[] vertices) {
        this.vertices.setVertices(vertices, 0, vertices.length);
    }

    public VertexAttribute getVertexAttribute(int usage) {
        VertexAttributes attributes = this.vertices.getAttributes();
        int len = attributes.size();
        for (int i = 0; i < len; ++i) {
            if (attributes.get((int)i).usage != usage) continue;
            return attributes.get(i);
        }
        return null;
    }

    @Override
    public int getVertexSizeInFloats() {
        return this.vertices.getAttributes().vertexSize / 4;
    }

    @Override
    public abstract void render(ShaderProgram var1, int var2);

    @Override
    public abstract void bind(ShaderProgram var1);

    @Override
    public abstract void unbind(ShaderProgram var1);
}