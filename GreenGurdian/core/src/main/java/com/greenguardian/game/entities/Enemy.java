package com.greenguardian.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.math.Rectangle;
import com.greenguardian.game.assets.AssetLoader;

public class Enemy extends Entity {
    private static final int INITIAL_MAX_HEALTH = 3;

    private float startX;
    private float patrolRange = 100f;
    private float speed = 50f;

    public Enemy(float startX, float startY, AssetLoader assets) {
        super(startX, startY, 30, 70);
        this.maxHealth = INITIAL_MAX_HEALTH;
        this.health = INITIAL_MAX_HEALTH;
        this.startX = startX;

        walkSheet = assets.enemyWalkSheet;
        attackSheet = assets.enemyAttackSheet;
        deathSheet = assets.enemyDeathSheet;

        walkAnim = createAnimation(walkSheet, 4, 0.2f);
        attackAnim = createAnimation(attackSheet, 3, 0.15f);
        deathAnim = createAnimation(deathSheet, 4, 0.2f);

        idleFrame = new TextureRegion(walkSheet, 0, 0, walkSheet.getWidth() / 4, walkSheet.getHeight());
    }

    public boolean playerInRange(Player player) {
        return (getBounds().x + 100 > player.getBounds().x || getBounds().x - 100 < player.getBounds().x);
    }

    public void update(float delta, Player player, MapObjects blocks, float scale) {
        if (!isDead) {
            float oldY = bounds.y;
            velocityY += GRAVITY * delta;
            bounds.y += velocityY * delta;

            if (checkCollision(bounds, blocks, scale)) {
                bounds.y = oldY;
                velocityY = 0;
            }

            stateTime += delta;
            float oldX = bounds.x;

            Rectangle attackReach = new Rectangle(bounds.x - 15, bounds.y, bounds.width + 30, bounds.height);
            if (!isAttacking && attackReach.overlaps(player.getBounds()) && !player.isDead()) {
                isAttacking = true;
                stateTime = 0;
                facingRight = player.getBounds().x > bounds.x;
            }
            
            if (isAttacking) {
                if (attackAnim.isAnimationFinished(stateTime)) {
                    isAttacking = false;
                    if (attackReach.overlaps(player.getBounds()) && !player.isDead()) {
                        player.takeDamage(1);
                    }
                }
            } else {
                if (facingRight) {
                    bounds.x += speed * delta;
                    if (bounds.x > startX + patrolRange) facingRight = false;
                } else {
                    bounds.x -= speed * delta;
                    if (bounds.x < startX - patrolRange) facingRight = true;
                }

                if (checkCollision(bounds, blocks, scale)) {
                    bounds.x = oldX;
                    facingRight = !facingRight;
                }
            }
        } else {
            stateTime += delta;
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = idleFrame;
        if (isDead) {
            currentFrame = deathAnim.getKeyFrame(stateTime, false);
        } else if (isAttacking) {
            currentFrame = attackAnim.getKeyFrame(stateTime, false);
        } else {
            currentFrame = walkAnim.getKeyFrame(stateTime, true);
        }
        float drawHeight = 100f;
        float aspect = (float) currentFrame.getRegionWidth() / currentFrame.getRegionHeight();
        float drawWidth = drawHeight * aspect;
        float drawX = bounds.x + (bounds.width / 2f) - (drawWidth / 2f);

        drawFlipped(batch, currentFrame, drawX, bounds.y, drawWidth, drawHeight, facingRight);
    }

    public void drawFloatingHealth(ShapeRenderer shapeRenderer) {
        if (!isDead && health < maxHealth) {
            float width = 40f;
            float height = 5f;
            float x = bounds.x + (bounds.width / 2) - (width / 2);
            float y = bounds.y + bounds.height + 15;

            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(x, y, width, height);

            shapeRenderer.setColor(Color.GREEN);
            float currentHealthWidth = width * ((float) health / maxHealth);
            shapeRenderer.rect(x, y, Math.max(0, currentHealthWidth), height);
        }
    }

    @Override
    public void dispose() {
        // Textures managed by AssetLoader
    }
}
