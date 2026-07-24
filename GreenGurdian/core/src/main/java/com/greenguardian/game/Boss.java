package com.greenguardian.game;

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

public class Boss {
    private Texture walkSheet, attackSheet, specialAttackSheet, dashSheet, deathSheet;
    private Texture chargeSheet;
    private Animation<TextureRegion> walkAnim, attackAnim, specialAttackAnim, dashAnim, deathAnim;
    private Animation<TextureRegion> chargeAnim;
    private TextureRegion idleFrame;
    public Rectangle bounds;
    private float stateTime;

    public boolean isDead = false;
    public boolean isAwake = false;
    private boolean isAttacking = false;
    private boolean isSpecialAttacking = false;
    private boolean isDashing = false;
    private boolean isCharging = false;
    private boolean facingRight = false;

    // --- Physics & Action Variables ---
    private float velocityY = 0;
    private final float GRAVITY = -1500f;
    private final float BASE_JUMP_SPEED = 700f;
    private float dashTime = 0f;
    private float dashSpeed = 500f;

    private float patrolMinX = 500f;
    private float patrolMaxX = 900f;
    private float baseSpeed = 100f;

    public int maxHealth = 40;
    public int health = 40;

    public Boss(float startX, float startY) {
        walkSheet = new Texture(Gdx.files.internal("EnemyWalking.png"));
        attackSheet = new Texture(Gdx.files.internal("EnemyAttack.png"));
        specialAttackSheet = new Texture(Gdx.files.internal("FirstBossAttack.png"));
        dashSheet = new Texture(Gdx.files.internal("bossDash.png"));
        deathSheet = new Texture(Gdx.files.internal("EnemyDeath.png"));
        chargeSheet = new Texture(Gdx.files.internal("bossCharge.png"));

        walkAnim = createAnimation(walkSheet, 4, 0.2f);
        attackAnim = createAnimation(attackSheet, 4, 0.15f);
        specialAttackAnim = createAnimation(specialAttackSheet, 4, 0.15f);
        dashAnim = createAnimation(dashSheet, 3, 0.1f);
        deathAnim = createAnimation(deathSheet, 4, 0.2f);
        chargeAnim = createAnimation(chargeSheet, 4, 0.15f);

        idleFrame = new TextureRegion(walkSheet, 0, 0, walkSheet.getWidth() / 4, walkSheet.getHeight());
        bounds = new Rectangle(startX, startY, 120, 120);

        patrolMinX = startX - 200f;
        patrolMaxX = startX + 200f;
    }

    private Animation<TextureRegion> createAnimation(Texture sheet, int frameCount, float frameDuration) {
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth() / frameCount, sheet.getHeight());
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = tmp[0][i];
        }
        return new Animation<>(frameDuration, frames);
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

            float distanceToPlayer = Math.abs(player.bounds.x - bounds.x);

            if (!isAwake) {
                if (distanceToPlayer < 600) {
                    isAwake = true;
                } else {
                    return;
                }
            }

            stateTime += delta;
            float oldX = bounds.x;

            // --- PHASE SYSTEM VARIABLES ---
            float currentSpeed = baseSpeed;
            float currentJumpSpeed = BASE_JUMP_SPEED;
            float jumpProbability = 0.01f;
            float teleportProbability = 0f;

            if (health <= maxHealth * 0.2f) {
                // PHASE 3 (Below 20%): Hyper aggressive, frequent teleports
                currentSpeed = baseSpeed * 2.2f;
                currentJumpSpeed = BASE_JUMP_SPEED + 150f;
                jumpProbability = 0.05f;
                teleportProbability = 0.015f;
            } else if (health <= maxHealth * 0.5f) {
                // PHASE 2 (Below 50%): Faster, frequent jumps, rare teleports
                currentSpeed = baseSpeed * 1.6f;
                currentJumpSpeed = BASE_JUMP_SPEED + 100f;
                jumpProbability = 0.03f;
                teleportProbability = 0.003f;
            }

            // --- EXECUTE STATES ---
            if (isCharging) {
                bounds.x += (facingRight ? 600f : -600f) * delta;
                if (chargeAnim.isAnimationFinished(stateTime)) {
                    isCharging = false;
                } else if (bounds.overlaps(player.bounds) && !player.isDead) {
                    player.takeDamage(3);
                    isCharging = false;
                }
            }
            else if (isDashing) {
                dashTime += delta;
                bounds.x += (facingRight ? dashSpeed : -dashSpeed) * delta;

                if (bounds.overlaps(player.bounds) && !player.isDead) {
                    player.takeDamage(1);
                    isDashing = false;
                }
                if (dashTime > 0.4f) {
                    isDashing = false;
                }
            }
            else if (isGrounded && !isAttacking && !isSpecialAttacking) {
                if (player.isDead) {
                    patrol(delta, currentSpeed);
                } else {
                    Rectangle attackReach = new Rectangle(bounds.x - 30, bounds.y, bounds.width + 60, bounds.height);

                    // 1. Check for Teleport
                    if (teleportProbability > 0 && Math.random() < teleportProbability) {
                        float teleportOffset = (Math.random() > 0.5) ? 80f : -80f;
                        bounds.x = player.bounds.x + teleportOffset;
                        bounds.y = player.bounds.y + 20f; // Drop in slightly above
                        facingRight = player.bounds.x > bounds.x;
                        velocityY = -100f; // Snap downward slightly
                        stateTime = 0;
                    }
                    // 2. Standard Attack Range
                    else if (attackReach.overlaps(player.bounds)) {
                        facingRight = player.bounds.x > bounds.x;
                        if (Math.random() < 0.3) {
                            isSpecialAttacking = true;
                        } else {
                            isAttacking = true;
                        }
                        stateTime = 0;
                    }
                    else if (distanceToPlayer < 400) {
                        facingRight = player.bounds.x > bounds.x;

                        // 3. Long Range Charge
                        if (distanceToPlayer > 300 && Math.random() < 0.02) {
                            isCharging = true;
                            stateTime = 0;
                        }
                        // 4. Mid Range Dash
                        else if (distanceToPlayer > 200 && Math.random() < 0.02) {
                            isDashing = true;
                            dashTime = 0;
                        }
                        // 5. Jump or March
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
                    facingRight = !facingRight;
                }
            }

            if (isAttacking && attackAnim.isAnimationFinished(stateTime)) {
                stateTime = 0;
                Rectangle attackReach = new Rectangle(bounds.x - 30, bounds.y, bounds.width + 60, bounds.height);
                if (attackReach.overlaps(player.bounds) && !player.isDead) {
                    player.takeDamage(1);
                }
                isAttacking = false;
            }
            else if (isSpecialAttacking && specialAttackAnim.isAnimationFinished(stateTime)) {
                stateTime = 0;
                Rectangle attackReach = new Rectangle(bounds.x - 30, bounds.y, bounds.width + 60, bounds.height);
                if (attackReach.overlaps(player.bounds) && !player.isDead) {
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

    private boolean checkCollision(Rectangle characterBounds, MapObjects blocks) {
        for (MapObject object : blocks) {
            Rectangle rect = null;

            if (object instanceof RectangleMapObject) {
                rect = ((RectangleMapObject) object).getRectangle();
            } else if (object instanceof PolygonMapObject) {
                rect = ((PolygonMapObject) object).getPolygon().getBoundingRectangle();
            }

            if (rect != null) {
                Rectangle scaledRect = new Rectangle(
                    rect.x * 2.5f, rect.y * 2.5f,
                    rect.width * 2.5f, rect.height * 2.5f
                );

                if (characterBounds.overlaps(scaledRect)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void takeDamage(int amount) {
        if (!isDead) {
            health -= amount;
            if (health <= 0) {
                isDead = true;
                health = 0;
                stateTime = 0;
            }
        }
    }

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

        drawFlipped(batch, currentFrame, bounds.x - 30, bounds.y, 180, 180, facingRight);
    }

    private void drawFlipped(SpriteBatch batch, TextureRegion region, float x, float y, float width, float height, boolean faceRight) {
        if (faceRight) {
            batch.draw(region, x, y, width, height);
        } else {
            batch.draw(region, x + width, y, -width, height);
        }
    }

    public void dispose() {
        walkSheet.dispose();
        attackSheet.dispose();
        specialAttackSheet.dispose();
        dashSheet.dispose();
        deathSheet.dispose();
        chargeSheet.dispose();
    }
}
