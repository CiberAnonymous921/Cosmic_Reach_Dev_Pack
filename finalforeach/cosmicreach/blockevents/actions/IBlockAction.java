package finalforeach.cosmicreach.blockevents.actions;

import java.lang.reflect.Field;
import java.util.Map;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import finalforeach.cosmicreach.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;

public interface IBlockAction extends Json.Serializable {
    public void act(BlockState var1, BlockEventTrigger var2, Zone var3, Map<String, Object> var4);

    default public String getActionId() {
        return this.getClass().getDeclaredAnnotation(ActionId.class).id();
    }

    @Override
    default public void write(Json json) {
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    default public void read(Json json, JsonValue jsonData) {
        try {
            JsonValue.PrettyPrintSettings pps = new JsonValue.PrettyPrintSettings();
            pps.outputType = JsonWriter.OutputType.json;
            JsonValue params = jsonData.getChild("parameters");
            while (params != null) {
                Field field = null;
                for (Class<?> c = this.getClass(); c != Object.class; c = c.getSuperclass()) {
                    try {
                        field = c.getDeclaredField(params.name);
                        break;
                    } catch (Exception ex) {
                        continue;
                    }
                }
                if (field == null) {
                    throw new RuntimeException("Field '" + params.name + "' is not valid for action " + this.getActionId());
                }
                field.set(this, json.fromJson(field.getType(), params.prettyPrint(pps)));
                params = params.next;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
