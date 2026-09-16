package com.greenguardian.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.math.Rectangle;
import com.greenguardian.game.assets.AssetLoader;

public class CentaurBoss extends Boss {
    private Texture smashAttackSheet, chargeSheet, dashSheet, shieldSheet;
    private Animation<TextureRegion> smashAttackAnim, chargeAnim, dashAnim, shieldAnim;

    private boolean isSmashing = false;
    private boolean isCharging = false;
    private boolean isShielding = false;

    private float baseSpeed = 130f;
    private float chargeSpeed = 650f;
    private float patrolMinX;
    private float patrolMaxX;

    public CentaurBoss(float startX, float startY, AssetLoader assets) {
        super(startX, startY, assets);

        // Override the bounds and health set by the default Boss constructor
        this.bounds.width = 90;
        this.bounds.height = 130;
        this.maxHealth = 80;
        this.health = 80;

        walkSheet = assets.boss2WalkSheet;
        attackSheet = assets.boss2AttackSheet;
        smashAttackSheet = assets.boss2SmashSheet;
        chargeSheet = assets.boss2ChargeSheet;
        dashSheet = assets.boss2DashSheet;
        shieldSheet = assets.boss2ShieldSheet;
        deathSheet = assets.boss2DeathSheet;

        // 4 columns x 2 rows = 8 frames each
        walkAnim = createGridAnimation(walkSheet, 4, 2, 0.12f);
        attackAnim = createGridAnimation(attackSheet, 4, 2, 0.10f);
        smashAttackAnim = createGridAnimation(smashAttackSheet, 4, 2, 0.12f);
        chargeAnim = createGridAnimation(chargeSheet, 4, 2, 0.10f);
        dashAnim = createGridAnimation(dashSheet, 4, 2, 0.10f);
        shieldAnim = createGridAnimation(shieldSheet, 4, 2, 0.12f);
        deathAnim = createGridAnimation(deathSheet, 4, 2, 0.15f);

        int frameW = walkSheet.getWidth() / 4;
        int frameH = walkSheet.getHeight() / 2;
        idleFrame = new TextureRegion(walkSheet, 0, 0, frameW, frameH);

        patrolMinX = startX - 300f;
        patrolMaxX = startX + 300f;
    }

    @Override
    public void update(float delta, Player player, MapObjects blocks, float scale) {
        if (!isDead) {
            float oldY = bounds.y;
            velocityY += GRAVITY * delta;
            bounds.y += velocityY * delta;

            boolean isGrounded = false;
            if (checkCollision(bounds, blocks, scale)) {
                bounds.y = oldY;
                if (velocityY < 0) isGrounded = true;
                velocityY = 0;
            }

            float distanceToPlayer = Math.abs(player.getBounds().x - bounds.x);

            if (!isAwake) {
                if (distanceToPlayer < 700) {
                    isAwake = true;
                } else {
                    return;
                }
            }

            stateTime += delta;
            float oldX = bounds.x;

            float currentSpeed = baseSpeed;
            if (health <= maxHealth * 0.4f) {
                currentSpeed = baseSpeed * 1.5f;
            }

            if (isCharging) {
                bounds.x += (facingRight ? chargeSpeed : -chargeSpeed) * delta;
                if (chargeAnim.isAnimationFinished(stateTime)) {
                    isCharging = false;
                } else if (bounds.overlaps(player.getBounds()) && !player.isDead()) {
                    player.takeDamage(2);
                    isCharging = false;
                }
            }
            else if (isShielding) {
                if (shieldAnim.isAnimationFinished(stateTime)) {
                    isShielding = false;
                    stateTime = 0;
                }
            }
            else if (isGrounded && !isAttacking && !isSmashing) {
                if (player.isDead()) {
                    patrol(delta, currentSpeed);
                } else {
                    Rectangle smashReach = new Rectangle(bounds.x - 60, bounds.y, bounds.width + 120, bounds.height);

                    if (smashReach.overlaps(player.getBounds())) {
                        facingRight = player.getBounds().x > bounds.x;
                        double rand = Math.random();
                        if (rand < 0.35) {
                            isSmashing = true;
                        } else if (rand < 0.6) {
                            isShielding = true;
                        } else {
                            isAttacking = true;
                        }
                        stateTime = 0;
                    }
                    else if (distanceToPlayer < 600) {
                        facingRight = player.getBounds().x > bounds.x;
                        if (distanceToPlayer > 280 && Math.random() < 0.03) {
                            isCharging = true;
                            stateTime = 0;
                        } else {
                            bounds.x += (facingRight ? currentSpeed : -currentSpeed) * delta;
                        }
                    } else {
                        patrol(delta, currentSpeed);
                    }
                }
            }

            if (checkCollision(bounds, blocks, scale)) {
                bounds.x = oldX;
                if (isCharging) isCharging = false;
                if (!isCharging && !isAttacking && !isSmashing && !isShielding) {
                    facingRight = !facingRight;
                }
            }

            if (isAttacking && attackAnim.isAnimationFinished(stateTime)) {
                stateTime = 0;
                Rectangle attackReach = new Rectangle(bounds.x - 50, bounds.y, bounds.width + 100, bounds.height);
                if (attackReach.overlaps(player.getBounds()) && !player.isDead()) {
                    player.takeDamage(1);
                }
                isAttacking = false;
            }
            else if (isSmashing && smashAttackAnim.isAnimationFinished(stateTime)) {
                stateTime = 0;
                Rectangle smashReach = new Rectangle(bounds.x - 70, bounds.y, bounds.width + 140, bounds.height);
                if (smashReach.overlaps(player.getBounds()) && !player.isDead()) {
                    player.takeDamage(3);
                }
                isSmashing = false;
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
    public void takeDamage(int amount) {
        // If shielding when attacked, block damage
        if (isShielding && !isDead) {
            amount = Math.max(1, amount / 2);
        }
        super.takeDamage(amount);
    }

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = idleFrame;

        if (isDead) {
            currentFrame = deathAnim.getKeyFrame(stateTime, false);
        } else if (!isAwake) {
            currentFrame = idleFrame;
        } else if (isCharging) {
            currentFrame = chargeAnim.getKeyFrame(stateTime, true);
        } else if (isShielding) {
            currentFrame = shieldAnim.getKeyFrame(stateTime, false);
        } else if (isSmashing) {
            currentFrame = smashAttackAnim.getKeyFrame(stateTime, false);
        } else if (isAttacking) {
            currentFrame = attackAnim.getKeyFrame(stateTime, false);
        } else if (velocityY != 0) {
            currentFrame = walkAnim.getKeyFrame(0);
        } else {
            currentFrame = walkAnim.getKeyFrame(stateTime, true);
        }

        float drawHeight = 220f;
        float aspect = (float) currentFrame.getRegionWidth() / currentFrame.getRegionHeight();
        float drawWidth = drawHeight * aspect;

        float drawX = bounds.x + (bounds.width / 2f) - (drawWidth / 2f);
        float drawY = bounds.y - 10f;
        drawFlipped(batch, currentFrame, drawX, drawY, drawWidth, drawHeight, facingRight);
    }
}
