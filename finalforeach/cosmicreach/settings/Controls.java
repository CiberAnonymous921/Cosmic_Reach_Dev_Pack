package finalforeach.cosmicreach.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.utils.Array;

public class Controls {
    private static Array<ControllerTracker> listeners = new Array<ControllerTracker>();
    public static Array<Controller> controllers = Controls.getControllers();

    public static void update() {
        for (ControllerTracker l : listeners) {
            l.flushJustMoved();
        }
    }

    private static Array<Controller> getControllers() {
        Array<Controller> controllers = Controllers.getControllers();
        for (Controller c : controllers) {
            ControllerTracker t = new ControllerTracker(c);
            listeners.add(t);
            c.addListener(t);
        }
        return controllers;
    }

    public static float getRightXAxis() {
        float x = 0.0f;
        for (Controller c : controllers) {
            x += c.getAxis(c.getMapping().axisRightX);
        }
        return x;
    }

    public static float getRightYAxis() {
        float x = 0.0f;
        for (Controller c : controllers) {
            x += c.getAxis(c.getMapping().axisRightY);
        }
        return x;
    }

    private static boolean buttonPressed(ButtonMappingOperator op) {
        for (Controller c : controllers) {
            if (!c.getButton(op.getInt(c.getMapping()))) continue;
            return true;
        }
        return false;
    }

    @SuppressWarnings("unused")
	private static boolean buttonJustPressed(ButtonMappingOperator op) {
        for (ControllerTracker l : listeners) {
            Controller c;
            ControllerMapping mapping;
            if (!l.buttonJustPressed(op.getInt(mapping = (c = l.controller).getMapping()))) continue;
            return true;
        }
        return false;
    }

    private static float axisPositive(ButtonMappingOperator op) {
        for (Controller c : controllers) {
            float a = c.getAxis(op.getInt(c.getMapping()));
            if (!(a > 0.0f)) continue;
            return a;
        }
        return 0.0f;
    }

    private static float axisNegative(ButtonMappingOperator op) {
        for (Controller c : controllers) {
            float a = c.getAxis(op.getInt(c.getMapping()));
            if (!(a < 0.0f)) continue;
            return -a;
        }
        return 0.0f;
    }

    public static float forwardPressed() {
        if (ControlSettings.keyForward.isPressed()) {
            return 1.0f;
        }
        return Controls.axisNegative(mapping -> mapping.axisLeftY);
    }

    public static float backwardPressed() {
        if (ControlSettings.keyBackward.isPressed()) {
            return 1.0f;
        }
        return Controls.axisPositive(mapping -> mapping.axisLeftY);
    }

    public static float leftPressed() {
        if (ControlSettings.keyLeft.isPressed()) {
            return 1.0f;
        }
        return Controls.axisNegative(mapping -> mapping.axisLeftX);
    }

    public static float rightPressed() {
        if (ControlSettings.keyRight.isPressed()) {
            return 1.0f;
        }
        return Controls.axisPositive(mapping -> mapping.axisLeftX);
    }

    public static boolean jumpPressed() {
        if (ControlSettings.keyJump.isPressed()) {
            return true;
        }
        return Controls.buttonPressed(mapping -> mapping.buttonA);
    }

    public static boolean crouchPressed() {
        return ControlSettings.keyCrouch.isPressed();
    }

    public static boolean sprintPressed() {
        return ControlSettings.keySprint.isPressed();
    }

    public static boolean pronePressed() {
        return ControlSettings.keyProne.isJustPressed();
    }

    public static boolean inventoryPressed() {
        return ControlSettings.keyInventory.isJustPressed();
    }

    public static boolean dropItemPressed() {
        if (ControlSettings.keyDropItem.isJustPressed()) {
            return true;
        }
        return Controls.buttonPressed(mapping -> mapping.buttonDpadDown);
    }

    public static boolean pickBlockPressed() {
        return Gdx.input.isButtonJustPressed(ControlSettings.buttonPickBlock);
    }

    public static boolean toggleHideUIPressed() {
        return ControlSettings.keyHideUI.isJustPressed();
    }

    public static boolean screenshotPressed() {
        return ControlSettings.keyScreenshot.isJustPressed();
    }

    public static boolean debugInfoPressed() {
        return ControlSettings.keyDebugInfo.isJustPressed();
    }

    public static boolean debugNoClipPressed() {
        return ControlSettings.keyDebugNoClip.isJustPressed();
    }

    public static boolean keyDebugReloadShadersJustPressed() {
        return ControlSettings.keyDebugReloadShaders.isJustPressed();
    }

    public static boolean keyFullscreenJustPressed() {
        return ControlSettings.keyFullscreen.isJustPressed();
    }

    public static boolean cycleItemLeft() {
        return Controls.buttonPressed(mapping -> mapping.buttonL2);
    }

    public static boolean cycleItemRight() {
        return Controls.buttonPressed(mapping -> mapping.buttonR2);
    }

    public static boolean breakPressed() {
        if (Gdx.input.isButtonPressed(0)) {
            return true;
        }
        return Controls.buttonPressed(mapping -> mapping.buttonR1);
    }

    public static boolean placePressed() {
        if (Gdx.input.isButtonPressed(1)) {
            return true;
        }
        return Controls.buttonPressed(mapping -> mapping.buttonL1);
    }

    public static interface ButtonMappingOperator {
        public int getInt(ControllerMapping var1);
    }
}