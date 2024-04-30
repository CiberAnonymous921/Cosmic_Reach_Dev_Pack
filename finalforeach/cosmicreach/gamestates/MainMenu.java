package finalforeach.cosmicreach.gamestates;

import java.io.File;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.RuntimeInfo;
import finalforeach.cosmicreach.io.SaveLocation;
import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.settings.Controls;
import finalforeach.cosmicreach.ui.FontRenderer;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.UIElement;
import finalforeach.cosmicreach.ui.VerticalAnchor;
import finalforeach.cosmicreach.world.WorldLoader;

public class MainMenu extends GameState {
    public static Texture textLogo = new Texture(GameAssetLoader.loadAsset("textures/text-logo-hd.png"));

    @Override
    public void create() {
        super.create();
        final MainMenu thisState = this;
        WorldLoader.worldLoader.readyToPlay = false;
        UIElement startButton = new UIElement(-150.0f, 50.0f, 250.0f, 50.0f){

            @Override
            public void onClick() {
                super.onClick();
                GameState.switchToGameState(new WorldSelectionMenu());
            }
        };
        startButton.setText(Lang.get("startButton"));
        startButton.show();
        this.uiObjects.add(startButton);
        UIElement loadButton = new UIElement(150.0f, 50.0f, 250.0f, 50.0f){

            @Override
            public void onClick() {
                super.onClick();
                try {
                    File saveFolder = SaveLocation.getSaveFolder();
                    SaveLocation.OpenFolderWithFileManager(saveFolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        loadButton.setText(Lang.get("loadButton"));
        loadButton.show();
        this.uiObjects.add(loadButton);
        UIElement optionsButton = new UIElement(-150.0f, 150.0f, 250.0f, 50.0f){

            @Override
            public void onClick() {
                super.onClick();
                GameState.switchToGameState(new OptionsMenu(thisState));
            }
        };
        optionsButton.setText(Lang.get("optionsButton"));
        optionsButton.show();
        this.uiObjects.add(optionsButton);
        UIElement quitButton = new UIElement(150.0f, 150.0f, 250.0f, 50.0f){

            @Override
            public void onClick() {
                super.onClick();
                System.exit(0);
            }
        };
        quitButton.setText(Lang.get("quitButton"));
        quitButton.show();
        this.uiObjects.add(quitButton);
    }

    @Override
    public void render(float partTick) {
        super.render(partTick);
        ScreenUtils.clear(0.0f, 0.0f, 0.0f, 1.0f, true);
        Gdx.gl.glEnable(2929);
        Gdx.gl.glDepthFunc(513);
        Gdx.gl.glEnable(2884);
        Gdx.gl.glCullFace(1029);
        Gdx.gl.glEnable(3042);
        Gdx.gl.glBlendFunc(770, 771);
        batch.setProjectionMatrix(this.uiCamera.combined);
        batch.begin();
        float scale = 4.0f;
        float logoW = 192.0f;
        float logoH = 64.0f;
        float logoX = -scale * logoW / 2.0f;
        float logoY = -this.uiViewport.getWorldHeight() / 2.0f;
        batch.draw(textLogo, logoX, logoY, 0.0f, 0.0f, logoW, logoH, scale, scale, 0.0f, 0, 0, textLogo.getWidth(), textLogo.getHeight(), false, true);
        Vector2 promoTextDim = new Vector2();
        float y = -8.0f;
        Object promoText = Lang.get("YT_Channel");
        FontRenderer.getTextDimensions(this.uiViewport, (String)promoText, promoTextDim);
        batch.setColor(Color.GRAY);
        FontRenderer.drawText(batch, this.uiViewport, (String)promoText, -7.0f, y + 1.0f, HorizontalAnchor.RIGHT_ALIGNED, VerticalAnchor.BOTTOM_ALIGNED);
        batch.setColor(Color.WHITE);
        FontRenderer.drawText(batch, this.uiViewport, (String)promoText, -8.0f, y, HorizontalAnchor.RIGHT_ALIGNED, VerticalAnchor.BOTTOM_ALIGNED);
        if (Controls.controllers.size > 0) {
            String controllerNoun = Controls.controllers.size == 1 ? Lang.get("Controller") : Lang.get("Controllers");
            String controllerWarning = controllerNoun + Lang.get("Controller_info");
            FontRenderer.drawText(batch, this.uiViewport, controllerWarning, 8.0f, y, HorizontalAnchor.LEFT_ALIGNED, VerticalAnchor.BOTTOM_ALIGNED);
        }
        if (RuntimeInfo.isMac) {
            String macWarning = Lang.get("MAC_warning");
            FontRenderer.drawText(batch, this.uiViewport, macWarning, 8.0f, y, HorizontalAnchor.CENTERED, VerticalAnchor.CENTERED);
        }
        promoText = "finalforeach.com";
        FontRenderer.getTextDimensions(this.uiViewport, (String)promoText, promoTextDim);
        batch.setColor(Color.GRAY);
        FontRenderer.drawText(batch, this.uiViewport, (String)promoText, -7.0f, (y -= promoTextDim.y + 2.0f) + 1.0f, HorizontalAnchor.RIGHT_ALIGNED, VerticalAnchor.BOTTOM_ALIGNED);
        batch.setColor(Color.WHITE);
        FontRenderer.drawText(batch, this.uiViewport, (String)promoText, -8.0f, y, HorizontalAnchor.RIGHT_ALIGNED, VerticalAnchor.BOTTOM_ALIGNED);
        promoText = Lang.get("Game_version") + RuntimeInfo.version;
        FontRenderer.getTextDimensions(this.uiViewport, (String)promoText, promoTextDim);
        batch.setColor(Color.GRAY);
        FontRenderer.drawText(batch, this.uiViewport, (String)promoText, -7.0f, (y -= promoTextDim.y) + 1.0f, HorizontalAnchor.RIGHT_ALIGNED, VerticalAnchor.BOTTOM_ALIGNED);
        batch.setColor(Color.WHITE);
        FontRenderer.drawText(batch, this.uiViewport, (String)promoText, -8.0f, y, HorizontalAnchor.RIGHT_ALIGNED, VerticalAnchor.BOTTOM_ALIGNED);
        y -= promoTextDim.y;
        batch.end();
        this.drawUIElements();
    }
}