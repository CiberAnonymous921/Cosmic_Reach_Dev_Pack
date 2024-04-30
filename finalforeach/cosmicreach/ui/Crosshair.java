package finalforeach.cosmicreach.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Crosshair {
    public static Texture crosshairTex;
    private static SpriteBatch batch;
    private static Viewport viewport;

    public Crosshair() {
        if (crosshairTex == null) {
            crosshairTex = new Texture("textures/crosshair.png");
            batch = new SpriteBatch(16);
            batch.setBlendFunction(775, 770);
            batch.enableBlending();
            viewport = new ScreenViewport();
        }
    }

    public void render(Camera uiCamera) {
        viewport.setCamera(uiCamera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport.apply();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        crosshairTex.bind(0);
        float cx = -crosshairTex.getWidth() / 2;
        float cy = -crosshairTex.getHeight() / 2;
        batch.draw(crosshairTex, cx, cy);
        batch.end();
    }
}