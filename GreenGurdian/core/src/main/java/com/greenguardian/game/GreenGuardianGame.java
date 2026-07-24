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
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
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
    private Array<Enemy> enemies;
    private Array<Projectile> projectiles;
    private Texture projectileTexture;

    // --- Game State ---
    private boolean isGameOver = false;
    private float pStartX = 100f, pStartY = 300f;
    private float bStartX = 2000f, bStartY = 300f;

    // --- Balanced Projectile Settings ---
    private final float MAX_PROJECTILE_RANGE = 450f; // Balanced mid-range shoot distance

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        font = new BitmapFont();
        font.getData().setScale(1.8f);

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);

        hudCamera = new OrthographicCamera();
        hudViewport = new FitViewport(1280, 720, hudCamera);

        map = new TmxMapLoader().load("map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 2.5f);
        mapBlocks = map.getLayers().get("blocks").getObjects();

        enemies = new Array<>();

        if (map.getLayers().get("spawns") != null) {
            for (MapObject obj : map.getLayers().get("spawns").getObjects()) {
                if (obj.getName() != null) {
                    float scaledX = (float) obj.getProperties().get("x") * 2.5f;
                    float scaledY = (float) obj.getProperties().get("y") * 2.5f;

                    if (obj.getName().equalsIgnoreCase("Player")) {
                        pStartX = scaledX;
                        pStartY = scaledY;
                    } else if (obj.getName().equalsIgnoreCase("Boss")) {
                        bStartX = scaledX;
                        bStartY = scaledY;
                    } else if (obj.getName().equalsIgnoreCase("Enemy")) {
                        enemies.add(new Enemy(scaledX, scaledY));
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
        for (Enemy e : enemies) {
            e.dispose();
        }
        enemies.clear();
        projectiles.clear();

        if (map.getLayers().get("spawns") != null) {
            for (MapObject obj : map.getLayers().get("spawns").getObjects()) {
                if (obj.getName() != null && obj.getName().equalsIgnoreCase("Enemy")) {
                    float scaledX = (float) obj.getProperties().get("x") * 2.5f;
                    float scaledY = (float) obj.getProperties().get("y") * 2.5f;
                    enemies.add(new Enemy(scaledX, scaledY));
                }
            }
        }

        player = new Player(pStartX, pStartY);
        boss = new Boss(bStartX, bStartY);
        isGameOver = false;
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
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
        if (camera.position.x < 640) camera.position.x = 640;

        float targetY = Math.max(360, player.bounds.y + (player.bounds.height / 2));
        camera.position.y += (targetY - camera.position.y) * 5.0f * delta;
        camera.update();

        mapRenderer.setView(camera);
        mapRenderer.render();

        // --- DRAW GAME WORLD OBJECTS ---
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Enemy e : enemies) {
            e.draw(batch);
        }
        player.draw(batch);
        boss.draw(batch);
        drawProjectiles();
        batch.end();

        // --- DRAW WORLD SHAPES (Floating enemy health bars) ---
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy e : enemies) {
            e.drawFloatingHealth(shapeRenderer);
        }
        shapeRenderer.end();

        // Enable alpha blending for sleek transparent UI backgrounds
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // --- DRAW HUD UI SHAPES ---
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        drawHealthBars();

        // Screen overlays on Game Over or Victory
        if (isGameOver) {
            // Dark vignette overlay
            shapeRenderer.setColor(0f, 0f, 0f, 0.65f);
            shapeRenderer.rect(0, 0, 1280, 720);

            // Modal dialog frame
            shapeRenderer.setColor(0.1f, 0.1f, 0.12f, 0.9f);
            shapeRenderer.rect(440, 260, 400, 200);
            shapeRenderer.setColor(0.8f, 0.1f, 0.1f, 1f);
            shapeRenderer.rect(440, 455, 400, 5); // Crimson top accent bar
        } else if (boss.isDead) {
            // Dark victory overlay
            shapeRenderer.setColor(0f, 0f, 0f, 0.5f);
            shapeRenderer.rect(0, 0, 1280, 720);

            // Button Shadow
            shapeRenderer.setColor(0f, 0f, 0f, 0.5f);
            shapeRenderer.rect(538, 293, 204, 54);

            // Button Body
            shapeRenderer.setColor(0.18f, 0.18f, 0.22f, 0.95f);
            shapeRenderer.rect(540, 295, 200, 50);

            // Button Gold Border
            shapeRenderer.setColor(0.9f, 0.75f, 0.2f, 1f);
            shapeRenderer.rectLine(540, 295, 740, 295, 2);
            shapeRenderer.rectLine(540, 345, 740, 345, 2);
            shapeRenderer.rectLine(540, 295, 540, 345, 2);
            shapeRenderer.rectLine(740, 295, 740, 345, 2);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // --- DRAW UI TEXT ---
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        if (isGameOver) {
            font.setColor(Color.RED);
            font.draw(batch, "GAME OVER", 550, 410);
            font.setColor(Color.WHITE);
            font.draw(batch, "Press ENTER to Restart", 485, 340);
        } else if (boss.isDead) {
            font.setColor(Color.GOLD);
            font.draw(batch, "VICTORY ACHIEVED!", 500, 430);
            font.setColor(Color.WHITE);
            font.draw(batch, "Next Level", 575, 328);
        } else {
            // Player HP text indicator above the bar
            font.setColor(Color.WHITE);
            font.draw(batch, "PLAYER HP", 25, 712);

            if (boss.isAwake) {
                font.setColor(Color.GOLD);
                font.draw(batch, "THE GUARDIAN", 555, 712);
            }
        }
        batch.end();
    }

    private void update(float delta) {
        player.update(delta, projectiles, mapBlocks);
        boss.update(delta, player, mapBlocks);

        for (Enemy e : enemies) {
            e.update(delta, player, mapBlocks);
        }

        if (player.health <= 0 || player.bounds.y < -100) {
            isGameOver = true;
        }

        // --- PROJECTILE LOGIC (Balanced Distance & Block Collision) ---
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update(delta);
            boolean projectileHit = false;

            // 1. Boss collision
            if (!boss.isDead && p.bounds.overlaps(boss.bounds)) {
                boss.takeDamage(1);
                projectiles.removeIndex(i);
                projectileHit = true;
            }

            // 2. Normal enemy collision
            if (!projectileHit) {
                for (Enemy e : enemies) {
                    if (!e.isDead && p.bounds.overlaps(e.bounds)) {
                        e.takeDamage(1);
                        projectiles.removeIndex(i);
                        projectileHit = true;
                        break;
                    }
                }
            }

            // 3. Terrain collision (Projectiles break on walls)
            if (!projectileHit && checkMapCollision(p.bounds)) {
                projectiles.removeIndex(i);
                projectileHit = true;
            }

            // 4. Maximum Range Limit Check (Removes long-range exploit)
            if (!projectileHit && Math.abs(p.bounds.x - player.bounds.x) > MAX_PROJECTILE_RANGE) {
                projectiles.removeIndex(i);
            }
        }
    }

    private boolean checkMapCollision(Rectangle rect) {
        for (MapObject object : mapBlocks) {
            Rectangle blockRect = null;
            if (object instanceof RectangleMapObject) {
                blockRect = ((RectangleMapObject) object).getRectangle();
            } else if (object instanceof PolygonMapObject) {
                blockRect = ((PolygonMapObject) object).getPolygon().getBoundingRectangle();
            }

            if (blockRect != null) {
                Rectangle scaledRect = new Rectangle(
                    blockRect.x * 2.5f, blockRect.y * 2.5f,
                    blockRect.width * 2.5f, blockRect.height * 2.5f
                );
                if (rect.overlaps(scaledRect)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void drawProjectiles() {
        for (Projectile p : projectiles) {
            batch.draw(projectileTexture, p.bounds.x, p.bounds.y, p.bounds.width, p.bounds.height);
        }
    }

    private void drawHealthBars() {
        // ==========================================
        // 1. PLAYER HEALTH BAR (Top Left)
        // ==========================================
        float pX = 20f, pY = 665f, pW = 250f, pH = 24f;

        // Outer Dark Border Frame
        shapeRenderer.setColor(0.08f, 0.08f, 0.1f, 0.9f);
        shapeRenderer.rect(pX - 4, pY - 4, pW + 8, pH + 8);

        // Gold Trim Border Lines
        shapeRenderer.setColor(0.85f, 0.7f, 0.2f, 1f);
        shapeRenderer.rectLine(pX - 4, pY - 4, pX + pW + 4, pY - 4, 2);
        shapeRenderer.rectLine(pX - 4, pY + pH + 4, pX + pW + 4, pY + pH + 4, 2);
        shapeRenderer.rectLine(pX - 4, pY - 4, pX - 4, pY + pH + 4, 2);
        shapeRenderer.rectLine(pX + pW + 4, pY - 4, pX + pW + 4, pY + pH + 4, 2);

        // Dark Crimson Background Fill
        shapeRenderer.setColor(0.3f, 0.05f, 0.05f, 1f);
        shapeRenderer.rect(pX, pY, pW, pH);

        // Neon Green Health Bar Fill
        float pRatio = (float) player.health / player.maxHealth;
        shapeRenderer.setColor(0.1f, 0.85f, 0.25f, 1f);
        shapeRenderer.rect(pX, pY, Math.max(0, pW * pRatio), pH);

        // Top Highlight Gloss Line
        shapeRenderer.setColor(1f, 1f, 1f, 0.2f);
        shapeRenderer.rect(pX, pY + pH - 4, Math.max(0, pW * pRatio), 4);


        // ==========================================
        // 2. BOSS HEALTH BAR (Top Center)
        // ==========================================
        if (!boss.isDead && boss.isAwake) {
            float bW = 500f, bH = 22f;
            float bX = 640f - (bW / 2f); // Centered
            float bY = 665f;

            // Outer Frame
            shapeRenderer.setColor(0.08f, 0.08f, 0.1f, 0.95f);
            shapeRenderer.rect(bX - 5, bY - 5, bW + 10, bH + 10);

            // Dark Gold Border Frame
            shapeRenderer.setColor(0.75f, 0.55f, 0.15f, 1f);
            shapeRenderer.rectLine(bX - 5, bY - 5, bX + bW + 5, bY - 5, 2);
            shapeRenderer.rectLine(bX - 5, bY + bH + 5, bX + bW + 5, bY + bH + 5, 2);
            shapeRenderer.rectLine(bX - 5, bY - 5, bX - 5, bY + bH + 5, 2);
            shapeRenderer.rectLine(bX + bW + 5, bY - 5, bX + bW + 5, bY + bH + 5, 2);

            // Dark Empty Container Background
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            shapeRenderer.rect(bX, bY, bW, bH);

            // Glowing Orange/Red Boss Health Fill
            float bRatio = (float) boss.health / boss.maxHealth;
            shapeRenderer.setColor(0.95f, 0.4f, 0.05f, 1f);
            shapeRenderer.rect(bX, bY, Math.max(0, bW * bRatio), bH);

            // Top Highlight Gloss Line
            shapeRenderer.setColor(1f, 1f, 1f, 0.25f);
            shapeRenderer.rect(bX, bY + bH - 4, Math.max(0, bW * bRatio), 4);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        player.dispose();
        boss.dispose();
        for (Enemy e : enemies) {
            e.dispose();
        }
        projectileTexture.dispose();
        map.dispose();
        mapRenderer.dispose();
        font.dispose();
    }
}
