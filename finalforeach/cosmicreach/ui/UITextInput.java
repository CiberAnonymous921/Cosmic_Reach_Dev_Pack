package finalforeach.cosmicreach.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.Viewport;

import finalforeach.cosmicreach.settings.Keybind;

public abstract class UITextInput extends UIElement {
    public String labelPrefix;
    public String inputText = this.getDefaultInputText();
    InputProcessor inputProcessor;
    boolean isDefaultText = true;
    int desiredCharIdx = Integer.MAX_VALUE;

    public UITextInput(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.updateText();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        if (activeElement == this) {
            activeElement = null;
            Gdx.input.setInputProcessor(this.inputProcessor);
            this.inputProcessor = null;
            if ("".equals(this.inputText)) {
                this.inputText = this.getDefaultInputText();
            }
            this.updateText();
        }
    }

    public String getDefaultInputText() {
        return "";
    }

    @Override
    public void onClick() {
        super.onClick();
        if (activeElement == this) {
            this.deactivate();
        } else {
            if (activeElement != null) {
                activeElement.deactivate();
            }
            activeElement = this;
            this.inputProcessor = Gdx.input.getInputProcessor();
            Gdx.input.setInputProcessor(this);
        }
        this.updateText();
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case 21: {
                --this.desiredCharIdx;
                this.isDefaultText = false;
                return true;
            }
            case 22: {
                ++this.desiredCharIdx;
                this.isDefaultText = false;
                return true;
            }
            case 3: {
                this.desiredCharIdx = 0;
                this.isDefaultText = false;
                return true;
            }
            case 123: {
                this.desiredCharIdx = Integer.MAX_VALUE;
                this.isDefaultText = false;
                return true;
            }
            case 66: {
                this.deactivate();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        switch (character) {
            case '\b': {
                if (this.inputText.length() > 0) {
                    this.inputText = this.inputText.substring(0, this.inputText.length() - 1);
                    this.isDefaultText = false;
                    this.updateText();
                }
                return true;
            }
            case '\u007f': {
                if (this.desiredCharIdx < this.inputText.length()) {
                    String worldNameA = this.inputText.substring(0, this.desiredCharIdx);
                    String worldNameB = this.inputText.substring(this.desiredCharIdx + 1);
                    this.inputText = worldNameA + worldNameB;
                }
                return true;
            }
            case '\n': {
                return false;
            }
        }
        if (!Keybind.isPrintableChar(character)) {
            return false;
        }
        if (this.isDefaultText) {
            this.inputText = "";
        }
        this.isDefaultText = false;
        this.desiredCharIdx = MathUtils.clamp(this.desiredCharIdx, 0, this.inputText.length());
        this.inputText = new StringBuilder(this.inputText).insert(this.desiredCharIdx, character).toString();
        ++this.desiredCharIdx;
        this.updateText();
        return true;
    }

    @Override
    public void updateText() {
        long msec = System.currentTimeMillis();
        String text = this.inputText;
        if (this == activeElement && msec % 1500L > 750L) {
            this.desiredCharIdx = MathUtils.clamp(this.desiredCharIdx, 0, text.length());
            text = new StringBuilder(text).insert(this.desiredCharIdx, '|').toString();
        }
        this.setText(this.labelPrefix + text);
    }

    @Override
    public void drawText(Viewport uiViewport, SpriteBatch batch) {
        this.updateText();
        super.drawText(uiViewport, batch);
    }
}