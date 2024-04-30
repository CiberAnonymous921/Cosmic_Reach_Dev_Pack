package finalforeach.cosmicreach.blocks;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.OrderedMap;

import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.io.SaveLocation;

public class Block {
    public static final Map<String, BlockState> allBlockStates;
    public static final Map<String, Block> blocksByName;
    public static final Map<String, Block> blocksByStringId;
    public static final Array<Block> allBlocks;
    public static final Block AIR;
    public static final Block GRASS;
    public static final Block STONE_BASALT;
    public static final Block DIRT;
    public static final Block WOODPLANKS;
    public static final Block HAZARD;
    public static final Block SAND;
    public static final Block TREELOG;
    public static final Block SNOW;
    public static final Block WATER;
    public static final Block LUNAR_SOIL;
    private String stringId;
    public OrderedMap<String, String> defaultParams;
    public OrderedMap<String, BlockState> blockStates;

    public static Block getInstance(String blockName) {
        if (blocksByName.containsKey(blockName)) {
            return blocksByName.get(blockName);
        }
        Json json = new Json();
        Block b = json.fromJson(Block.class, GameAssetLoader.loadAsset("blocks/" + blockName + ".json"));
        Array<String> blockStateKeysToAdd = b.blockStates.keys().toArray();
        for (String stateKey : blockStateKeysToAdd) {
            BlockState blockState = (BlockState)b.blockStates.get(stateKey);
            blockState.stringId = stateKey;
            blockState.initialize(b);
            allBlockStates.put(blockState.stringId, blockState);
        }
        blocksByStringId.put(b.stringId, b);
        blocksByName.put(blockName, b);
        return b;
    }

    public static BlockState getBlockStateInstance(String blockStateStringId) {
        return allBlockStates.get(blockStateStringId);
    }

    public BlockState getDefaultBlockState() {
        BlockState defaultBlockState = (BlockState)this.blockStates.get("default");
        if (defaultBlockState == null) {
            return this.blockStates.values().next();
        }
        return defaultBlockState;
    }

    private Block() {
        allBlocks.add(this);
    }

    public String getStringId() {
        return this.stringId;
    }

    public String toString() {
        return this.stringId;
    }

    public static int getNumberOfTotalBlockStates() {
        int i = 0;
        for (Block block : allBlocks) {
            i += block.blockStates.size;
        }
        return i;
    }

    static {
        allBlockStates = new HashMap<String, BlockState>();
        blocksByName = new HashMap<String, Block>();
        blocksByStringId = new HashMap<String, Block>();
        allBlocks = new Array<Block>(Block.class);
        for (String f : Gdx.files.internal("assets.txt").readString().split("\n")) {
            try {
                if (!f.startsWith("blocks/") || !f.endsWith(".json") || !Gdx.files.internal(f).exists()) continue;
                String fileName = f.replace("blocks/", "");
                Block.getInstance(fileName.replace(".json", ""));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        for (FileHandle f : Gdx.files.absolute(SaveLocation.getSaveFolderLocation() + "/mods/assets/blocks").list()) {
            if (!f.name().endsWith(".json")) continue;
            Block.getInstance(f.nameWithoutExtension());
        }
        AIR = Block.getInstance("block_air");
        GRASS = Block.getInstance("block_grass");
        STONE_BASALT = Block.getInstance("block_stone_basalt");
        DIRT = Block.getInstance("block_dirt");
        WOODPLANKS = Block.getInstance("block_wood_planks");
        HAZARD = Block.getInstance("block_hazard");
        SAND = Block.getInstance("block_sand");
        TREELOG = Block.getInstance("block_tree_log");
        SNOW = Block.getInstance("block_snow");
        WATER = Block.getInstance("block_water");
        LUNAR_SOIL = Block.getInstance("block_lunar_soil");
    }
}