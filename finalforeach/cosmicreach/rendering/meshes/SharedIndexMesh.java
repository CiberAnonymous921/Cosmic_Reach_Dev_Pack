package finalforeach.cosmicreach.rendering.meshes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexData;
import com.badlogic.gdx.utils.Disposable;

public class SharedIndexMesh extends GameMesh implements Disposable {
    public boolean autoBind = true;

    public SharedIndexMesh(boolean isStatic, int maxVertices, VertexAttributes attributes) {
        super(isStatic, maxVertices, attributes);
    }

    @Override
    public void render(ShaderProgram shader, int primitiveType) {
        this.render(shader, primitiveType, 0, this.getNumVertices() * this.getVertexSizeInFloats(), this.autoBind);
    }

    public void render(ShaderProgram shader, int primitiveType, int offset, int count, boolean autoBind) {
        if (count == 0) {
            return;
        }
        if (autoBind) {
            this.bind(shader);
        }
        Gdx.gl20.glDrawElements(primitiveType, count, 5125, offset * 4);
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
    }

    @Override
    public void unbind(ShaderProgram shader) {
        this.unbind(shader, null);
    }

    public void unbind(ShaderProgram shader, int[] locations) {
        this.vertices.unbind(shader, locations);
    }

    @Override
    public void dispose() {
        this.vertices.dispose();
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