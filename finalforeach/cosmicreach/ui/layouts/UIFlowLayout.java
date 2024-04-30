package finalforeach.cosmicreach.ui.layouts;

import com.badlogic.gdx.utils.Array;

import finalforeach.cosmicreach.ui.UIObject;

public class UIFlowLayout extends UILayout {
    public UIFlowLayout(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    @Override
    public void restructure() {
        float ox = this.x + this.margin;
        float oy = this.y + this.margin;
        float addH = 0.0f;
        boolean autoPad = true;
        Array<UIObject> row = new Array<UIObject>();
        float totalWidthPlusPadding = 0.0f;
        for (UIObject o : this.uiObjects) {
            if (ox + o.getWidth() > this.x + this.w - this.margin) {
                ox = this.x + this.margin;
                oy += addH + this.padding;
                addH = 0.0f;
                if (autoPad) {
                    float cpad = (this.w - totalWidthPlusPadding) / (float)(row.size + 1);
                    float cx = this.x + this.margin + cpad;
                    for (UIObject r : row) {
                        r.setX(cx);
                        cx += r.getWidth() + this.padding + cpad;
                    }
                }
                totalWidthPlusPadding = 0.0f;
                row.clear();
            }
            o.setX(ox);
            o.setY(oy);
            addH = Math.max(addH, o.getHeight());
            ox += o.getWidth() + this.padding;
            totalWidthPlusPadding += o.getWidth() + this.padding;
            row.add(o);
        }
        if (autoPad) {
            float cpad = (this.w - totalWidthPlusPadding) / (float)(row.size + 1);
            float cx = this.x + this.margin + cpad;
            for (UIObject r : row) {
                r.setX(cx);
                cx += r.getWidth() + this.padding + cpad;
            }
        }
        this.flagForRestructure = false;
    }
}