package com.greenguardian.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GreenGuardianGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private OrthographicCamera camera;
    private Viewport viewport;

    private OrthographicCamera hudCamera;
    private Viewport hudViewport;

    // --- Map Variables ---
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private MapObjects mapBlocks;

    // --- Game Objects ---
    private Player player;
    private Boss boss;
    private Array<Projectile> projectiles;
    private Texture projectileTexture;

    // --- Game State ---
    private boolean isGameOver = false;
    private float pStartX = 100f, pStartY = 300f;
    private float bStartX = 2000f, bStartY = 300f;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        font = new BitmapFont();
        font.getData().setScale(2f);

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);

        hudCamera = new OrthographicCamera();
        hudViewport = new FitViewport(1280, 720, hudCamera);

        map = new TmxMapLoader().load("map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 2.5f);
        mapBlocks = map.getLayers().get("blocks").getObjects();

        if (map.getLayers().get("spawns") != null) {
            for (com.badlogic.gdx.maps.MapObject obj : map.getLayers().get("spawns").getObjects()) {
                if (obj.getName() != null) {
                    float scaledX = (float) obj.getProperties().get("x") * 2.5f;
                    float scaledY = (float) obj.getProperties().get("y") * 2.5f;

                    if (obj.getName().equalsIgnoreCase("Player")) {
                        pStartX = scaledX;
                        pStartY = scaledY;
                    } else if (obj.getName().equalsIgnoreCase("Boss")) {
                        bStartX = scaledX;
                        bStartY = scaledY;
                    }
                }
            }
        }

        player = new Player(pStartX, pStartY);
        boss = new Boss(bStartX, bStartY);
        projectiles = new Array<>();

        Pixmap pixmap = new Pixmap(16, 8, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.GREEN);
        pixmap.fill();
        projectileTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudViewport.update(width, height, true);
    }

    private void resetGame() {
        player.dispose();
        boss.dispose();

        player = new Player(pStartX, pStartY);
        boss = new Boss(bStartX, bStartY);
        projectiles.clear();
        isGameOver = false;
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f); // Changed to black background for standard aspect ratio bars
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float delta = Gdx.graphics.getDeltaTime();

        if (!isGameOver) {
            update(delta);
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                resetGame();
            }
        }

        float targetX = player.bounds.x + (player.bounds.width / 2);
        camera.position.x += (targetX - camera.position.x) * 5.0f * delta;
        if(camera.position.x < 640) camera.position.x = 640;

        float targetY = Math.max(360, player.bounds.y + (player.bounds.height / 2));
        camera.position.y += (targetY - camera.position.y) * 5.0f * delta;
        camera.update();

        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        player.draw(batch);
        boss.draw(batch);
        drawProjectiles();
        batch.end();

        // --- DRAW UI SHAPES ---
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawHealthBars();

        // Draw the visual dummy button if the Boss is dead
        if (boss.isDead) {
            shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
            shapeRenderer.rect(540, 300, 200, 50); // The dark gray button box
        }
        shapeRenderer.end();

        // --- DRAW UI TEXT ---
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        if (isGameOver) {
            font.setColor(Color.RED);
            font.draw(batch, "GAME OVER", 540, 400);
            font.setColor(Color.WHITE);
            font.draw(batch, "Press ENTER to Restart", 480, 350);
        } else if (boss.isDead) {
            font.setColor(Color.GOLD);
            font.draw(batch, "YOU WIN!", 550, 420);

            // Draw the text exactly inside the gray button box we just made
            font.setColor(Color.WHITE);
            font.draw(batch, "Next Level", 560, 335);
        }
        batch.end();
    }

    private void update(float delta) {
        player.update(delta, projectiles, mapBlocks);
        boss.update(delta, player, mapBlocks);

        if (player.health <= 0 || player.bounds.y < -100) {
            isGameOver = true;
        }

        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update(delta);

            if (!boss.isDead && p.bounds.overlaps(boss.bounds)) {
                boss.takeDamage(1);
                projectiles.removeIndex(i);
            } else if (Math.abs(p.bounds.x - player.bounds.x) > 1200) {
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
        float pY = 680f;

        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(pX, pY, pHealthWidth, pHealthHeight);
        shapeRenderer.setColor(Color.GREEN);
        float pCurrentHealthWidth = pHealthWidth * ((float) player.health / player.maxHealth);
        shapeRenderer.rect(pX, pY, Math.max(0, pCurrentHealthWidth), pHealthHeight);

        if (!boss.isDead) {
            float bHealthWidth = 200f;
            float bHealthHeight = 20f;
            float bX = 1280f - bHealthWidth - 20f;
            float bY = 680f;

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
        map.dispose();
        mapRenderer.dispose();
        font.dispose();
    }
}
