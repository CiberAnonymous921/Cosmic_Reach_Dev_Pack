package finalforeach.cosmicreach.settings;

public class ControlSettings {
    public static final BooleanSetting invertedMouse = new BooleanSetting("invertMouse", false);
    public static final FloatSetting mouseSensitivity = new FloatSetting("mouseSensitivity", 1);
    public static final Keybind keyForward = new Keybind("forward", 51);
    public static final Keybind keyBackward = new Keybind("backward", 47);
    public static final Keybind keyLeft = new Keybind("left", 29);
    public static final Keybind keyRight = new Keybind("right", 32);
    public static final Keybind keyJump = new Keybind("jump", 62);
    public static final Keybind keyCrouch = new Keybind("crouch", 59);
    public static final Keybind keySprint = new Keybind("sprint", 129);
    public static final Keybind keyProne = new Keybind("prone", 54);
    public static final Keybind keyInventory = new Keybind("openInventory", 33);
    public static final Keybind keyDropItem = new Keybind("dropItem", 45);
    public static int buttonPickBlock = 2;
    public static int keyHotbar1 = 8;
    public static int keyHotbar2 = 9;
    public static int keyHotbar3 = 10;
    public static int keyHotbar4 = 11;
    public static int keyHotbar5 = 12;
    public static int keyHotbar6 = 13;
    public static int keyHotbar7 = 14;
    public static int keyHotbar8 = 15;
    public static int keyHotbar9 = 16;
    public static int keyHotbar10 = 7;
    public static final Keybind keyHideUI = new Keybind("hideUI", 131);
    public static final Keybind keyScreenshot = new Keybind("screenshot", 132);
    public static final Keybind keyDebugInfo = new Keybind("debugInfo", 133);
    public static final Keybind keyDebugNoClip = new Keybind("debugNoClip", 134);
    public static final Keybind keyDebugReloadShaders = new Keybind("reloadShaders", 136);
    public static final Keybind keyFullscreen = new Keybind("fullscreen", 141);
}