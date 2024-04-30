package finalforeach.cosmicreach.blockevents;

import java.util.Map;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import finalforeach.cosmicreach.blockevents.actions.IBlockAction;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;

public class BlockEventTrigger implements Json.Serializable {
    private IBlockAction action;

    @Override
    public void write(Json json) {
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        String actionId = json.readValue(String.class, jsonData.get("actionId"));
        Class<? extends IBlockAction> actionClass = BlockEvents.ALL_ACTIONS.get(actionId);
        if (actionClass == null) {
            throw new RuntimeException("Could not find action for id: " + actionId);
        }
        this.action = json.fromJson(actionClass, jsonData.toString());
    }

    public IBlockAction getAction() {
        return this.action;
    }

    public void act(BlockState srcBlockState, Zone zone, Map<String, Object> args) {
        this.action.act(srcBlockState, this, zone, args);
    }
}