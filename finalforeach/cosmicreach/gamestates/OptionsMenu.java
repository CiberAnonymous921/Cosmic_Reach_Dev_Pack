package finalforeach.cosmicreach.gamestates;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.settings.ControlSettings;
import finalforeach.cosmicreach.settings.GraphicsSettings;
import finalforeach.cosmicreach.settings.SoundSettings;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.ToggleButton;
import finalforeach.cosmicreach.ui.UIElement;
import finalforeach.cosmicreach.ui.UISlider;
import finalforeach.cosmicreach.ui.VerticalAnchor;

public class OptionsMenu extends GameState {
    private final String vSyncButtonStr = Lang.get("vSyncButton");
    private final String invertedMouseButtonStr = Lang.get("invertedMouseButton");
    private final String soundButtonStr = Lang.get("soundButton");
    private final String renderDistStr = Lang.get("renderDistSlider");
    private final String mouseSensitivitySliderStr = Lang.get("mouseSensitivitySlider");
    private final String fovSliderStr = Lang.get("fovSlider");
    private final String maxFPSStr = Lang.get("maxFPS");
    private final String maxFPSStrUnlimited = Lang.get("maxFPSUnlimited");

    public OptionsMenu(GameState previousState) {
        this.previousState = previousState;
        final OptionsMenu thisState = this;
        ToggleButton vSyncButton = new ToggleButton(this.vSyncButtonStr, GraphicsSettings.vSyncEnabled, -137.0f, -200.0f, 250.0f, 50.0f){

            @Override
            public void updateValue() {
                Gdx.graphics.setVSync(this.getValue());
            }
        };
        vSyncButton.show();
        this.uiObjects.add(vSyncButton);
        UISlider renderDistSlider = new UISlider(6.0f, 96.0f, GraphicsSettings.renderDistanceInChunks.getValue(), 137.0f, -200.0f, 250.0f, 50.0f){

            @Override
            public void onCreate() {
                super.onCreate();
                this.updateText();
            }

            @Override
            public void onMouseUp() {
                super.onMouseUp();
                GraphicsSettings.renderDistanceInChunks.setValue((int)this.currentValue);
                this.updateText();
            }

            @Override
            public void validate() {
                super.validate();
                this.currentValue = (int)this.currentValue;
                this.updateText();
            }

            @Override
            public void updateText() {
                this.setText(OptionsMenu.this.renderDistStr + (int)this.currentValue);
            }
        };
        renderDistSlider.show();
        this.uiObjects.add(renderDistSlider);
        ToggleButton invertedMouseButton = new ToggleButton(this.invertedMouseButtonStr, ControlSettings.invertedMouse, -137.0f, -125.0f, 250.0f, 50.0f);
        invertedMouseButton.show();
        this.uiObjects.add(invertedMouseButton);
        UISlider mouseSensitivitySlider = new UISlider(0.01f, 5.0f, ControlSettings.mouseSensitivity.getValue(), 137.0f, -125.0f, 250.0f, 50.0f){
            NumberFormat format;

            @Override
            public void onCreate() {
                super.onCreate();
                this.updateText();
            }

            @Override
            public void onMouseUp() {
                super.onMouseUp();
                ControlSettings.mouseSensitivity.setValue(this.currentValue);
                this.updateText();
            }

            @Override
            public void validate() {
                super.validate();
                this.updateText();
            }

            @Override
            public void updateText() {
                if (this.format == null) {
                    this.format = DecimalFormat.getPercentInstance();
                }
                this.setText(OptionsMenu.this.mouseSensitivitySliderStr + this.format.format(this.currentValue));
            }
        };
        mouseSensitivitySlider.show();
        this.uiObjects.add(mouseSensitivitySlider);
        ToggleButton soundButton = new ToggleButton(this.soundButtonStr, SoundSettings.soundEnabled, -137.0f, -50.0f, 250.0f, 50.0f);
        soundButton.show();
        this.uiObjects.add(soundButton);
        UISlider fovSlider = new UISlider(10.0f, 150.0f, GraphicsSettings.fieldOfView.getValue(), 137.0f, -50.0f, 250.0f, 50.0f){

            @Override
            public void onCreate() {
                super.onCreate();
                this.updateText();
            }

            @Override
            public void onMouseUp() {
                super.onMouseUp();
                GraphicsSettings.fieldOfView.setValue(this.currentValue);
                this.updateText();
            }

            @Override
            public void validate() {
                super.validate();
                this.currentValue = (int)this.currentValue;
                this.updateText();
            }

            @Override
            public void updateText() {
                this.setText(OptionsMenu.this.fovSliderStr + this.currentValue);
            }
        };
        fovSlider.show();
        this.uiObjects.add(fovSlider);
        UISlider maxFPSSlider = new UISlider(10.0f, 250.0f, GraphicsSettings.maxFPS.getValue(), 137.0f, 25.0f, 250.0f, 50.0f){

            @Override
            public void onCreate() {
                super.onCreate();
                if (this.currentValue == 0.0f) {
                    this.currentValue = 250.0f;
                }
                this.updateText();
            }

            @Override
            public void onMouseUp() {
                super.onMouseUp();
                int fps = (int)this.currentValue;
                if (fps == 250) {
                    fps = 0;
                }
                GraphicsSettings.maxFPS.setValue(fps);
                Gdx.graphics.setForegroundFPS(fps);
                this.updateText();
            }

            @Override
            public void validate() {
                super.validate();
                this.currentValue = 10 * ((int)this.currentValue / 10);
                this.updateText();
            }

            @Override
            public void updateText() {
                int fps = (int)this.currentValue;
                if (fps != 250 && fps != 0) {
                    this.setText(OptionsMenu.this.maxFPSStr + fps);
                } else {
                    this.setText(OptionsMenu.this.maxFPSStrUnlimited);
                }
            }
        };
        maxFPSSlider.show();
        this.uiObjects.add(maxFPSSlider);
        UIElement langButton = new UIElement(-137.0f, 100.0f, 250.0f, 50.0f){

            @Override
            public void onClick() {
                super.onClick();
                GameState.switchToGameState(new LanguagesMenu(thisState));
            }
        };
        langButton.setText(Lang.get("languagesButton"));
        langButton.show();
        this.uiObjects.add(langButton);
        UIElement keybindsButton = new UIElement(137.0f, 100.0f, 250.0f, 50.0f){

            @Override
            public void onClick() {
                super.onClick();
                GameState.switchToGameState(new KeybindsMenu(thisState));
            }
        };
        keybindsButton.setText(Lang.get("keybindsButton"));
        keybindsButton.show();
        this.uiObjects.add(keybindsButton);
        UIElement doneButton = new UIElement(0.0f, 200.0f, 250.0f, 50.0f){

            @Override
            public void onClick() {
                super.onClick();
                OptionsMenu.this.returnToPrevious();
            }
        };
        doneButton.setText(Lang.get("doneButton"));
        doneButton.show();
        this.uiObjects.add(doneButton);
        UIElement crashMeButton = new UIElement(-5.0f, -5.0f, 250.0f, 50.0f){

            @Override
            public void onClick() {
                super.onClick();
                throw new RuntimeException("Caused a manual crash, not a bug!");
            }
        };
        crashMeButton.setText(Lang.get("debugCrashButton"));
        crashMeButton.vAnchor = VerticalAnchor.BOTTOM_ALIGNED;
        crashMeButton.hAnchor = HorizontalAnchor.RIGHT_ALIGNED;
        crashMeButton.show();
        this.uiObjects.add(crashMeButton);
    }

    private void returnToPrevious() {
        if (this.previousState instanceof MainMenu) {
            OptionsMenu.switchToGameState(new MainMenu());
        } else if (this.previousState instanceof PauseMenu) {
            OptionsMenu.switchToGameState(new PauseMenu(((PauseMenu)this.previousState).cursorCaught));
        } else {
            OptionsMenu.switchToGameState(this.previousState);
        }
    }

    @Override
    public void render(float partTick) {
        super.render(partTick);
        if (Gdx.input.isKeyJustPressed(111)) {
            this.returnToPrevious();
        }
        ScreenUtils.clear(0.145f, 0.078f, 0.153f, 1.0f, true);
        Gdx.gl.glEnable(2929);
        Gdx.gl.glDepthFunc(513);
        Gdx.gl.glEnable(2884);
        Gdx.gl.glCullFace(1029);
        Gdx.gl.glEnable(3042);
        Gdx.gl.glBlendFunc(770, 771);
        this.drawUIElements();
    }
}