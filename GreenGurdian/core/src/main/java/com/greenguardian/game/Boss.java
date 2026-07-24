package com.greenguardian.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Boss {
    private Texture walkSheet, attackSheet, specialAttackSheet, dashSheet, deathSheet;
    private Animation<TextureRegion> walkAnim, attackAnim, specialAttackAnim, dashAnim, deathAnim;
    private TextureRegion idleFrame;
    public Rectangle bounds;
    private float stateTime;

    public boolean isDead = false;
    private boolean isAttacking = false;
    private boolean isSpecialAttacking = false;
    private boolean isDashing = false;
    private boolean facingRight = false;

    // --- Physics & Action Variables ---
    private float velocityY = 0;
    private final float GRAVITY = -1500f;
    private final float JUMP_SPEED = 700f;
    private final float GROUND_LEVEL = 100f;
    private float dashTime = 0f;
    private float dashSpeed = 500f;

    private float patrolMinX = 500f;
    private float patrolMaxX = 900f;
    private float speed = 100f;
    public int maxHealth = 15;
    public int health = 15;

    public Boss() {
        walkSheet = new Texture(Gdx.files.internal("EnemyWalking.png"));
        attackSheet = new Texture(Gdx.files.internal("EnemyAttack.png"));
        specialAttackSheet = new Texture(Gdx.files.internal("FirstBossAttack.png"));
        dashSheet = new Texture(Gdx.files.internal("bossDash.png"));
        deathSheet = new Texture(Gdx.files.internal("EnemyDeath.png"));

        walkAnim = createAnimation(walkSheet, 4, 0.2f);
        attackAnim = createAnimation(attackSheet, 4, 0.15f);
        specialAttackAnim = createAnimation(specialAttackSheet, 4, 0.15f);
        dashAnim = createAnimation(dashSheet, 3, 0.1f);
        deathAnim = createAnimation(deathSheet, 4, 0.2f);

        idleFrame = new TextureRegion(walkSheet, 0, 0, walkSheet.getWidth() / 4, walkSheet.getHeight());
        bounds = new Rectangle(700, GROUND_LEVEL, 80, 80);
    }

    private Animation<TextureRegion> createAnimation(Texture sheet, int frameCount, float frameDuration) {
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth() / frameCount, sheet.getHeight());
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = tmp[0][i];
        }
        return new Animation<>(frameDuration, frames);
    }

    public void update(float delta, Player player) {
        stateTime += delta;

        if (!isDead) {
            // 1. Gravity
            velocityY += GRAVITY * delta;
            bounds.y += velocityY * delta;

            if (bounds.y <= GROUND_LEVEL) {
                bounds.y = GROUND_LEVEL;
                velocityY = 0;
            }

            // 2. Dash Execution
            if (isDashing) {
                dashTime += delta;
                bounds.x += (facingRight ? dashSpeed : -dashSpeed) * delta;

                if (bounds.overlaps(player.bounds) && !player.isDead) {
                    player.takeDamage(1);
                    isDashing = false;
                }
                if (dashTime > 0.4f) {
                    isDashing = false;
                }
                return; // Skip standard AI while locked in a dash
            }

            // 3. Grounded AI Decision Logic
            if (bounds.y == GROUND_LEVEL && !isAttacking && !isSpecialAttacking) {
                if (player.isDead) {
                    // Patrol if player is defeated
                    patrol(delta);
                } else {
                    float distanceToPlayer = Math.abs(player.bounds.x - bounds.x);

                    // Creates a hitbox slightly wider than the boss to detect melee range
                    Rectangle attackReach = new Rectangle(bounds.x - 15, bounds.y, bounds.width + 30, bounds.height);

                    if (attackReach.overlaps(player.bounds)) {
                        // Close Range: Trigger Attack
                        facingRight = player.bounds.x > bounds.x;
                        if (Math.random() < 0.3) {
                            isSpecialAttacking = true;
                        } else {
                            isAttacking = true;
                        }
                        stateTime = 0;
                    }
                    else if (distanceToPlayer < 400) {
                        // Mid Range: Chase, Dash, or Jump
                        facingRight = player.bounds.x > bounds.x;

                        if (Math.random() < 0.02) {
                            if (Math.random() < 0.5) {
                                isDashing = true;
                                dashTime = 0;
                            } else {
                                velocityY = JUMP_SPEED;
                            }
                        } else {
                            bounds.x += (facingRight ? speed : -speed) * delta;
                        }
                    }
                    else {
                        // Long Range: Ignore and Patrol
                        patrol(delta);
                    }
                }
            }

            // 4. Process Attack Animations & Deal Damage
            if (isAttacking && attackAnim.isAnimationFinished(stateTime)) {
                stateTime = 0;
                Rectangle attackReach = new Rectangle(bounds.x - 15, bounds.y, bounds.width + 30, bounds.height);
                // Check if player is STILL in the hitbox before dealing damage
                if (attackReach.overlaps(player.bounds) && !player.isDead) {
                    player.takeDamage(1);
                }
                isAttacking = false;
            }
            else if (isSpecialAttacking && specialAttackAnim.isAnimationFinished(stateTime)) {
                stateTime = 0;
                Rectangle attackReach = new Rectangle(bounds.x - 15, bounds.y, bounds.width + 30, bounds.height);
                if (attackReach.overlaps(player.bounds) && !player.isDead) {
                    player.takeDamage(2);
                }
                isSpecialAttacking = false;
            }
        }
    }

    private void patrol(float delta) {
        if (facingRight) {
            bounds.x += speed * delta;
            if (bounds.x > patrolMaxX) facingRight = false;
        } else {
            bounds.x -= speed * delta;
            if (bounds.x < patrolMinX) facingRight = true;
        }
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
        } else if (isDashing) {
            currentFrame = dashAnim.getKeyFrame(dashTime, true);
        } else if (isSpecialAttacking) {
            currentFrame = specialAttackAnim.getKeyFrame(stateTime, true);
        } else if (isAttacking) {
            currentFrame = attackAnim.getKeyFrame(stateTime, true);
        } else if (bounds.y > GROUND_LEVEL) {
            currentFrame = walkAnim.getKeyFrame(0);
        } else {
            currentFrame = walkAnim.getKeyFrame(stateTime, true);
        }

        drawFlipped(batch, currentFrame, bounds.x, bounds.y, 120, 120, facingRight);
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
    }
}
