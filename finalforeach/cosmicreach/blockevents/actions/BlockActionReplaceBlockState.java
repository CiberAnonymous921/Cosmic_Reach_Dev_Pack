package finalforeach.cosmicreach.blockevents.actions;

import java.util.Map;

import com.badlogic.gdx.utils.Queue;

import finalforeach.cosmicreach.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.BlockSetter;
import finalforeach.cosmicreach.world.Zone;

@ActionId(id="base:replace_block_state")
public class BlockActionReplaceBlockState implements IBlockAction {
    String blockStateId;
    int xOff;
    int yOff;
    int zOff;

    @Override
    public void act(BlockState srcBlockState, BlockEventTrigger blockEventTrigger, Zone zone, Map<String, Object> args) {
        this.act(srcBlockState, blockEventTrigger, zone, (BlockPosition)args.get("blockPos"));
    }

    public void act(BlockState srcBlockState, BlockEventTrigger blockEventTrigger, Zone zone, BlockPosition sourcePos) {
        BlockState blockState = "self".equals(this.blockStateId) ? srcBlockState : BlockState.getInstance(this.blockStateId);
        BlockPosition bp = sourcePos.getOffsetBlockPos(zone, this.xOff, this.yOff, this.zOff);
        if (bp != null) {
            BlockSetter.replaceBlock(zone, blockState, bp, new Queue<BlockPosition>());
        }
    }
}