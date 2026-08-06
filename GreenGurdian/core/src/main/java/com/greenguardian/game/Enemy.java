package com.greenguardian.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;

public class Enemy {
    private Texture walkSheet, attackSheet, deathSheet;
    private Animation<TextureRegion> walkAnim, attackAnim, deathAnim;
    private TextureRegion idleFrame;
    public Rectangle bounds;
    private float stateTime;

    public boolean isDead = false;
    private boolean isAttacking = false;
    private boolean facingRight = false;

    // --- Patrol Variables ---
    private float startX;
    private float patrolRange = 100f;
    private float speed = 50f;

    private float velocityY = 0;
    private final float GRAVITY = -1500f;

    // --- Stats ---
    public int maxHealth = 3;
    public int health = 3;

    public Enemy(float startX, float startY) {
        this.startX = startX;

        walkSheet = new Texture(Gdx.files.internal("normalEnemyWalking.png"));
        attackSheet = new Texture(Gdx.files.internal("normalEnemyAttacking.png"));
        deathSheet = new Texture(Gdx.files.internal("normalEnemyDeath.png"));

        walkAnim = createAnimation(walkSheet, 4, 0.2f);
        attackAnim = createAnimation(attackSheet, 3, 0.15f);
        deathAnim = createAnimation(deathSheet, 4, 0.2f);

        idleFrame = new TextureRegion(walkSheet, 0, 0, walkSheet.getWidth() / 4, walkSheet.getHeight());
        bounds = new Rectangle(startX, startY, 30, 60);
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
            // Y-Axis (Gravity)
            float oldY = bounds.y;
            velocityY += GRAVITY * delta;
            bounds.y += velocityY * delta;

            if (checkCollision(bounds, blocks)) {
                bounds.y = oldY;
                velocityY = 0;
            }

            stateTime += delta;
            float oldX = bounds.x;

            // Attack Logic
            Rectangle attackReach = new Rectangle(bounds.x - 15, bounds.y, bounds.width + 30, bounds.height);
            if (!isAttacking && attackReach.overlaps(player.bounds) && !player.isDead) {
                isAttacking = true;
                stateTime = 0;
                facingRight = player.bounds.x > bounds.x;
            }

            if (isAttacking) {
                if (attackAnim.isAnimationFinished(stateTime)) {
                    isAttacking = false;
                    if (attackReach.overlaps(player.bounds) && !player.isDead) {
                        player.takeDamage(1);
                    }
                }
            } else {
                // Walk back and forth in a tight space
                if (facingRight) {
                    bounds.x += speed * delta;
                    if (bounds.x > startX + patrolRange) facingRight = false;
                } else {
                    bounds.x -= speed * delta;
                    if (bounds.x < startX - patrolRange) facingRight = true;
                }

                if (checkCollision(bounds, blocks)) {
                    bounds.x = oldX;
                    facingRight = !facingRight;
                }
            }
        } else {
            stateTime += delta; // Continue death animation
        }
    }

    private boolean checkCollision(Rectangle characterBounds, MapObjects blocks) {
        for (MapObject object : blocks) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                Rectangle scaledRect = new Rectangle(
                    rect.x * 2.5f, rect.y * 2.5f,
                    rect.width * 2.5f, rect.height * 2.5f
                );

                if (characterBounds.overlaps(scaledRect)) {
                    return true;
                }
            } else if (object instanceof PolygonMapObject) {
                com.badlogic.gdx.math.Polygon polygon = ((PolygonMapObject) object).getPolygon();
                float[] vertices = polygon.getTransformedVertices();
                float[] scaledVertices = new float[vertices.length];
                for (int i = 0; i < vertices.length; i++) {
                    scaledVertices[i] = vertices[i] * 2.5f;
                }
                com.badlogic.gdx.math.Polygon scaledPolygon = new com.badlogic.gdx.math.Polygon(scaledVertices);
                
                com.badlogic.gdx.math.Polygon charPoly = new com.badlogic.gdx.math.Polygon(new float[] {
                    characterBounds.x, characterBounds.y,
                    characterBounds.x + characterBounds.width, characterBounds.y,
                    characterBounds.x + characterBounds.width, characterBounds.y + characterBounds.height,
                    characterBounds.x, characterBounds.y + characterBounds.height
                });
                
                if (com.badlogic.gdx.math.Intersector.overlapConvexPolygons(charPoly, scaledPolygon)) {
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
        } else if (isAttacking) {
            currentFrame = attackAnim.getKeyFrame(stateTime, false);
        } else {
            currentFrame = walkAnim.getKeyFrame(stateTime, true);
        }
        // Center the 80x80 sprite over the 30x60 hitbox
        float drawX = bounds.x + (bounds.width / 2f) - (80f / 2f);
        drawFlipped(batch, currentFrame, drawX, bounds.y, 80, 80, facingRight);
    }

    // Renders the tiny floating health bar attached to the enemy's world position
    public void drawFloatingHealth(ShapeRenderer shapeRenderer) {
        if (!isDead && health < maxHealth) { // Only show health if they took damage
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

    private void drawFlipped(SpriteBatch batch, TextureRegion region, float x, float y, float width, float height, boolean faceRight) {
        if (faceRight) batch.draw(region, x, y, width, height);
        else batch.draw(region, x + width, y, -width, height);
    }

    public void dispose() {
        walkSheet.dispose();
        attackSheet.dispose();
        deathSheet.dispose();
    }
}
