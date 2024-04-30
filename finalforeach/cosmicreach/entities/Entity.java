package finalforeach.cosmicreach.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;

public class Entity {
    public static Vector3 gravity = new Vector3(0.0f, -17.15f, 0.0f);
    public float gravityModifier = 1.0f;
    public boolean hasGravity = true;
    public boolean isOnGround;
    public boolean collidedX;
    public boolean collidedZ;
    public boolean noClip;
    public float maxStepHeight = 0.5f;
    public float fluidImmersionRatio = 0.0f;
    public boolean isSneaking;
    public Vector3 viewDirection = new Vector3(0.0f, 0.0f, 1.0f);
    public Vector3 position = new Vector3();
    public Vector3 lastPosition = new Vector3();
    public Vector3 viewPositionOffset = new Vector3();
    private Vector3 acceleration = new Vector3();
    public Vector3 velocity = new Vector3();
    public Vector3 onceVelocity = new Vector3();
    public BoundingBox localBoundingBox = new BoundingBox();
    private transient BoundingBox tmpEntityBoundingBox = new BoundingBox();
    private transient BoundingBox tmpEntityBoundingBox2 = new BoundingBox();
    private transient BoundingBox tmpBlockBoundingBox = new BoundingBox();
    private transient BoundingBox tmpBlockBoundingBox2 = new BoundingBox();
    private transient float footstepTimer = 0.0f;
    private transient Array<BoundingBox> tmpBlockBoundingBoxes = new Array<BoundingBox>();

    public Entity() {
        this.localBoundingBox.min.set(-0.5f, -0.5f, -0.5f);
        this.localBoundingBox.max.set(0.5f, 0.5f, 0.5f);
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    public boolean isInFluid() {
        return this.fluidImmersionRatio > 0.0f;
    }

    public void update(Zone zone, double deltaTime) {
        if (this.viewDirection.isZero()) {
            this.viewDirection.set(0.0f, 0.0f, 1.0f);
        }
        this.tmpEntityBoundingBox.set(this.localBoundingBox);
        this.tmpEntityBoundingBox.min.add(this.position);
        this.tmpEntityBoundingBox.max.add(this.position);
        this.tmpEntityBoundingBox.update();
        int minBx = (int)Math.floor(this.tmpEntityBoundingBox.min.x);
        int minBy = (int)Math.floor(this.tmpEntityBoundingBox.min.y);
        int minBz = (int)Math.floor(this.tmpEntityBoundingBox.min.z);
        int maxBx = (int)Math.floor(this.tmpEntityBoundingBox.max.x);
        int maxBy = (int)Math.floor(this.tmpEntityBoundingBox.max.y);
        int maxBz = (int)Math.floor(this.tmpEntityBoundingBox.max.z);
        this.fluidImmersionRatio = 0.0f;
        block0: for (int bx = minBx; bx <= maxBx; ++bx) {
            for (int by = maxBy; by >= minBy; --by) {
                for (int bz = minBz; bz <= maxBz; ++bz) {
                    BlockState blockAdj = zone.getBlockState(bx, by, bz);
                    if (blockAdj == null || !blockAdj.isFluid) continue;
                    blockAdj.getBoundingBox(this.tmpBlockBoundingBox, bx, by, bz);
                    if (!this.tmpBlockBoundingBox.intersects(this.tmpEntityBoundingBox)) continue;
                    this.fluidImmersionRatio = Math.max(this.fluidImmersionRatio, 1.0f - (this.tmpEntityBoundingBox.max.y - this.tmpBlockBoundingBox.max.y) / this.tmpEntityBoundingBox.getHeight());
                    this.fluidImmersionRatio = Math.min(this.fluidImmersionRatio, 1.0f);
                    break block0;
                }
            }
        }
        this.acceleration.add(0.0f, -this.velocity.y * 4.0f * this.fluidImmersionRatio, 0.0f);
        float fluidGravModifier = MathUtils.clamp(1.0f - this.fluidImmersionRatio, 0.03f, 1.0f);
        if (this.hasGravity && !this.noClip) {
            boolean goingDown = this.onceVelocity.y + this.velocity.y < 0.0f;
            fluidGravModifier = goingDown ? Math.max(fluidGravModifier, 0.25f) : fluidGravModifier;
            this.acceleration.add(Entity.gravity.x * this.gravityModifier * fluidGravModifier, Entity.gravity.y * this.gravityModifier * fluidGravModifier, Entity.gravity.z * this.gravityModifier * fluidGravModifier);
        }
        this.updatePositions(zone, deltaTime);
    }

    public void playFootstepSound() {
        this.footstepTimer = 0.0f;
    }

    public void updatePositions(Zone zone, double deltaTime) {
        this.lastPosition.set(this.position);
        float ax = this.acceleration.x * (float)deltaTime;
        float ay = this.acceleration.y * (float)deltaTime;
        float az = this.acceleration.z * (float)deltaTime;
        this.velocity.add(ax, ay, az);
        this.velocity.add(this.onceVelocity);
        float vx = this.velocity.x * (float)deltaTime;
        float vy = this.velocity.y * (float)deltaTime;
        float vz = this.velocity.z * (float)deltaTime;
        Vector3 posDiff = new Vector3(vx, vy, vz);
        Vector3 targetPosition = new Vector3(this.position).add(posDiff);
        if (!this.noClip) {
            float d = targetPosition.dst(this.position);
            if (d < 1.0f) {
                this.updateConstraints(zone, targetPosition);
            } else {
                posDiff.set(targetPosition).sub(this.position).scl(1.0f / d);
                targetPosition.set(this.position);
                float floor = (float)Math.floor(d);
                for (float l = 0.0f; l < floor; l += 1.0f) {
                    targetPosition.add(posDiff);
                    this.updateConstraints(zone, targetPosition);
                }
                if (d - floor > 0.0f) {
                    posDiff.scl(d - floor);
                    targetPosition.add(posDiff);
                    this.updateConstraints(zone, targetPosition);
                }
            }
        } else {
            this.position.add(posDiff);
        }
        this.velocity.sub(this.onceVelocity);
        this.acceleration.setZero();
        this.onceVelocity.setZero();
        if (this.isOnGround) {
            this.velocity.setZero();
            if (this.footstepTimer >= 0.45f) {
                this.playFootstepSound();
            }
            float dist = (float)((double)Vector2.dst2(this.lastPosition.x, this.lastPosition.z, this.position.x, this.position.z) / deltaTime);
            if (this.position.x - this.lastPosition.x != 0.0f || this.position.z - this.lastPosition.z != 0.0f) {
                float factor = 1.0f;
                if ((double)dist > 0.3) {
                    factor = 2.0f;
                }
                if ((double)dist < 0.1) {
                    factor = 0.5f;
                }
                if ((double)dist < 0.02) {
                    factor = 0.0f;
                }
                this.footstepTimer = (float)((double)this.footstepTimer + deltaTime * (double)factor);
            }
        }
    }

    public void updateConstraints(Zone zone, Vector3 targetPosition) {
        BlockState blockAbove;
        int baz;
        int bay;
        int bax;
        boolean canStepUp;
        float currentDesiredStepUp;
        boolean didStepUp;
        BlockState blockAdj;
        this.tmpEntityBoundingBox.set(this.localBoundingBox);
        this.tmpEntityBoundingBox.min.add(this.position);
        this.tmpEntityBoundingBox.max.add(this.position);
        this.tmpEntityBoundingBox.min.y = this.localBoundingBox.min.y + targetPosition.y;
        this.tmpEntityBoundingBox.max.y = this.localBoundingBox.max.y + targetPosition.y;
        this.tmpEntityBoundingBox.update();
        int minBx = (int)Math.floor(this.tmpEntityBoundingBox.min.x);
        int minBy = (int)Math.floor(this.tmpEntityBoundingBox.min.y);
        int minBz = (int)Math.floor(this.tmpEntityBoundingBox.min.z);
        int maxBx = (int)Math.floor(this.tmpEntityBoundingBox.max.x);
        int maxBy = (int)Math.floor(this.tmpEntityBoundingBox.max.y);
        int maxBz = (int)Math.floor(this.tmpEntityBoundingBox.max.z);
        boolean isOnGround = false;
        float minPosY = targetPosition.y;
        float maxPosY = targetPosition.y;
        for (int bx = minBx; bx <= maxBx; ++bx) {
            for (int by = minBy; by <= maxBy; ++by) {
                for (int bz = minBz; bz <= maxBz; ++bz) {
                    BlockState blockAdj2 = zone.getBlockState(bx, by, bz);
                    if (blockAdj2 == null || blockAdj2.walkThrough) continue;
                    blockAdj2.getBoundingBox(this.tmpBlockBoundingBox, bx, by, bz);
                    if (!this.tmpBlockBoundingBox.intersects(this.tmpEntityBoundingBox)) continue;
                    blockAdj2.getAllBoundingBoxes(this.tmpBlockBoundingBoxes, bx, by, bz);
                    float oldY = this.tmpEntityBoundingBox.min.y;
                    for (BoundingBox bb : this.tmpBlockBoundingBoxes) {
                        if (!bb.intersects(this.tmpEntityBoundingBox)) continue;
                        this.velocity.y = 0.0f;
                        this.onceVelocity.y = 0.0f;
                        if (oldY <= bb.max.y && oldY >= bb.min.y) {
                            minPosY = Math.max(minPosY, bb.max.y - this.localBoundingBox.min.y);
                            maxPosY = Math.max(maxPosY, minPosY);
                            if (!this.isOnGround) {
                                this.footstepTimer = 0.45f;
                            }
                            isOnGround = true;
                            continue;
                        }
                        maxPosY = Math.min(maxPosY, bb.min.y - this.localBoundingBox.getHeight() - 0.01f);
                    }
                }
            }
        }
        targetPosition.y = MathUtils.clamp(targetPosition.y, minPosY, maxPosY);
        this.isOnGround = isOnGround;
        this.tmpEntityBoundingBox.min.x = this.localBoundingBox.min.x + targetPosition.x;
        this.tmpEntityBoundingBox.max.x = this.localBoundingBox.max.x + targetPosition.x;
        this.tmpEntityBoundingBox.min.y = this.localBoundingBox.min.y + targetPosition.y + 0.01f;
        this.tmpEntityBoundingBox.max.y = this.localBoundingBox.max.y + targetPosition.y;
        this.tmpEntityBoundingBox.update();
        minBx = (int)Math.floor(this.tmpEntityBoundingBox.min.x);
        minBy = (int)Math.floor(this.tmpEntityBoundingBox.min.y);
        minBz = (int)Math.floor(this.tmpEntityBoundingBox.min.z);
        maxBx = (int)Math.floor(this.tmpEntityBoundingBox.max.x);
        maxBy = (int)Math.floor(this.tmpEntityBoundingBox.max.y);
        maxBz = (int)Math.floor(this.tmpEntityBoundingBox.max.z);
        boolean constrainBySneaking = this.shouldConstrainBySneak(zone, this.tmpBlockBoundingBox, this.tmpEntityBoundingBox, minBx, minBy, minBz, maxBx, maxBz);
        if (constrainBySneaking) {
            this.onceVelocity.x = 0.0f;
            this.velocity.x = 0.0f;
            targetPosition.x = this.position.x;
        }
        this.collidedX = false;
        this.collidedZ = false;
        boolean steppedUpForAll = true;
        float desiredStepUp = targetPosition.y;
        if (!constrainBySneaking) {
            for (int bx = minBx; bx <= maxBx; ++bx) {
                for (int by = minBy; by <= maxBy; ++by) {
                    for (int bz = minBz; bz <= maxBz; ++bz) {
                        blockAdj = zone.getBlockState(bx, by, bz);
                        if (blockAdj == null || blockAdj.walkThrough) continue;
                        blockAdj.getBoundingBox(this.tmpBlockBoundingBox, bx, by, bz);
                        if (!this.tmpBlockBoundingBox.intersects(this.tmpEntityBoundingBox)) continue;
                        didStepUp = false;
                        for (BoundingBox bb : blockAdj.getAllBoundingBoxes(this.tmpBlockBoundingBoxes, bx, by, bz)) {
                            if (!bb.intersects(this.tmpEntityBoundingBox)) continue;
                            if (isOnGround && bb.max.y - this.tmpEntityBoundingBox.min.y <= this.maxStepHeight && bb.max.y > this.tmpEntityBoundingBox.min.y) {
                                currentDesiredStepUp = Math.max(desiredStepUp, bb.max.y - this.localBoundingBox.min.y);
                                this.tmpEntityBoundingBox2.set(this.tmpEntityBoundingBox);
                                this.tmpEntityBoundingBox2.min.y = currentDesiredStepUp;
                                this.tmpEntityBoundingBox2.max.y = currentDesiredStepUp + this.localBoundingBox.getHeight();
                                this.tmpEntityBoundingBox2.update();
                                canStepUp = true;
                                block8: for (bax = minBx; bax <= maxBx; ++bax) {
                                    for (bay = by + 1; bay <= maxBy + 1; ++bay) {
                                        for (baz = minBz; baz <= maxBz; ++baz) {
                                            blockAbove = zone.getBlockState(bax, bay, baz);
                                            if (blockAbove == null || blockAbove.walkThrough) continue;
                                            blockAbove.getBoundingBox(this.tmpBlockBoundingBox2, bax, bay, baz);
                                            if (!(canStepUp &= !this.tmpBlockBoundingBox2.intersects(this.tmpEntityBoundingBox2))) break block8;
                                        }
                                    }
                                }
                                if (!canStepUp) continue;
                                desiredStepUp = currentDesiredStepUp;
                                didStepUp = true;
                                continue;
                            }
                            didStepUp = false;
                            steppedUpForAll = false;
                            break;
                        }
                        if (didStepUp) continue;
                        for (BoundingBox bb : blockAdj.getAllBoundingBoxes(this.tmpBlockBoundingBoxes, bx, by, bz)) {
                            if (!bb.intersects(this.tmpEntityBoundingBox)) continue;
                            float centX = this.tmpBlockBoundingBox.getCenterX();
                            targetPosition.x = centX > targetPosition.x ? bb.min.x - this.tmpEntityBoundingBox.getWidth() / 2.0f - 0.01f : bb.max.x + this.tmpEntityBoundingBox.getWidth() / 2.0f + 0.01f;
                            this.collidedX = true;
                            this.onceVelocity.x = 0.0f;
                            this.velocity.x = 0.0f;
                        }
                    }
                }
            }
        }
        if (steppedUpForAll) {
            targetPosition.y = desiredStepUp;
        }
        this.tmpEntityBoundingBox.min.x = this.localBoundingBox.min.x + targetPosition.x;
        this.tmpEntityBoundingBox.max.x = this.localBoundingBox.max.x + targetPosition.x;
        this.tmpEntityBoundingBox.min.y = this.localBoundingBox.min.y + targetPosition.y + 0.01f;
        this.tmpEntityBoundingBox.max.y = this.localBoundingBox.max.y + targetPosition.y;
        this.tmpEntityBoundingBox.min.z = this.localBoundingBox.min.z + targetPosition.z;
        this.tmpEntityBoundingBox.max.z = this.localBoundingBox.max.z + targetPosition.z;
        this.tmpEntityBoundingBox.update();
        minBx = (int)Math.floor(this.tmpEntityBoundingBox.min.x);
        minBy = (int)Math.floor(this.tmpEntityBoundingBox.min.y);
        minBz = (int)Math.floor(this.tmpEntityBoundingBox.min.z);
        maxBx = (int)Math.floor(this.tmpEntityBoundingBox.max.x);
        maxBy = (int)Math.floor(this.tmpEntityBoundingBox.max.y);
        maxBz = (int)Math.floor(this.tmpEntityBoundingBox.max.z);
        constrainBySneaking = this.shouldConstrainBySneak(zone, this.tmpBlockBoundingBox, this.tmpEntityBoundingBox, minBx, minBy, minBz, maxBx, maxBz);
        if (constrainBySneaking) {
            this.onceVelocity.z = 0.0f;
            this.velocity.z = 0.0f;
            targetPosition.z = this.position.z;
        }
        steppedUpForAll = true;
        desiredStepUp = targetPosition.y;
        if (!constrainBySneaking) {
            for (int bx = minBx; bx <= maxBx; ++bx) {
                for (int by = minBy; by <= maxBy; ++by) {
                    for (int bz = minBz; bz <= maxBz; ++bz) {
                        blockAdj = zone.getBlockState(bx, by, bz);
                        if (blockAdj == null || blockAdj.walkThrough) continue;
                        blockAdj.getBoundingBox(this.tmpBlockBoundingBox, bx, by, bz);
                        if (!this.tmpBlockBoundingBox.intersects(this.tmpEntityBoundingBox)) continue;
                        didStepUp = false;
                        for (BoundingBox bb : blockAdj.getAllBoundingBoxes(this.tmpBlockBoundingBoxes, bx, by, bz)) {
                            if (!bb.intersects(this.tmpEntityBoundingBox)) continue;
                            if (isOnGround && bb.max.y - this.tmpEntityBoundingBox.min.y <= this.maxStepHeight && bb.max.y > this.tmpEntityBoundingBox.min.y) {
                                currentDesiredStepUp = Math.max(desiredStepUp, bb.max.y - this.localBoundingBox.min.y);
                                this.tmpEntityBoundingBox2.set(this.tmpEntityBoundingBox);
                                this.tmpEntityBoundingBox2.min.y = currentDesiredStepUp;
                                this.tmpEntityBoundingBox2.max.y = currentDesiredStepUp + this.localBoundingBox.getHeight();
                                this.tmpEntityBoundingBox2.update();
                                canStepUp = true;
                                block16: for (bax = minBx; bax <= maxBx; ++bax) {
                                    for (bay = by + 1; bay <= maxBy + 1; ++bay) {
                                        for (baz = minBz; baz <= maxBz; ++baz) {
                                            blockAbove = zone.getBlockState(bax, bay, baz);
                                            if (blockAbove == null || blockAbove.walkThrough) continue;
                                            blockAbove.getBoundingBox(this.tmpBlockBoundingBox2, bax, bay, baz);
                                            if (!(canStepUp &= !this.tmpBlockBoundingBox2.intersects(this.tmpEntityBoundingBox2))) break block16;
                                        }
                                    }
                                }
                                if (!canStepUp) continue;
                                desiredStepUp = currentDesiredStepUp;
                                didStepUp = true;
                                continue;
                            }
                            didStepUp = false;
                            steppedUpForAll = false;
                            break;
                        }
                        if (didStepUp) continue;
                        for (BoundingBox bb : blockAdj.getAllBoundingBoxes(this.tmpBlockBoundingBoxes, bx, by, bz)) {
                            if (!bb.intersects(this.tmpEntityBoundingBox)) continue;
                            float centZ = this.tmpBlockBoundingBox.getCenterZ();
                            targetPosition.z = centZ > targetPosition.z ? bb.min.z - this.tmpEntityBoundingBox.getDepth() / 2.0f - 0.01f : bb.max.z + this.tmpEntityBoundingBox.getDepth() / 2.0f + 0.01f;
                            this.collidedZ = true;
                            this.onceVelocity.z = 0.0f;
                            this.velocity.z = 0.0f;
                        }
                    }
                }
            }
        }
        if (steppedUpForAll) {
            targetPosition.y = desiredStepUp;
        }
        this.position.set(targetPosition.x, targetPosition.y, targetPosition.z);
    }

    private boolean shouldConstrainBySneak(Zone zone, BoundingBox blockBoundingBox, BoundingBox entityBoundingBox, int minBx, int minBy, int minBz, int maxBx, int maxBz) {
        if (!this.isSneaking || !this.isOnGround) {
            return false;
        }
        BoundingBox bb = new BoundingBox(entityBoundingBox);
        bb.min.y = minBy - 1;
        bb.update();
        for (int bx = minBx; bx <= maxBx; ++bx) {
            for (int yo = 0; yo <= 1; ++yo) {
                for (int bz = minBz; bz <= maxBz; ++bz) {
                    BlockState blockBelowAdj = zone.getBlockState(bx, minBy - yo, bz);
                    if (blockBelowAdj == null || blockBelowAdj.walkThrough) continue;
                    blockBelowAdj.getBoundingBox(blockBoundingBox, bx, minBy - yo, bz);
                    if (!blockBoundingBox.intersects(bb)) continue;
                    return false;
                }
            }
        }
        return true;
    }

    public Vector3 getPosition() {
        return this.position;
    }

    public void accelerate(float x, float y, float z) {
        this.acceleration.add(x, y, z);
    }
}