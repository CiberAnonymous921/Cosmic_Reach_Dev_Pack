package finalforeach.cosmicreach.ui.debug;

public class DebugIntItem extends DebugItem {
    final IDebugGetInt getIntVal;
    final IDebugIntToLine toDebugLine;
    int currentValue;

    public DebugIntItem(String prefix, IDebugGetInt getIntVal) {
        this.getIntVal = getIntVal;
        this.toDebugLine = i -> prefix + i;
    }

    public DebugIntItem(IDebugGetInt getIntVal, IDebugIntToLine toDebugLine) {
        this.getIntVal = getIntVal;
        this.toDebugLine = toDebugLine;
    }

    @Override
    public void update() {
        int newVal = this.getIntVal.getValue();
        if (this.line == null || this.currentValue != newVal) {
            this.currentValue = newVal;
            this.line = this.toDebugLine.getLine(newVal);
            this.dirty = true;
        }
    }
}