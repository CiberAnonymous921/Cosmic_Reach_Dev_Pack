package finalforeach.cosmicreach.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL32;

public class UniformBufferObject implements Disposable {
    boolean isBound;
    int handle = Gdx.gl.glGenBuffer();

    UniformBufferObject(Buffer buffer) {
        if (this.handle == 0) {
            throw new RuntimeException("Failed to generate UniformBufferObject handle");
        }
        Gdx.gl.glBindBuffer(35345, this.handle);
        Gdx.gl.glBufferData(35345, buffer.limit(), buffer, 35044);
    }

    public static UniformBufferObject fromFloats(float[] floats) {
        FloatBuffer f = BufferUtils.createFloatBuffer(floats.length);
        f.put(floats);
        f.flip();
        return new UniformBufferObject(f);
    }

    public void bind() {
        this.isBound = true;
        GL32.glBindBufferBase(35345, 0, this.handle);
    }

    public void unbind() {
        if (this.isBound) {
            Gdx.gl.glBindBuffer(35345, 0);
            this.isBound = false;
        }
    }

    @Override
    public void dispose() {
        this.unbind();
        Gdx.gl.glDeleteBuffer(this.handle);
    }
}