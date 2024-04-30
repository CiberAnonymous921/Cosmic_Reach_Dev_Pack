package finalforeach.cosmicreach.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;

import finalforeach.cosmicreach.io.ChunkSaver;
import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.ui.FontRenderer;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.UIElement;
import finalforeach.cosmicreach.ui.VerticalAnchor;
import finalforeach.cosmicreach.world.Sky;
import finalforeach.cosmicreach.world.WorldLoader;

public class PauseMenu extends GameState {
    boolean cursorCaught;

    public PauseMenu(boolean cursorCaught) {
        this.cursorCaught = cursorCaught;
    }

    @Override
    public void create() {
        super.create();
        WorldLoader.worldLoader.requestSave();
        final PauseMenu thisState = this;
        final SkyLightingSetting[] lightingChoices = new SkyLightingSetting[]{new SkyLightingSetting(Lang.get("Space_Day"), Color.BLACK, new Color(0.75f, 0.75f, 0.75f, 1.0f), true), new SkyLightingSetting(Lang.get("Dark_Night"), Color.BLACK, new Color(0.1f, 0.1f, 0.2f, 1.0f), true), new SkyLightingSetting(Lang.get("Earth_Day"), new Color(0.3f, 0.6f, 0.8f, 1.0f), new Color(0.75f, 0.75f, 0.75f, 1.0f), false), new SkyLightingSetting(Lang.get("Orange_Sky"), new Color(0.9f, 0.4f, 0.0f, 1.0f), new Color(0.9f, 0.6f, 0.3f, 1.0f), false), new SkyLightingSetting(Lang.get("Pitch_Dark"), Color.BLACK, Color.BLACK, false), new SkyLightingSetting(Lang.get("Overcast"), new Color(0.75f, 0.75f, 0.75f, 1.0f).mul(0.2f), new Color(0.75f, 0.75f, 0.75f, 1.0f).mul(0.75f), false)};
        UIElement skyColorButton = new UIElement(0.0f, -200.0f, 250.0f, 50.0f){
            static int lightingIndex = 0;

            @Override
            public void onCreate() {
                super.onCreate();
                this.updateText();
            }

            @Override
            public void onClick() {
                super.onClick();
                ++lightingIndex;
                SkyLightingSetting l = lightingChoices[lightingIndex %= lightingChoices.length];
                Sky.ambientColor = l.ambientColor;
                Sky.skyColor = l.skyColor;
                Sky.shouldDrawStars = l.shouldDrawStars;
                this.updateText();
            }

            @Override
            public void updateText() {
                this.setText(Lang.get("Lighting") + lightingChoices[lightingIndex].name);
            }
        };
        skyColorButton.show();
        this.uiObjects.add(skyColorButton);
        UIElement respawnButton = new UIElement(0.0f, -100.0f, 250.0f, 50.0f){

            @Override
            public void onClick() {
                super.onClick();
                InGame.getLocalPlayer().respawn(InGame.world);
            }
        };
        respawnButton.setText(Lang.get("Respawn"));
        respawnButton.show();
        this.uiObjects.add(respawnButton);
        UIElement returnToGameButton = new UIElement(0.0f, 0.0f, 250.0f, 50.0f){

            @Override
            public void onClick() {
                super.onClick();
                GameState.switchToGameState(GameState.IN_GAME);
                Gdx.input.setCursorCatched(PauseMenu.this.cursorCaught);
            }
        };
        returnToGameButton.setText(Lang.get("Return_To_Game"));
        returnToGameButton.show();
        this.uiObjects.add(returnToGameButton);
        UIElement optionsButton = new UIElement(0.0f, 100.0f, 250.0f, 50.0f){

            @Override
            public void onClick() {
                super.onClick();
                GameState.switchToGameState(new OptionsMenu(thisState));
            }
        };
        optionsButton.setText(Lang.get("optionsButton"));
        optionsButton.show();
        this.uiObjects.add(optionsButton);
        UIElement quitButton = new UIElement(0.0f, 200.0f, 250.0f, 50.0f){

            @Override
            public void onClick() {
                super.onClick();
                ChunkSaver.saveWorld(InGame.world);
                GameState.IN_GAME.unloadWorld();
                GameState.switchToGameState(new MainMenu());
            }
        };
        quitButton.setText(Lang.get("Return_to_Main_Menu"));
        quitButton.show();
        this.uiObjects.add(quitButton);
        System.gc();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        IN_GAME.resize(width, height);
    }

    @Override
    public void render(float partTick) {
        super.render(partTick);
        if (!this.firstFrame && Gdx.input.isKeyJustPressed(111)) {
            PauseMenu.switchToGameState(IN_GAME);
        }
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1.0f, true);
        Gdx.gl.glEnable(2929);
        Gdx.gl.glDepthFunc(513);
        Gdx.gl.glEnable(2884);
        Gdx.gl.glCullFace(1029);
        Gdx.gl.glEnable(3042);
        Gdx.gl.glBlendFunc(770, 771);
        IN_GAME.render(partTick);
        if (ChunkSaver.isSaving) {
            String savingText = Lang.get("Saving");
            batch.begin();
            FontRenderer.drawText(batch, this.uiViewport, savingText, 8.0f, -8.0f, HorizontalAnchor.LEFT_ALIGNED, VerticalAnchor.BOTTOM_ALIGNED);
            batch.end();
        }
        this.drawUIElements();
    }

    class SkyLightingSetting {
        String name;
        Color skyColor;
        Color ambientColor;
        boolean shouldDrawStars;

        public SkyLightingSetting(String name, Color skyColor, Color ambientColor, boolean shouldDrawStars) {
            this.name = name;
            this.skyColor = skyColor;
            this.ambientColor = ambientColor;
            this.shouldDrawStars = shouldDrawStars;
        }
    }
}