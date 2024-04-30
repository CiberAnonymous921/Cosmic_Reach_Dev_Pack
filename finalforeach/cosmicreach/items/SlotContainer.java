package finalforeach.cosmicreach.items;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class SlotContainer {
    protected PerspectiveCamera itemCam = new PerspectiveCamera(1.1f, 100.0f, 100.0f);
    protected FitViewport itemViewport = new FitViewport(100.0f, 100.0f, this.itemCam);
    public Array<ItemSlot> slots = new Array<ItemSlot>();
    public float x;
    public float y;
    public float w;
    public float h;
    public boolean shown = true;
    private ItemSlot selectedSlot;
    protected int lastSelectedSlotNum = -1;

    public SlotContainer(int numSlots) {
        this.itemCam.near = 30.0f;
        this.itemCam.far = 100.0f;
        this.itemCam.position.set(50.0f, 50.0f, 50.0f);
        this.itemCam.lookAt(0.5f, 0.5f, 0.5f);
        this.itemCam.update();
        for (int i = 0; i < numSlots; ++i) {
            this.slots.add(new ItemSlot(this, i));
        }
    }

    public ItemSlot getSelectedSlot() {
        return this.selectedSlot;
    }

    public void selectSlot(int slotNum) {
        if (this.selectedSlot != null) {
            this.selectedSlot.selected = false;
        }
        this.selectedSlot = this.getSlot(slotNum);
        if (this.selectedSlot != null) {
            this.selectedSlot.selected = true;
            this.lastSelectedSlotNum = slotNum;
        } else {
            this.lastSelectedSlotNum = -1;
        }
    }

    public void deselect() {
        if (this.selectedSlot != null) {
            this.selectedSlot.selected = false;
        }
        this.selectedSlot = null;
    }

    public ItemSlot getFirstEmptyItemSlot() {
        for (int i = 0; i < this.slots.size; ++i) {
            ItemSlot slot = this.getSlot(i);
            if (slot.itemStack != null) continue;
            return slot;
        }
        return null;
    }

    public boolean addItemStack(ItemStack itemStack) {
        ItemSlot itemSlot = this.getFirstEmptyItemSlot();
        if (itemSlot == null) {
            return false;
        }
        itemSlot.itemStack = itemStack;
        return true;
    }

    public ItemSlot getSlot(int slotNum) {
        return this.slots.get(slotNum);
    }

    public int getNumSlots() {
        return this.slots.size;
    }

    public ItemStack getSelectedItemStack() {
        if (this.selectedSlot == null) {
            return null;
        }
        return this.selectedSlot.itemStack;
    }

    public boolean isPointInBounds(float x, float y) {
        return x >= this.x && y >= this.y && x < this.x + this.w && y < this.y + this.h;
    }

    public void toggleShown() {
        this.setShown(!this.shown);
    }

    public void setShown(boolean shown) {
        if (shown) {
            this.show();
        } else {
            this.hide();
        }
    }

    public void show() {
        this.shown = true;
    }

    public void hide() {
        this.shown = false;
    }

    public void drawItems(Viewport uiViewport) {
    }
}