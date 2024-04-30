package finalforeach.cosmicreach.blocks;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.io.SaveLocation;

public class BlockStateGenerator {
    private static HashMap<String, BlockStateGenerator> generators;
    public String stringId;
    public String[] include = new String[0];
    public String modelName;
    public OrderedMap<String, String> params;
    public OrderedMap<String, ?> overrides;

    private static void loadGeneratorsFromFile(String fileName) {
        String jsonStr = GameAssetLoader.loadAsset(fileName).readString();
        JsonReader reader = new JsonReader();
        JsonValue value = reader.parse(jsonStr);
        JsonValue allGens = value.get("generators");
        JsonValue currentGenVal = allGens.child;
        Json json = new Json();
        while (currentGenVal != null) {
            BlockStateGenerator generator = json.fromJson(BlockStateGenerator.class, currentGenVal.toJson(JsonWriter.OutputType.json));
            if ((generator.params == null || generator.params.size == 0) && generator.include.length == 0) {
                throw new RuntimeException("Generator " + generator.stringId + " must declare params or reference other generators");
            }
            generators.put(generator.stringId, generator);
            currentGenVal = currentGenVal.next;
        }
    }

    public static BlockStateGenerator getInstance(String genKey) {
        return generators.get(genKey);
    }

    @SuppressWarnings("rawtypes")
    public void generate(BlockState oldState) {
        for (String string : this.include) {
            BlockStateGenerator subGenerator = BlockStateGenerator.getInstance(string);
            subGenerator.generate(oldState);
        }
        if (this.params == null || this.params.size == 0) {
            return;
        }
        BlockState blockState = oldState.copy(false);
        blockState.stateGenerators = null;
        String newSaveKey = oldState.getSaveKey();
        for (ObjectMap.Entry entry : this.params.entries()) {
            newSaveKey = BlockState.getModifiedSaveKey(newSaveKey, (String)entry.key, ((String)entry.value).toString());
        }
        blockState.stringId = newSaveKey.replaceFirst(oldState.getBlockId(), "");
        blockState.stringId = blockState.stringId.substring(1, blockState.stringId.length() - 1);
        try {
            if (this.overrides != null) {
                for (String string : this.overrides.keys()) {
                    Float of;
                    Field f = BlockState.class.getField(string);
                    Object overrideVal = this.overrides.get(string);
                    if (f.getType() == Integer.TYPE && overrideVal instanceof Float && (of = (Float)overrideVal).floatValue() == (float)of.intValue()) {
                        overrideVal = of.intValue();
                    }
                    f.set(blockState, overrideVal);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        blockState.initialize(oldState.getBlock());
        if (this.modelName != null) {
            String genModelName = "gen_model::" + oldState.getBlockId() + "[" + blockState.stringId + "]";
            GameSingletons.blockModelInstantiator.createGeneratedModelInstance(blockState, oldState.getModel(), this.modelName, genModelName, blockState.rotXZ);
            blockState.setBlockModel(genModelName);
        }
        Block.allBlockStates.put(blockState.stringId, blockState);
        blockState.getBlock().blockStates.put(blockState.stringId, blockState);
    }

    static {
        int var5_7 = 0;
        generators = new HashMap<String, BlockStateGenerator>();
        String folderName = "block_state_generators";
        String[] defaultAssetList = Gdx.files.internal("assets.txt").readString().split("\n");
        HashSet<Object> assetsToLoad = new HashSet<Object>();
        String[] stringArray = defaultAssetList;
        int n = stringArray.length;
        while (var5_7 < n) {
            String f = stringArray[var5_7];
            try {
                if (f.startsWith(folderName) && f.endsWith(".json") && Gdx.files.internal(f).exists()) {
                    assetsToLoad.add(f);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ++var5_7;
        }
        FileHandle[] moddedBlockDir = Gdx.files.absolute(SaveLocation.getSaveFolderLocation() + "/mods/assets/" + folderName).list();
        for (FileHandle f : moddedBlockDir) {
            if (!f.name().endsWith(".json")) continue;
            assetsToLoad.add(folderName + "/" + f.name());
        }
        for (Object string : assetsToLoad) {
            BlockStateGenerator.loadGeneratorsFromFile((String) string);
        }
    }
}