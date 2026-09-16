package com.greenguardian.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;

public abstract class Entity {
    protected Texture walkSheet, attackSheet, deathSheet, standSheet;
    protected Animation<TextureRegion> walkAnim, attackAnim, deathAnim;
    protected TextureRegion idleFrame;

    protected Rectangle bounds;
    protected float stateTime;

    protected boolean isDead = false;
    protected boolean facingRight = true;
    protected boolean isAttacking = false;

    protected float velocityY = 0;
    protected final float GRAVITY = -1500f;

    protected float maxHealth;
    protected float health;

    private static final Rectangle tmpRect = new Rectangle();
    private static final Polygon tmpPolygon = new Polygon();
    private static final Polygon charPoly = new Polygon(new float[8]);
    private static float[] tmpVertices = new float[0];

    public Entity(float startX, float startY, float width, float height) {
        this.bounds = new Rectangle(startX, startY, width, height);
    }

    protected Animation<TextureRegion> createAnimation(Texture sheet, int frameCount, float frameDuration) {
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth() / frameCount, sheet.getHeight());
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = tmp[0][i];
        }
        return new Animation<>(frameDuration, frames);
    }

    protected Animation<TextureRegion> createGridAnimation(Texture sheet, int cols, int rows, float frameDuration) {
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth() / cols, sheet.getHeight() / rows);
        TextureRegion[] frames = new TextureRegion[cols * rows];
        int index = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                frames[index++] = tmp[r][c];
            }
        }
        return new Animation<>(frameDuration, frames);
    }

    public void takeDamage(float amount) {
        if (!isDead) {
            health -= amount;
            if (health <= 0) {
                isDead = true;
                health = 0;
                stateTime = 0;
            }
        }
    }

    public void takeDamage(int amount) {
        takeDamage((float) amount);
    }

    protected void drawFlipped(SpriteBatch batch, TextureRegion region, float x, float y, float width, float height, boolean faceRight) {
        if (faceRight) {
            batch.draw(region, x, y, width, height);
        } else {
            batch.draw(region, x + width, y, -width, height);
        }
    }

    protected boolean checkCollision(Rectangle characterBounds, MapObjects blocks, float scale) {
        for (MapObject object : blocks) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                tmpRect.set(
                    rect.x * scale, rect.y * scale,
                    rect.width * scale, rect.height * scale
                );

                if (characterBounds.overlaps(tmpRect)) {
                    return true;
                }
            } else if (object instanceof PolygonMapObject) {
                Polygon polygon = ((PolygonMapObject) object).getPolygon();
                float[] vertices = polygon.getTransformedVertices();

                if (tmpVertices.length != vertices.length) {
                    tmpVertices = new float[vertices.length];
                }

                for (int i = 0; i < vertices.length; i++) {
                    tmpVertices[i] = vertices[i] * scale;
                }
                tmpPolygon.setVertices(tmpVertices);

                float[] charVertices = charPoly.getVertices();
                charVertices[0] = characterBounds.x;
                charVertices[1] = characterBounds.y;
                charVertices[2] = characterBounds.x + characterBounds.width;
                charVertices[3] = characterBounds.y;
                charVertices[4] = characterBounds.x + characterBounds.width;
                charVertices[5] = characterBounds.y + characterBounds.height;
                charVertices[6] = characterBounds.x;
                charVertices[7] = characterBounds.y + characterBounds.height;
                charPoly.dirty();

                if (Intersector.overlapConvexPolygons(charPoly, tmpPolygon)) {
                    return true;
                }
            }
        }
        return false;
    }

    public abstract void draw(SpriteBatch batch);
    public abstract void dispose();

    // Getters for encapsulation
    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isDead() {
        return isDead;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public int getHealth() {
        return (int) Math.ceil(health);
    }

    public int getMaxHealth() {
        return (int) maxHealth;
    }

    public boolean isAwake() {
        // By default, assume standard entities (like Player and normal Enemies) are always awake.
        // The Boss and CentaurBoss classes will automatically override this with their custom aggro logic.
        return true;
    }
}
