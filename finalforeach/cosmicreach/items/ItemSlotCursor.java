package finalforeach.cosmicreach.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import finalforeach.cosmicreach.blocks.Block;

public class ItemSlotCursor extends SlotContainer {
    public ItemSlotCursor() {
        super(1);
        this.addItemStack(new ItemStack(new ItemBlock(Block.DIRT.getDefaultBlockState()), 1));
    }

    @Override
    public void drawItems(Viewport uiViewport) {
        if (!this.shown) {
            return;
        }
        Vector2 mVec = new Vector2(Gdx.graphics.getWidth() / 2 + Gdx.input.getX(), 3 * Gdx.graphics.getHeight() / 2 - Gdx.input.getY());
        uiViewport.unproject(mVec);
        int numSlots = this.getNumSlots();
        for (int i = 0; i < numSlots; ++i) {
            ItemSlot slot = this.getSlot(i);
            slot.x = mVec.x - 16.0f;
            slot.y = mVec.y - 16.0f;
        }
        float ratioX = (float)uiViewport.getScreenWidth() / uiViewport.getWorldWidth();
        float ratioY = (float)uiViewport.getScreenHeight() / uiViewport.getWorldHeight();
        for (int i = 0; i < numSlots; ++i) {
            ItemSlot slot = this.getSlot(i);
            ItemStack itemStack = slot.itemStack;
            if (itemStack == null) continue;
            float sx = this.x + slot.x * ratioX;
            float sy = this.y + slot.y * ratioY;
            float sw = slot.size * ratioX;
            float sh = slot.size * ratioY;
            this.itemViewport.setScreenBounds((int)sx, (int)sy, (int)sw, (int)sh);
            this.itemViewport.apply();
            itemStack.item.render(this.itemCam);
        }
    }
}