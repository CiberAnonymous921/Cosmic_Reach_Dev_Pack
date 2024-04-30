package finalforeach.cosmicreach.blocks;

import java.util.HashMap;

public class BlockStateMissing extends BlockState {
    public static HashMap<String, BlockState> missingBlockStates = new HashMap<String, BlockState>();
    String saveKey;

    public BlockStateMissing(String blockStateSaveKey) {
        this.saveKey = blockStateSaveKey;
    }

    public static BlockState fromMissingKey(String blockStateSaveKey) {
        BlockState missing = missingBlockStates.get(blockStateSaveKey);
        if (missing == null) {
            missing = new BlockStateMissing(blockStateSaveKey);
            missing.setBlockModel("model_debug");
            missingBlockStates.put(blockStateSaveKey, missing);
        }
        return missing;
    }

    @Override
    public String getSaveKey() {
        return this.saveKey;
    }

    @Override
    public String getStateParamsStr() {
        String paramStr = this.saveKey.replaceFirst(".*\\[", "");
        paramStr = paramStr.substring(0, paramStr.length() - 1);
        return paramStr;
    }

    @Override
    public Block getBlock() {
        return Block.getInstance("block_debug");
    }
}