package finalforeach.cosmicreach.ui.debug;

public class DebugObjectItem<T> extends DebugItem {
    final IDebugGetObject<T> getVal;
    final IDebugToLine<T> toDebug;
    final boolean displayIfNull;
    T obj;

    public DebugObjectItem(boolean displayIfNull, IDebugGetObject<T> getVal, IDebugToLine<T> toDebug) {
        this.displayIfNull = displayIfNull;
        this.getVal = getVal;
        this.toDebug = toDebug;
    }

    @Override
    public void update() {
        T newObj = this.getVal.getValue();
        if (this.obj != newObj) {
            this.obj = newObj;
            this.line = this.displayIfNull || this.obj != null ? this.toDebug.getLine(this.obj) : "";
            this.dirty = true;
        }
    }
}