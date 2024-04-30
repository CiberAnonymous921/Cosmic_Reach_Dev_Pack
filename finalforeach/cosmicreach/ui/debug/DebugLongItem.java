package finalforeach.cosmicreach.ui.debug;

public class DebugLongItem extends DebugItem {
    final IDebugGetLong getVal;
    final IDebugLongToLine toDebugLine;
    long currentValue;

    public DebugLongItem(String prefix, IDebugGetLong getIntVal) {
        this.getVal = getIntVal;
        this.toDebugLine = i -> prefix + i;
    }

    public DebugLongItem(IDebugGetLong getVal, IDebugLongToLine toDebugLine) {
        this.getVal = getVal;
        this.toDebugLine = toDebugLine;
    }

    @Override
    public void update() {
        long newVal = this.getVal.getValue();
        if (this.line == null || this.currentValue != newVal) {
            this.currentValue = newVal;
            this.line = this.toDebugLine.getLine(newVal);
            this.dirty = true;
        }
    }
}