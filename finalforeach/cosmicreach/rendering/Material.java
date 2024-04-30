package finalforeach.cosmicreach.rendering;

import java.util.Objects;

import finalforeach.cosmicreach.rendering.shaders.GameShader;

public class Material {
    GameShader shader;
    RenderOrder renderOrder;

    public int hashCode() {
        int result = 1;
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
        return this.renderOrder == other.renderOrder && Objects.equals(this.shader, other.shader);
    }
}