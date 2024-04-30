package finalforeach.cosmicreach.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.items.ItemCatalog;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.ItemSlotCursor;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.settings.Controls;
import finalforeach.cosmicreach.ui.debug.DebugInfo;

public class UI implements InputProcessor {
    private Viewport uiViewport;
    private static Crosshair crosshair;
    OrthographicCamera uiCamera;
    ShapeRenderer shapeRenderer = new ShapeRenderer();
    public static Hotbar hotbar;
    public static ItemCatalog itemCatalog;
    public static ItemSlotCursor itemCursor;
    public static boolean uiNeedMouse;
    public static boolean mouseOverUI;
    public static SpriteBatch batch;
    public static boolean renderUI;
    private boolean renderDebugInfo = false;

    public UI() {
        crosshair = new Crosshair();
        this.uiCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.uiViewport = new ExtendViewport(800.0f, 600.0f, this.uiCamera);
        this.uiCamera.up.set(0.0f, -1.0f, 0.0f);
        this.uiCamera.direction.set(0.0f, 0.0f, 1.0f);
        this.uiCamera.update();
        this.uiViewport.apply(false);
        hotbar = new Hotbar();
        itemCatalog = new ItemCatalog(Block.getNumberOfTotalBlockStates());
        itemCatalog.hide();
        itemCursor = new ItemSlotCursor();
        itemCursor.hide();
    }

    public void resize(int width, int height) {
        this.uiViewport.update(width, height);
    }

    public void render() {
        if (Controls.toggleHideUIPressed()) {
            renderUI = !renderUI;
        }
        if (Controls.debugInfoPressed()) {
            this.renderDebugInfo = !this.renderDebugInfo;
        }
        if (Controls.inventoryPressed()) {
            itemCatalog.toggleShown();
        }
        uiNeedMouse = UI.itemCatalog.shown;
        if (Controls.cycleItemLeft()) {
            hotbar.scrolled(0.0f, -1.0f);
        }
        if (Controls.cycleItemRight()) {
            hotbar.scrolled(0.0f, 1.0f);
        }
        Gdx.gl.glClear(256);
        if (renderUI) {
            crosshair.render(this.uiCamera);
            Gdx.gl.glEnable(3042);
            Gdx.gl.glDepthFunc(519);
            Gdx.gl.glBlendFunc(770, 771);
            Gdx.gl.glCullFace(1028);
            this.uiViewport.apply(false);
            this.shapeRenderer.setProjectionMatrix(this.uiCamera.combined);
            itemCatalog.render(this.uiViewport, this.shapeRenderer);
            this.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            hotbar.render(this.uiViewport, this.shapeRenderer);
            this.shapeRenderer.end();
            Gdx.gl.glClear(256);
            Gdx.gl.glEnable(2929);
            Gdx.gl.glDepthFunc(513);
            Gdx.gl.glEnable(2884);
            Gdx.gl.glCullFace(1029);
            Gdx.gl.glEnable(3042);
            Gdx.gl.glBlendFunc(770, 771);
            hotbar.drawItems(this.uiViewport);
            itemCatalog.drawItems(this.uiViewport);
            itemCursor.drawItems(this.uiViewport);
            this.uiViewport.apply();
            this.uiViewport.apply(false);
            if (this.renderDebugInfo) {
                Gdx.gl.glActiveTexture(33984);
                batch.setProjectionMatrix(this.uiCamera.combined);
                batch.begin();
                DebugInfo.drawDebugText(batch, this.uiViewport);
                batch.end();
            }
            Gdx.gl.glActiveTexture(33984);
            Gdx.gl.glBindTexture(3553, 0);
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        return hotbar.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float sy;
        float sx;
        if (UI.itemCatalog.shown && itemCatalog.isPointInBounds(sx = (float)(screenX -= this.uiViewport.getScreenWidth() / 2) / (float)this.uiViewport.getScreenWidth() * this.uiViewport.getWorldWidth(), sy = (float)(screenY -= this.uiViewport.getScreenHeight() / 2) / (float)this.uiViewport.getScreenHeight() * this.uiViewport.getWorldHeight())) {
            mouseOverUI = true;
            int slotStart = UI.itemCatalog.pagesToItemSlotNum.get(UI.itemCatalog.curPage);
            int slotEnd = itemCatalog.getNumSlots() - 1;
            if (UI.itemCatalog.curPage + 1 < UI.itemCatalog.pagesToItemSlotNum.size) {
                slotEnd = UI.itemCatalog.pagesToItemSlotNum.get(UI.itemCatalog.curPage + 1) - 1;
            }
            for (int i = slotStart; i <= slotEnd; ++i) {
                ItemSlot slot = itemCatalog.getSlot(i);
                ItemStack itemStack = slot.itemStack;
                if (itemStack == null || !slot.isHoveredOver(this.uiViewport, sx, sy)) continue;
                hotbar.addItemStack(itemStack);
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        mouseOverUI = false;
        if (UI.itemCatalog.shown && itemCatalog.isPointInBounds((float)(screenX -= this.uiViewport.getScreenWidth() / 2) / (float)this.uiViewport.getScreenWidth() * this.uiViewport.getWorldWidth(), (float)(screenY -= this.uiViewport.getScreenHeight() / 2) / (float)this.uiViewport.getScreenHeight() * this.uiViewport.getWorldHeight())) {
            mouseOverUI = true;
        }
        return mouseOverUI;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        mouseOverUI = false;
        if (UI.itemCatalog.shown) {
            float sx = screenX - this.uiViewport.getScreenWidth() / 2;
            if (itemCatalog.isPointInBounds(sx / (float)this.uiViewport.getScreenWidth() * this.uiViewport.getWorldWidth(), sx / (float)this.uiViewport.getScreenHeight() * this.uiViewport.getWorldHeight())) {
                mouseOverUI = true;
            }
            if (UI.hotbar.shown) {
                Vector2 mVec = new Vector2(Gdx.graphics.getWidth() / 2 + Gdx.input.getX(), Gdx.input.getY());
                this.uiViewport.unproject(mVec);
            }
        }
        return mouseOverUI;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return hotbar.scrolled(amountX, amountY);
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    static {
        batch = new SpriteBatch();
        renderUI = true;
    }
}