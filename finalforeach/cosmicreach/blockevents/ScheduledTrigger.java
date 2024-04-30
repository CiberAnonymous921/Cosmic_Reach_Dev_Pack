package finalforeach.cosmicreach.blockevents;

import java.util.HashMap;

import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;

public record ScheduledTrigger(int triggerTick, String triggerId, BlockPosition triggerPos, Zone zone, BlockState targetBlockState) {
    public void run() {
        BlockState tbs = this.targetBlockState != null ? this.targetBlockState : this.triggerPos.getBlockState();
        if (tbs == null) {
            return;
        }
        BlockEventTrigger[] customTrigger = tbs.getTrigger(this.triggerId);
        if (customTrigger == null) {
            return;
        }
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("blockPos", this.triggerPos);
        for (BlockEventTrigger t : customTrigger) {
            t.act(tbs, this.zone, args);
        }
    }
}