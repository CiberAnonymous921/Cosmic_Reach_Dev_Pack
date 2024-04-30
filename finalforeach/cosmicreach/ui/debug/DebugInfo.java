package finalforeach.cosmicreach.ui.debug;

import java.text.DecimalFormat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

import finalforeach.cosmicreach.RuntimeInfo;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.ui.FontRenderer;
import finalforeach.cosmicreach.world.BlockSelection;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Region;
import finalforeach.cosmicreach.world.World;
import finalforeach.cosmicreach.world.Zone;

public class DebugInfo {
    private static Array<DebugItem> items = new Array<DebugItem>();
    private static String entireStr;
    private static StringBuilder stringBuilder;

    public static void addDebugItem(DebugItem item) {
        items.add(item);
    }

    public static void removeDebugItem(DebugItem item) {
        items.removeValue(item, true);
    }

    private static void update() {
        int i;
        boolean dirty = false;
        for (i = 0; i < DebugInfo.items.size; ++i) {
            DebugItem item = items.get(i);
            item.update();
            dirty |= DebugInfo.items.get((int)i).dirty;
            item.dirty = false;
        }
        if (dirty) {
            stringBuilder.setLength(0);
            for (i = 0; i < DebugInfo.items.size; ++i) {
                String l = DebugInfo.items.get((int)i).line;
                if (l == null) continue;
                stringBuilder.append(l);
                if (!DebugInfo.items.get((int)i).endInNewLine) continue;
                stringBuilder.append("\n");
            }
            entireStr = stringBuilder.toString();
        }
    }

    public static void drawDebugText(SpriteBatch batch, Viewport uiViewport) {
        DebugInfo.update();
        if (entireStr != null) {
            FontRenderer.drawText(batch, uiViewport, entireStr, -uiViewport.getWorldWidth() / 2.0f, -uiViewport.getWorldHeight() / 2.0f);
        }
    }

    static {
        DecimalFormat debugPositionFormat = new DecimalFormat("0.00");
        DebugInfo.addDebugItem(new DebugObjectItem<String>(false, () -> RuntimeInfo.version, s -> "Cosmic Reach Pre-alpha Version: " + s));
        DebugInfo.addDebugItem(new DebugIntItem("FPS:", () -> Gdx.graphics.getFramesPerSecond()));
        DebugInfo.addDebugItem(new DebugObjectItem<String>(false, () -> RuntimeInfo.getJavaHeapUseStr(), s -> "Java Heap Memory: " + s));
        DebugInfo.addDebugItem(new DebugVec3Item(() -> InGame.getLocalPlayer().getEntity().position, pos -> "Position: (" + debugPositionFormat.format(pos.x) + ", " + debugPositionFormat.format(pos.y) + ", " + debugPositionFormat.format(pos.z) + ")"));
        DebugInfo.addDebugItem(new DebugObjectItem<Chunk>(false, () -> InGame.getLocalPlayer().getChunk(InGame.world), c -> "Chunk: " + c));
        DebugInfo.addDebugItem(new DebugObjectItem<BlockState>(false, () -> BlockSelection.getBlockLookingAt(), bs -> "Looking at " + BlockSelection.getBlockPositionLookingAt() + ": " + bs.getSaveKey()));
        DebugIntItem debugLighting = new DebugIntItem(() -> InGame.getLocalPlayer().getBlockLight(InGame.world), blockLight -> {
            int lightR = (blockLight & 0xF00) >> 8;
            int lightG = (blockLight & 0xF0) >> 4;
            int lightB = blockLight & 0xF;
            return "Lighting: (" + lightR + ", " + lightG + ", " + lightB + ")";
        });
        debugLighting.endInNewLine = false;
        DebugInfo.addDebugItem(debugLighting);
        DebugInfo.addDebugItem(new DebugIntItem(", Sky: ", () -> InGame.getLocalPlayer().getSkyLight(InGame.world)));
        DebugInfo.addDebugItem(new DebugIntItem("Chunks loaded: ", () -> {
            Zone playerZone = InGame.getLocalPlayer().getZone(InGame.world);
            int numChunks = 0;
            for (Region r : playerZone.regions.values()) {
                numChunks += r.getNumberOfChunks();
            }
            return numChunks;
        }));
        DebugInfo.addDebugItem(new DebugIntItem("Regions loaded: ", () -> InGame.getLocalPlayer().getZone((World)InGame.world).regions.size()));
        DebugInfo.addDebugItem(new DebugLongItem("World Seed: ", () -> InGame.world.worldSeed));
        DebugInfo.addDebugItem(new DebugLongItem("Zone Seed: ", () -> InGame.getLocalPlayer().getZone((World)InGame.world).zoneGenerator.seed));
        stringBuilder = new StringBuilder();
    }
}