package finalforeach.cosmicreach.settings;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class Keybind extends IntSetting {
    public static HashMap<String, Keybind> allKeybinds = new HashMap<String, Keybind>();
    public static final Keybind MISSINGKEYBIND = new Keybind("MISSINGKEYBIND", -1){

        @Override
        public String getKeyName() {
            return "MISSINGKEYBIND";
        }
    };
    String key;
    CharSetting charSetting;

    public Keybind(String key, int defaultValue) {
        super("keybind_" + key, defaultValue);
        allKeybinds.put(key, this);
        this.key = key;
        this.charSetting = new CharSetting("keybindDisplay_" + key, '\u0000');
    }

    public boolean isPressed() {
        return Gdx.input.isKeyPressed(this.getValue());
    }

    public boolean isJustPressed() {
        return Gdx.input.isKeyJustPressed(this.getValue());
    }

    public String getKeyName() {
        return Input.Keys.toString(this.getValue());
    }

    public String getKeyName(int key) {
        char c = this.charSetting.getValue();
        if (c != '\u0000') {
            return ("" + c).toUpperCase();
        }
        return Input.Keys.toString(key);
    }

    public void setDisplayString(char character) {
        if (Keybind.isPrintableChar(character)) {
            this.charSetting.setValue(character);
        } else {
            this.charSetting.setValue('\u0000');
        }
    }

    public static boolean isPrintableChar(char c) {
        Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(c);
        return !Character.isISOControl(c) && c != '\uffff' && unicodeBlock != null && unicodeBlock != Character.UnicodeBlock.SPECIALS;
    }

    public char getDisplayChar() {
        return this.charSetting.getValue();
    }
}