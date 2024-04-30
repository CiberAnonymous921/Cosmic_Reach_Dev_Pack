package finalforeach.cosmicreach.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.Player;
import finalforeach.cosmicreach.io.ChunkLoader;
import finalforeach.cosmicreach.io.EntitySaveSystem;
import finalforeach.cosmicreach.io.SaveLocation;
import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.settings.Controls;
import finalforeach.cosmicreach.settings.GraphicsSettings;
import finalforeach.cosmicreach.ui.FontRenderer;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.ui.VerticalAnchor;
import finalforeach.cosmicreach.world.BlockSelection;
import finalforeach.cosmicreach.world.Region;
import finalforeach.cosmicreach.world.Sky;
import finalforeach.cosmicreach.world.World;
import finalforeach.cosmicreach.world.WorldLoader;
import finalforeach.cosmicreach.world.Zone;

public class InGame extends GameState {
    public static World world;
    private static Player player;
    private static PerspectiveCamera rawWorldCamera;
    private BlockSelection blockSelection;
    private Viewport viewport;
    private UI ui;
    private transient float screenshotMsgCountdownTimer = 0.0f;
    private transient String lastScreenshotFileName;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void unloadWorld() {
        Object object = WorldLoader.worldGenLock;
        synchronized (object) {
            WorldLoader.worldLoader.readyToPlay = false;
            for (Zone z : world.getZones()) {
                for (Region r : z.regions.values()) {
                    z.removeRegion(r);
                }
                z.dispose();
            }
            player = null;
            GameSingletons.zoneRenderer.unload();
            world = null;
        }
    }

    public void loadWorld(String worldNameToLoad) {
        World world = ChunkLoader.loadWorld(worldNameToLoad);
        if (world == null) {
            throw new RuntimeException("Could not load world: " + worldNameToLoad);
        }
        this.loadWorld(world);
    }

    public void loadWorld(World world) {
        this.setWorld(world);
        EntitySaveSystem.loadPlayers(world);
        if (player == null) {
            player = new Player();
            InGame.player.zoneId = world.defaultZoneId;
            Entity playerEntity = new Entity();
            player.setEntity(playerEntity);
            player.setPosition(0.0f, 300.0f, 0.0f);
            Zone playerZone = player.getZone(world);
            playerZone.allEntities.add(playerEntity);
        }
    }

    @Override
    public void create() {
        super.create();
        Gdx.graphics.setVSync(GraphicsSettings.vSyncEnabled.getValue());
        this.blockSelection = new BlockSelection();
        rawWorldCamera = new PerspectiveCamera(GraphicsSettings.fieldOfView.getValue(), Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        InGame.rawWorldCamera.near = 0.1f;
        InGame.rawWorldCamera.far = 2500.0f;
        this.viewport = new ExtendViewport(1.0f, 1.0f, rawWorldCamera);
        this.ui = new UI();
        Gdx.input.setInputProcessor(this.ui);
    }

    public static Player getLocalPlayer() {
        return player;
    }

    public static void setLocalPlayer(Player player) {
        Zone playerZone = player.getZone(world);
        if (InGame.player != null) {
            playerZone.allEntities.removeValue(InGame.player.getEntity(), true);
        }
        InGame.player = player;
        player.updateCamera(rawWorldCamera, 0.0f);
        playerZone.allEntities.add(player.getEntity());
    }

    public void setWorld(World world) {
        if (InGame.world != null) {
            for (Zone z : InGame.world.getZones()) {
                for (Region r : z.regions.values()) {
                    z.removeRegion(r);
                }
                z.dispose();
            }
        }
        InGame.world = world;
    }

    public Camera getWorldCamera() {
        return rawWorldCamera;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        if (!WorldLoader.worldLoader.readyToPlay) {
            return;
        }
        InGame.rawWorldCamera.fieldOfView = GraphicsSettings.fieldOfView.getValue();
        if (player.getEntity() != null && InGame.player.getEntity().position.y < InGame.player.getZone((World)InGame.world).respawnHeight) {
            player.respawn(world);
        }
        for (Zone z : world.getZones()) {
            z.runScheduledTriggers();
            for (Entity e : z.allEntities) {
                e.update(z, deltaTime);
            }
        }
    }

    @Override
    public void render(float partTick) {
        super.render(partTick);
        Zone playerZone = player.getZone(world);
        if (currentGameState == this) {
            player.update(playerZone);
            this.blockSelection.raycast(playerZone, rawWorldCamera);
            player.updateCamera(rawWorldCamera, partTick);
        }
        if (!this.firstFrame && Gdx.input.isKeyJustPressed(111)) {
            if (UI.itemCatalog.shown) {
                UI.itemCatalog.setShown(false);
            } else {
                boolean cursorCatched = Gdx.input.isCursorCatched();
                Gdx.input.setCursorCatched(false);
                InGame.switchToGameState(new PauseMenu(cursorCatched));
            }
        }
        ScreenUtils.clear(Sky.skyColor, true);
        this.viewport.apply();
        Sky.drawStars(rawWorldCamera);
        GameSingletons.zoneRenderer.render(playerZone, rawWorldCamera);
        this.blockSelection.render(rawWorldCamera);
        this.ui.render();
        this.drawUIElements();
        float maxScreenshotMsgCountdownTimer = 5.0f;
        if (Controls.screenshotPressed()) {
            this.lastScreenshotFileName = this.takeScreenshot();
            this.screenshotMsgCountdownTimer = maxScreenshotMsgCountdownTimer;
        }
        if (this.screenshotMsgCountdownTimer > 0.0f) {
            batch.setColor(1.0f, 1.0f, 1.0f, MathUtils.clamp(this.screenshotMsgCountdownTimer / maxScreenshotMsgCountdownTimer, 0.0f, 1.0f));
            batch.begin();
            Object saveText = this.lastScreenshotFileName != null ? Lang.get("Screenshot_info") + this.lastScreenshotFileName.replace(SaveLocation.getSaveFolderLocation(), "") : Lang.get("Screenshot_error");
            FontRenderer.drawText(batch, this.uiViewport, (String)saveText, 0.0f, 8.0f, HorizontalAnchor.LEFT_ALIGNED, VerticalAnchor.TOP_ALIGNED);
            batch.end();
            this.screenshotMsgCountdownTimer -= Gdx.graphics.getDeltaTime();
            if (this.screenshotMsgCountdownTimer <= 0.0f) {
                this.lastScreenshotFileName = null;
            }
            batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        this.viewport.update(width, height);
        this.ui.resize(width, height);
    }

    @Override
    public void dispose() {
        this.unloadWorld();
    }
}