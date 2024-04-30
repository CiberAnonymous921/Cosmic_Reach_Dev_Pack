package finalforeach.cosmicreach.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import finalforeach.cosmicreach.BlockGame;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.settings.ControlSettings;
import finalforeach.cosmicreach.settings.Controls;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.World;
import finalforeach.cosmicreach.world.WorldLoader;
import finalforeach.cosmicreach.world.Zone;

public class Player {
    public String zoneId;
    private Entity controlledEntity;
    public boolean isSprinting;
    public boolean isProne;
    public boolean isProneIntended;
    public boolean isSneakIntended;
    float lastPlayerSpeed = 5.0f;
    private transient float startMouseX;
    private transient float startMouseY;
    private transient BoundingBox standingBoundingBox = new BoundingBox();
    private transient BoundingBox proneBoundingBox = new BoundingBox();
    private transient BoundingBox sneakingBoundingBox = new BoundingBox();
    Vector3 standingViewPositionOffset = new Vector3(0.0f, 1.8f, 0.0f);
    Vector3 proneViewPositionOffset = new Vector3(0.0f, 0.9f, 0.0f);
    Vector3 sneakingViewPositionOffset = new Vector3(0.0f, 1.37f, 0.0f);
    private transient BoundingBox tmpBlockBoundingBox = new BoundingBox();
    private transient BoundingBox tmpGlobalBoundingBox = new BoundingBox();
    private Vector3 lastVelocity = new Vector3();
    private Vector3 wallJumpVelocity = new Vector3();

    public Player() {
        this.standingBoundingBox.min.set(-0.25f, 0.0f, -0.25f);
        this.standingBoundingBox.max.set(0.25f, 1.9f, 0.25f);
        this.standingBoundingBox.update();
        this.proneBoundingBox.min.set(-0.25f, 0.0f, -0.25f);
        this.proneBoundingBox.max.set(0.25f, 0.95f, 0.25f);
        this.proneBoundingBox.update();
        this.sneakingBoundingBox.min.set(-0.25f, 0.0f, -0.25f);
        this.sneakingBoundingBox.max.set(0.25f, 1.45f, 0.25f);
        this.sneakingBoundingBox.update();
    }

    public void setEntity(Entity entity) {
        this.controlledEntity = entity;
        if (entity != null) {
            this.controlledEntity.localBoundingBox.set(this.standingBoundingBox);
            this.controlledEntity.viewPositionOffset.set(this.standingViewPositionOffset);
        }
    }

    public Entity getEntity() {
        return this.controlledEntity;
    }

    public void updateCamera(Camera playerCamera, float partTick) {
        if (this.controlledEntity != null) {
            Vector3 camPos = playerCamera.position;
            Vector3 lastPos = this.controlledEntity.lastPosition;
            Vector3 nextPos = this.controlledEntity.getPosition();
            camPos.x = MathUtils.lerp(lastPos.x, nextPos.x, partTick);
            camPos.y = MathUtils.lerp(lastPos.y, nextPos.y, partTick);
            camPos.z = MathUtils.lerp(lastPos.z, nextPos.z, partTick);
            camPos.add(this.controlledEntity.viewPositionOffset);
            playerCamera.direction.set(this.controlledEntity.viewDirection);
            if (playerCamera.direction.isZero()) {
                playerCamera.direction.set(0.0f, 0.0f, 1.0f);
            }
        }
        playerCamera.up.set(0.0f, 1.0f, 0.0f);
        playerCamera.update();
    }

    public void proneCheck(Zone zone) {
        if (Controls.pronePressed()) {
            this.isProneIntended = !this.isProneIntended;
        }
        this.isProne = this.isProneIntended;
        if (!this.isProne && !this.controlledEntity.noClip) {
            this.tmpGlobalBoundingBox.set(this.sneakingBoundingBox);
            this.tmpGlobalBoundingBox.min.add(this.controlledEntity.position);
            this.tmpGlobalBoundingBox.min.y += this.controlledEntity.maxStepHeight;
            this.tmpGlobalBoundingBox.max.add(this.controlledEntity.position);
            this.tmpGlobalBoundingBox.update();
            int minBx = (int)Math.floor(this.tmpGlobalBoundingBox.min.x);
            int minBy = (int)Math.floor(this.tmpGlobalBoundingBox.min.y);
            int minBz = (int)Math.floor(this.tmpGlobalBoundingBox.min.z);
            int maxBx = (int)Math.floor(this.tmpGlobalBoundingBox.max.x);
            int maxBy = (int)Math.floor(this.tmpGlobalBoundingBox.max.y);
            int maxBz = (int)Math.floor(this.tmpGlobalBoundingBox.max.z);
            for (int bx = minBx; bx <= maxBx; ++bx) {
                for (int by = minBy; by <= maxBy; ++by) {
                    for (int bz = minBz; bz <= maxBz; ++bz) {
                        BlockState blockAdj = zone.getBlockState(bx, by, bz);
                        if (blockAdj == null || blockAdj.walkThrough) continue;
                        blockAdj.getBoundingBox(this.tmpBlockBoundingBox, bx, by, bz);
                        if (!this.tmpBlockBoundingBox.intersects(this.tmpGlobalBoundingBox)) continue;
                        this.isProne = true;
                        return;
                    }
                }
            }
        }
    }

    public void crouchCheck(Zone zone) {
        this.controlledEntity.isSneaking = this.isSneakIntended = Controls.crouchPressed();
        if (!this.controlledEntity.isSneaking && !this.controlledEntity.noClip) {
            this.tmpGlobalBoundingBox.set(this.standingBoundingBox);
            this.tmpGlobalBoundingBox.min.add(this.controlledEntity.position);
            this.tmpGlobalBoundingBox.min.y += this.controlledEntity.maxStepHeight;
            this.tmpGlobalBoundingBox.max.add(this.controlledEntity.position);
            this.tmpGlobalBoundingBox.update();
            int minBx = (int)Math.floor(this.tmpGlobalBoundingBox.min.x);
            int minBy = (int)Math.floor(this.tmpGlobalBoundingBox.min.y);
            int minBz = (int)Math.floor(this.tmpGlobalBoundingBox.min.z);
            int maxBx = (int)Math.floor(this.tmpGlobalBoundingBox.max.x);
            int maxBy = (int)Math.floor(this.tmpGlobalBoundingBox.max.y);
            int maxBz = (int)Math.floor(this.tmpGlobalBoundingBox.max.z);
            for (int bx = minBx; bx <= maxBx; ++bx) {
                for (int by = minBy; by <= maxBy; ++by) {
                    for (int bz = minBz; bz <= maxBz; ++bz) {
                        BlockState blockAdj = zone.getBlockState(bx, by, bz);
                        if (blockAdj == null || blockAdj.walkThrough) continue;
                        blockAdj.getBoundingBox(this.tmpBlockBoundingBox, bx, by, bz);
                        if (!this.tmpBlockBoundingBox.intersects(this.tmpGlobalBoundingBox)) continue;
                        this.controlledEntity.isSneaking = true;
                        return;
                    }
                }
            }
        }
    }

    public void respawn(World world) {
        this.respawn(this.getZone(world));
    }

    public void respawn(Zone zone) {
        float spawnX = zone.spawnPoint.x;
        float spawnY = zone.spawnPoint.y;
        float spawnZ = zone.spawnPoint.z;
        boolean collidesWithGround = true;
        while (collidesWithGround) {
            collidesWithGround = false;
            this.tmpGlobalBoundingBox.set(this.proneBoundingBox);
            this.tmpGlobalBoundingBox.min.add(spawnX, spawnY, spawnZ);
            this.tmpGlobalBoundingBox.min.y += this.controlledEntity.maxStepHeight;
            this.tmpGlobalBoundingBox.max.add(spawnX, spawnY, spawnZ);
            this.tmpGlobalBoundingBox.update();
            int minBx = (int)Math.floor(this.tmpGlobalBoundingBox.min.x);
            int minBy = (int)Math.floor(this.tmpGlobalBoundingBox.min.y);
            int minBz = (int)Math.floor(this.tmpGlobalBoundingBox.min.z);
            int maxBx = (int)Math.floor(this.tmpGlobalBoundingBox.max.x);
            int maxBy = (int)Math.floor(this.tmpGlobalBoundingBox.max.y);
            int maxBz = (int)Math.floor(this.tmpGlobalBoundingBox.max.z);
            block1: for (int bx = minBx; bx <= maxBx; ++bx) {
                for (int by = minBy; by <= maxBy; ++by) {
                    for (int bz = minBz; bz <= maxBz; ++bz) {
                        BlockState blockAdj = zone.getBlockState(bx, by, bz);
                        if (blockAdj == null || blockAdj.walkThrough) continue;
                        blockAdj.getBoundingBox(this.tmpBlockBoundingBox, bx, by, bz);
                        if (!this.tmpBlockBoundingBox.intersects(this.tmpGlobalBoundingBox)) continue;
                        collidesWithGround = true;
                        break block1;
                    }
                }
            }
            if (!collidesWithGround) break;
            spawnY += 1.0f;
        }
        this.controlledEntity.velocity.setZero();
        this.setPosition(spawnX, spawnY, spawnZ);
        WorldLoader.worldLoader.readyToPlay = false;
        InGame.switchToGameState(GameState.LOADING_GAME);
    }

    public void update(Zone zone) {
        Vector3 viewDir = this.controlledEntity.viewDirection;
        if (Float.isNaN(viewDir.x) || Float.isNaN(viewDir.y) || Float.isNaN(viewDir.z)) {
            viewDir.setZero();
        }
        if (Controls.debugNoClipPressed()) {
            this.controlledEntity.noClip = !this.controlledEntity.noClip;
            this.controlledEntity.velocity.setZero();
        }
        if (Controls.sprintPressed()) {
            this.isSprinting = true;
        }
        this.proneCheck(zone);
        if (!this.isProne) {
            this.crouchCheck(zone);
        }
        float playerSpeed = 200.0f;
        if (this.isSprinting) {
            playerSpeed *= 2.0f;
        }
        if (this.isProne) {
            this.controlledEntity.viewPositionOffset.set(this.proneViewPositionOffset);
            this.controlledEntity.localBoundingBox.set(this.proneBoundingBox);
            playerSpeed *= 0.3f;
        } else {
            this.controlledEntity.viewPositionOffset.set(this.standingViewPositionOffset);
            this.controlledEntity.localBoundingBox.set(this.standingBoundingBox);
        }
        if (this.controlledEntity.isSneaking) {
            playerSpeed /= 2.0f;
            if (!this.isProne) {
                this.controlledEntity.viewPositionOffset.set(this.sneakingViewPositionOffset);
                this.controlledEntity.localBoundingBox.set(this.sneakingBoundingBox);
            }
        }
        if (this.controlledEntity.noClip) {
            playerSpeed *= 2.0f;
            this.wallJumpVelocity.setZero();
        }
        playerSpeed /= 3.0f;
        Vector3 movement = new Vector3();
        Vector3 forward = new Vector3(viewDir);
        forward.y = 0.0f;
        forward.nor();
        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();
        if (GameState.lastFrameGameState != GameState.IN_GAME) {
            this.startMouseX = screenX;
            this.startMouseY = screenY;
        }
        if (BlockGame.isFocused && !UI.uiNeedMouse && GameState.currentGameState == GameState.IN_GAME) {
            float ySign = ControlSettings.invertedMouse.getValue() ? -1.0f : 1.0f;
            float screenDim = Math.max(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            float mouseSensitivity = ControlSettings.mouseSensitivity.getValue();
            float deltaX = mouseSensitivity * ((float)screenX - this.startMouseX) / screenDim;
            float deltaY = ySign * mouseSensitivity * (this.startMouseY - (float)screenY) / screenDim;
            if (Float.isNaN(deltaX)) {
                deltaX = 0.0f;
            }
            if (Float.isNaN(deltaY)) {
                deltaY = 0.0f;
            }
            float invControllerSensitivityX = 50.0f;
            float invControllerSensitivityY = 100.0f;
            float controllerX = Controls.getRightXAxis() / invControllerSensitivityX;
            float controllerY = -Controls.getRightYAxis() / invControllerSensitivityY;
            if ((double)Math.abs(controllerX) > 0.01 / (double)invControllerSensitivityX) {
                deltaX += controllerX;
            }
            if ((double)Math.abs(controllerY) > 0.01 / (double)invControllerSensitivityY) {
                deltaY += controllerY;
            }
            Vector3 tmpV1 = new Vector3(viewDir).crs(0.0f, 1.0f, 0.0f);
            tmpV1.y = 0.0f;
            tmpV1.nor();
            float oldX = viewDir.x;
            float oldY = viewDir.y;
            float oldZ = viewDir.z;
            this.controlledEntity.viewDirection.rotate(deltaY * 360.0f, tmpV1.x, tmpV1.y, tmpV1.z);
            if (Math.signum(oldX) != Math.signum(viewDir.x) || Math.signum(oldZ) != Math.signum(viewDir.z)) {
                if (Math.abs(viewDir.y) < Math.abs(oldY)) {
                    viewDir.x = oldX;
                    viewDir.y = oldY;
                    viewDir.z = oldZ;
                } else {
                    viewDir.x = Math.abs(viewDir.x) * Math.signum(oldX);
                    viewDir.z = Math.abs(viewDir.z) * Math.signum(oldZ);
                }
            }
            viewDir.rotate(deltaX * -360.0f, 0.0f, 1.0f, 0.0f);
            viewDir.nor();
            Gdx.input.setCursorCatched(true);
        } else {
            Gdx.input.setCursorCatched(false);
        }
        this.startMouseX = screenX;
        this.startMouseY = screenY;
        float forwardSpeed = Controls.forwardPressed();
        float backwardSpeed = Controls.backwardPressed();
        float leftSpeed = Controls.leftPressed();
        float rightSpeed = Controls.rightPressed();
        movement.add(forward.x * forwardSpeed, forward.y * forwardSpeed, forward.z * forwardSpeed);
        movement.sub(forward.x * backwardSpeed, forward.y * backwardSpeed, forward.z * backwardSpeed);
        movement.add(new Vector3(forward).crs(0.0f, 1.0f, 0.0f).scl(-1.0f * leftSpeed));
        movement.add(new Vector3(forward).crs(0.0f, 1.0f, 0.0f).scl(rightSpeed));
        if (movement.len2() < 0.01f) {
            movement.setZero();
        }
        if (!(this.controlledEntity.noClip || this.controlledEntity.isOnGround || movement.x == 0.0f && movement.z == 0.0f)) {
            playerSpeed = this.lastPlayerSpeed;
        }
        this.lastPlayerSpeed = playerSpeed;
        if (movement.len2() > 1.0f) {
            movement.nor().scl(playerSpeed);
        } else {
            movement.scl(playerSpeed);
        }
        boolean isWallRunning = !this.controlledEntity.isInFluid() && this.isSprinting && !this.controlledEntity.isOnGround && (this.controlledEntity.collidedX && Math.abs(movement.z) > Math.abs(movement.x) || this.controlledEntity.collidedZ && Math.abs(movement.x) > Math.abs(movement.z));
        int collideCount = this.controlledEntity.collidedX ? 1 : 0;
        if ((collideCount += this.controlledEntity.collidedZ ? 1 : 0) > 1) {
            isWallRunning = false;
        }
        this.controlledEntity.gravityModifier = isWallRunning && this.controlledEntity.lastPosition.y >= this.controlledEntity.position.y ? 0.05f : 1.0f;
        if (this.controlledEntity.isInFluid() || this.controlledEntity.isOnGround) {
            this.wallJumpVelocity.setZero();
        }
        if (this.controlledEntity.collidedX) {
            this.wallJumpVelocity.x = Math.min(this.wallJumpVelocity.x, 0.0f);
        }
        if (this.controlledEntity.collidedZ) {
            this.wallJumpVelocity.z = Math.min(this.wallJumpVelocity.z, 0.0f);
        }
        if (this.controlledEntity.noClip) {
            if (Controls.crouchPressed()) {
                movement.add(0.0f, -playerSpeed, 0.0f);
            }
            if (Controls.jumpPressed()) {
                movement.add(0.0f, playerSpeed, 0.0f);
            }
        } else if (Controls.jumpPressed()) {
            if (this.controlledEntity.isOnGround) {
                this.controlledEntity.isOnGround = false;
                this.controlledEntity.velocity.add(0.0f, 7.0f, 0.0f);
            } else if (this.controlledEntity.isInFluid() && this.controlledEntity.fluidImmersionRatio > 0.5f) {
                this.controlledEntity.accelerate(0.0f, 600.0f * Gdx.graphics.getDeltaTime(), 0.0f);
            }
        }
        if (movement.len2() == 0.0f) {
            this.isSprinting = false;
        }
        this.wallJumpVelocity.scl(Gdx.graphics.getDeltaTime());
        this.controlledEntity.onceVelocity.add(this.wallJumpVelocity);
        movement.scl(Gdx.graphics.getDeltaTime());
        this.controlledEntity.onceVelocity.add(movement.x, movement.y, movement.z);
        this.lastVelocity.set(this.controlledEntity.velocity).add(this.controlledEntity.onceVelocity);
    }

    public void setPosition(float x, float y, float z) {
        this.controlledEntity.setPosition(x, y, z);
    }

    public Zone getZone(World world) {
        return world.getZone(this.zoneId);
    }

    public Chunk getChunk(World world) {
        int bx = (int)Math.floor(this.controlledEntity.position.x);
        int by = (int)Math.floor(this.controlledEntity.position.y);
        int bz = (int)Math.floor(this.controlledEntity.position.z);
        Zone z = this.getZone(world);
        if (z == null) {
            return null;
        }
        return z.getChunkAtBlock(bx, by, bz);
    }

    public short getBlockLight(World world) {
        int bx = (int)Math.floor(this.controlledEntity.position.x);
        int by = (int)Math.floor(this.controlledEntity.position.y);
        int bz = (int)Math.floor(this.controlledEntity.position.z);
        Chunk chunk = this.getChunk(world);
        if (chunk == null) {
            return 0;
        }
        return chunk.getBlockLight(bx - chunk.blockX, by - chunk.blockY, bz - chunk.blockZ);
    }

    public int getSkyLight(World world) {
        int bx = (int)Math.floor(this.controlledEntity.position.x);
        int by = (int)Math.floor(this.controlledEntity.position.y);
        int bz = (int)Math.floor(this.controlledEntity.position.z);
        Chunk chunk = this.getChunk(world);
        if (chunk == null) {
            return 0;
        }
        return chunk.getSkyLight(bx - chunk.blockX, by - chunk.blockY, bz - chunk.blockZ);
    }
}