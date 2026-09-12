package com.greenguardian.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.utils.Array;
import com.greenguardian.game.assets.AssetLoader;

public class Player extends Entity {
    private Texture staffWalkSheet, staffAttackSheet;
    private Animation<TextureRegion> staffWalkAnim, staffAttackAnim;
    // private TextureRegion staffIdleFrame;

    private boolean hasStaff = false;
    private boolean inWater = false;

    private final float JUMP_SPEED = 1000f;
    private final float PLAYER_SPEED = 500f;
    private static final int INITIAL_MAX_HEALTH = 1000;

    public Player(float startX, float startY, AssetLoader assets) {
        super(startX, startY, 30, 70);
        this.maxHealth = INITIAL_MAX_HEALTH;
        this.health = INITIAL_MAX_HEALTH;

        walkSheet = assets.playerWalkSheet;
        attackSheet = assets.playerAttackSheet;
        deathSheet = assets.playerDeathSheet;
        standSheet = assets.playerStandSheet;

        staffWalkSheet = assets.playerStaffWalkSheet;
        staffAttackSheet = assets.playerStaffAttackSheet;

        walkAnim = createAnimation(walkSheet, 4, 0.15f);
        attackAnim = createAnimation(attackSheet, 3, 0.1f);
        deathAnim = createAnimation(deathSheet, 4, 0.2f);

        staffWalkAnim = createAnimation(staffWalkSheet, 4, 0.15f);
        staffAttackAnim = createAnimation(staffAttackSheet, 3, 0.1f);

        idleFrame = new TextureRegion(standSheet);
        // staffIdleFrame = new TextureRegion(staffWalkSheet, 0, 0, staffWalkSheet.getWidth() / 4, staffWalkSheet.getHeight());
    }

    public void equipStaff() {
        this.hasStaff = true;
    }

    public boolean hasStaff() {
        return hasStaff;
    }

    public void update(float delta, Array<Projectile> projectiles, MapObjects blocks, MapObjects waterZones, float scale) {
        if (health <= 0 && !isDead) {
            isDead = true;
            stateTime = 0;
        }

        stateTime += delta;
        if (isDead) return;

        float oldY = bounds.y;
        velocityY += GRAVITY * delta;
        bounds.y += velocityY * delta;

        boolean isGrounded = false;
        if (checkCollision(bounds, blocks, scale)) {
            bounds.y = oldY;
            if (velocityY < 0) isGrounded = true;
            velocityY = 0;
        }

        // Check water collision
        inWater = false;
        if (waterZones != null) {
            inWater = checkCollision(bounds, waterZones, scale);
        }

        // Reduce jump height in water
        float currentJumpSpeed = inWater ? JUMP_SPEED * 0.6f : JUMP_SPEED;
        if (isGrounded && !isAttacking && (Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE))) {
            velocityY = currentJumpSpeed;
        }

        float oldX = bounds.x;

        // Reduce movement speed by 60% in water
        float currentSpeed = inWater ? PLAYER_SPEED * 0.4f : PLAYER_SPEED;

        if (!isAttacking) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                bounds.x -= currentSpeed * delta;
                facingRight = false;
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                bounds.x += currentSpeed * delta;
                facingRight = true;
            }

            if (checkCollision(bounds, blocks, scale)) {
                bounds.x = oldX;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                isAttacking = true;
                stateTime = 0;
                spawnProjectile(projectiles);
            }
        }

        Animation<TextureRegion> currentAttackAnim = hasStaff ? staffAttackAnim : attackAnim;
        if (isAttacking && currentAttackAnim.isAnimationFinished(stateTime)) {
            isAttacking = false;
        }
    }

    private void spawnProjectile(Array<Projectile> projectiles) {
        float px = facingRight ? bounds.x + 50 : bounds.x - 20;
        float py = bounds.y + 30;

        int projType = hasStaff ? 2 : 1;
        projectiles.add(new Projectile(px, py, facingRight, projType));
    }

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = idleFrame;

        if (isDead) {
            currentFrame = deathAnim.getKeyFrame(stateTime, false);
        } else if (isAttacking) {
            currentFrame = (hasStaff ? staffAttackAnim : attackAnim).getKeyFrame(stateTime, false);
        } else if (velocityY != 0 && !inWater) {
            currentFrame = (hasStaff ? staffWalkAnim : walkAnim).getKeyFrame(0);
        } else if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            currentFrame = (hasStaff ? staffWalkAnim : walkAnim).getKeyFrame(stateTime, true);
        }

        // Submerge logic: slice the bottom 40% of the sprite
        float fullDrawHeight = 100f;
        float drawHeight = inWater ? 60f : fullDrawHeight;

        TextureRegion renderFrame = currentFrame;
        if (inWater) {
            // Create a temporary region capturing only the top 60% of the texture
            renderFrame = new TextureRegion(currentFrame, 0, 0,
                currentFrame.getRegionWidth(),
                (int)(currentFrame.getRegionHeight() * 0.6f));
        }

        float aspect = (float) currentFrame.getRegionWidth() / currentFrame.getRegionHeight();
        float drawWidth = fullDrawHeight * aspect;
        float drawX = bounds.x + (bounds.width / 2f) - (drawWidth / 2f);

        // Shift the Y rendering position up by 40 units to compensate for the missing bottom half
        float drawY = inWater ? bounds.y + 40f : bounds.y;

        drawFlipped(batch, renderFrame, drawX, drawY, drawWidth, drawHeight, facingRight);
    }

    @Override
    public void dispose() {
        // Textures are managed by AssetLoader
    }
}
