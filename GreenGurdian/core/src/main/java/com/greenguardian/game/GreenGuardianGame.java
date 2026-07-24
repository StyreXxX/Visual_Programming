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

    private Texture swordProjectileTexture;
    private Texture staffProjectileTexture;
    private Texture staffDisplayTexture; // Magic Staff item icon for store

    // --- UI & Economy ---
    private Texture shopTexture;
    private Texture soulTexture;
    private int playerSouls = 0;

    // --- State Flags ---
    private boolean isGameOver = false;
    private boolean isShopOpen = false; // NEW Shop Open State
    private String shopMessage = "";

    private float pStartX = 100f, pStartY = 300f;
    private float bStartX = 2000f, bStartY = 300f;

    private final float MAX_PROJECTILE_RANGE = 450f;

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

        shopTexture = new Texture(Gdx.files.internal("shop.png"));
        soulTexture = new Texture(Gdx.files.internal("soulCurrency.png"));
        staffDisplayTexture = new Texture(Gdx.files.internal("magicStaff.png"));

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

        // Create Sword Projectile Texture (Green)
        Pixmap p1 = new Pixmap(16, 8, Pixmap.Format.RGBA8888);
        p1.setColor(Color.GREEN);
        p1.fill();
        swordProjectileTexture = new Texture(p1);
        p1.dispose();

        // Create Staff Projectile Texture (Cyan Arc)
        Pixmap p2 = new Pixmap(28, 16, Pixmap.Format.RGBA8888);
        p2.setColor(Color.CYAN);
        p2.fill();
        staffProjectileTexture = new Texture(p2);
        p2.dispose();
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
        playerSouls = 0;

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
        isShopOpen = false;
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float delta = Gdx.graphics.getDeltaTime();

        // --- INPUT & UPDATE HANDLING ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.B) && !isGameOver && !boss.isDead) {
            isShopOpen = !isShopOpen; // Toggle Shop Menu
            shopMessage = "";
        }

        if (!isGameOver && !isShopOpen) {
            update(delta);
        } else if (isGameOver) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                resetGame();
            }
        } else if (isShopOpen) {
            // Purchase logic inside shop
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                if (player.hasStaff) {
                    shopMessage = "ALREADY OWNED!";
                } else if (playerSouls >= 45) {
                    playerSouls -= 45;
                    player.equipStaff();
                    shopMessage = "PURCHASE SUCCESSFUL!";
                } else {
                    shopMessage = "NOT ENOUGH SOULS!";
                }
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

        // Enable alpha blending
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // --- DRAW HUD UI SHAPES ---
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        drawHealthBars();

        // OVERLAYS (Game Over / Victory / Shop)
        if (isGameOver) {
            shapeRenderer.setColor(0f, 0f, 0f, 0.65f);
            shapeRenderer.rect(0, 0, 1280, 720);

            shapeRenderer.setColor(0.1f, 0.1f, 0.12f, 0.9f);
            shapeRenderer.rect(440, 260, 400, 200);
            shapeRenderer.setColor(0.8f, 0.1f, 0.1f, 1f);
            shapeRenderer.rect(440, 455, 400, 5);
        } else if (isShopOpen) {
            // Dark Shop Overlay Window
            shapeRenderer.setColor(0f, 0f, 0f, 0.75f);
            shapeRenderer.rect(0, 0, 1280, 720);

            shapeRenderer.setColor(0.12f, 0.12f, 0.16f, 0.95f);
            shapeRenderer.rect(340, 160, 600, 400);

            shapeRenderer.setColor(0.85f, 0.7f, 0.2f, 1f); // Gold Border
            shapeRenderer.rectLine(340, 160, 940, 160, 3);
            shapeRenderer.rectLine(340, 560, 940, 560, 3);
            shapeRenderer.rectLine(340, 160, 340, 560, 3);
            shapeRenderer.rectLine(940, 160, 940, 560, 3);
        } else if (boss.isDead) {
            shapeRenderer.setColor(0f, 0f, 0f, 0.5f);
            shapeRenderer.rect(0, 0, 1280, 720);

            shapeRenderer.setColor(0f, 0f, 0f, 0.5f);
            shapeRenderer.rect(538, 293, 204, 54);

            shapeRenderer.setColor(0.18f, 0.18f, 0.22f, 0.95f);
            shapeRenderer.rect(540, 295, 200, 50);

            shapeRenderer.setColor(0.9f, 0.75f, 0.2f, 1f);
            shapeRenderer.rectLine(540, 295, 740, 295, 2);
            shapeRenderer.rectLine(540, 345, 740, 345, 2);
            shapeRenderer.rectLine(540, 295, 540, 345, 2);
            shapeRenderer.rectLine(740, 295, 740, 345, 2);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // --- DRAW UI TEXT & IMAGES ---
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        if (!isGameOver && !boss.isDead) {
            // Shop Icon Top-Right
            batch.draw(shopTexture, 1150, 580, 100, 100);

            // Soul Currency Bottom-Right
            batch.draw(soulTexture, 1120, 30, 50, 50);
            font.setColor(Color.valueOf("B47EE5"));
            font.draw(batch, "x " + playerSouls, 1180, 68);

            font.setColor(Color.WHITE);
            font.draw(batch, "PLAYER HP", 25, 712);

            if (boss.isAwake) {
                font.setColor(Color.GOLD);
                font.draw(batch, "THE GUARDIAN", 555, 712);
            }
        }

        // TEXT OVERLAYS
        if (isShopOpen) {
            font.setColor(Color.GOLD);
            font.draw(batch, "MYSTIC SHOP (Press B to Close)", 420, 530);

            batch.draw(staffDisplayTexture, 380, 340, 100, 100);
            font.setColor(Color.WHITE);
            font.draw(batch, "Magic Staff", 500, 420);
            font.setColor(Color.GREEN);
            font.draw(batch, "Damage: 2 | Fast Speed", 500, 380);

            font.setColor(Color.valueOf("B47EE5"));
            font.draw(batch, "Cost: 45 Souls", 500, 340);

            font.setColor(Color.GOLD);
            font.draw(batch, "[Press ENTER to Buy]", 500, 280);

            if (!shopMessage.isEmpty()) {
                if (shopMessage.contains("SUCCESSFUL")) font.setColor(Color.GREEN);
                else font.setColor(Color.RED);
                font.draw(batch, shopMessage, 500, 230);
            }
        } else if (isGameOver) {
            font.setColor(Color.RED);
            font.draw(batch, "GAME OVER", 550, 410);
            font.setColor(Color.WHITE);
            font.draw(batch, "Press ENTER to Restart", 485, 340);
        } else if (boss.isDead) {
            font.setColor(Color.GOLD);
            font.draw(batch, "VICTORY ACHIEVED!", 500, 430);
            font.setColor(Color.WHITE);
            font.draw(batch, "Next Level", 575, 328);
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

        // --- PROJECTILE LOGIC ---
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update(delta);
            boolean projectileHit = false;

            if (!boss.isDead && p.bounds.overlaps(boss.bounds)) {
                boss.takeDamage(p.damage); // Deals projectile damage dynamically!
                projectiles.removeIndex(i);
                projectileHit = true;
            }

            if (!projectileHit) {
                for (Enemy e : enemies) {
                    if (!e.isDead && p.bounds.overlaps(e.bounds)) {
                        e.takeDamage(p.damage);

                        if (e.isDead) {
                            playerSouls += 15;
                        }

                        projectiles.removeIndex(i);
                        projectileHit = true;
                        break;
                    }
                }
            }

            if (!projectileHit && checkMapCollision(p.bounds)) {
                projectiles.removeIndex(i);
                projectileHit = true;
            }

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
            Texture tex = (p.type == 2) ? staffProjectileTexture : swordProjectileTexture;
            batch.draw(tex, p.bounds.x, p.bounds.y, p.bounds.width, p.bounds.height);
        }
    }

    private void drawHealthBars() {
        float pX = 20f, pY = 665f, pW = 250f, pH = 24f;

        shapeRenderer.setColor(0.08f, 0.08f, 0.1f, 0.9f);
        shapeRenderer.rect(pX - 4, pY - 4, pW + 8, pH + 8);

        shapeRenderer.setColor(0.85f, 0.7f, 0.2f, 1f);
        shapeRenderer.rectLine(pX - 4, pY - 4, pX + pW + 4, pY - 4, 2);
        shapeRenderer.rectLine(pX - 4, pY + pH + 4, pX + pW + 4, pY + pH + 4, 2);
        shapeRenderer.rectLine(pX - 4, pY - 4, pX - 4, pY + pH + 4, 2);
        shapeRenderer.rectLine(pX + pW + 4, pY - 4, pX + pW + 4, pY + pH + 4, 2);

        shapeRenderer.setColor(0.3f, 0.05f, 0.05f, 1f);
        shapeRenderer.rect(pX, pY, pW, pH);

        float pRatio = (float) player.health / player.maxHealth;
        shapeRenderer.setColor(0.1f, 0.85f, 0.25f, 1f);
        shapeRenderer.rect(pX, pY, Math.max(0, pW * pRatio), pH);

        shapeRenderer.setColor(1f, 1f, 1f, 0.2f);
        shapeRenderer.rect(pX, pY + pH - 4, Math.max(0, pW * pRatio), 4);

        if (!boss.isDead && boss.isAwake) {
            float bW = 500f, bH = 22f;
            float bX = 640f - (bW / 2f);
            float bY = 665f;

            shapeRenderer.setColor(0.08f, 0.08f, 0.1f, 0.95f);
            shapeRenderer.rect(bX - 5, bY - 5, bW + 10, bH + 10);

            shapeRenderer.setColor(0.75f, 0.55f, 0.15f, 1f);
            shapeRenderer.rectLine(bX - 5, bY - 5, bX + bW + 5, bY - 5, 2);
            shapeRenderer.rectLine(bX - 5, bY + bH + 5, bX + bW + 5, bY + bH + 5, 2);
            shapeRenderer.rectLine(bX - 5, bY - 5, bX - 5, bY + bH + 5, 2);
            shapeRenderer.rectLine(bX + bW + 5, bY - 5, bX + bW + 5, bY + bH + 5, 2);

            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            shapeRenderer.rect(bX, bY, bW, bH);

            float bRatio = (float) boss.health / boss.maxHealth;
            shapeRenderer.setColor(0.95f, 0.4f, 0.05f, 1f);
            shapeRenderer.rect(bX, bY, Math.max(0, bW * bRatio), bH);

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
        swordProjectileTexture.dispose();
        staffProjectileTexture.dispose();
        staffDisplayTexture.dispose();
        shopTexture.dispose();
        soulTexture.dispose();
        map.dispose();
        mapRenderer.dispose();
        font.dispose();
    }
}
