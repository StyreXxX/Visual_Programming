package com.greenguardian.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class GreenGuardianGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;

    // --- Game Objects ---
    private Player player;
    private Boss boss;
    private Array<Projectile> projectiles;
    private Texture projectileTexture;

    // --- Environment ---
    private Rectangle groundBounds;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, 800, 480);

        groundBounds = new Rectangle(-1000, 0, 3000, 100);

        player = new Player();
        boss = new Boss();
        projectiles = new Array<>();

        Pixmap pixmap = new Pixmap(16, 8, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.GREEN);
        pixmap.fill();
        projectileTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.5f, 0.8f, 1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float delta = Gdx.graphics.getDeltaTime();
        update(delta);

        // SMOOTH CAMERA FIX (Lerp)
        // Moves the camera slightly towards the player's center each frame rather than instantly snapping
        float targetX = player.bounds.x + (player.bounds.width / 2);
        camera.position.x += (targetX - camera.position.x) * 5.0f * delta;
        camera.update();

        // Draw Environment
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.6f, 0.2f, 1);
        shapeRenderer.rect(groundBounds.x, groundBounds.y, groundBounds.width, groundBounds.height);
        shapeRenderer.end();

        // Draw Game Objects
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        player.draw(batch);
        boss.draw(batch);
        drawProjectiles();
        batch.end();

        // Draw UI
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawHealthBars();
        shapeRenderer.end();
    }

    private void update(float delta) {
        player.update(delta, projectiles);
        boss.update(delta, player);

        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update(delta);

            if (!boss.isDead && p.bounds.overlaps(boss.bounds)) {
                boss.takeDamage(1);
                projectiles.removeIndex(i);
            } else if (Math.abs(p.bounds.x - player.bounds.x) > 800) {
                projectiles.removeIndex(i);
            }
        }
    }

    private void drawProjectiles() {
        for (Projectile p : projectiles) {
            batch.draw(projectileTexture, p.bounds.x, p.bounds.y, p.bounds.width, p.bounds.height);
        }
    }

    private void drawHealthBars() {
        float pHealthWidth = 200f;
        float pHealthHeight = 20f;
        float pX = 20f;
        float pY = 440f;

        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(pX, pY, pHealthWidth, pHealthHeight);
        shapeRenderer.setColor(Color.GREEN);
        float pCurrentHealthWidth = pHealthWidth * ((float) player.health / player.maxHealth);
        shapeRenderer.rect(pX, pY, Math.max(0, pCurrentHealthWidth), pHealthHeight);

        if (!boss.isDead) {
            float bHealthWidth = 200f;
            float bHealthHeight = 20f;
            float bX = 800f - bHealthWidth - 20f;
            float bY = 440f;

            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(bX, bY, bHealthWidth, bHealthHeight);
            shapeRenderer.setColor(Color.ORANGE);
            float bCurrentHealthWidth = bHealthWidth * ((float) boss.health / boss.maxHealth);
            shapeRenderer.rect(bX, bY, Math.max(0, bCurrentHealthWidth), bHealthHeight);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        player.dispose();
        boss.dispose();
        projectileTexture.dispose();
    }
}
