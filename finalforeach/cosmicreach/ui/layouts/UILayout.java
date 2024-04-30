package finalforeach.cosmicreach.ui.layouts;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import finalforeach.cosmicreach.ui.UIObject;

public abstract class UILayout implements UIObject {
    protected Array<UIObject> uiObjects = new Array<UIObject>();
    float x;
    float y;
    float w;
    float h;
    private boolean shown;
    float padding = 4.0f;
    float margin = 4.0f;
    boolean flagForRestructure = true;

    public UILayout(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void add(UIObject o) {
        this.uiObjects.add(o);
        this.flagForRestructure = true;
    }

    @Override
    public void show() {
        this.shown = true;
    }

    @Override
    public void hide() {
        this.shown = false;
    }

    @Override
    public void deactivate() {
        for (UIObject o : this.uiObjects) {
            o.deactivate();
        }
    }

    public void flagForRestructure() {
        this.flagForRestructure = true;
    }

    public abstract void restructure();

    @Override
    public void drawBackground(Viewport uiViewport, SpriteBatch batch, float x, float y) {
        if (!this.shown) {
            return;
        }
        if (this.flagForRestructure) {
            this.restructure();
        }
        for (int i = 0; i < this.uiObjects.size; ++i) {
            this.uiObjects.get(i).drawBackground(uiViewport, batch, x, y);
        }
    }

    @Override
    public void drawText(Viewport uiViewport, SpriteBatch batch) {
        if (!this.shown) {
            return;
        }
        if (this.flagForRestructure) {
            this.restructure();
        }
        for (int i = 0; i < this.uiObjects.size; ++i) {
            this.uiObjects.get(i).drawText(uiViewport, batch);
        }
    }

    @Override
    public void updateText() {
        for (int i = 0; i < this.uiObjects.size; ++i) {
            this.uiObjects.get(i).updateText();
        }
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
    public void setX(float x) {
        this.flagForRestructure |= this.x != x;
        this.x = x;
    }

    @Override
    public void setY(float y) {
        this.flagForRestructure |= this.y != y;
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

    public void setWidth(float width) {
        this.flagForRestructure |= this.w != width;
        this.w = width;
    }

    public void setHeight(float height) {
        this.flagForRestructure |= this.h != height;
        this.h = height;
    }
}