package finalforeach.cosmicreach.ui.debug;

import com.badlogic.gdx.math.Vector3;

public class DebugVec3Item extends DebugItem {
    final IDebugGetObject<Vector3> getVal;
    final IDebugToLine<Vector3> toDebug;
    float curX;
    float curY;
    float curZ;

    public DebugVec3Item(IDebugGetObject<Vector3> getVal, IDebugToLine<Vector3> toDebug) {
        this.getVal = getVal;
        this.toDebug = toDebug;
    }

    @Override
    public void update() {
        Vector3 vec3 = this.getVal.getValue();
        if (this.line == null || this.curX != vec3.x || this.curY != vec3.y || this.curZ != vec3.z) {
            this.curX = vec3.x;
            this.curZ = vec3.y;
            this.curY = vec3.z;
            this.line = this.toDebug.getLine(vec3);
            this.dirty = true;
        }
    }
}