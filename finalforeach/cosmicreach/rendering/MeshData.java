package finalforeach.cosmicreach.rendering;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;

import finalforeach.cosmicreach.rendering.meshes.IGameMesh;
import finalforeach.cosmicreach.rendering.meshes.IntIndexedMesh;
import finalforeach.cosmicreach.rendering.meshes.SharedIndexMesh;
import finalforeach.cosmicreach.rendering.meshes.ShortIndexedMesh;
import finalforeach.cosmicreach.rendering.shaders.GameShader;

public class MeshData implements IMeshData {
    FloatArray vertices;
    IntArray indices;
    GameShader shader;
    RenderOrder renderOrder;
    IGameMesh mesh;
    boolean meshDirty = true;

    public MeshData(FloatArray vertices, IntArray indices, GameShader shader, RenderOrder renderOrder) {
        this.vertices = vertices;
        this.indices = indices;
        this.shader = shader;
        this.renderOrder = renderOrder;
    }

    public MeshData(GameShader shader, RenderOrder order) {
        this(new FloatArray(), new IntArray(), shader, order);
    }

    public void clear() {
        this.vertices.clear();
        this.indices.clear();
        this.meshDirty = true;
    }

    @Override
    public void ensureVerticesCapacity(int verticesSize) {
        this.vertices.ensureCapacity(verticesSize);
    }

    public IntIndexedMesh toIntIndexedMesh(boolean isStatic) {
        if (this.vertices.size > 0) {
            VertexAttributes a = new VertexAttributes(this.shader.allVertexAttributes);
            IntIndexedMesh m = new IntIndexedMesh(isStatic, this.vertices.size / (a.vertexSize / 4), this.indices.size, a);
            m.setVertices(this.vertices.toArray());
            if (this.indices.size > 0) {
                m.setIndices(this.indices.toArray());
            }
            return m;
        }
        return null;
    }

    public ShortIndexedMesh toShortIndexedMesh(boolean isStatic) {
        if (this.vertices.size > 0) {
            VertexAttributes a = new VertexAttributes(this.shader.allVertexAttributes);
            ShortIndexedMesh m = new ShortIndexedMesh(isStatic, this.vertices.size / (a.vertexSize / 4), this.indices.size, a);
            m.setVertices(this.vertices.toArray());
            if (this.indices.size > 0) {
                m.setIndices(this.indices.toArray());
            }
            return m;
        }
        return null;
    }

    public SharedIndexMesh toSharedIndexMesh(boolean isStatic) {
        if (this.vertices.size > 0) {
            VertexAttributes a = new VertexAttributes(this.shader.allVertexAttributes);
            SharedIndexMesh m = new SharedIndexMesh(isStatic, this.vertices.size / (a.vertexSize / 4), a);
            m.setVertices(this.vertices.toArray());
            return m;
        }
        return null;
    }

    public void shrink() {
        this.vertices.shrink();
        this.indices.shrink();
    }

    @Override
    public FloatArray getVertices() {
        return this.vertices;
    }

    @Override
    public IntArray getIndices() {
        return this.indices;
    }

    public RenderOrder getRenderOrder() {
        return this.renderOrder;
    }

    public GameShader getShader() {
        return this.shader;
    }
}