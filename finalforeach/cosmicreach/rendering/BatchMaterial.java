package finalforeach.cosmicreach.rendering;

import java.util.Objects;

import finalforeach.cosmicreach.rendering.shaders.GameShader;

public class BatchMaterial {
    BatchCoords batchCoords;
    GameShader shader;
    RenderOrder renderOrder;

    public BatchMaterial() {
    }

    public BatchMaterial(BatchCoords batchCoords, GameShader shader, RenderOrder renderOrder) {
        this.set(batchCoords, shader, renderOrder);
    }

    public BatchMaterial(BatchMaterial tmpMat) {
        this(tmpMat.batchCoords, tmpMat.shader, tmpMat.renderOrder);
    }

    public void set(BatchCoords batchCoords, GameShader shader, RenderOrder renderOrder) {
        this.batchCoords = batchCoords;
        this.shader = shader;
        this.renderOrder = renderOrder;
    }

    public int hashCode() {
        int result = 1;
        result = 31 * result + (this.batchCoords == null ? 0 : this.batchCoords.hashCode());
        result = 31 * result + (this.renderOrder == null ? 0 : this.renderOrder.hashCode());
        result = 31 * result + (this.shader == null ? 0 : this.shader.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        BatchMaterial other = (BatchMaterial)obj;
        return Objects.equals(this.batchCoords, other.batchCoords) && this.renderOrder == other.renderOrder && Objects.equals(this.shader, other.shader);
    }
}