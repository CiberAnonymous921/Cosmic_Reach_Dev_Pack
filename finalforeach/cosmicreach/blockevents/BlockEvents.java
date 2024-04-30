package finalforeach.cosmicreach.blockevents;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.blockevents.actions.ActionId;
import finalforeach.cosmicreach.blockevents.actions.BlockActionExplode;
import finalforeach.cosmicreach.blockevents.actions.BlockActionPlaySound2D;
import finalforeach.cosmicreach.blockevents.actions.BlockActionReplaceBlockState;
import finalforeach.cosmicreach.blockevents.actions.BlockActionRunTrigger;
import finalforeach.cosmicreach.blockevents.actions.BlockActionSetCuboid;
import finalforeach.cosmicreach.blockevents.actions.BlockActionSetSphere;
import finalforeach.cosmicreach.blockevents.actions.BlockActionSetSphericalSegment;
import finalforeach.cosmicreach.blockevents.actions.BlockEventActionSetBlockStateParams;
import finalforeach.cosmicreach.blockevents.actions.IBlockAction;
import finalforeach.cosmicreach.io.SaveLocation;

public class BlockEvents implements Json.Serializable {
    public static final HashMap<String, Class<? extends IBlockAction>> ALL_ACTIONS = new HashMap<String, Class<? extends IBlockAction>>();
    public static final HashMap<String, BlockEvents> INSTANCES = new HashMap<String, BlockEvents>();
    String parent;
    String stringId;
    private OrderedMap<String, BlockEventTrigger[]> triggers = new OrderedMap<String, BlockEventTrigger[]>();
    private transient boolean initTriggers;

    public static void initBlockEvents() {
        BlockEvents.registerBlockEventAction(BlockActionReplaceBlockState.class);
        BlockEvents.registerBlockEventAction(BlockActionPlaySound2D.class);
        BlockEvents.registerBlockEventAction(BlockActionRunTrigger.class);
        BlockEvents.registerBlockEventAction(BlockEventActionSetBlockStateParams.class);
        BlockEvents.registerBlockEventAction(BlockActionExplode.class);
        BlockEvents.registerBlockEventAction(BlockActionSetCuboid.class);
        BlockEvents.registerBlockEventAction(BlockActionSetSphere.class);
        BlockEvents.registerBlockEventAction(BlockActionSetSphericalSegment.class);
        for (String f : Gdx.files.internal("assets.txt").readString().split("\n")) {
            try {
                String folderName = "block_events";
                if (f.startsWith(folderName + "/examples") || !f.startsWith(folderName + "/") || !f.endsWith(".json") || !Gdx.files.internal(f).exists()) continue;
                String fileName = f.replace(folderName + "/", "");
                BlockEvents.getInstance(fileName.replace(".json", ""));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        for (FileHandle f : Gdx.files.absolute(SaveLocation.getSaveFolderLocation() + "/mods/assets/block_events").list()) {
            if (!f.name().endsWith(".json")) continue;
            BlockEvents.getInstance(f.nameWithoutExtension());
        }
    }

    public static BlockEvents getInstance(String blockEventId) {
        return BlockEvents.getInstance(blockEventId, blockEventId);
    }

    public static BlockEvents getInstance(String blockEventId, String fileName) {
        if (blockEventId == null && !"block_events_default".equals(blockEventId)) {
            return BlockEvents.getInstance("block_events_default");
        }
        if (INSTANCES.containsKey(blockEventId)) {
            return INSTANCES.get(blockEventId);
        }
        String jsonStr = GameAssetLoader.loadAsset("block_events/" + fileName + ".json").readString();
        Json json = new Json();
        BlockEvents blockEvents = json.fromJson(BlockEvents.class, jsonStr);
        INSTANCES.put(blockEvents.stringId, blockEvents);
        return blockEvents;
    }

    public BlockEvents getParent() {
        if (this.parent == null) {
            return null;
        }
        return BlockEvents.getInstance(this.parent);
    }

    @SuppressWarnings("rawtypes")
    public OrderedMap<String, BlockEventTrigger[]> getTriggerMap() {
        if (!this.initTriggers) {
            OrderedMap<String, BlockEventTrigger[]> parentTriggers;
            BlockEvents parentEvent = this.getParent();
            if (parentEvent != null && (parentTriggers = parentEvent.getTriggerMap()) != null) {
                for (ObjectMap.Entry entry : parentTriggers.entries()) {
                    if (this.triggers.containsKey((String)entry.key)) continue;
                    this.triggers.put((String)entry.key, (BlockEventTrigger[])entry.value);
                }
            }
            this.initTriggers = true;
        }
        return this.triggers;
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        this.parent = json.readValue(String.class, jsonData.get("parent"));
        this.stringId = json.readValue(String.class, jsonData.get("stringId"));
        json.readField(this, "triggers", jsonData);
    }

    @Override
    public void write(Json json) {
        json.writeValue("stringId", this.stringId);
        json.writeValue("triggers", this.triggers);
    }

    public static void registerBlockEventAction(Class<? extends IBlockAction> actionClass) {
        ActionId actionIdAnnotation = actionClass.getAnnotation(ActionId.class);
        if (actionIdAnnotation == null) {
            throw new RuntimeException("Class " + actionClass.getSimpleName() + " must have an @" + ActionId.class.getSimpleName() + " annotation");
        }
        String actionId = actionIdAnnotation.id();
        if (actionId == null) {
            throw new RuntimeException("Class " + actionClass.getSimpleName() + " cannot have a null action Id.");
        }
        if (ALL_ACTIONS.get(actionId) != null) {
            throw new RuntimeException("Duplicate block event action key for " + actionId);
        }
        ALL_ACTIONS.put(actionId, actionClass);
    }

    public BlockEventTrigger[] getTriggers(String triggerId) {
        OrderedMap<String, BlockEventTrigger[]> triggers = this.getTriggerMap();
        return (BlockEventTrigger[])triggers.get(triggerId);
    }
}
