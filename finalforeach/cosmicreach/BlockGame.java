package finalforeach.cosmicreach;

import java.nio.IntBuffer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Json;

import finalforeach.cosmicreach.audio.SoundManager;
import finalforeach.cosmicreach.blockevents.BlockEvents;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.BlockStateInstantiator;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.rendering.BatchedZoneRenderer;
import finalforeach.cosmicreach.rendering.WorldRenderingMeshGenThread;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModel;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJson;
import finalforeach.cosmicreach.rendering.blockmodels.IBlockModelInstantiator;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.settings.Controls;
import finalforeach.cosmicreach.settings.GraphicsSettings;
import finalforeach.cosmicreach.worldgen.ZoneGenerator;
import games.rednblack.miniaudio.MiniAudio;

public class BlockGame extends ApplicationAdapter {
    public static MiniAudio miniAudio;
    public static Lwjgl3Application lwjglApp;
    public static boolean gameStarted;
    static final int GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX = 36935;
    static final int GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX = 36936;
    static final int GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX = 36937;
    static final int GPU_MEMORY_INFO_EVICTION_COUNT_NVX = 36938;
    static final int GPU_MEMORY_INFO_EVICTED_MEMORY_NVX = 36939;
    static final int GL_NUM_EXTENSIONS = 33309;
    long totalFrames;
    double totalDelta;
    double lastFrameFPS;
    public static boolean isFocused;
    private int defaultWindowWidth;
    private int defaultWindowHeight;
    float fixedUpdateAccumulator = 0.0f;
    double lastUpdateTime = -1.0;
    double secondsSinceLastUpdate;

    @Override
    public void dispose() {
        try {
            miniAudio.dispose();
            GameState.IN_GAME.dispose();
        } finally {
            System.out.println("Dispose() called! Closing the game.");
            System.exit(0);
        }
    }

    @Override
    public void create() {
        lwjglApp = (Lwjgl3Application)Gdx.app;
        System.out.println("GL_VENDOR: " + Gdx.gl.glGetString(7936));
        System.out.println("GL_RENDERER: " + Gdx.gl.glGetString(7937));
        System.out.println("GL_VERSION: " + Gdx.gl.glGetString(7938));
        System.out.println(Gdx.graphics.getGLVersion().getDebugVersionString());
        Lang.loadLanguages();
        miniAudio = new MiniAudio();
        IntBuffer i = BufferUtils.newIntBuffer(1);
        Gdx.gl.glGetIntegerv(33309, i);
        System.out.println("GL_NUM_EXTENSIONS: " + i.get());
        Gdx.graphics.setForegroundFPS(GraphicsSettings.maxFPS.getValue());
        GameShader.initShaders();
        this.defaultWindowWidth = Gdx.graphics.getWidth();
        this.defaultWindowHeight = Gdx.graphics.getHeight();
        GameState.currentGameState.create();
        BlockEvents.initBlockEvents();
        GameSingletons.meshGenThread = new WorldRenderingMeshGenThread();
        GameSingletons.zoneRenderer = new BatchedZoneRenderer();
        GameSingletons.soundManager = new ISoundManager(){

            @Override
            public void playSound(Sound sound, float volume, float pitch, float pan) {
                SoundManager.playSound(sound, volume, pitch, pan);
            }
        };
        GameSingletons.blockModelInstantiator = new IBlockModelInstantiator(){

            @Override
            public BlockModel getInstance(String modelName, int rotXZ) {
                return BlockModelJson.getInstance(modelName, rotXZ);
            }

            @Override
            public void createGeneratedModelInstance(BlockState blockState, BlockModel parentModel, String parentModelName, String modelName, int rotXZ) {
                Json json = new Json();
                String modelJson = "{\"parent\": \"" + parentModelName + "\", \"textures\":";
                modelJson = modelJson + json.toJson(((BlockModelJson)parentModel).getTextures());
                modelJson = modelJson + "}";
                BlockModelJson.getInstanceFromJsonStr(modelName, modelJson, rotXZ);
            }
        };
        ZoneGenerator.BLOCKSTATE_INSTANTIATOR = new BlockStateInstantiator(){

            @Override
            public BlockState getBlockStateInstance(String blockStateId) {
                return BlockState.getInstance(blockStateId);
            }
        };
        gameStarted = true;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (!Gdx.graphics.isFullscreen()) {
            this.defaultWindowWidth = Gdx.graphics.getWidth();
            this.defaultWindowHeight = Gdx.graphics.getHeight();
        }
    }

    private void runTicks() {
        this.fixedUpdateAccumulator += Gdx.graphics.getDeltaTime();
        float fixedUpdateTimestep = 0.05f;
        double curUpdateTime = System.currentTimeMillis();
        while (this.fixedUpdateAccumulator >= fixedUpdateTimestep) {
            GameState.currentGameState.update(fixedUpdateTimestep);
            this.fixedUpdateAccumulator -= fixedUpdateTimestep;
            this.lastUpdateTime = curUpdateTime;
        }
        this.secondsSinceLastUpdate = this.lastUpdateTime == -1.0 ? 0.0 : (curUpdateTime - this.lastUpdateTime) / 1000.0;
    }

    @Override
    public void render() {
        Controls.update();
        GameState thisFrameGameState = GameState.currentGameState;
        this.runTicks();
        float partTick = (float)(this.secondsSinceLastUpdate / (double)0.05f);
        GameState.currentGameState.render(partTick);
        GameState.currentGameState.firstFrame = false;
        if (Controls.keyDebugReloadShadersJustPressed()) {
            ChunkShader.reloadAllShaders();
        }
        if (Controls.keyFullscreenJustPressed()) {
            Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(this.defaultWindowWidth, this.defaultWindowHeight);
                System.setProperty("org.lwjgl.opengl.Window.undecorated", "false");
                Gdx.graphics.setUndecorated(false);
            } else {
                Gdx.graphics.setFullscreenMode(displayMode);
            }
        }
        GameState.lastFrameGameState = thisFrameGameState;
    }

    static {
        gameStarted = false;
        isFocused = true;
    }
}