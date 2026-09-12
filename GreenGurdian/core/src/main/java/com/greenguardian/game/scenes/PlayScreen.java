package com.greenguardian.game.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
// import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.greenguardian.game.GreenGuardianGame;
import com.greenguardian.game.entities.Boss;
import com.greenguardian.game.entities.Enemy;
import com.greenguardian.game.entities.Player;
import com.greenguardian.game.entities.Projectile;
import com.greenguardian.game.ui.HUD;

public class PlayScreen extends BaseScreen {
    private final float buffer = 6.0f; // Small buffer to prevent camera boundary issues

    private ShapeRenderer shapeRenderer;

    private OrthographicCamera camera;
    private Viewport viewport;

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private MapObjects mapBlocks;

    private Player player;
    private Boss boss;
    private Array<Enemy> enemies;
    private Array<Projectile> projectiles;
    private Array<Rectangle> mapSouls;

    private int playerSouls = 140;
    private final int STAFF_COST = 150;

    public enum GameState {
        PLAYING, SHOP, GAME_OVER
    }
    private GameState gameState = GameState.PLAYING;
    private String shopMessage = "";

    private float pStartX = 100f, pStartY = 300f;
    private float bStartX = 2000f, bStartY = 300f;
    private final float MAX_PROJECTILE_RANGE = 450f;

    private HUD hud;
    private int levelIndex;

    public PlayScreen(GreenGuardianGame game, SpriteBatch batch, BitmapFont font, OrthographicCamera hudCamera, int levelIndex) {
        super(game, batch, font, hudCamera);
        this.levelIndex = levelIndex;
        this.shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera(); // orthographic means no depth needed, it flattens everything to 2d
        viewport = new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT, camera); // stretch viewport means it will stretch the game world to fit the screen
        // center of the screen
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);

        String mapPath = (levelIndex == 2) ? "maps/level2/map2.tmx" : "maps/level1/copy.tmx";
        System.out.println("LOADING LEVEL: " + levelIndex + " WITH MAP: " + mapPath);
        map = new TmxMapLoader().load(mapPath); // load the map
        mapRenderer = new OrthogonalTiledMapRenderer(map, SCALE_FACTOR); // render the map
        if (map.getLayers().get("blocks") != null) {
            mapBlocks = map.getLayers().get("blocks").getObjects(); // get the blocks from the map
        } else if (map.getLayers().get("Object Layer 1") != null) {
            mapBlocks = map.getLayers().get("Object Layer 1").getObjects();
        }

        enemies = new Array<>();
        mapSouls = new Array<>();
        projectiles = new Array<>();

        MapObjects spawnObjects = null;
        if (map.getLayers().get("spawns") != null) {
            spawnObjects = map.getLayers().get("spawns").getObjects();
        } else if (map.getLayers().get("SpawnPoints") != null) {
            spawnObjects = map.getLayers().get("SpawnPoints").getObjects();
        }

        if (spawnObjects != null) {
            for (MapObject obj : spawnObjects) {
                if (obj.getName() != null) {
                    float scaledX = (float) obj.getProperties().get("x") * SCALE_FACTOR;
                    float scaledY = (float) obj.getProperties().get("y") * SCALE_FACTOR;

                    String name = obj.getName().toLowerCase();
                    if (name.equals("player")) {
                        pStartX = scaledX;
                        pStartY = scaledY;
                    } else if (name.equals("boss")) {
                        bStartX = scaledX;
                        bStartY = scaledY;
                    } else if (name.startsWith("enemy")) {
                        enemies.add(new Enemy(scaledX, scaledY, game.assets)); 
                    } else if (name.equals("soul")) {
                        mapSouls.add(new Rectangle(scaledX, scaledY, TILE_SIZE, TILE_SIZE));
                    }
                }
            }
        }
        System.out.println("SPAWN POSITIONS - Player: (" + pStartX + ", " + pStartY + ") Boss: (" + bStartX + ", " + bStartY + ")");

        player = new Player(pStartX, pStartY, game.assets);
        boss = new Boss(bStartX, bStartY, game.assets);

        hud = new HUD(batch, shapeRenderer, font, game.assets);
    }

    private void resetGame() {
        player.dispose();
        boss.dispose();
        for (Enemy e : enemies) {
            e.dispose();
        }
        enemies.clear();
        projectiles.clear();
        mapSouls.clear();
        playerSouls = 0;

        MapObjects spawnObjects = null;
        if (map.getLayers().get("spawns") != null) {
            spawnObjects = map.getLayers().get("spawns").getObjects();
        } else if (map.getLayers().get("SpawnPoints") != null) {
            spawnObjects = map.getLayers().get("SpawnPoints").getObjects();
        }

        if (spawnObjects != null) {
            for (MapObject obj : spawnObjects) {
                if (obj.getName() != null) {
                    float scaledX = (float) obj.getProperties().get("x") * SCALE_FACTOR;
                    float scaledY = (float) obj.getProperties().get("y") * SCALE_FACTOR;

                    String name = obj.getName().toLowerCase();
                    if (name.startsWith("enemy")) {
                        enemies.add(new Enemy(scaledX, scaledY, game.assets));
                    } else if (name.equals("soul")) {
                        mapSouls.add(new Rectangle(scaledX, scaledY, TILE_SIZE, TILE_SIZE));
                    }
                }
            }
        }

        player = new Player(pStartX, pStartY, game.assets);
        boss = new Boss(bStartX, bStartY, game.assets);
        gameState = GameState.PLAYING;
    }

    private float victoryTime = 0f;

    @Override
    public void render(float delta) {
        if (levelIndex == 2) {
            Gdx.gl.glClearColor(0.1f, 0.2f, 0.1f, 1f); // Dark green for swamp
        } else {
            Gdx.gl.glClearColor(0.2f, 0.4f, 0.8f, 1); // Blue sky for level 1
        } // Clears the screen with black color
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clears the color buffer

        if (boss.isDead()) {
            victoryTime += delta;
        }

        if (boss.isDead() && victoryTime > 1.0f && Gdx.input.justTouched()) {
            com.badlogic.gdx.math.Vector3 mousePos = new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            hudCamera.unproject(mousePos);
            if (mousePos.x >= 540 && mousePos.x <= 740 && mousePos.y >= 295 && mousePos.y <= 345) {
                ((GreenGuardianGame) game).playButtonSound();
                if (levelIndex == 1) {
                    game.setScreen(new PlayScreen(game, batch, font, hudCamera, 2));
                } else {
                    game.setScreen(new MenuScreen(game, batch, font, hudCamera));
                }
                return;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.B) && gameState != GameState.GAME_OVER && !boss.isDead()) {
            gameState = (gameState == GameState.SHOP) ? GameState.PLAYING : GameState.SHOP;
            shopMessage = "";
        }

        if (gameState == GameState.PLAYING) {
            updateWorld(delta);
        } else if (gameState == GameState.GAME_OVER) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                resetGame();
            }
        } else if (gameState == GameState.SHOP) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                if (player.hasStaff()) {
                    shopMessage = "ALREADY OWNED!";
                } else if (playerSouls >= STAFF_COST) {
                    playerSouls -= STAFF_COST;
                    player.equipStaff();
                    shopMessage = "PURCHASE SUCCESSFUL!";
                } else {
                    shopMessage = "NOT ENOUGH SOULS!";
                }
            }
        }

        float mapWidthInPixels = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class) * SCALE_FACTOR;
        float mapHeightInPixels = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class) * SCALE_FACTOR;

        float targetX = player.getBounds().x + (player.getBounds().width / 2);
        camera.position.x += (targetX - camera.position.x) * 5.0f * delta; // Follows the player    
        camera.position.x = Math.max(WORLD_WIDTH / 2 + buffer, Math.min(camera.position.x, mapWidthInPixels - WORLD_WIDTH / 2 - buffer)); // Clamps X within map bounds

        float targetY = player.getBounds().y + (player.getBounds().height / 2); 
        camera.position.y += (targetY - camera.position.y) * 5.0f * delta; // Follows the player 
        camera.position.y = Math.max(WORLD_HEIGHT / 2 + buffer, Math.min(camera.position.y, mapHeightInPixels - WORLD_HEIGHT / 2 - buffer)); // Clamps Y within map bounds
        camera.update(); // Updates the camera

        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined); // Sets the projection matrix to camera.combined? meaning it will draw the sprites in the camera's view
        batch.begin(); // Begins the sprite batch
        for (Rectangle s : mapSouls) {
            batch.draw(game.assets.soulTexture, s.x, s.y, s.width, s.height);
        }
        for (Enemy e : enemies) {
            e.draw(batch);
        }
        player.draw(batch);
        boss.draw(batch);
        for (Projectile p : projectiles) {
            batch.draw((p.type == 2) ? game.assets.staffProjectileTexture : game.assets.swordProjectileTexture, p.bounds.x, p.bounds.y, p.bounds.width, p.bounds.height);
        }
        batch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy e : enemies) {
            e.drawFloatingHealth(shapeRenderer);
        }
        shapeRenderer.end();

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        hud.drawHealthBars(player, boss);
        shapeRenderer.end();

        hud.drawOverlays(gameState == GameState.GAME_OVER, gameState == GameState.SHOP, boss);

        batch.setProjectionMatrix(hudCamera.combined);
        hud.drawTextAndIcons(gameState == GameState.GAME_OVER, gameState == GameState.SHOP, boss, playerSouls, STAFF_COST, shopMessage, levelIndex);
    }

    private void updateWorld(float delta) {
        player.update(delta, projectiles, mapBlocks, SCALE_FACTOR);
        boss.update(delta, player, mapBlocks, SCALE_FACTOR);

        for (Enemy e : enemies) {
            e.update(delta, player, mapBlocks, SCALE_FACTOR);
        }

        for (int i = mapSouls.size - 1; i >= 0; i--) {
            Rectangle s = mapSouls.get(i);
            if (player.getBounds().overlaps(s)) {
                playerSouls += 10;
                mapSouls.removeIndex(i);
            }
        }

        if (player.getHealth() <= 0 || player.getBounds().y < -100) {
            gameState = GameState.GAME_OVER;
        }

        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update(delta);
            boolean projectileHit = false;

            if (!boss.isDead() && p.bounds.overlaps(boss.getBounds())) {
                boss.takeDamage(p.damage);
                projectiles.removeIndex(i);
                projectileHit = true;
            }

            if (!projectileHit) {
                for (Enemy e : enemies) {
                    if (!e.isDead() && p.bounds.overlaps(e.getBounds())) {
                        e.takeDamage(p.damage);
                        if (e.isDead()) playerSouls += 15;
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

            if (!projectileHit && Math.abs(p.bounds.x - player.getBounds().x) > MAX_PROJECTILE_RANGE) {
                projectiles.removeIndex(i);
            }
        }
    }

    private Rectangle tmpBlockRect = new Rectangle();

    private boolean checkMapCollision(Rectangle rect) {
        for (MapObject object : mapBlocks) {
            Rectangle blockRect = null; // Checks the type of map object and gets the rectangle 
            if (object instanceof RectangleMapObject) { // If the map object is a rectangle
                blockRect = ((RectangleMapObject) object).getRectangle();
            } else if (object instanceof PolygonMapObject) { // If the map object is a polygon
                blockRect = ((PolygonMapObject) object).getPolygon().getBoundingRectangle();
            }

            if (blockRect != null) { // Checks if the block rectangle overlaps with the given rectangle 
                tmpBlockRect.set(
                    blockRect.x * SCALE_FACTOR, blockRect.y * SCALE_FACTOR,
                    blockRect.width * SCALE_FACTOR, blockRect.height * SCALE_FACTOR
                ); // Scale the block rectangle to match the game world
                if (rect.overlaps(tmpBlockRect)) { // Checks if the given rectangle overlaps with the block rectangle
                    return true; // Returns true if there is a collision
                }
            }
        }
        return false; // Returns false if there is no collision
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // Updates the viewport with the new width and height
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
        ((GreenGuardianGame) game).playGameplayMusic();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        player.dispose();
        boss.dispose();
        for (Enemy e : enemies) {
            e.dispose();
        }
        map.dispose();
        mapRenderer.dispose();
    }
}
