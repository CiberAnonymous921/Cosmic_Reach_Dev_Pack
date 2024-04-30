package finalforeach.cosmicreach.ui;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;

public interface UIObject extends InputProcessor {
    public void drawBackground(Viewport var1, SpriteBatch var2, float var3, float var4);

    public void drawText(Viewport var1, SpriteBatch var2);

    public void updateText();

    public void show();

    public void hide();

    public void deactivate();

    public void setX(float var1);

    public void setY(float var1);

    public float getWidth();

    public float getHeight();
}
