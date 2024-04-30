package finalforeach.cosmicreach.blockevents.actions;

import java.util.Map;

import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;

@ActionId(id="base:play_sound_2d")
public class BlockActionPlaySound2D implements IBlockAction {
    String sound;
    float volume;
    float pitch;
    float pan;

    @Override
    public void act(BlockState srcBlockState, BlockEventTrigger blockEventTrigger, Zone zone, Map<String, Object> args) {
        this.act(srcBlockState, blockEventTrigger, zone);
    }

    public void act(BlockState srcBlockState, BlockEventTrigger blockEventTrigger, Zone zone) {
        GameSingletons.soundManager.playSound(GameAssetLoader.getSound("sounds/blocks/" + this.sound), this.volume, this.pitch, this.pan);
    }
}