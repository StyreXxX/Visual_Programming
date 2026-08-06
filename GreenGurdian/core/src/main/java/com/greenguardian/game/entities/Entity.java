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
    protected Texture walkSheet, attackSheet, deathSheet;
    protected Animation<TextureRegion> walkAnim, attackAnim, deathAnim;
    protected TextureRegion idleFrame;

    protected Rectangle bounds;
    protected float stateTime;

    protected boolean isDead = false;
    protected boolean facingRight = true;
    protected boolean isAttacking = false;

    protected float velocityY = 0;
    protected final float GRAVITY = -1500f;

    protected int maxHealth;
    protected int health;

    public Entity(float startX, float startY, float width, float height, int maxHealth) {
        this.bounds = new Rectangle(startX, startY, width, height);
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    protected Animation<TextureRegion> createAnimation(Texture sheet, int frameCount, float frameDuration) {
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth() / frameCount, sheet.getHeight());
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = tmp[0][i];
        }
        return new Animation<>(frameDuration, frames);
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

    protected void drawFlipped(SpriteBatch batch, TextureRegion region, float x, float y, float width, float height, boolean faceRight) {
        if (faceRight) {
            batch.draw(region, x, y, width, height);
        } else {
            batch.draw(region, x + width, y, -width, height);
        }
    }

    protected boolean checkCollision(Rectangle characterBounds, MapObjects blocks) {
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
                Polygon polygon = ((PolygonMapObject) object).getPolygon();
                float[] vertices = polygon.getTransformedVertices();
                float[] scaledVertices = new float[vertices.length];
                for (int i = 0; i < vertices.length; i++) {
                    scaledVertices[i] = vertices[i] * 2.5f;
                }
                Polygon scaledPolygon = new Polygon(scaledVertices);
                
                Polygon charPoly = new Polygon(new float[] {
                    characterBounds.x, characterBounds.y,
                    characterBounds.x + characterBounds.width, characterBounds.y,
                    characterBounds.x + characterBounds.width, characterBounds.y + characterBounds.height,
                    characterBounds.x, characterBounds.y + characterBounds.height
                });
                
                if (Intersector.overlapConvexPolygons(charPoly, scaledPolygon)) {
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

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }
}
