package finalforeach.cosmicreach.rendering.meshes;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexData;
import com.badlogic.gdx.utils.Disposable;

public interface IGameMesh extends Disposable {
    public void bind(ShaderProgram var1);

    public int getNumVertices();

    public void unbind(ShaderProgram var1);

    public void render(ShaderProgram var1, int var2);

    public VertexData getVertices();

    public void setAutoBind(boolean var1);

    public int getNumMaxVertices();

    public int getVertexSizeInFloats();

    public void setVertices(float[] var1);
}