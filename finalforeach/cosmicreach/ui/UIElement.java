package finalforeach.cosmicreach.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.audio.SoundManager;

public class UIElement implements UIObject {
    public static UIElement activeElement;
    public static Texture uiPanelTex;
    public static Texture uiPanelBoundsTex;
    public static Texture uiPanelHoverBoundsTex;
    public static Texture uiPanelPressedTex;
    public static Sound onHoverSound;
    public static Sound onClickSound;
    String text;
    public float x;
    public float y;
    public float w;
    public float h;
    boolean shown;
    boolean hoveredOver;
    public HorizontalAnchor hAnchor = HorizontalAnchor.CENTERED;
    public VerticalAnchor vAnchor = VerticalAnchor.CENTERED;
    public static UIElement currentlyHeldElement;
    public boolean isHeld;
    protected Texture buttonTex = uiPanelTex;
    private final Vector2 tmpVec = new Vector2();

    public UIElement(float x, float y, float w, float h) {
        this(x, y, w, h, true);
    }

    public UIElement(float x, float y, float w, float h, boolean triggerOnCreate) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        if (triggerOnCreate) {
            this.onCreate();
        }
    }

    @SuppressWarnings("incomplete-switch")
	protected float getDisplayX(Viewport uiViewport) {
        switch (this.hAnchor) {
            case LEFT_ALIGNED: {
                return this.x - uiViewport.getWorldWidth() / 2.0f;
            }
            case RIGHT_ALIGNED: {
                return this.x + uiViewport.getWorldWidth() / 2.0f - this.w;
            }
        }
        return this.x - this.w / 2.0f;
    }

    @SuppressWarnings("incomplete-switch")
	protected float getDisplayY(Viewport uiViewport) {
        switch (this.vAnchor) {
            case TOP_ALIGNED: {
                return this.y - uiViewport.getWorldHeight() / 2.0f;
            }
            case BOTTOM_ALIGNED: {
                return this.y + uiViewport.getWorldHeight() / 2.0f - this.h;
            }
        }
        return this.y - this.h / 2.0f;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void show() {
        this.shown = true;
    }

    @Override
    public void hide() {
        this.shown = false;
    }

    public boolean isHoveredOver(Viewport viewport, float x, float y) {
        float dx = this.getDisplayX(viewport);
        float dy = this.getDisplayY(viewport);
        return x >= dx && y >= dy && x < dx + this.w && y < dy + this.h;
    }

    @Override
    public void drawBackground(Viewport uiViewport, SpriteBatch batch, float mouseX, float mouseY) {
        if (!this.shown) {
            return;
        }
        this.buttonTex = uiPanelTex;
        if (this == currentlyHeldElement && Gdx.input.isButtonPressed(0)) {
            this.isHeld = true;
        }
        if (this.isHeld && !Gdx.input.isButtonPressed(0)) {
            this.isHeld = false;
            this.onMouseUp();
        }
        if (this.isHoveredOver(uiViewport, mouseX, mouseY)) {
            if (!this.hoveredOver) {
                SoundManager.playSound(onHoverSound);
                this.hoveredOver = true;
            }
            if (Gdx.input.isButtonJustPressed(0)) {
                currentlyHeldElement = this;
                this.onMouseDown();
            }
            if (currentlyHeldElement == this) {
                this.buttonTex = uiPanelPressedTex;
            }
        } else {
            this.hoveredOver = false;
            if (currentlyHeldElement == this && !Gdx.input.isButtonPressed(0)) {
                currentlyHeldElement = null;
            }
        }
        this.drawElementBackground(uiViewport, batch);
        if (currentlyHeldElement == this && !Gdx.input.isButtonPressed(0)) {
            this.buttonTex = uiPanelPressedTex;
            currentlyHeldElement = null;
            this.onClick();
            SoundManager.playSound(onClickSound);
        }
    }

    public void drawElementBackground(Viewport uiViewport, SpriteBatch batch) {
        float x = this.getDisplayX(uiViewport);
        float y = this.getDisplayY(uiViewport);
        if (currentlyHeldElement == this || this.hoveredOver && currentlyHeldElement == null) {
            batch.draw(uiPanelHoverBoundsTex, x, y, 0.0f, 0.0f, this.w, this.h, 1.0f, 1.0f, 0.0f, 0, 0, this.buttonTex.getWidth(), this.buttonTex.getHeight(), false, true);
        } else {
            batch.draw(uiPanelBoundsTex, x, y, 0.0f, 0.0f, this.w, this.h, 1.0f, 1.0f, 0.0f, 0, 0, this.buttonTex.getWidth(), this.buttonTex.getHeight(), false, true);
        }
        batch.draw(this.buttonTex, x + 1.0f, y + 1.0f, 1.0f, 1.0f, this.w - 2.0f, this.h - 2.0f, 1.0f, 1.0f, 0.0f, 0, 0, this.buttonTex.getWidth(), this.buttonTex.getHeight(), false, true);
    }

    public void onCreate() {
    }

    public void onClick() {
        if (this != activeElement && activeElement != null) {
            activeElement.deactivate();
            activeElement = null;
        }
    }

    public void onMouseDown() {
    }

    public void onMouseUp() {
    }

    @Override
    public void drawText(Viewport uiViewport, SpriteBatch batch) {
        if (!this.shown || this.text == null || this.text.length() == 0) {
            return;
        }
        float x = this.getDisplayX(uiViewport);
        float y = this.getDisplayY(uiViewport);
        FontRenderer.getTextDimensions(uiViewport, this.text, this.tmpVec);
        if (this.tmpVec.x > this.w) {
            FontRenderer.drawTextbox(batch, uiViewport, this.text, x, y, this.w);
            return;
        }
        UIElement uiElement = this;
        float maxX = x;
        float maxY = y;
        block4: for (int i = 0; i < this.text.length(); ++i) {
            char c = this.text.charAt(i);
            FontTexture f = FontRenderer.getFontTexOfChar(c);
            if (f == null) {
                c = '?';
                f = FontRenderer.getFontTexOfChar(c);
            }
            TextureRegion texReg = f.getTexRegForChar(c);
            x -= f.getCharStartPos((char)c).x % (float)texReg.getRegionWidth();
            switch (c) {
                case '\n': {
                    x = uiElement.x;
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y += (float)texReg.getRegionHeight());
                    continue block4;
                }
                case ' ': {
                    maxX = Math.max(maxX, x += f.getCharSize((char)c).x / 4.0f);
                    continue block4;
                }
                default: {
                    maxX = Math.max(maxX, x += f.getCharSize((char)c).x + f.getCharStartPos((char)c).x % (float)texReg.getRegionWidth() + 2.0f);
                    maxY = Math.max(maxY, y + (float)texReg.getRegionHeight());
                }
            }
        }
        x = this.getDisplayX(uiViewport);
        y = this.getDisplayY(uiViewport);
        x += this.w / 2.0f - (maxX - x) / 2.0f;
        y += this.h / 2.0f - (maxY - y) / 2.0f;
        FontRenderer.drawText(batch, uiViewport, this.text, x, y);
    }

    @Override
    public void updateText() {
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
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
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public void deactivate() {
    }

    @Override
    public void setX(float x) {
        this.x = x;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }

    @Override
    public float getWidth() {
        return this.w;
    }

    @Override
    public float getHeight() {
        return this.h;
    }

    static {
        uiPanelTex = new Texture(GameAssetLoader.loadAsset("textures/ui-panel.png"));
        uiPanelBoundsTex = new Texture(GameAssetLoader.loadAsset("textures/ui-panel-boundary.png"));
        uiPanelHoverBoundsTex = new Texture(GameAssetLoader.loadAsset("textures/ui-panel-hover-boundary.png"));
        uiPanelPressedTex = new Texture(GameAssetLoader.loadAsset("textures/ui-panel-pressed.png"));
        onHoverSound = Gdx.audio.newSound(GameAssetLoader.loadAsset("sounds/ui/e-button-hover.ogg"));
        onClickSound = Gdx.audio.newSound(GameAssetLoader.loadAsset("sounds/ui/e-button-click.ogg"));
    }
}