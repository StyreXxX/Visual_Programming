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

    private final float JUMP_SPEED = 875f;
    private final float PLAYER_SPEED = 315f;
    private static final int INITIAL_MAX_HEALTH = 10;

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

    public void update(float delta, Array<Projectile> projectiles, MapObjects blocks, float scale) {
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

        if (isGrounded && !isAttacking && (Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE))) {
            velocityY = JUMP_SPEED;
        }

        float oldX = bounds.x;
        if (!isAttacking) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                bounds.x -= PLAYER_SPEED * delta;
                facingRight = false;
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                bounds.x += PLAYER_SPEED * delta;
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
        } else if (velocityY != 0) {
            currentFrame = (hasStaff ? staffWalkAnim : walkAnim).getKeyFrame(0);
        } else if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            currentFrame = (hasStaff ? staffWalkAnim : walkAnim).getKeyFrame(stateTime, true);
        }

        float drawHeight = 100f;
        float aspect = (float) currentFrame.getRegionWidth() / currentFrame.getRegionHeight();
        float drawWidth = drawHeight * aspect;
        float drawX = bounds.x + (bounds.width / 2f) - (drawWidth / 2f);

        drawFlipped(batch, currentFrame, drawX, bounds.y, drawWidth, drawHeight, facingRight);
    }

    @Override
    public void dispose() {
        // Textures are managed by AssetLoader
    }
}
