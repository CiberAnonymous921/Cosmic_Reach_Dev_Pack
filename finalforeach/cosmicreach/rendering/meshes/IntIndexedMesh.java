package finalforeach.cosmicreach.rendering.meshes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexData;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class IntIndexedMesh extends GameMesh implements Disposable {
    final IntIndexData indexData;
    public boolean autoBind = true;

    public IntIndexedMesh(boolean isStatic, int maxVertices, int maxIndices, VertexAttribute ... attributes) {
        this(isStatic, maxVertices, maxIndices, new VertexAttributes(attributes));
    }

    public IntIndexedMesh(boolean isStatic, int maxVertices, int maxIndices, VertexAttributes attributes) {
        super(isStatic, maxVertices, attributes);
        this.indexData = new IntIndexData(isStatic, maxIndices);
    }

    @Override
    public void render(ShaderProgram shader, int primitiveType) {
        this.render(shader, primitiveType, 0, this.getNumMaxIndices() > 0 ? this.getNumIndices() : this.getNumVertices(), this.autoBind);
    }

    public void render(ShaderProgram shader, int primitiveType, int offset, int count, boolean autoBind) {
        if (count == 0) {
            return;
        }
        if (autoBind) {
            this.bind(shader);
        }
        if (this.getNumIndices() > 0) {
            if (count + offset > this.getNumMaxIndices()) {
                throw new GdxRuntimeException("Mesh attempting to access memory outside of the index buffer (count: " + count + ", offset: " + offset + ", max: " + this.getNumMaxIndices() + ")");
            }
            Gdx.gl20.glDrawElements(primitiveType, count, 5125, offset * 4);
        } else {
            Gdx.gl20.glDrawArrays(primitiveType, offset, count);
        }
        if (autoBind) {
            this.unbind(shader);
        }
    }

    @Override
    public void bind(ShaderProgram shader) {
        this.bind(shader, null);
    }

    public void bind(ShaderProgram shader, int[] locations) {
        this.vertices.bind(shader, locations);
        if (this.getNumIndices() > 0) {
            this.indexData.bind();
        }
    }

    @Override
    public void unbind(ShaderProgram shader) {
        this.unbind(shader, null);
    }

    public void unbind(ShaderProgram shader, int[] locations) {
        this.vertices.unbind(shader, locations);
        if (this.getNumIndices() > 0) {
            this.indexData.unbind();
        }
    }

    public int getNumIndices() {
        return this.indexData.getNumIndices();
    }

    public int getNumMaxIndices() {
        return this.indexData.getNumMaxIndices();
    }

    @Override
    public void dispose() {
        this.vertices.dispose();
        this.indexData.dispose();
    }

    public void setIndices(int[] indices) {
        this.indexData.setIndices(indices);
    }

    @Override
    public VertexData getVertices() {
        return this.vertices;
    }

    @Override
    public void setAutoBind(boolean autoBind) {
        this.autoBind = autoBind;
    }
}
