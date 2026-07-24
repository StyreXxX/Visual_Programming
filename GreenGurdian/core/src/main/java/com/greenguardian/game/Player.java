package com.greenguardian.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Player {
    private Texture walkSheet, attackSheet, deathSheet;
    private Animation<TextureRegion> walkAnim, attackAnim, deathAnim;
    private TextureRegion idleFrame;
    public Rectangle bounds;
    private float stateTime;
    private boolean isAttacking;
    public boolean facingRight = true;
    public boolean isDead = false;

    // --- Physics Variables ---
    private float velocityY = 0;
    private final float GRAVITY = -1500f;
    private final float JUMP_SPEED = 600f;
    private final float GROUND_LEVEL = 100f;
    private final float PLAYER_SPEED = 350f; // Increased for better balance and dodging

    public int maxHealth = 10;
    public int health = 10;

    public Player() {
        walkSheet = new Texture(Gdx.files.internal("MCWalking.png"));
        attackSheet = new Texture(Gdx.files.internal("MainCharacterAttack.png"));
        deathSheet = new Texture(Gdx.files.internal("deathanimation.png"));

        walkAnim = createAnimation(walkSheet, 4, 0.15f);
        attackAnim = createAnimation(attackSheet, 3, 0.1f);
        deathAnim = createAnimation(deathSheet, 4, 0.2f);

        idleFrame = new TextureRegion(walkSheet, 0, 0, walkSheet.getWidth() / 4, walkSheet.getHeight());
        bounds = new Rectangle(100, GROUND_LEVEL, 64, 64);
    }

    private Animation<TextureRegion> createAnimation(Texture sheet, int frameCount, float frameDuration) {
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth() / frameCount, sheet.getHeight());
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = tmp[0][i];
        }
        return new Animation<>(frameDuration, frames);
    }

    public void update(float delta, Array<Projectile> projectiles) {
        if (health <= 0 && !isDead) {
            isDead = true;
            stateTime = 0;
        }

        stateTime += delta;

        if (!isDead) {
            velocityY += GRAVITY * delta;
            bounds.y += velocityY * delta;

            if (bounds.y <= GROUND_LEVEL) {
                bounds.y = GROUND_LEVEL;
                velocityY = 0;

                if (!isAttacking && (Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE))) {
                    velocityY = JUMP_SPEED;
                }
            }
        }

        if (isDead) return;

        if (!isAttacking) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                bounds.x -= PLAYER_SPEED * delta;
                facingRight = false;
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                bounds.x += PLAYER_SPEED * delta;
                facingRight = true;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                isAttacking = true;
                stateTime = 0;
                spawnProjectile(projectiles);
            }
        }

        if (isAttacking && attackAnim.isAnimationFinished(stateTime)) {
            isAttacking = false;
        }
    }

    public void takeDamage(int amount) {
        if (!isDead) {
            health -= amount;
            if (health <= 0) {
                isDead = true;
                stateTime = 0;
            }
        }
    }

    private void spawnProjectile(Array<Projectile> projectiles) {
        float px = facingRight ? bounds.x + 50 : bounds.x - 20;
        float py = bounds.y + 30;
        projectiles.add(new Projectile(px, py, facingRight));
    }

    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = idleFrame;

        if (isDead) {
            currentFrame = deathAnim.getKeyFrame(stateTime, false);
        } else if (isAttacking) {
            currentFrame = attackAnim.getKeyFrame(stateTime, false);
        } else if (bounds.y > GROUND_LEVEL) {
            currentFrame = walkAnim.getKeyFrame(0);
        } else if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            currentFrame = walkAnim.getKeyFrame(stateTime, true);
        }

        drawFlipped(batch, currentFrame, bounds.x, bounds.y, 100, 100, facingRight);
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
        deathSheet.dispose();
    }
}
