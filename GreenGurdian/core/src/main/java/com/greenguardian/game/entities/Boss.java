package com.greenguardian.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.greenguardian.game.assets.AssetLoader;

public class Boss extends Entity {
    private Texture specialAttackSheet, dashSheet, chargeSheet;
    private Animation<TextureRegion> specialAttackAnim, dashAnim, chargeAnim;

    public boolean isAwake = false;
    private boolean isSpecialAttacking = false;
    private boolean isDashing = false;
    private boolean isCharging = false;

    private final float BASE_JUMP_SPEED = 700f;
    private float dashTime = 0f;
    private float dashSpeed = 500f;

    private float patrolMinX;
    private float patrolMaxX;
    private float baseSpeed = 100f;

    public Boss(float startX, float startY, AssetLoader assets) {
        super(startX, startY, 60, 120, 40);

        walkSheet = assets.bossWalkSheet;
        attackSheet = assets.bossAttackSheet;
        specialAttackSheet = assets.bossSpecialAttackSheet;
        dashSheet = assets.bossDashSheet;
        deathSheet = assets.bossDeathSheet;
        chargeSheet = assets.bossChargeSheet;

        walkAnim = createAnimation(walkSheet, 4, 0.2f);
        attackAnim = createAnimation(attackSheet, 4, 0.15f);
        specialAttackAnim = createAnimation(specialAttackSheet, 4, 0.15f);
        dashAnim = createAnimation(dashSheet, 3, 0.1f);
        deathAnim = createAnimation(deathSheet, 4, 0.2f);
        chargeAnim = createAnimation(chargeSheet, 4, 0.15f);

        idleFrame = new TextureRegion(walkSheet, 0, 0, walkSheet.getWidth() / 4, walkSheet.getHeight());

        patrolMinX = startX - 200f;
        patrolMaxX = startX + 200f;
    }

    public void update(float delta, Player player, MapObjects blocks) {
        if (!isDead) {
            float oldY = bounds.y;
            velocityY += GRAVITY * delta;
            bounds.y += velocityY * delta;

            boolean isGrounded = false;
            if (checkCollision(bounds, blocks)) {
                bounds.y = oldY;
                if (velocityY < 0) isGrounded = true;
                velocityY = 0;
            }

            float distanceToPlayer = Math.abs(player.getBounds().x - bounds.x);

            if (!isAwake) {
                if (distanceToPlayer < 600) {
                    isAwake = true;
                } else {
                    return;
                }
            }

            stateTime += delta;
            float oldX = bounds.x;

            float currentSpeed = baseSpeed;
            float currentJumpSpeed = BASE_JUMP_SPEED;
            float jumpProbability = 0.01f;
            float teleportProbability = 0f;

            if (health <= maxHealth * 0.2f) {
                currentSpeed = baseSpeed * 2.2f;
                currentJumpSpeed = BASE_JUMP_SPEED + 150f;
                jumpProbability = 0.05f;
                teleportProbability = 0.015f;
            } else if (health <= maxHealth * 0.5f) {
                currentSpeed = baseSpeed * 1.6f;
                currentJumpSpeed = BASE_JUMP_SPEED + 100f;
                jumpProbability = 0.03f;
                teleportProbability = 0.003f;
            }

            if (isCharging) {
                bounds.x += (facingRight ? 600f : -600f) * delta;
                if (chargeAnim.isAnimationFinished(stateTime)) {
                    isCharging = false;
                } else if (bounds.overlaps(player.getBounds()) && !player.isDead()) {
                    player.takeDamage(3);
                    isCharging = false;
                }
            }
            else if (isDashing) {
                dashTime += delta;
                bounds.x += (facingRight ? dashSpeed : -dashSpeed) * delta;

                if (bounds.overlaps(player.getBounds()) && !player.isDead()) {
                    player.takeDamage(1);
                    isDashing = false;
                }
                if (dashTime > 0.4f) {
                    isDashing = false;
                }
            }
            else if (isGrounded && !isAttacking && !isSpecialAttacking) {
                if (player.isDead()) {
                    patrol(delta, currentSpeed);
                } else {
                    Rectangle attackReach = new Rectangle(bounds.x - 30, bounds.y, bounds.width + 60, bounds.height);

                    if (teleportProbability > 0 && Math.random() < teleportProbability) {
                        float teleportOffset = (Math.random() > 0.5) ? 80f : -80f;
                        bounds.x = player.getBounds().x + teleportOffset;
                        bounds.y = player.getBounds().y + 20f;
                        facingRight = player.getBounds().x > bounds.x;
                        velocityY = -100f;
                        stateTime = 0;
                    }
                    else if (attackReach.overlaps(player.getBounds())) {
                        facingRight = player.getBounds().x > bounds.x;
                        if (Math.random() < 0.3) {
                            isSpecialAttacking = true;
                        } else {
                            isAttacking = true;
                        }
                        stateTime = 0;
                    }
                    else if (distanceToPlayer < 400) {
                        facingRight = player.getBounds().x > bounds.x;

                        if (distanceToPlayer > 300 && Math.random() < 0.02) {
                            isCharging = true;
                            stateTime = 0;
                        }
                        else if (distanceToPlayer > 200 && Math.random() < 0.02) {
                            isDashing = true;
                            dashTime = 0;
                        }
                        else if (distanceToPlayer <= 200 && Math.random() < jumpProbability) {
                            velocityY = currentJumpSpeed;
                        }
                        else {
                            bounds.x += (facingRight ? currentSpeed : -currentSpeed) * delta;
                        }
                    }
                    else {
                        patrol(delta, currentSpeed);
                    }
                }
            }

            if (checkCollision(bounds, blocks)) {
                bounds.x = oldX;
                if (isDashing) isDashing = false;
                if (isCharging) isCharging = false;

                if (!isDashing && !isCharging && !isAttacking && !isSpecialAttacking) {
                    if (isGrounded) {
                        velocityY = currentJumpSpeed;
                    }
                    facingRight = !facingRight;
                }
            }

            if (isAttacking && attackAnim.isAnimationFinished(stateTime)) {
                stateTime = 0;
                Rectangle attackReach = new Rectangle(bounds.x - 30, bounds.y, bounds.width + 60, bounds.height);
                if (attackReach.overlaps(player.getBounds()) && !player.isDead()) {
                    player.takeDamage(1);
                }
                isAttacking = false;
            }
            else if (isSpecialAttacking && specialAttackAnim.isAnimationFinished(stateTime)) {
                stateTime = 0;
                Rectangle attackReach = new Rectangle(bounds.x - 30, bounds.y, bounds.width + 60, bounds.height);
                if (attackReach.overlaps(player.getBounds()) && !player.isDead()) {
                    player.takeDamage(2);
                }
                isSpecialAttacking = false;
            }
        } else {
            stateTime += delta;
        }
    }

    private void patrol(float delta, float currentSpeed) {
        if (facingRight) {
            bounds.x += currentSpeed * delta;
            if (bounds.x > patrolMaxX) facingRight = false;
        } else {
            bounds.x -= currentSpeed * delta;
            if (bounds.x < patrolMinX) facingRight = true;
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = idleFrame;

        if (isDead) {
            currentFrame = deathAnim.getKeyFrame(stateTime, false);
        } else if (!isAwake) {
            currentFrame = idleFrame;
        } else if (isCharging) {
            currentFrame = chargeAnim.getKeyFrame(stateTime, false);
        } else if (isDashing) {
            currentFrame = dashAnim.getKeyFrame(dashTime, true);
        } else if (isSpecialAttacking) {
            currentFrame = specialAttackAnim.getKeyFrame(stateTime, true);
        } else if (isAttacking) {
            currentFrame = attackAnim.getKeyFrame(stateTime, true);
        } else if (velocityY != 0) {
            currentFrame = walkAnim.getKeyFrame(0);
        } else {
            currentFrame = walkAnim.getKeyFrame(stateTime, true);
        }

        float drawX = bounds.x + (bounds.width / 2f) - (180f / 2f);
        drawFlipped(batch, currentFrame, drawX, bounds.y, 180, 180, facingRight);
    }

    @Override
    public void dispose() {
        // Textures managed by AssetLoader
    }
    
    public boolean isAwake() {
        return isAwake;
    }
}
