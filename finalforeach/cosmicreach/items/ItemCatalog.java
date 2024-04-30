package finalforeach.cosmicreach.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.viewport.Viewport;

import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.UIElement;
import finalforeach.cosmicreach.ui.VerticalAnchor;

public class ItemCatalog extends SlotContainer {
    private transient float lastW = 0.0f;
    private transient float lastH = 0.0f;
    float bottomNextPageMargin = 100.0f;
    public int curPage = 0;
    public IntArray pagesToItemSlotNum = new IntArray();
    private float padding = 2.0f;
    UIElement buttonPrevPage;
    UIElement buttonNextPage;

    public ItemCatalog(int numSlots) {
        super(numSlots);
        for (Block block : Block.allBlocks) {
            for (BlockState state : block.blockStates.values()) {
                if (state.hasEmptyModel() || state.catalogHidden) continue;
                this.addItemStack(new ItemStack(new ItemBlock(state), 1));
            }
        }
    }

    public void calculatePages() {
        this.pagesToItemSlotNum.clear();
        int i = 0;
        do {
            this.pagesToItemSlotNum.add(i);
        } while ((i = this.calculateNextPage(i)) < this.getNumSlots());
        this.curPage = MathUtils.clamp(this.curPage, 0, this.pagesToItemSlotNum.size - 1);
    }

    private int calculateNextPage(int startSlot) {
        ItemSlot prevSlot = null;
        float maxX = 0.0f;
        for (int i = startSlot; i < this.getNumSlots(); ++i) {
            ItemSlot slot = this.getSlot(i);
            ItemStack itemStack = slot.itemStack;
            if (itemStack == null) continue;
            slot.x = this.padding + 8.0f;
            slot.y = slot.size;
            if (prevSlot != null) {
                slot.x = prevSlot.x + (slot.size + this.padding);
                slot.y = prevSlot.y;
            }
            if (slot.x > (this.w - slot.size)) {
                slot.y += this.padding + slot.size;
                slot.x = this.padding + 8.0f;
            }
            maxX = Math.max(maxX, slot.x);
            prevSlot = slot;
            if (!(slot.y > this.h - this.bottomNextPageMargin)) continue;
            return i;
        }
        return this.getNumSlots();
    }

    public void render(Viewport uiViewport, ShapeRenderer shapeRenderer) {
        if (this.shown) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            this.w = uiViewport.getWorldWidth() / 4.0f;
            this.w -= this.w % 36.0f;
            this.h = uiViewport.getCamera().viewportHeight;
            float screenRight2 = uiViewport.getWorldWidth() / 2.0f;
            float screenTop = -uiViewport.getWorldHeight() / 2.0f;
            this.x = screenRight2 - this.w;
            this.y = screenTop;
            shapeRenderer.setColor(0.0f, 0.0f, 0.0f, 0.4f);
            shapeRenderer.rect(this.x, this.y, this.w, this.h);
            shapeRenderer.end();
            if (this.lastW != this.w || this.lastH != this.h) {
                this.lastW = this.w;
                this.lastH = this.h;
                this.calculatePages();
            }
        }
    }

    @Override
    public void drawItems(Viewport uiViewport) {
        int slotStart;
        if (!this.shown) {
            return;
        }
        this.buttonPrevPage.x = this.x + 48.0f;
        this.buttonNextPage.x = this.x + this.w - 48.0f;
        float ratioX = (float)uiViewport.getScreenWidth() / uiViewport.getWorldWidth();
        float ratioY = (float)uiViewport.getScreenHeight() / uiViewport.getWorldHeight();
        int numSlots = this.getNumSlots();
        ItemSlot prevSlot = null;
        Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        uiViewport.unproject(mouse);
        float maxX = 0.0f;
        int slotEnd = slotStart = this.pagesToItemSlotNum.get(this.curPage);
        for (int i = slotStart; i < numSlots; ++i) {
            ItemSlot slot = this.getSlot(i);
            ItemStack itemStack = slot.itemStack;
            if (itemStack == null) continue;
            slot.x = this.padding + 8.0f;
            slot.y = slot.size;
            if (prevSlot != null) {
                slot.x = prevSlot.x + (slot.size + this.padding);
                slot.y = prevSlot.y;
            }
            if (slot.x > (this.w - slot.size)) {
                slot.y += this.padding + slot.size;
                slot.x = this.padding + 8.0f;
            }
            maxX = Math.max(maxX, slot.x);
            prevSlot = slot;
            if (slot.y > this.h - this.bottomNextPageMargin) break;
            slotEnd = i;
        }
        Vector2 tmp = new Vector2();
        for (int i = slotStart; i <= slotEnd; ++i) {
            ItemSlot slot = this.getSlot(i);
            ItemStack itemStack = slot.itemStack;
            if (itemStack == null) continue;
            float sw = slot.size * ratioX;
            float sh = slot.size * ratioY;
            float sx = this.x + slot.x;
            float sy = this.y + slot.y;
            tmp.set(sx, sy);
            uiViewport.project(tmp);
            sx = tmp.x;
            sy = tmp.y;
            if (slot.isHoveredOver(uiViewport, mouse.x, mouse.y)) {
                this.itemViewport.setScreenBounds((int)sx - 4, (int)sy - 4, (int)sw + 8, (int)sh + 8);
            } else {
                this.itemViewport.setScreenBounds((int)sx, (int)sy, (int)sw, (int)sh);
            }
            this.itemViewport.apply();
            itemStack.item.render(this.itemCam);
        }
    }

    @Override
    public void show() {
        super.show();
        if (this.buttonNextPage == null) {
            this.buttonPrevPage = new UIElement(this.x + 48.0f, -16.0f, 64.0f, 64.0f){

                @Override
                public void onClick() {
                    ItemCatalog.this.curPage = MathUtils.clamp(ItemCatalog.this.curPage - 1, 0, ItemCatalog.this.pagesToItemSlotNum.size - 1);
                    super.onClick();
                }
            };
            this.buttonPrevPage.setText("<");
            this.buttonPrevPage.hAnchor = HorizontalAnchor.CENTERED;
            this.buttonPrevPage.vAnchor = VerticalAnchor.BOTTOM_ALIGNED;
            this.buttonNextPage = new UIElement(this.x + this.w - 48.0f, -16.0f, 64.0f, 64.0f){

                @Override
                public void onClick() {
                    ItemCatalog.this.curPage = MathUtils.clamp(ItemCatalog.this.curPage + 1, 0, ItemCatalog.this.pagesToItemSlotNum.size - 1);
                    super.onClick();
                }
            };
            this.buttonNextPage.hAnchor = HorizontalAnchor.CENTERED;
            this.buttonNextPage.vAnchor = VerticalAnchor.BOTTOM_ALIGNED;
            this.buttonNextPage.setText(">");
            this.buttonPrevPage.show();
            this.buttonNextPage.show();
        }
        GameState.IN_GAME.uiObjects.add(this.buttonPrevPage);
        GameState.IN_GAME.uiObjects.add(this.buttonNextPage);
    }

    @Override
    public void hide() {
        super.hide();
        GameState.IN_GAME.uiObjects.removeValue(this.buttonPrevPage, true);
        GameState.IN_GAME.uiObjects.removeValue(this.buttonNextPage, true);
    }
}