package finalforeach.cosmicreach.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL32;

public class TextureBuffer implements Disposable {
    boolean isBound;
    int handle = Gdx.gl.glGenBuffer();
    int textureHandle;

    TextureBuffer(Buffer buffer) {
        if (this.handle == 0) {
            throw new RuntimeException("Failed to generate TextureBuffer handle");
        }
        this.textureHandle = Gdx.gl.glGenTexture();
        if (this.textureHandle == 0) {
            throw new RuntimeException("Failed to generate TextureBuffer texture handle");
        }
        Gdx.gl.glBindBuffer(35882, this.handle);
        Gdx.gl.glBufferData(35882, buffer.limit(), buffer, 35044);
        Gdx.gl.glBindTexture(35882, this.textureHandle);
        GL32.glTexBuffer(35882, 33326, this.handle);
    }

    public static TextureBuffer fromFloats(float[] floats) {
        FloatBuffer f = BufferUtils.createFloatBuffer(floats.length);
        f.put(floats);
        f.flip();
        return new TextureBuffer(f);
    }

    public void bind(int texUnit) {
        this.isBound = true;
        Gdx.gl.glBindBuffer(35882, this.handle);
        Gdx.gl.glActiveTexture(33984 + texUnit);
        Gdx.gl.glBindTexture(35882, this.textureHandle);
    }

    public void unbind() {
        if (this.isBound) {
            Gdx.gl.glBindBuffer(35882, 0);
            Gdx.gl.glBindTexture(35882, 0);
            this.isBound = false;
        }
    }

    @Override
    public void dispose() {
        this.unbind();
        GL32.glDeleteTextures(this.textureHandle);
        Gdx.gl.glDeleteBuffer(this.handle);
    }
}