package finalforeach.cosmicreach.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.ScreenUtils;

import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.settings.ControlSettings;
import finalforeach.cosmicreach.settings.Keybind;
import finalforeach.cosmicreach.ui.UIElement;
import finalforeach.cosmicreach.ui.VerticalAnchor;

public class KeybindsMenu extends GameState {
    private static KeybindButton activeKeybindButton;
    private boolean keybindJustSet;
    int ix = 0;
    int iy = 0;

    private void addKeybindButton(String label, Keybind keybind) {
        KeybindButton keybindButton = new KeybindButton(label, keybind, 275.0f * ((float)this.iy - 1.0f), 50 + 75 * this.ix, 250.0f, 50.0f);
        keybindButton.updateText();
        keybindButton.vAnchor = VerticalAnchor.TOP_ALIGNED;
        keybindButton.show();
        this.uiObjects.add(keybindButton);
        ++this.ix;
        if (this.ix > 5) {
            this.ix = 0;
            ++this.iy;
        }
    }

    public KeybindsMenu(final GameState previousState) {
        this.previousState = previousState;
        this.addKeybindButton(Lang.get("Forward"), ControlSettings.keyForward);
        this.addKeybindButton(Lang.get("Backward"), ControlSettings.keyBackward);
        this.addKeybindButton(Lang.get("Left"), ControlSettings.keyLeft);
        this.addKeybindButton(Lang.get("Right"), ControlSettings.keyRight);
        this.addKeybindButton(Lang.get("Jump"), ControlSettings.keyJump);
        this.addKeybindButton(Lang.get("Crouch"), ControlSettings.keyCrouch);
        this.addKeybindButton(Lang.get("Sprint"), ControlSettings.keySprint);
        this.addKeybindButton(Lang.get("Prone"), ControlSettings.keyProne);
        this.addKeybindButton(Lang.get("Inventory"), ControlSettings.keyInventory);
        this.addKeybindButton(Lang.get("Drop_Item"), ControlSettings.keyDropItem);
        this.addKeybindButton(Lang.get("Hide_UI"), ControlSettings.keyHideUI);
        this.addKeybindButton(Lang.get("Screenshot"), ControlSettings.keyScreenshot);
        this.addKeybindButton(Lang.get("Debug_Info"), ControlSettings.keyDebugInfo);
        this.addKeybindButton(Lang.get("No_Clip"), ControlSettings.keyDebugNoClip);
        this.addKeybindButton(Lang.get("Reload_Shaders"), ControlSettings.keyDebugReloadShaders);
        this.addKeybindButton(Lang.get("Fullscreen"), ControlSettings.keyFullscreen);
        UIElement doneButton = new UIElement(0.0f, -50.0f, 250.0f, 50.0f){

            @Override
            public void onClick() {
                super.onClick();
                GameState.switchToGameState(previousState);
            }
        };
        doneButton.vAnchor = VerticalAnchor.BOTTOM_ALIGNED;
        doneButton.setText(Lang.get("doneButton"));
        doneButton.show();
        this.uiObjects.add(doneButton);
    }

    @Override
    public void render(float partTick) {
        super.render(partTick);
        if (Gdx.input.isKeyJustPressed(111) && !this.keybindJustSet) {
            KeybindsMenu.switchToGameState(this.previousState);
        }
        if (this.keybindJustSet) {
            this.keybindJustSet = false;
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

    class KeybindButton extends UIElement {
        InputProcessor inputProcessor;
        final Keybind keybind;
        String label;

        public KeybindButton(String label, Keybind keybind, float x, float y, float w, float h) {
            super(x, y, w, h);
            this.keybind = keybind;
            this.label = label;
        }

        @Override
        public void onCreate() {
            super.onCreate();
        }

        @Override
        public void deactivate() {
            super.deactivate();
            if (this == activeKeybindButton) {
                activeKeybindButton = null;
                KeybindsMenu.this.keybindJustSet = true;
                this.updateText();
                Gdx.input.setInputProcessor(this.inputProcessor);
                this.inputProcessor = null;
            }
        }

        @Override
        public void onClick() {
            super.onClick();
            if (activeKeybindButton == this) {
                this.deactivate();
            } else {
                if (activeKeybindButton != null) {
                    activeKeybindButton.deactivate();
                }
                activeKeybindButton = this;
                KeybindsMenu.this.keybindJustSet = true;
                this.inputProcessor = Gdx.input.getInputProcessor();
                Gdx.input.setInputProcessor(this);
            }
            this.updateText();
        }

        @Override
        public boolean keyDown(int keycode) {
            if (this != activeKeybindButton) {
                return false;
            }
            this.deactivate();
            if (keycode != 111) {
                this.keybind.setValue(keycode);
                String qwertyKeyName = Input.Keys.toString(keycode);
                if (qwertyKeyName.length() > 1 || !Keybind.isPrintableChar(qwertyKeyName.charAt(0))) {
                    this.keybind.setDisplayString('\u0000');
                }
            }
            this.updateText();
            return true;
        }

        @Override
        public boolean keyTyped(char character) {
            this.keybind.setDisplayString(character);
            this.updateText();
            return true;
        }

        @Override
        public void updateText() {
            int key = this.keybind.getValue();
            Object keyStr = "[" + this.keybind.getKeyName(key) + "]";
            if (activeKeybindButton == this) {
                keyStr = "[???]";
            }
            this.setText(this.label + ": " + (String)keyStr);
        }
    }

    public static interface SetKeyMappingAction {
        public void setKey(int var1);
    }

    public static interface GetKeyMappingAction {
        public int getKey();

        public String getKeyName(int var1);
    }
}
