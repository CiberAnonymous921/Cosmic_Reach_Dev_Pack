package finalforeach.cosmicreach.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.Viewport;

public class UISlider extends UIElement {
    private float min;
    private float max;
    protected float currentValue;

    public UISlider(float min, float max, float defaultVal, float x, float y, float w, float h) {
        super(x, y, w, h, false);
        this.min = min;
        this.max = max;
        this.currentValue = defaultVal;
        this.updateText();
        this.onCreate();
    }

    @Override
    public void onClick() {
        super.onClick();
    }

    @Override
    public void onMouseDown() {
        super.onMouseDown();
    }

    @Override
    public void onMouseUp() {
        super.onMouseUp();
        this.validate();
    }

    public void validate() {
        this.currentValue = MathUtils.clamp(this.currentValue, this.min, this.max);
    }

    @Override
    public void drawBackground(Viewport uiViewport, SpriteBatch batch, float mouseX, float mouseY) {
        super.drawBackground(uiViewport, batch, mouseX, mouseY);
        if (this.isHeld) {
            float sx = mouseX;
            float x = this.getDisplayX(uiViewport);
            float ratio = (sx - x) / this.w;
            this.currentValue = this.min + ratio * (this.max - this.min);
            this.validate();
        }
        this.buttonTex = UIElement.uiPanelTex;
        this.drawKnobBackground(uiViewport, batch);
    }

    public void drawKnobBackground(Viewport uiViewport, SpriteBatch batch) {
        super.drawElementBackground(uiViewport, batch);
        float x = this.getDisplayX(uiViewport);
        float y = this.getDisplayY(uiViewport);
        float ratio = (this.currentValue - this.min) / (this.max - this.min);
        float knobW = 10.0f;
        float knobH = this.h + 8.0f;
        float knobX = x + ratio * this.w - knobW / 2.0f;
        float knobY = y - 4.0f;
        batch.draw(uiPanelHoverBoundsTex, knobX, knobY, 1.0f, 1.0f, knobW, knobH, 1.0f, 1.0f, 0.0f, 0, 0, this.buttonTex.getWidth(), this.buttonTex.getHeight(), false, true);
        batch.draw(uiPanelTex, knobX + 1.0f, knobY + 1.0f, 1.0f, 1.0f, knobW - 2.0f, knobH - 2.0f, 1.0f, 1.0f, 0.0f, 0, 0, this.buttonTex.getWidth(), this.buttonTex.getHeight(), false, true);
    }
}