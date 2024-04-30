package finalforeach.cosmicreach.items;

import com.badlogic.gdx.utils.viewport.Viewport;

public class ItemSlot {
    public final int slotId;
    public float x;
    public float y;
    public float size = 32.0f;
    public boolean enabled = true;
    public boolean selected;
    public ItemStack itemStack;
    public final SlotContainer container;

    public ItemSlot(SlotContainer container, int slotId) {
        this.slotId = slotId;
        this.container = container;
    }

    public boolean isHoveredOver(Viewport uiViewport, float x, float y) {
        return x >= this.container.x + this.x && y >= this.container.y + this.y - this.size && x < this.container.x + this.x + this.size && y < this.container.y + this.y;
    }

    public void select() {
        this.container.selectSlot(this.slotId);
    }
}