package finalforeach.cosmicreach.blockevents.actions;

import java.util.Map;

import com.badlogic.gdx.utils.Queue;

import finalforeach.cosmicreach.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;

@ActionId(id="base:set_cuboid")
public class BlockActionSetCuboid extends BlockActionSetArea {
    int x1Off;
    int y1Off;
    int z1Off;
    int x2Off;
    int y2Off;
    int z2Off;

    @Override
    public void act(BlockState srcBlockState, BlockEventTrigger blockEventTrigger, Zone zone, Map<String, Object> args) {
        this.act(srcBlockState, blockEventTrigger, zone, (BlockPosition)args.get("blockPos"));
    }

    public void act(BlockState srcBlockState, BlockEventTrigger blockEventTrigger, Zone zone, BlockPosition sourcePos) {
        Queue<BlockPosition> setQueue = new Queue<BlockPosition>();
        int startX = Math.min(this.x1Off, this.x2Off);
        int endX = Math.max(this.x1Off, this.x2Off);
        int startY = Math.min(this.y1Off, this.y2Off);
        int endY = Math.max(this.y1Off, this.y2Off);
        int startZ = Math.min(this.z1Off, this.z2Off);
        int endZ = Math.max(this.z1Off, this.z2Off);
        for (int i = startX; i <= endX; ++i) {
            for (int j = startY; j <= endY; ++j) {
                for (int k = startZ; k <= endZ; ++k) {
                    BlockPosition pos = sourcePos.getOffsetBlockPos(zone, i, j, k);
                    if (pos == null) continue;
                    setQueue.addLast(pos);
                }
            }
        }
        this.setForQueue(srcBlockState, setQueue, zone);
    }
}