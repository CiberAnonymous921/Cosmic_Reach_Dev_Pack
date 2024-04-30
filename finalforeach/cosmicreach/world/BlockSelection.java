package finalforeach.cosmicreach.world;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;

import finalforeach.cosmicreach.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemBlock;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.settings.Controls;
import finalforeach.cosmicreach.ui.UI;

public class BlockSelection {
    public static ShapeRenderer shapeRenderer = new ShapeRenderer();
    public static boolean enabled;
    private BlockState lastSelectedBlockState;
    private static BlockState selectedBlockState;
    private BlockPosition lastSelectedBlockPos;
    private static BlockPosition selectedBlockPos;
    private Array<BoundingBox> blockBoundingBoxes = new Array<BoundingBox>(4);
    private Ray ray = new Ray();
    private BoundingBox tmpBoundingBox = new BoundingBox();
    private Vector3 intersection = new Vector3();
    private float maximumRaycastDist = 6.0f;
    private Array<BlockPosition> toVisit = new Array<BlockPosition>();
    private Vector3 workingPos = new Vector3();
    private Queue<BlockPosition> blockQueue = new Queue<BlockPosition>();
    private double timeSinceBlockModify = 0.0;
    private Vector3 mouseCoords = new Vector3();
    private Vector3 mouseCoords2 = new Vector3();
    private Array<BoundingBox> tmpBoundingBoxes = new Array<BoundingBox>();

    public static BlockState getBlockLookingAt() {
        if (!enabled) {
            return null;
        }
        return selectedBlockState;
    }

    public static BlockPosition getBlockPositionLookingAt() {
        if (!enabled) {
            return null;
        }
        return selectedBlockPos;
    }

    public void render(Camera worldCamera) {
        if (!enabled) {
            return;
        }
        if (selectedBlockState != null) {
            if (selectedBlockState != this.lastSelectedBlockState || selectedBlockPos != this.lastSelectedBlockPos) {
                selectedBlockState.getAllBoundingBoxes(this.blockBoundingBoxes, selectedBlockPos);
                for (BoundingBox bb : this.blockBoundingBoxes) {
                    bb.min.sub(0.001f);
                    bb.max.add(0.001f);
                    bb.update();
                }
                this.lastSelectedBlockState = selectedBlockState;
                this.lastSelectedBlockPos = selectedBlockPos;
            }
            shapeRenderer.setProjectionMatrix(worldCamera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.BLACK);
            for (BoundingBox bb : this.blockBoundingBoxes) {
                shapeRenderer.box(bb.min.x, bb.min.y, bb.min.z, bb.getWidth(), bb.getHeight(), -bb.getDepth());
            }
            shapeRenderer.end();
        }
    }

    private void addBlockToQueue(Zone zone, BlockPosition bp, int dx, int dy, int dz) {
        BlockState block;
        BlockPosition step = bp.getOffsetBlockPos(zone, dx, dy, dz);
        if (step != null && !this.toVisit.contains(step, false) && (block = bp.getBlockState()) != null) {
            block.getBoundingBox(this.tmpBoundingBox, step);
            if (Intersector.intersectRayBounds(this.ray, this.tmpBoundingBox, this.intersection)) {
                this.blockQueue.addLast(step);
                this.toVisit.add(step);
            }
        }
    }

    private boolean intersectsWithBlock(BlockState block, BlockPosition nextBlockPos) {
        block.getBoundingBox(this.tmpBoundingBox, nextBlockPos);
        if (!Intersector.intersectRayBounds(this.ray, this.tmpBoundingBox, this.intersection)) {
            return false;
        }
        block.getAllBoundingBoxes(this.tmpBoundingBoxes, nextBlockPos);
        for (BoundingBox bb : this.tmpBoundingBoxes) {
            if (!Intersector.intersectRayBounds(this.ray, bb, this.intersection)) continue;
            return true;
        }
        return false;
    }

    public void raycast(Zone zone, Camera worldCamera) {
        Item item;
        enabled = false;
        if (UI.mouseOverUI) {
            return;
        }
        BlockPosition placingBlockPos = null;
        BlockPosition breakingBlockPos = null;
        BlockPosition lastBlockPosAtPoint = null;
        BlockPosition lastBlockPosInQueue = null;
        this.toVisit.clear();
        this.blockQueue.clear();
        if (Gdx.input.isCursorCatched()) {
            this.ray.set(worldCamera.position, worldCamera.direction);
        } else {
            this.mouseCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0.0f);
            this.mouseCoords2.set(Gdx.input.getX(), Gdx.input.getY(), 1.0f);
            worldCamera.unproject(this.mouseCoords);
            worldCamera.unproject(this.mouseCoords2);
            this.mouseCoords2.sub(this.mouseCoords).nor();
            this.ray.set(this.mouseCoords, this.mouseCoords2);
        }
        this.workingPos.set(this.ray.origin);
        while (this.workingPos.dst(this.ray.origin) <= this.maximumRaycastDist) {
            block34: {
                Chunk c;
                int dz;
                int dy;
                int dx;
                int bz;
                int by;
                int bx;
                block33: {
                    bx = (int)Math.floor(this.workingPos.x);
                    by = (int)Math.floor(this.workingPos.y);
                    bz = (int)Math.floor(this.workingPos.z);
                    dx = 0;
                    dy = 0;
                    dz = 0;
                    if (lastBlockPosAtPoint == null) break block33;
                    if (lastBlockPosAtPoint.getGlobalX() == bx && lastBlockPosAtPoint.getGlobalY() == by && lastBlockPosAtPoint.getGlobalZ() == bz) break block34;
                    dx = bx - lastBlockPosAtPoint.getGlobalX();
                    dy = by - lastBlockPosAtPoint.getGlobalY();
                    dz = bz - lastBlockPosAtPoint.getGlobalZ();
                }
                if ((c = zone.getChunkAtBlock(bx, by, bz)) != null) {
                    BlockPosition nextBlockPos = new BlockPosition(c, bx - c.blockX, by - c.blockY, bz - c.blockZ);
                    if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > 1) {
                        if (dx != 0) {
                            this.addBlockToQueue(zone, lastBlockPosAtPoint, dx, 0, 0);
                        }
                        if (dy != 0) {
                            this.addBlockToQueue(zone, lastBlockPosAtPoint, 0, dy, 0);
                        }
                        if (dz != 0) {
                            this.addBlockToQueue(zone, lastBlockPosAtPoint, 0, 0, dz);
                        }
                        if (dx != 0 && dy != 0) {
                            this.addBlockToQueue(zone, lastBlockPosAtPoint, dx, dy, 0);
                        }
                        if (dx != 0 && dz != 0) {
                            this.addBlockToQueue(zone, lastBlockPosAtPoint, dx, 0, dz);
                        }
                        if (dy != 0 && dz != 0) {
                            this.addBlockToQueue(zone, lastBlockPosAtPoint, 0, dy, dz);
                        }
                    }
                    if (nextBlockPos != null && !this.toVisit.contains(nextBlockPos, false)) {
                        BlockState block = nextBlockPos.getBlockState();
                        block.getBoundingBox(this.tmpBoundingBox, nextBlockPos);
                        if (Intersector.intersectRayBounds(this.ray, this.tmpBoundingBox, this.intersection)) {
                            this.blockQueue.addLast(nextBlockPos);
                            this.toVisit.add(nextBlockPos);
                        } else if (block.canRaycastForReplace()) {
                            this.tmpBoundingBox.min.set(nextBlockPos.getGlobalX(), nextBlockPos.getGlobalY(), nextBlockPos.getGlobalZ());
                            this.tmpBoundingBox.max.set(this.tmpBoundingBox.min).add(1.0f, 1.0f, 1.0f);
                            if (Intersector.intersectRayBounds(this.ray, this.tmpBoundingBox, this.intersection)) {
                                this.blockQueue.addLast(nextBlockPos);
                                this.toVisit.add(nextBlockPos);
                            }
                        }
                    }
                    while (this.blockQueue.notEmpty()) {
                        BlockPosition curBlockPos = this.blockQueue.removeFirst();
                        BlockState blockState = curBlockPos.getBlockState();
                        if (!blockState.hasEmptyModel() && !this.intersectsWithBlock(blockState, curBlockPos)) continue;
                        if (breakingBlockPos == null && blockState.canRaycastForBreak()) {
                            breakingBlockPos = curBlockPos;
                            enabled = true;
                            selectedBlockState = blockState;
                            selectedBlockPos = curBlockPos;
                        }
                        if (placingBlockPos == null && blockState.canRaycastForPlaceOn() && lastBlockPosInQueue != null && (lastBlockPosInQueue.getBlockState()).canRaycastForReplace()) {
                            placingBlockPos = lastBlockPosInQueue;
                            enabled = true;
                            selectedBlockState = blockState;
                            selectedBlockPos = curBlockPos;
                        }
                        if (breakingBlockPos != null && placingBlockPos != null) break;
                        lastBlockPosInQueue = curBlockPos;
                    }
                    if (breakingBlockPos != null && placingBlockPos != null) break;
                    lastBlockPosAtPoint = nextBlockPos;
                }
            }
            this.workingPos.add(this.ray.direction);
        }
        BlockState targetBlockState = null;
        ItemStack itemStack = UI.hotbar.getSelectedItemStack();
        if (itemStack != null && (item = itemStack.item) instanceof ItemBlock) {
            ItemBlock itemBlock = (ItemBlock)item;
            targetBlockState = itemBlock.blockState;
        }
        this.timeSinceBlockModify -= (double)Gdx.graphics.getDeltaTime();
        boolean breakPressed = this.timeSinceBlockModify <= 0.0 && Controls.breakPressed();
        boolean placePressed = this.timeSinceBlockModify <= 0.0 && Controls.placePressed();
        breakPressed |= Gdx.input.isButtonJustPressed(0);
        boolean interactJustPressed = Gdx.input.isButtonJustPressed(1);
        boolean interactHeld = placePressed |= Gdx.input.isButtonJustPressed(1);
        if (breakingBlockPos != null && Controls.pickBlockPressed()) {
            UI.hotbar.pickBlock(breakingBlockPos.getBlockState());
        }
        if (breakingBlockPos != null && breakPressed) {
            this.breakBlock(zone, breakingBlockPos, this.timeSinceBlockModify);
            this.timeSinceBlockModify = 0.25;
        }
        if (placingBlockPos != null && placePressed && targetBlockState != null) {
            Entity playerEntity = InGame.getLocalPlayer().getEntity();
            boolean positionBlockedByPlayer = false;
            if (!targetBlockState.walkThrough) {
                BoundingBox blockBoundingBox = new BoundingBox();
                BoundingBox playerBoundingBox = new BoundingBox();
                playerBoundingBox.set(playerEntity.localBoundingBox);
                playerBoundingBox.min.add(playerEntity.position);
                playerBoundingBox.max.add(playerEntity.position);
                playerBoundingBox.update();
                targetBlockState.getBoundingBox(blockBoundingBox, placingBlockPos);
                if (blockBoundingBox.intersects(playerBoundingBox) && blockBoundingBox.max.y - playerBoundingBox.min.y > playerEntity.maxStepHeight) {
                    positionBlockedByPlayer = true;
                }
            }
            if (!positionBlockedByPlayer || playerEntity.noClip) {
                this.timeSinceBlockModify = 0.25;
                this.placeBlock(zone, targetBlockState, placingBlockPos, this.timeSinceBlockModify);
            }
        } else if (breakingBlockPos != null && (interactJustPressed || interactHeld)) {
            this.interactWith(zone, breakingBlockPos, interactJustPressed, interactHeld, this.timeSinceBlockModify);
            this.timeSinceBlockModify = 0.25;
        }
    }

    private void breakBlock(Zone zone, BlockPosition blockPos, double timeSinceLastInteract) {
        BlockState blockState = blockPos.getBlockState();
        if (blockState == null) {
            return;
        }
        BlockEventTrigger[] triggers = blockState.getTrigger("onBreak");
        if (triggers == null) {
            return;
        }
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("blockPos", blockPos);
        args.put("timeSinceLastInteract", timeSinceLastInteract);
        for (int i = 0; i < triggers.length; ++i) {
            triggers[i].act(blockState, zone, args);
        }
    }

    private void placeBlock(Zone zone, BlockState targetBlockState, BlockPosition blockPos, double timeSinceLastInteract) {
        float zDiff;
        float xDiff;
        HashMap<String, String> m;
        BlockState blockState = blockPos.getBlockState();
        if (blockState == null) {
            return;
        }
        BlockEventTrigger[] triggers = targetBlockState.getTrigger("onPlace");
        if (triggers == null) {
            return;
        }
        String targetId = targetBlockState.getStateParamsStr();
        if (targetId.contains("slab_type=top") || targetId.contains("slab_type=bottom")) {
            m = new HashMap<String, String>();
            float yDiff = this.intersection.y - (float)blockPos.getGlobalY();
            if ((double)yDiff < 0.5) {
                m.put("slab_type", "bottom");
            } else {
                m.put("slab_type", "top");
            }
            targetBlockState = targetBlockState.getVariantWithParams(m);
        }
        if (targetId.contains("slab_type=vertical")) {
            m = new HashMap<String, String>();
            xDiff = this.intersection.x - (float)blockPos.getGlobalX();
            zDiff = this.intersection.z - (float)blockPos.getGlobalZ();
            if (Math.abs((double)xDiff - 0.5) > Math.abs((double)zDiff - 0.5)) {
                if ((double)xDiff < 0.5) {
                    m.put("slab_type", "verticalNegX");
                } else {
                    m.put("slab_type", "verticalPosX");
                }
            } else if ((double)zDiff < 0.5) {
                m.put("slab_type", "verticalNegZ");
            } else {
                m.put("slab_type", "verticalPosZ");
            }
            targetBlockState = targetBlockState.getVariantWithParams(m);
        }
        if (targetId.contains("stair_type")) {
            m = new HashMap<String, String>();
            xDiff = this.intersection.x - (float)blockPos.getGlobalX();
            zDiff = this.intersection.z - (float)blockPos.getGlobalZ();
            if (Math.abs((double)xDiff - 0.5) > Math.abs((double)zDiff - 0.5)) {
                if ((double)xDiff < 0.5) {
                    m.put("stair_type", "bottom_NegX");
                } else {
                    m.put("stair_type", "bottom_PosX");
                }
            } else if ((double)zDiff < 0.5) {
                m.put("stair_type", "bottom_NegZ");
            } else {
                m.put("stair_type", "bottom_PosZ");
            }
            targetBlockState = targetBlockState.getVariantWithParams(m);
        }
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("blockPos", blockPos);
        args.put("targetBlockState", targetBlockState);
        args.put("timeSinceLastInteract", timeSinceLastInteract);
        for (int i = 0; i < triggers.length; ++i) {
            triggers[i].act(targetBlockState, zone, args);
        }
    }

    private void interactWith(Zone zone, BlockPosition blockPos, boolean interactJustPressed, boolean interactHeld, double timeSinceLastInteract) {
        BlockState blockState = blockPos.getBlockState();
        if (blockState == null) {
            return;
        }
        BlockEventTrigger[] triggers = blockState.getTrigger("onInteract");
        if (triggers == null) {
            return;
        }
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("blockPos", blockPos);
        args.put("interactJustPressed", interactJustPressed);
        args.put("interactHeld", interactHeld);
        args.put("timeSinceLastInteract", timeSinceLastInteract);
        for (int i = 0; i < triggers.length; ++i) {
            triggers[i].act(blockState, zone, args);
        }
    }
}