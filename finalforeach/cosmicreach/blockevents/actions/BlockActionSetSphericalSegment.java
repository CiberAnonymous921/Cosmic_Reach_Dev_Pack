package finalforeach.cosmicreach.blockevents.actions;

import java.util.Map;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Queue;

import finalforeach.cosmicreach.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;

@ActionId(id="base:set_spherical_segment")
public class BlockActionSetSphericalSegment extends BlockActionSetArea {
    int xOff;
    int yOff;
    int zOff;
    float radius;
    float angleDeg;
    float xNormal;
    float yNormal;
    float zNormal;

    @Override
    public void act(BlockState srcBlockState, BlockEventTrigger blockEventTrigger, Zone zone, Map<String, Object> args) {
        this.act(srcBlockState, blockEventTrigger, zone, (BlockPosition)args.get("blockPos"));
    }

    public void act(BlockState srcBlockState, BlockEventTrigger blockEventTrigger, Zone zone, BlockPosition sourcePos) {
        float radiusSq = this.radius * this.radius;
        Queue<BlockPosition> setQueue = new Queue<BlockPosition>();
        float ca = MathUtils.cosDeg(this.angleDeg);
        for (float i = -this.radius; i <= this.radius; i += 1.0f) {
            for (float j = -this.radius; j <= this.radius; j += 1.0f) {
                for (float k = -this.radius; k <= this.radius; k += 1.0f) {
                    BlockPosition pos;
                    float workingRadiusSq = Vector3.len2(i, j, k);
                    if (!(workingRadiusSq <= radiusSq)) continue;
                    int x = (int)((float)this.xOff + i);
                    int y = (int)((float)this.yOff + j);
                    int z = (int)((float)this.zOff + k);
                    float workingRadius = (float)Math.sqrt(workingRadiusSq);
                    float dot = Vector3.dot(x - this.xOff, y - this.yOff, z - this.zOff, this.xNormal, this.yNormal, this.zNormal);
                    float cos = dot / workingRadius;
                    if (cos < ca || (pos = sourcePos.getOffsetBlockPos(zone, x, y, z)) == null) continue;
                    setQueue.addLast(pos);
                }
            }
        }
        this.setForQueue(srcBlockState, setQueue, zone);
    }
}