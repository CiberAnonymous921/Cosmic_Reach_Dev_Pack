package finalforeach.cosmicreach.rendering.meshes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class IntIndexData implements Disposable {
    protected static final int BYTES_PER_INDEX = 4;
    final ByteBuffer indexByteBuffer;
    public final IntBuffer indexBuffer;
    final int usage;
    int indexBufferHandle;
    boolean isDirty = true;
    boolean isBound = false;

    public IntIndexData(boolean isStatic, int maxIndices) {
        this.usage = isStatic ? 35044 : 35048;
        this.indexByteBuffer = BufferUtils.newUnsafeByteBuffer(maxIndices * 4);
        this.indexBuffer = this.indexByteBuffer.asIntBuffer();
        this.indexBuffer.flip();
        this.indexByteBuffer.flip();
        this.indexBufferHandle = Gdx.gl.glGenBuffer();
    }

    public void bind() {
        if (this.indexBufferHandle == 0) {
            throw new GdxRuntimeException("No buffer allocated!");
        }
        Gdx.gl20.glBindBuffer(34963, this.indexBufferHandle);
        if (this.isDirty) {
            this.indexByteBuffer.limit(this.indexBuffer.limit() * 4);
            Gdx.gl20.glBufferData(34963, this.indexByteBuffer.limit(), this.indexByteBuffer, this.usage);
            this.isDirty = false;
        }
        this.isBound = true;
    }

    public int getNumIndices() {
        return this.indexBuffer.limit();
    }

    public int getNumMaxIndices() {
        return this.indexBuffer.capacity();
    }

    @Override
    public void dispose() {
        this.unbind();
        Gdx.gl20.glDeleteBuffer(this.indexBufferHandle);
        this.indexBufferHandle = 0;
        BufferUtils.disposeUnsafeByteBuffer(this.indexByteBuffer);
    }

    public void setIndices(int[] indices) {
        int offset = 0;
        int count = indices.length;
        this.isDirty = true;
        this.indexBuffer.clear();
        this.indexBuffer.put(indices, offset, count);
        this.indexBuffer.flip();
        this.indexByteBuffer.position(0);
        this.indexByteBuffer.limit(count * 4);
        if (this.isBound) {
            Gdx.gl20.glBufferData(34963, this.indexByteBuffer.limit(), this.indexByteBuffer, this.usage);
            this.isDirty = false;
        }
    }

    public void unbind() {
        if (this.isBound) {
            Gdx.gl20.glBindBuffer(34963, 0);
            this.isBound = false;
        }
    }

    public static IntIndexData createQuadIndexData(int numIndices) {
        numIndices = (int)Math.ceil((float)numIndices / 6.0f) * 6;
        int[] indices = IntIndexData.createQuadIndices(numIndices);
        IntIndexData indexData = new IntIndexData(true, numIndices);
        indexData.setIndices(indices);
        return indexData;
    }

    public static int[] createQuadIndices(int numIndices) {
        numIndices = (int)Math.ceil((float)numIndices / 6.0f) * 6;
        int[] indices = new int[numIndices];
        int f = 0;
        for (int i = 0; i < numIndices; i += 6) {
            indices[i] = f + 0;
            indices[i + 1] = f + 1;
            indices[i + 2] = f + 2;
            indices[i + 3] = f + 2;
            indices[i + 4] = f + 3;
            indices[i + 5] = f + 0;
            f += 4;
        }
        return indices;
    }
}