package com.greenguardian.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Player {
    private Texture walkSheet, attackSheet, deathSheet;
    private Texture staffWalkSheet, staffAttackSheet;

    private Animation<TextureRegion> walkAnim, attackAnim, deathAnim;
    private Animation<TextureRegion> staffWalkAnim, staffAttackAnim;
    private TextureRegion idleFrame, staffIdleFrame;

    public Rectangle bounds;
    private float stateTime;
    private boolean isAttacking;
    public boolean facingRight = true;
    public boolean isDead = false;
    public boolean hasStaff = false;

    private float velocityY = 0;
    private final float GRAVITY = -1500f;
    private final float JUMP_SPEED = 900f;
    private final float PLAYER_SPEED = 550f;

    public int maxHealth = 10;
    public int health = 10;

    public Player(float startX, float startY) {
        // Standard Sheets
        walkSheet = new Texture(Gdx.files.internal("MCWalking.png"));
        attackSheet = new Texture(Gdx.files.internal("MainCharacterAttack.png"));
        deathSheet = new Texture(Gdx.files.internal("deathanimation.png"));

        // Staff Sheets
        staffWalkSheet = new Texture(Gdx.files.internal("mainCharacterStaffWalking.png"));
        staffAttackSheet = new Texture(Gdx.files.internal("mainCharacterStaffAttack.png"));

        walkAnim = createAnimation(walkSheet, 4, 0.15f);
        attackAnim = createAnimation(attackSheet, 3, 0.1f);
        deathAnim = createAnimation(deathSheet, 4, 0.2f);

        staffWalkAnim = createAnimation(staffWalkSheet, 4, 0.15f);
        staffAttackAnim = createAnimation(staffAttackSheet, 3, 0.1f);

        idleFrame = new TextureRegion(walkSheet, 0, 0, walkSheet.getWidth() / 4, walkSheet.getHeight());
        staffIdleFrame = new TextureRegion(staffWalkSheet, 0, 0, staffWalkSheet.getWidth() / 4, staffWalkSheet.getHeight());

        bounds = new Rectangle(startX, startY, 30, 70);
    }

    private Animation<TextureRegion> createAnimation(Texture sheet, int frameCount, float frameDuration) {
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth() / frameCount, sheet.getHeight());
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = tmp[0][i];
        }
        return new Animation<>(frameDuration, frames);
    }

    public void equipStaff() {
        this.hasStaff = true;
    }

    public void update(float delta, Array<Projectile> projectiles, MapObjects blocks) {
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
        if (checkCollision(bounds, blocks)) {
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

            if (checkCollision(bounds, blocks)) {
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

    private boolean checkCollision(Rectangle characterBounds, MapObjects blocks) {
        for (MapObject object : blocks) {
            Rectangle rect = null;

            if (object instanceof RectangleMapObject) {
                rect = ((RectangleMapObject) object).getRectangle();
                Rectangle scaledRect = new Rectangle(
                    rect.x * 2.5f, rect.y * 2.5f,
                    rect.width * 2.5f, rect.height * 2.5f
                );

                if (characterBounds.overlaps(scaledRect)) {
                    return true;
                }
            } else if (object instanceof com.badlogic.gdx.maps.objects.PolygonMapObject) {
                com.badlogic.gdx.math.Polygon polygon = ((com.badlogic.gdx.maps.objects.PolygonMapObject) object).getPolygon();
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
                stateTime = 0;
            }
        }
    }

    private void spawnProjectile(Array<Projectile> projectiles) {
        float px = facingRight ? bounds.x + 50 : bounds.x - 20;
        float py = bounds.y + 30;

        // Spawn staff projectile (type 2) if staff is equipped, otherwise standard (type 1)
        int projType = hasStaff ? 2 : 1;
        projectiles.add(new Projectile(px, py, facingRight, projType));
    }

    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = hasStaff ? staffIdleFrame : idleFrame;

        if (isDead) {
            currentFrame = deathAnim.getKeyFrame(stateTime, false);
        } else if (isAttacking) {
            currentFrame = (hasStaff ? staffAttackAnim : attackAnim).getKeyFrame(stateTime, false);
        } else if (velocityY != 0) {
            currentFrame = (hasStaff ? staffWalkAnim : walkAnim).getKeyFrame(0);
        } else if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            currentFrame = (hasStaff ? staffWalkAnim : walkAnim).getKeyFrame(stateTime, true);
        }

        // Lock the height to 100 to keep the character's vertical size consistent
        float drawHeight = 100f;

        // Calculate the width dynamically based on the frame's true aspect ratio
        float aspect = (float) currentFrame.getRegionWidth() / currentFrame.getRegionHeight();
        float drawWidth = drawHeight * aspect;

        // Center the sprite perfectly over your physics hitbox
        float drawX = bounds.x + (bounds.width / 2f) - (drawWidth / 2f);

        drawFlipped(batch, currentFrame, drawX, bounds.y, drawWidth, drawHeight, facingRight);
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
        staffWalkSheet.dispose();
        staffAttackSheet.dispose();
    }
}
