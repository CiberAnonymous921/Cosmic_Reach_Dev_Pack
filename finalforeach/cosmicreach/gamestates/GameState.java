package finalforeach.cosmicreach.gamestates;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import finalforeach.cosmicreach.io.SaveLocation;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.ui.UIObject;

public class GameState implements Disposable {
    public static final float FIXED_UPDATE_TIMESTEP = 0.05f;
    public static final InGame IN_GAME = new InGame();
    public static final LoadingGame LOADING_GAME = new LoadingGame();
    public static GameState currentGameState;
    public static GameState lastFrameGameState;
    public static SpriteBatch batch;
    public Array<UIObject> uiObjects = new Array<UIObject>();
    Vector2 mouse = new Vector2();
    protected Viewport uiViewport;
    protected OrthographicCamera uiCamera;
    GameState previousState;
    boolean created = false;
    public boolean firstFrame;
    int curWidth = -1;
    int curHeight = -1;
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    String lastDateStr;
    int i = 1;

    public void create() {
        this.created = true;
        this.uiCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.uiViewport = new ExtendViewport(800.0f, 600.0f, this.uiCamera);
        this.uiCamera.up.set(0.0f, -1.0f, 0.0f);
        this.uiCamera.direction.set(0.0f, 0.0f, 1.0f);
        this.uiCamera.update();
        this.uiViewport.apply(false);
        this.firstFrame = true;
    }

    public void update(float deltaTime) {
    }

    public static void switchToGameState(GameState gameState) {
        System.out.println("Switched to different gamestate: " + gameState.getClass().getSimpleName());
        if (currentGameState != null) {
            for (int i = 0; i < GameState.currentGameState.uiObjects.size; ++i) {
                UIObject u = GameState.currentGameState.uiObjects.get(i);
                u.deactivate();
            }
        }
        if (!gameState.created) {
            gameState.create();
        }
        currentGameState = gameState;
        GameState.currentGameState.firstFrame = true;
    }

    public void render(float partTick) {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        if (this.curWidth != screenWidth || this.curHeight != screenHeight) {
            this.resize(screenWidth, screenHeight);
        }
    }

    public void drawUIElements() {
        if (this == IN_GAME && !UI.renderUI) {
            return;
        }
        this.uiViewport.apply(false);
        this.mouse.set(Gdx.input.getX(), Gdx.input.getY());
        this.uiViewport.unproject(this.mouse);
        batch.setProjectionMatrix(this.uiCamera.combined);
        batch.begin();
        for (UIObject uiObj : this.uiObjects) {
            uiObj.drawBackground(this.uiViewport, batch, this.mouse.x, this.mouse.y);
        }
        for (UIObject uiObj : this.uiObjects) {
            uiObj.drawText(this.uiViewport, batch);
        }
        batch.end();
    }

    public String takeScreenshot() {
        try {
            Object dateStr = this.dateFormat.format(new Date());
            if (((String)dateStr).equals(this.lastDateStr)) {
                dateStr = (String)dateStr + "_" + this.i;
                ++this.i;
            } else {
                this.lastDateStr = (String) dateStr;
                this.i = 1;
            }
            String screenshotDirLoc = SaveLocation.getScreenshotFolderLocation();
            new File(screenshotDirLoc).mkdirs();
            Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            ByteBuffer pixels = pixmap.getPixels();
            int size = Gdx.graphics.getBackBufferWidth() * Gdx.graphics.getBackBufferHeight() * 4;
            for (int i = 3; i < size; i += 4) {
                pixels.put(i, (byte)-1);
            }
            String screenshotFileName = screenshotDirLoc + "/" + (String)dateStr + ".png";
            PixmapIO.writePNG(Gdx.files.absolute(screenshotFileName), pixmap, -1, true);
            pixmap.dispose();
            return screenshotFileName;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void dispose() {
    }

    public void resize(int width, int height) {
        this.curWidth = width;
        this.curHeight = height;
        this.uiViewport.update(width, height);
    }

    static {
        lastFrameGameState = currentGameState = new PrealphaPreamble();
        batch = new SpriteBatch();
    }
}