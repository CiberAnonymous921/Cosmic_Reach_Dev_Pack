package finalforeach.cosmicreach.blockevents.actions;

import java.util.HashMap;
import java.util.Map;

import finalforeach.cosmicreach.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.blockevents.ScheduledTrigger;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;

@ActionId(id="base:run_trigger")
public class BlockActionRunTrigger implements IBlockAction {
    String triggerId;
    int xOff;
    int yOff;
    int zOff;
    int tickDelay = 0;

    @Override
    public void act(BlockState srcBlockState, BlockEventTrigger blockEventTrigger, Zone zone, Map<String, Object> args) {
        this.act(srcBlockState, blockEventTrigger, zone, (BlockPosition)args.get("blockPos"));
    }

    public void act(BlockState srcBlockState, BlockEventTrigger blockEventTrigger, Zone zone, BlockPosition sourcePos) {
        BlockPosition triggerPos = sourcePos.getOffsetBlockPos(zone, this.xOff, this.yOff, this.zOff);
        this.scheduleTrigger(this.tickDelay, this.triggerId, triggerPos, zone);
    }

    private void scheduleTrigger(int tickDelay, String triggerId, BlockPosition triggerPos, Zone zone) {
        if (triggerPos == null) {
            return;
        }
        if (tickDelay <= 0) {
            BlockState targetBlockState = triggerPos.getBlockState();
            if (targetBlockState == null) {
                return;
            }
            BlockEventTrigger[] customTrigger = targetBlockState.getTrigger(triggerId);
            if (customTrigger == null) {
                return;
            }
            HashMap<String, Object> args = new HashMap<String, Object>();
            args.put("blockPos", triggerPos);
            for (BlockEventTrigger t : customTrigger) {
                t.act(targetBlockState, zone, args);
            }
        } else {
            int tick = zone.currentTick + tickDelay;
            ScheduledTrigger st = new ScheduledTrigger(tick, triggerId, triggerPos, zone, triggerPos.getBlockState());
            zone.eventQueue.add(st);
        }
    }
}