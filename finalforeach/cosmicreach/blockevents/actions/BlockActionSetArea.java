package finalforeach.cosmicreach.blockevents.actions;

import java.util.HashMap;

import com.badlogic.gdx.utils.Queue;

import finalforeach.cosmicreach.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.BlockSetter;
import finalforeach.cosmicreach.world.Zone;

public abstract class BlockActionSetArea implements IBlockAction {
    String blockStateId;
    String triggerBeforeSetId;
    String triggerAfterSetId;

    protected void setForQueue(BlockState srcBlockState, Queue<BlockPosition> setQueue, Zone zone) {
        if (this.triggerBeforeSetId != null) {
            this.fireTriggers(this.triggerBeforeSetId, setQueue, zone);
        }
        if (this.blockStateId != null) {
            BlockState blockState = "self".equals(this.blockStateId) ? srcBlockState : BlockState.getInstance(this.blockStateId);
            BlockSetter.replaceBlocks(zone, blockState, setQueue, new Queue<BlockPosition>());
        }
        if (this.triggerAfterSetId != null) {
            this.fireTriggers(this.triggerAfterSetId, setQueue, zone);
        }
    }

    protected void fireTriggers(String trigger, Queue<BlockPosition> queue, Zone zone) {
        HashMap<String, Object> newArgs = new HashMap<String, Object>();
        for (BlockPosition pos : queue) {
            BlockEventTrigger[] t;
            BlockState setBlock = pos.getBlockState();
            if (setBlock == null || (t = setBlock.getTrigger(trigger)) == null) continue;
            for (BlockEventTrigger e : t) {
                newArgs.put("blockPos", pos);
                e.act(setBlock, zone, newArgs);
            }
        }
    }
}