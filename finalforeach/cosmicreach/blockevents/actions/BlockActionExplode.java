package finalforeach.cosmicreach.blockevents.actions;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Queue;

import finalforeach.cosmicreach.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.BlockSetter;
import finalforeach.cosmicreach.world.Zone;

@ActionId(id="base:explode")
public class BlockActionExplode implements IBlockAction {
    String blockStateId;
    int xOff;
    int yOff;
    int zOff;
    float radius;

    @Override
    public void act(BlockState srcBlockState, BlockEventTrigger blockEventTrigger, Zone zone, Map<String, Object> args) {
        this.act(srcBlockState, blockEventTrigger, zone, (BlockPosition)args.get("blockPos"));
    }

    public void act(BlockState srcBlockState, BlockEventTrigger blockEventTrigger, Zone zone, BlockPosition sourcePos) {
        float radiusSq = this.radius * this.radius;
        Queue<BlockPosition> explodeQueue = new Queue<BlockPosition>();
        for (float i = -this.radius; i <= this.radius; i += 1.0f) {
            for (float j = -this.radius; j <= this.radius; j += 1.0f) {
                for (float k = -this.radius; k <= this.radius; k += 1.0f) {
                    BlockPosition pos;
                    float workingRadiusSq = Vector3.len2(i, j, k);
                    if (!(workingRadiusSq <= radiusSq) || (pos = sourcePos.getOffsetBlockPos(zone, (int)((float)this.xOff + i), (int)((float)this.yOff + j), (int)((float)this.zOff + k))) == null) continue;
                    explodeQueue.addLast(pos);
                }
            }
        }
        BlockState blockState = "self".equals(this.blockStateId) ? srcBlockState : BlockState.getInstance(this.blockStateId);
        HashMap<String, Object> newArgs = new HashMap<String, Object>();
        for (BlockPosition explodedPos : explodeQueue) {
            BlockEventTrigger[] explode;
            BlockState explodedBlock;
            if (explodedPos.equals(sourcePos) || (explodedBlock = explodedPos.getBlockState()) == null || (explode = explodedBlock.getTrigger("onExplode")) == null) continue;
            for (BlockEventTrigger e : explode) {
                newArgs.put("blockPos", explodedPos);
                e.act(explodedBlock, zone, newArgs);
            }
        }
        BlockSetter.replaceBlocks(zone, blockState, explodeQueue, new Queue<BlockPosition>());
    }
}
