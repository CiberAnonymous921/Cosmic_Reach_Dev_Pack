package finalforeach.cosmicreach.settings;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.utils.IntFloatMap;
import com.badlogic.gdx.utils.IntSet;

class ControllerTracker implements ControllerListener {
    public Controller controller;
    private boolean flipFlop = false;
    private IntFloatMap axisJustMovedA = new IntFloatMap();
    private IntFloatMap axisJustMovedB = new IntFloatMap();
    private IntFloatMap axisJustMovedCurrent = this.axisJustMovedA;
    private IntSet buttonJustPressedA = new IntSet();
    private IntSet buttonJustPressedB = new IntSet();
    private IntSet buttonJustPressedCurrent = this.buttonJustPressedA;
    boolean setBefore = false;
    boolean setNow = false;

    public ControllerTracker(Controller c) {
        this.controller = c;
    }

    @Override
    public void disconnected(Controller controller) {
    }

    @Override
    public void connected(Controller controller) {
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        this.buttonJustPressedCurrent.add(buttonCode);
        this.setNow = true;
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        this.axisJustMovedCurrent.put(axisCode, value + this.axisJustMovedCurrent.get(axisCode, 0.0f));
        return false;
    }

    public void flushJustMoved() {
        if (this.setBefore || this.setNow) {
            // empty if block
        }
        if (this.flipFlop) {
            this.axisJustMovedCurrent = this.axisJustMovedA;
            this.buttonJustPressedCurrent = this.buttonJustPressedA;
        } else {
            this.axisJustMovedCurrent = this.axisJustMovedB;
            this.buttonJustPressedCurrent = this.buttonJustPressedB;
        }
        this.setBefore = this.setNow;
        this.setNow = false;
        this.axisJustMovedCurrent.clear();
        this.buttonJustPressedCurrent.clear();
    }

    public float getAxisMovedBy(int axisCode) {
        return this.axisJustMovedA.get(axisCode, 0.0f) + this.axisJustMovedB.get(axisCode, 0.0f);
    }

    public boolean buttonJustPressed(int buttonCode) {
        if (this.setBefore || this.setNow) {
            // empty if block
        }
        return this.buttonJustPressedA.contains(buttonCode) || this.buttonJustPressedB.contains(buttonCode);
    }
}