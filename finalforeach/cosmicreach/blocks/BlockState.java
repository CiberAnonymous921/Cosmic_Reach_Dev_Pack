package finalforeach.cosmicreach.blocks;

import java.util.HashSet;
import java.util.Map;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.blockevents.BlockEvents;
import finalforeach.cosmicreach.rendering.IMeshData;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModel;
import finalforeach.cosmicreach.savelib.blocks.IBlockState;

public class BlockState implements IBlockState {
    private transient Block block;
    private String blockId;
    private String modelName;
    public String stringId;
    public String blockEventsId;
    public float blastResistance = 100.0f;
    public boolean catalogHidden;
    public String[] stateGenerators;
    private transient BlockModel blockModel;
    public boolean isOpaque = true;
    public boolean isTransparent = false;
    public boolean walkThrough = false;
    public int lightAttenuation = 15;
    public boolean cullsSelf = true;
    public boolean itemCatalogHidden = false;
    private boolean canRaycastForBreak = true;
    private boolean canRaycastForPlaceOn = true;
    private boolean canRaycastForReplace = false;
    public boolean isFluid = false;
    public int lightLevelRed = 0;
    public int lightLevelGreen = 0;
    public int lightLevelBlue = 0;
    public boolean isPosXFaceOccluding;
    public boolean isNegXFaceOccluding;
    public boolean isPosYFaceOccluding;
    public boolean isNegYFaceOccluding;
    public boolean isPosZFaceOccluding;
    public boolean isNegZFaceOccluding;
    public boolean isSelfPosXFaceOccluding;
    public boolean isSelfNegXFaceOccluding;
    public boolean isSelfPosYFaceOccluding;
    public boolean isSelfNegYFaceOccluding;
    public boolean isSelfPosZFaceOccluding;
    public boolean isSelfNegZFaceOccluding;
    public boolean isPosXFacePartOccluding;
    public boolean isNegXFacePartOccluding;
    public boolean isPosYFacePartOccluding;
    public boolean isNegYFacePartOccluding;
    public boolean isPosZFacePartOccluding;
    public boolean isNegZFacePartOccluding;
    public int rotXZ;
    @Deprecated
    public boolean generateSlabs;

    public void addVertices(IMeshData meshData, int bx, int by, int bz, int opaqueBitmask, short[] blockLightLevels, int[] skyLightLevels) {
        this.blockModel.addVertices(meshData, bx, by, bz, opaqueBitmask, blockLightLevels, skyLightLevels);
    }

    public void addVertices(IMeshData meshData, int bx, int by, int bz) {
        short[] blockLightLevels = new short[8];
        int[] skyLightLevels = new int[8];
        for (int s = 0; s < blockLightLevels.length; ++s) {
            blockLightLevels[s] = 4095;
        }
        this.blockModel.addVertices(meshData, bx, by, bz, 0, blockLightLevels, skyLightLevels);
    }

    public Block getBlock() {
        return this.block;
    }

    public void initialize(String blockId) {
        this.initialize(Block.getInstance(blockId));
    }

    public BlockState copy() {
        return this.copy(true);
    }

    public BlockState copy(boolean initialize) {
        Json json = new Json();
        String jsonStr = json.toJson(this);
        BlockState blockState = json.fromJson(BlockState.class, jsonStr);
        if (initialize) {
            blockState.initialize(this.block);
        }
        return blockState;
    }

    public void initialize(Block block) {
        this.block = block;
        this.blockId = block.getStringId();
        this.setBlockModel(this.modelName);
        if (this.generateSlabs && this.stateGenerators == null) {
            this.stateGenerators = new String[]{"base:slabs_all"};
        }
        if (this.stateGenerators != null) {
            for (String genKey : this.stateGenerators) {
                BlockStateGenerator generator = BlockStateGenerator.getInstance(genKey);
                generator.generate(this);
            }
        }
    }

    public void setBlockModel(String modelName) {
        this.blockModel = GameSingletons.blockModelInstantiator.getInstance(modelName, this.rotXZ);
        this.getBlockEvents();
        this.isPosXFaceOccluding = this.blockModel.isPosXFaceOccluding && !this.isTransparent;
        this.isNegXFaceOccluding = this.blockModel.isNegXFaceOccluding && !this.isTransparent;
        this.isPosYFaceOccluding = this.blockModel.isPosYFaceOccluding && !this.isTransparent;
        this.isNegYFaceOccluding = this.blockModel.isNegYFaceOccluding && !this.isTransparent;
        this.isPosZFaceOccluding = this.blockModel.isPosZFaceOccluding && !this.isTransparent;
        this.isNegZFaceOccluding = this.blockModel.isNegZFaceOccluding && !this.isTransparent;
        this.isSelfPosXFaceOccluding = this.blockModel.isPosXFaceOccluding;
        this.isSelfNegXFaceOccluding = this.blockModel.isNegXFaceOccluding;
        this.isSelfPosYFaceOccluding = this.blockModel.isPosYFaceOccluding;
        this.isSelfNegYFaceOccluding = this.blockModel.isNegYFaceOccluding;
        this.isSelfPosZFaceOccluding = this.blockModel.isPosZFaceOccluding;
        this.isSelfNegZFaceOccluding = this.blockModel.isNegZFaceOccluding;
        this.isPosXFacePartOccluding = this.blockModel.isPosXFacePartOccluding;
        this.isNegXFacePartOccluding = this.blockModel.isNegXFacePartOccluding;
        this.isPosYFacePartOccluding = this.blockModel.isPosYFacePartOccluding;
        this.isNegYFacePartOccluding = this.blockModel.isNegYFacePartOccluding;
        this.isPosZFacePartOccluding = this.blockModel.isPosZFacePartOccluding;
        this.isNegZFacePartOccluding = this.blockModel.isNegZFacePartOccluding;
    }

    public BlockEvents getBlockEvents() {
        return BlockEvents.getInstance(this.blockEventsId);
    }

    public BlockModel getModel() {
        return this.blockModel;
    }

    public boolean canRaycastForBreak() {
        return this.canRaycastForBreak;
    }

    public boolean canRaycastForPlaceOn() {
        return this.canRaycastForPlaceOn;
    }

    public boolean canRaycastForReplace() {
        return this.canRaycastForReplace;
    }

    public boolean hasEmptyModel() {
        return this.getModel().isEmpty();
    }

    public String toString() {
        return this.getSaveKey();
    }

    public String getSaveKey() {
        return this.blockId + "[" + this.stringId + "]";
    }

    public void getBoundingBox(BoundingBox blockBoundingBox, int bx, int by, int bz) {
        blockBoundingBox.min.set(this.getModel().boundingBox.min);
        blockBoundingBox.max.set(this.getModel().boundingBox.max);
        blockBoundingBox.min.add(bx, by, bz);
        blockBoundingBox.max.add(bx, by, bz);
        blockBoundingBox.update();
    }

    public void getBoundingBox(BoundingBox boundingBox, BlockPosition position) {
        this.getBoundingBox(boundingBox, position.getGlobalX(), position.getGlobalY(), position.getGlobalZ());
    }

    public Array<BoundingBox> getAllBoundingBoxes(Array<BoundingBox> blockBoundingBoxes, BlockPosition blockPos) {
        this.getAllBoundingBoxes(blockBoundingBoxes, blockPos.getGlobalX(), blockPos.getGlobalY(), blockPos.getGlobalZ());
        return blockBoundingBoxes;
    }

    public Array<BoundingBox> getAllBoundingBoxes(Array<BoundingBox> boundingBoxes, int bx, int by, int bz) {
        this.getModel().getAllBoundingBoxes(boundingBoxes, bx, by, bz);
        return boundingBoxes;
    }

    public String getBlockId() {
        return this.blockId;
    }

    public static BlockState getInstance(String blockStateSaveKey) {
        for (int i = 0; i < blockStateSaveKey.length(); ++i) {
            char c = blockStateSaveKey.charAt(i);
            if (c != '[') continue;
            String blockId = blockStateSaveKey.substring(0, i);
            Block block = Block.blocksByStringId.get(blockId);
            if (block == null) {
                if (blockStateSaveKey.equals("base:stone[default]")) {
                    return BlockState.getInstance("base:stone_basalt[default]");
                }
                return BlockStateMissing.fromMissingKey(blockStateSaveKey);
            }
            return BlockState.getBlockStateOfBlock(block, blockStateSaveKey);
        }
        return BlockStateMissing.fromMissingKey(blockStateSaveKey);
    }

    @SuppressWarnings("rawtypes")
    private static String getSortedParamListStr(Block block, String paramStr) {
        int var6_9 = 0;
        HashSet<String> addedKeys = new HashSet<String>();
        Array<Object> sortedParamList = new Array<Object>();
        String[] stringArray = paramStr.split(",");
        int n = stringArray.length;
        while (var6_9 < n) {
            String p = stringArray[var6_9];
            sortedParamList.add(p);
            addedKeys.add(p.split("=")[0]);
            ++var6_9;
        }
        if (block.defaultParams != null) {
            for (ObjectMap.Entry entry : block.defaultParams) {
                if (addedKeys.contains(entry.key)) continue;
                sortedParamList.add((String)entry.key + "=" + (String)entry.value);
            }
        }
        sortedParamList.sort();
        StringBuilder sb = new StringBuilder();
        for (Object string : sortedParamList) {
            sb.append(string);
        }
        return sb.toString();
    }

    @SuppressWarnings("rawtypes")
    public static BlockState getBlockStateOfBlock(Block block, String blockStateSaveKey) {
        String paramStr = blockStateSaveKey.replaceFirst(block.getStringId() + "\\[", "");
        BlockState bs = (BlockState)block.blockStates.get(paramStr = paramStr.substring(0, paramStr.length() - 1));
        if (bs != null) {
            return bs;
        }
        String sortedParams = BlockState.getSortedParamListStr(block, paramStr);
        for (ObjectMap.Entry entry : block.blockStates.entries()) {
            String candidateParamStr = (String)entry.key;
            String candidateSortedParams = BlockState.getSortedParamListStr(block, candidateParamStr);
            if (!candidateSortedParams.equals(sortedParams)) continue;
            return (BlockState)entry.value;
        }
        return BlockStateMissing.fromMissingKey(blockStateSaveKey);
    }

    public static String getModifiedSaveKey(String oldSaveKey, String paramName, String paramVal) {
        String newSaveKey2;
        String newSaveKey = oldSaveKey;
        Object paramSet = paramName + "=" + paramVal;
        if (paramVal.length() == 0) {
            paramSet = "";
        }
        if (newSaveKey.equals(newSaveKey2 = newSaveKey.replaceAll(paramName + "=[^,\\]]+", (String)paramSet))) {
            if (!newSaveKey.contains((CharSequence)paramSet)) {
                newSaveKey = newSaveKey.replaceFirst("\\[", "[" + (String)paramSet + ",").replaceAll(",\\]", "]");
            }
        } else {
            newSaveKey = newSaveKey2;
        }
        return newSaveKey;
    }

    public BlockState getVariantWithParams(Map<String, ?> paramMap) {
        String newSaveKey = this.getSaveKey();
        for (Map.Entry<String, ?> param : paramMap.entrySet()) {
            newSaveKey = BlockState.getModifiedSaveKey(newSaveKey, param.getKey(), param.getValue().toString());
        }
        return BlockState.getInstance(newSaveKey);
    }

    @SuppressWarnings("rawtypes")
    public BlockState getVariantWithParams(OrderedMap<String, ?> paramMap) {
        String newSaveKey = this.getSaveKey();
        for (ObjectMap.Entry entry : paramMap.entries()) {
            newSaveKey = BlockState.getModifiedSaveKey(newSaveKey, (String)entry.key, entry.value.toString());
        }
        return BlockState.getInstance(newSaveKey);
    }

    public boolean isLightEmitter() {
        return this.lightLevelRed > 0 || this.lightLevelGreen > 0 || this.lightLevelBlue > 0;
    }

    public BlockEventTrigger[] getTrigger(String triggerName) {
        BlockEvents events = this.getBlockEvents();
        BlockEventTrigger[] triggers = events.getTriggers(triggerName);
        return triggers;
    }

    public String getStateParamsStr() {
        return this.stringId;
    }
}