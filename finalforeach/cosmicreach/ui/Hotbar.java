package finalforeach.cosmicreach.ui;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemBlock;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.items.SlotContainer;
import finalforeach.cosmicreach.settings.ControlSettings;

public class Hotbar extends SlotContainer {
    public Hotbar() {
        super(10);
    }

    public boolean keyDown(int keycode) {
        ItemSlot slot;
        if (keycode == ControlSettings.keyDropItem.getValue() && (slot = this.getSelectedSlot()) != null) {
            slot.itemStack = null;
            return true;
        }
        if (keycode == ControlSettings.keyHotbar1) {
            this.selectSlot(0);
            return true;
        }
        if (keycode == ControlSettings.keyHotbar2) {
            this.selectSlot(1);
            return true;
        }
        if (keycode == ControlSettings.keyHotbar3) {
            this.selectSlot(2);
            return true;
        }
        if (keycode == ControlSettings.keyHotbar4) {
            this.selectSlot(3);
            return true;
        }
        if (keycode == ControlSettings.keyHotbar5) {
            this.selectSlot(4);
            return true;
        }
        if (keycode == ControlSettings.keyHotbar6) {
            this.selectSlot(5);
            return true;
        }
        if (keycode == ControlSettings.keyHotbar7) {
            this.selectSlot(6);
            return true;
        }
        if (keycode == ControlSettings.keyHotbar8) {
            this.selectSlot(7);
            return true;
        }
        if (keycode == ControlSettings.keyHotbar9) {
            this.selectSlot(8);
            return true;
        }
        if (keycode == ControlSettings.keyHotbar10) {
            this.selectSlot(9);
            return true;
        }
        return false;
    }

    public boolean scrolled(float amountX, float amountY) {
        if (amountY > 0.0f) {
            this.selectSlot((this.lastSelectedSlotNum + 1) % this.getNumSlots());
            return true;
        }
        if (amountY < 0.0f) {
            this.selectSlot((this.lastSelectedSlotNum + this.getNumSlots() - 1) % this.getNumSlots());
            return true;
        }
        return false;
    }

    public void pickBlock(BlockState blockState) {
        ItemSlot slot2;
        for (ItemSlot slot : this.slots) {
            Item item;
            ItemStack itemStack = slot.itemStack;
            if (itemStack == null || !((item = itemStack.item) instanceof ItemBlock)) continue;
            ItemBlock itemBlock = (ItemBlock)item;
            if (itemBlock.blockState != blockState) continue;
            slot.select();
            return;
        }
        ItemStack newItemStack = new ItemStack(new ItemBlock(blockState), 1);
        slot2 = this.getFirstEmptyItemSlot();
        if (slot2 != null) {
            slot2.select();
        } else {
            slot2 = this.getSelectedSlot();
        }
        if (slot2 == null) {
            slot2 = this.getSlot(0);
        }
        slot2.itemStack = newItemStack;
    }

    public void render(Viewport uiViewport, ShapeRenderer shapeRenderer) {
        if (!this.shown) {
            return;
        }
        float screenBottom = uiViewport.getWorldHeight() / 2.0f;
        shapeRenderer.setColor(0.0f, 0.0f, 0.0f, 0.8f);
        float s = 32.0f;
        float padding = 2.0f;
        int numSlots = this.getNumSlots();
        float startX = -((s + padding) * (float)numSlots / 2.0f);
        for (int i = 0; i < numSlots; ++i) {
            ItemSlot slot = this.getSlot(i);
            float thisSlotSize = s;
            if (slot.selected) {
                float g = 0.3f;
                shapeRenderer.setColor(g, g, g, 0.4f);
                thisSlotSize = 34.0f;
            } else {
                shapeRenderer.setColor(0.0f, 0.0f, 0.0f, 0.4f);
            }
            float sizeDiff = thisSlotSize - s;
            float rX = startX + (float)i * (s + padding) - sizeDiff / 2.0f;
            float rY = screenBottom - s - padding - sizeDiff / 2.0f;
            shapeRenderer.rect(rX, rY, thisSlotSize, thisSlotSize);
            this.w = rX + thisSlotSize - startX;
            this.h = thisSlotSize + padding;
        }
        this.y = screenBottom - this.h;
    }

    @Override
    public void drawItems(Viewport uiViewport) {
        if (!this.shown) {
            return;
        }
        float s = 32.0f;
        int numSlots = this.getNumSlots();
        float padding = 2.0f;
        float startX = -((s + padding) * (float)numSlots / 2.0f);
        float ratioX = (float)uiViewport.getScreenWidth() / uiViewport.getWorldWidth();
        float ratioY = (float)uiViewport.getScreenHeight() / uiViewport.getWorldHeight();
        this.x = uiViewport.getScreenWidth() / 2;
        this.y = padding * ratioY;
        for (int i = 0; i < numSlots; ++i) {
            ItemSlot slot = this.getSlot(i);
            ItemStack itemStack = slot.itemStack;
            if (itemStack == null) continue;
            slot.x = startX + (float)i * (slot.size + padding);
            float sx = this.x + slot.x * ratioX;
            float sy = this.y + slot.y * ratioY;
            float sw = slot.size * ratioX;
            float sh = slot.size * ratioY;
            this.itemViewport.setScreenBounds((int)sx, (int)sy, (int)sw, (int)sh);
            this.itemViewport.apply();
            itemStack.item.render(this.itemCam);
        }
    }

    @Override
    public boolean isPointInBounds(float x, float y) {
        return x >= this.x && y >= this.y && x < this.x + this.w && y < this.y + this.h;
    }
}