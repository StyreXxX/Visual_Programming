package com.greenguardian.game.scenes;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
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
import com.greenguardian.game.entities.*;
import com.greenguardian.game.ui.FloatingText;
import com.greenguardian.game.ui.HUD;

public class PlayScreen extends BaseScreen {

    private final float buffer = 6.0f;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private MapObjects mapBlocks;
    private MapObjects mapWater;

    private Player player;
    private Boss activeBoss; // Reverted to Boss so HUD doesn't crash

    private Array<Enemy> enemies;
    private Array<Projectile> projectiles;
    private Array<Rectangle> mapSouls;
    private Array<Rectangle> mapKeys;
    private Array<FloatingText> floatingTexts = new Array<>();

    private int playerSouls = 140;
    private int playerKeys = 0;
    private final int STAFF_COST = 100;
    private final int HEALTH_ELIXIR_COST = 75;
    private final int DAMAGE_STONE_COST = 60;
    private final int SOUL_MAGNET_COST = 80;
    private int shopSelectedIndex = 0;

    public enum GameState {
        PLAYING, SHOP, GAME_OVER, PAUSE, OPTIONS
    }

    private GameState gameState = GameState.PLAYING;
    private String shopMessage = "";

    // Pause / Options UI
    private Stage uiStage;
    private Table pauseTable;
    private Table optionsTable;
    private Label volumeLabel;

    private float pStartX = 100f;
    private float pStartY = 300f;
    private float bStartX = 2000f;
    private float bStartY = 300f;
    private final float MAX_PROJECTILE_RANGE = 450f;

    private HUD hud;
    private int levelIndex;
    private float victoryTime = 0f;

    public PlayScreen(GreenGuardianGame game, SpriteBatch batch, BitmapFont font, OrthographicCamera hudCamera, int levelIndex) {
        super(game, batch, font, hudCamera);
        this.levelIndex = levelIndex;
        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera();
        viewport = new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);

        String mapPath = (levelIndex == 2) ? "maps/level2/map2.tmx" : "maps/level1/test.tmx";
        System.out.println("LOADING LEVEL: " + levelIndex + " WITH MAP: " + mapPath);

        map = new TmxMapLoader().load(mapPath);
        mapRenderer = new OrthogonalTiledMapRenderer(map, SCALE_FACTOR) {
            @Override
            public void renderObject(MapObject object) {
                if (object instanceof com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject) {
                    com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject tileObj = (com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject) object;
                    com.badlogic.gdx.graphics.g2d.TextureRegion region = tileObj.getTextureRegion();
                    if (region != null) {
                        float x = tileObj.getX() * unitScale;
                        float y = tileObj.getY() * unitScale;
                        float w = region.getRegionWidth() * unitScale;
                        float h = region.getRegionHeight() * unitScale;

                        boolean flipX = tileObj.isFlipHorizontally();
                        boolean flipY = tileObj.isFlipVertically();
                        if (flipX || flipY) region.flip(flipX, flipY);

                        getBatch().draw(region, x, y, w, h);

                        if (flipX || flipY) region.flip(flipX, flipY); // restore
                    }
                }
            }
        };

        if (map.getLayers().get("blocks") != null) {
            mapBlocks = map.getLayers().get("blocks").getObjects();
        } else if (map.getLayers().get("Object Layer 1") != null) {
            mapBlocks = map.getLayers().get("Object Layer 1").getObjects();
        }

        if (map.getLayers().get("water") != null) {
            mapWater = map.getLayers().get("water").getObjects();
        }

        enemies = new Array<>();
        mapSouls = new Array<>();
        mapKeys = new Array<>();
        projectiles = new Array<>();

        MapObjects spawnObjects = null;
        if (map.getLayers().get("spawns") != null) {
            spawnObjects = map.getLayers().get("spawns").getObjects();
        } else if (map.getLayers().get("SpawnPoints") != null) {
            spawnObjects = map.getLayers().get("SpawnPoints").getObjects();
        }

        if (spawnObjects != null) {
            for (MapObject obj : spawnObjects) {
                if (obj.getProperties().get("x") != null && obj.getProperties().get("y") != null) {
                    float scaledX = ((Number) obj.getProperties().get("x")).floatValue() * SCALE_FACTOR;
                    float scaledY = ((Number) obj.getProperties().get("y")).floatValue() * SCALE_FACTOR;
                    String name = obj.getName() != null ? obj.getName().toLowerCase() : "";
                    String type = "";
                    if (obj.getProperties().get("type") != null) {
                        type = obj.getProperties().get("type").toString().toLowerCase();
                    } else if (obj.getProperties().get("class") != null) {
                        type = obj.getProperties().get("class").toString().toLowerCase();
                    }

                    if (name.equals("player") || type.contains("player") || name.contains("player")) {
                        pStartX = scaledX;
                        pStartY = scaledY;
                    } else if (name.equals("boss") || type.contains("boss") || name.contains("boss")) {
                        bStartX = scaledX;
                        bStartY = scaledY;
                    } else if (name.startsWith("enemy") || type.contains("enemy")) {
                        enemies.add(new Enemy(scaledX, scaledY, game.assets, levelIndex));
                    } else if (name.equals("soul") || type.contains("soul")) {
                        mapSouls.add(new Rectangle(scaledX, scaledY, TILE_SIZE, TILE_SIZE));
                    } else if (name.equals("key") || type.contains("key")) {
                        mapKeys.add(new Rectangle(scaledX, scaledY, TILE_SIZE * 2.5f, TILE_SIZE * 2.5f));
                    }
                }
            }
        }

        player = new Player(pStartX, pStartY, game.assets);

        // Instantiation works perfectly because CentaurBoss IS a Boss now
        if (levelIndex == 2) {
            activeBoss = new CentaurBoss(bStartX, bStartY, game.assets);
        } else {
            activeBoss = new Boss(bStartX, bStartY, game.assets);
        }

        hud = new HUD(batch, shapeRenderer, font, game.assets);

        uiStage = new Stage(new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT), batch);
        buildPauseMenu();
    }

    private void buildPauseMenu() {
        Skin skin = new Skin();
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        pixmap.dispose();

        skin.add("default", new Label.LabelStyle(font, Color.WHITE));

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
        btnStyle.over = skin.newDrawable("white", Color.GRAY);
        btnStyle.down = skin.newDrawable("white", Color.LIGHT_GRAY);
        btnStyle.font = font;
        skin.add("default", btnStyle);

        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = skin.newDrawable("white", Color.DARK_GRAY);
        sliderStyle.background.setMinHeight(10f);
        sliderStyle.knob = skin.newDrawable("white", Color.LIGHT_GRAY);
        sliderStyle.knob.setMinWidth(20f);
        sliderStyle.knob.setMinHeight(30f);
        skin.add("default-horizontal", sliderStyle);

        pauseTable = new Table();
        pauseTable.setFillParent(true);
        pauseTable.center();
        pauseTable.setVisible(false);

        TextButton optionsBtn = new TextButton("Options", skin);
        TextButton quitBtn = new TextButton("Quit Game", skin);

        pauseTable.add(optionsBtn).width(300).height(60).pad(10).row();
        pauseTable.add(quitBtn).width(300).height(60).pad(10);

        optionsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.playButtonSound();
                gameState = GameState.OPTIONS;
                pauseTable.setVisible(false);
                optionsTable.toFront();
                optionsTable.setVisible(true);
            }
        });

        quitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.playButtonSound();
                game.setScreen(new MenuScreen(game, batch, font, hudCamera));
            }
        });

        optionsTable = new Table();
        optionsTable.setFillParent(true);
        optionsTable.center();
        optionsTable.setVisible(false);

        Label volumeTitle = new Label("Volume:", skin);
        final Slider volumeSlider = new Slider(0f, 1f, 0.05f, false, skin);
        volumeSlider.setValue(game.globalVolume);
        volumeLabel = new Label((int) (game.globalVolume * 100) + "%", skin);

        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float val = volumeSlider.getValue();
                game.setGlobalVolume(val);
                volumeLabel.setText((int) (val * 100) + "%");
            }
        });

        TextButton level1Btn = new TextButton("Fast Travel: Level 1", skin);
        TextButton level2Btn = new TextButton("Fast Travel: Level 2", skin);
        TextButton backBtn = new TextButton("Back", skin);

        level1Btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.playButtonSound();
                game.setScreen(new PlayScreen(game, batch, font, hudCamera, 1));
            }
        });

        level2Btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.playButtonSound();
                game.setScreen(new PlayScreen(game, batch, font, hudCamera, 2));
            }
        });

        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.playButtonSound();
                gameState = GameState.PAUSE;
                optionsTable.setVisible(false);
                pauseTable.toFront();
                pauseTable.setVisible(true);
            }
        });

        Table sliderTable = new Table();
        sliderTable.add(volumeTitle).padRight(20);
        sliderTable.add(volumeSlider).width(250).padRight(20);
        sliderTable.add(volumeLabel).width(60);

        optionsTable.add(sliderTable).padBottom(40).row();
        optionsTable.add(level1Btn).width(400).height(60).pad(10).row();
        optionsTable.add(level2Btn).width(400).height(60).pad(10).row();
        optionsTable.add(backBtn).width(300).height(60).padTop(30);

        uiStage.addActor(pauseTable);
        uiStage.addActor(optionsTable);
    }

    private void resetGame() {
        player.dispose();
        activeBoss.dispose();

        for (Enemy e : enemies) e.dispose();

        enemies.clear();
        projectiles.clear();
        mapSouls.clear();
        mapKeys.clear();
        floatingTexts.clear();
        playerSouls = 0;
        playerKeys = 0;

        MapObjects spawnObjects = null;
        if (map.getLayers().get("spawns") != null) {
            spawnObjects = map.getLayers().get("spawns").getObjects();
        } else if (map.getLayers().get("SpawnPoints") != null) {
            spawnObjects = map.getLayers().get("SpawnPoints").getObjects();
        }

        if (spawnObjects != null) {
            for (MapObject obj : spawnObjects) {
                if (obj.getProperties().get("x") != null && obj.getProperties().get("y") != null) {
                    float scaledX = ((Number) obj.getProperties().get("x")).floatValue() * SCALE_FACTOR;
                    float scaledY = ((Number) obj.getProperties().get("y")).floatValue() * SCALE_FACTOR;
                    String name = obj.getName() != null ? obj.getName().toLowerCase() : "";
                    String type = "";
                    if (obj.getProperties().get("type") != null) {
                        type = obj.getProperties().get("type").toString().toLowerCase();
                    } else if (obj.getProperties().get("class") != null) {
                        type = obj.getProperties().get("class").toString().toLowerCase();
                    }

                    if (name.startsWith("enemy") || type.contains("enemy")) {
                        enemies.add(new Enemy(scaledX, scaledY, game.assets, levelIndex));
                    } else if (name.equals("soul") || type.contains("soul")) {
                        mapSouls.add(new Rectangle(scaledX, scaledY, TILE_SIZE, TILE_SIZE));
                    } else if (name.equals("key") || type.contains("key")) {
                        mapKeys.add(new Rectangle(scaledX, scaledY, TILE_SIZE * 2, TILE_SIZE * 2));
                    }
                }
            }
        }

        player = new Player(pStartX, pStartY, game.assets);

        if (levelIndex == 2) {
            activeBoss = new CentaurBoss(bStartX, bStartY, game.assets);
        } else {
            activeBoss = new Boss(bStartX, bStartY, game.assets);
        }

        gameState = GameState.PLAYING;
    }

    @Override
    public void render(float delta) {
        if (levelIndex == 2) {
            Gdx.gl.glClearColor(0.1f, 0.2f, 0.1f, 1f);
        } else {
            Gdx.gl.glClearColor(0.2f, 0.4f, 0.8f, 1f);
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (activeBoss.isDead()) {
            victoryTime += delta;
        }

        if (activeBoss.isDead() && victoryTime > 1.0f && Gdx.input.justTouched()) {
            com.badlogic.gdx.math.Vector3 mousePos = new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            hudCamera.unproject(mousePos);

            if (mousePos.x >= 490 && mousePos.x <= 790 && mousePos.y >= 295 && mousePos.y <= 345) {
                ((GreenGuardianGame) game).playButtonSound();
                if (levelIndex == 1) {
                    game.setScreen(new PlayScreen(game, batch, font, hudCamera, 2));
                } else {
                    game.setScreen(new MenuScreen(game, batch, font, hudCamera));
                }
                return;
            }
        }

        // Shop Key Toggle: only allowed during PLAYING or SHOP
        if (Gdx.input.isKeyJustPressed(Input.Keys.B) && gameState != GameState.GAME_OVER && !activeBoss.isDead() && (gameState == GameState.PLAYING || gameState == GameState.SHOP)) {
            gameState = (gameState == GameState.SHOP) ? GameState.PLAYING : GameState.SHOP;
            shopMessage = "";
            if (gameState == GameState.PLAYING) {
                Gdx.input.setInputProcessor(null);
            }
        }

        // Mouse click on Mystic Shop logo below Minimap to toggle Shop
        if (gameState == GameState.PLAYING && !activeBoss.isDead() && Gdx.input.justTouched()) {
            com.badlogic.gdx.math.Vector3 mPos = new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            hudCamera.unproject(mPos);
            if (mPos.x >= 1118f && mPos.x <= 1118f + 84f && mPos.y >= 502f && mPos.y <= 502f + 84f) {
                ((GreenGuardianGame) game).playButtonSound();
                gameState = GameState.SHOP;
                shopMessage = "";
            }
        } else if (gameState == GameState.SHOP && Gdx.input.justTouched()) {
            com.badlogic.gdx.math.Vector3 mPos = new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            hudCamera.unproject(mPos);
            // Click outside modal or on the top-right header area cleanly closes shop
            float modalX = 230f, modalY = 95f, modalW = 820f, modalH = 530f;
            if (mPos.x < modalX || mPos.x > modalX + modalW || mPos.y < modalY || mPos.y > modalY + modalH || (mPos.x >= modalX + modalW - 220f && mPos.y >= modalY + modalH - 45f)) {
                ((GreenGuardianGame) game).playButtonSound();
                gameState = GameState.PLAYING;
                shopMessage = "";
                Gdx.input.setInputProcessor(null);
            }
        }

        if (gameState == GameState.PLAYING) {
            updateWorld(delta);
        } else if (gameState == GameState.GAME_OVER) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                resetGame();
            }
        } else if (gameState == GameState.SHOP) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                shopSelectedIndex = (shopSelectedIndex - 1 + 4) % 4;
                ((GreenGuardianGame) game).playButtonSound();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                shopSelectedIndex = (shopSelectedIndex + 1) % 4;
                ((GreenGuardianGame) game).playButtonSound();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) {
                shopSelectedIndex = 0;
                buySelectedItem();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) {
                shopSelectedIndex = 1;
                buySelectedItem();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_3)) {
                shopSelectedIndex = 2;
                buySelectedItem();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_4)) {
                shopSelectedIndex = 3;
                buySelectedItem();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                buySelectedItem();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (gameState == GameState.SHOP) {
                // ESC key cleanly closes the shop modal and returns to playing
                gameState = GameState.PLAYING;
                shopMessage = "";
                Gdx.input.setInputProcessor(null);
            } else if (gameState == GameState.PLAYING) {
                gameState = GameState.PAUSE;
                if (uiStage != null) {
                    uiStage.unfocusAll();
                    uiStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
                }
                Gdx.input.setInputProcessor(uiStage);
                pauseTable.toFront();
                pauseTable.setVisible(true);
                optionsTable.setVisible(false);
            } else if (gameState == GameState.PAUSE || gameState == GameState.OPTIONS) {
                gameState = GameState.PLAYING;
                Gdx.input.setInputProcessor(null);
                pauseTable.setVisible(false);
                optionsTable.setVisible(false);
            }
        }

        float mapWidthInPixels = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class) * SCALE_FACTOR;
        float mapHeightInPixels = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class) * SCALE_FACTOR;

        float targetX = player.getBounds().x + (player.getBounds().width / 2);
        camera.position.x += (targetX - camera.position.x) * 5.0f * delta;
        camera.position.x = Math.max(WORLD_WIDTH / 2 + buffer, Math.min(camera.position.x, mapWidthInPixels - WORLD_WIDTH / 2 - buffer));

        float targetY = player.getBounds().y + (player.getBounds().height / 2);
        camera.position.y += (targetY - camera.position.y) * 5.0f * delta;
        camera.position.y = Math.max(WORLD_HEIGHT / 2 + buffer, Math.min(camera.position.y, mapHeightInPixels - WORLD_HEIGHT / 2 - buffer));
        camera.update();

        // Expand culling bounds by 750 world pixels so large objects & trees (up to 200px tiles * 2.5 scale) do not disappear prematurely
        float extraCullPadding = 750f;
        mapRenderer.setView(
            camera.combined,
            camera.position.x - (WORLD_WIDTH / 2f) - extraCullPadding,
            camera.position.y - (WORLD_HEIGHT / 2f) - extraCullPadding,
            WORLD_WIDTH + (extraCullPadding * 2f),
            WORLD_HEIGHT + (extraCullPadding * 2f)
        );
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Rectangle s : mapSouls) {
            batch.draw(game.assets.soulTexture, s.x, s.y, s.width, s.height);
        }
        for (Rectangle k : mapKeys) {
            batch.draw(game.assets.keyTexture, k.x, k.y, k.width, k.height);
        }
        for (Enemy e : enemies) e.draw(batch);

        player.draw(batch);
        activeBoss.draw(batch);

        for (Projectile p : projectiles) {
            batch.draw((p.type == 2) ? game.assets.staffProjectileTexture : game.assets.swordProjectileTexture, p.bounds.x, p.bounds.y, p.bounds.width, p.bounds.height);
        }

        for (int i = floatingTexts.size - 1; i >= 0; i--) {
            FloatingText ft = floatingTexts.get(i);
            if (ft.update(delta)) {
                floatingTexts.removeIndex(i);
            } else {
                ft.draw(batch, font);
            }
        }
        batch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy e : enemies) e.drawFloatingHealth(shapeRenderer);
        shapeRenderer.end();

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // These HUD calls will compile perfectly now that activeBoss is a type of Boss
        hud.drawHealthBars(player, activeBoss, levelIndex);
        hud.drawWeaponHotbarSlots(player, gameState == GameState.GAME_OVER, gameState == GameState.SHOP, activeBoss);
        hud.drawMinimap(shapeRenderer, mapWidthInPixels, mapHeightInPixels, player, enemies, activeBoss, mapSouls, mapKeys, camera, mapBlocks, SCALE_FACTOR, gameState == GameState.GAME_OVER || gameState == GameState.SHOP);
        shapeRenderer.end();

        hud.drawOverlays(gameState == GameState.GAME_OVER, gameState == GameState.SHOP, activeBoss, shopSelectedIndex);

        batch.setProjectionMatrix(hudCamera.combined);
        hud.drawTextAndIcons(gameState == GameState.GAME_OVER, gameState == GameState.SHOP, activeBoss, playerSouls, playerKeys, STAFF_COST, shopMessage, levelIndex, player, shopSelectedIndex);

        if (gameState == GameState.PAUSE || gameState == GameState.OPTIONS) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            shapeRenderer.setProjectionMatrix(hudCamera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, 0.7f);
            shapeRenderer.rect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            shapeRenderer.end();

            Gdx.gl.glDisable(GL20.GL_BLEND);

            uiStage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
            uiStage.draw();
        }
    }

    private void buySelectedItem() {
        if (shopSelectedIndex == 0) {
            // Magic Staff
            if (player.hasUnlockedStaff()) {
                shopMessage = "ALREADY OWNED!";
            } else if (playerSouls >= STAFF_COST) {
                playerSouls -= STAFF_COST;
                player.unlockStaff();
                ((GreenGuardianGame) game).playButtonSound();
                shopMessage = "PURCHASE SUCCESSFUL! [Press 1 or 2 to Switch]";
            } else {
                shopMessage = "NOT ENOUGH SOULS!";
            }
        } else if (shopSelectedIndex == 1) {
            // Max Health Elixir (+200 HP)
            if (playerSouls >= HEALTH_ELIXIR_COST) {
                playerSouls -= HEALTH_ELIXIR_COST;
                player.addMaxHealth(200);
                ((GreenGuardianGame) game).playButtonSound();
                shopMessage = "PURCHASE SUCCESSFUL! Max HP increased by +200!";
            } else {
                shopMessage = "NOT ENOUGH SOULS!";
            }
        } else if (shopSelectedIndex == 2) {
            // Damage Stone (+20% Damage)
            if (playerSouls >= DAMAGE_STONE_COST) {
                playerSouls -= DAMAGE_STONE_COST;
                player.addDamageStone();
                ((GreenGuardianGame) game).playButtonSound();
                shopMessage = "PURCHASE SUCCESSFUL! Damage increased by +20%!";
            } else {
                shopMessage = "NOT ENOUGH SOULS!";
            }
        } else if (shopSelectedIndex == 3) {
            // Soul Magnet
            if (player.hasSoulMagnet()) {
                shopMessage = "ALREADY OWNED!";
            } else if (playerSouls >= SOUL_MAGNET_COST) {
                playerSouls -= SOUL_MAGNET_COST;
                player.unlockSoulMagnet();
                ((GreenGuardianGame) game).playButtonSound();
                shopMessage = "PURCHASE SUCCESSFUL! Soul Magnet active!";
            } else {
                shopMessage = "NOT ENOUGH SOULS!";
            }
        }
    }

    public void spawnFloatingText(float x, float y, String text, Color color) {
        spawnFloatingText(x, y, text, color, 1.0f, 1.0f);
    }

    public void spawnFloatingText(float x, float y, String text, Color color, float duration, float scale) {
        floatingTexts.add(new FloatingText(x, y, text, color, duration, scale));
    }

    private void updateWorld(float delta) {
        // Player Heal with 'H' key: costs 50 souls and restores 50% health
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            if (player.getHealth() >= player.getMaxHealth()) {
                spawnFloatingText(player.getBounds().x + 5, player.getBounds().y + 80, "FULL HP!", Color.GOLD, 0.9f, 1.0f);
            } else if (playerSouls < 50) {
                spawnFloatingText(player.getBounds().x - 10, player.getBounds().y + 80, "NEED 50 SOULS!", Color.valueOf("EF4444"), 0.9f, 1.0f);
            } else if (player.canHeal()) {
                playerSouls -= 50;
                player.heal();
                spawnFloatingText(player.getBounds().x + 5, player.getBounds().y + 85, "+500 HP", Color.valueOf("4ADE80"), 1.3f, 1.3f);
            }
        }

        // Floating text when player takes damage
        if (player.pollJustTookDamage()) {
            spawnFloatingText(player.getBounds().x + player.getBounds().width / 2f - 10, player.getBounds().y + 75, "-" + (int) player.getLastDamageTaken(), Color.valueOf("EF4444"), 0.9f, 1.1f);
        }

        player.update(delta, projectiles, mapBlocks, mapWater, SCALE_FACTOR);

        // activeBoss is already a Boss, so it updates polymorphically without a cast
        activeBoss.update(delta, player, mapBlocks, SCALE_FACTOR);

        for (Enemy e : enemies) e.update(delta, player, mapBlocks, SCALE_FACTOR);

        // Sword melee attack check (only applies when player swings sword)
        if (player.canDealMeleeDamage()) {
            Rectangle meleeHitbox = player.getMeleeHitbox();
            boolean hitAnything = false;
            float damage = player.getMeleeDamage();

            if (!activeBoss.isDead() && meleeHitbox.overlaps(activeBoss.getBounds())) {
                activeBoss.takeDamage(damage);
                spawnFloatingText(activeBoss.getBounds().x + activeBoss.getBounds().width / 2f - 15, activeBoss.getBounds().y + activeBoss.getBounds().height / 2f, "-" + (int) damage, Color.valueOf("F59E0B"), 0.9f, 1.2f);
                hitAnything = true;
            }

            for (Enemy e : enemies) {
                if (!e.isDead() && meleeHitbox.overlaps(e.getBounds())) {
                    e.takeDamage(damage);
                    spawnFloatingText(e.getBounds().x + e.getBounds().width / 2f - 10, e.getBounds().y + e.getBounds().height + 10, "-" + (int) damage, Color.valueOf("FBBF24"), 0.9f, 1.0f);
                    if (e.isDead()) {
                        playerSouls += 15;
                        spawnFloatingText(e.getBounds().x, e.getBounds().y + 35, "+15 Souls", Color.valueOf("C084FC"), 1.0f, 1.0f);
                    }
                    hitAnything = true;
                }
            }

            if (hitAnything) {
                player.setDealtMeleeDamage(true);
            }
        }

        for (int i = mapSouls.size - 1; i >= 0; i--) {
            Rectangle s = mapSouls.get(i);
            if (player.hasSoulMagnet()) {
                float px = player.getBounds().x + player.getBounds().width / 2f;
                float py = player.getBounds().y + player.getBounds().height / 2f;
                float sx = s.x + s.width / 2f;
                float sy = s.y + s.height / 2f;
                float dx = px - sx;
                float dy = py - sy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist < 400f && dist > 1f) {
                    float pullSpeed = 350f * delta;
                    s.x += (dx / dist) * pullSpeed;
                    s.y += (dy / dist) * pullSpeed;
                }
            }
            if (player.getBounds().overlaps(s)) {
                playerSouls += 10;
                spawnFloatingText(s.x, s.y + 20, "+10 Souls", Color.valueOf("C084FC"), 0.9f, 0.9f);
                mapSouls.removeIndex(i);
            }
        }

        for (int i = mapKeys.size - 1; i >= 0; i--) {
            Rectangle k = mapKeys.get(i);
            if (player.getBounds().overlaps(k)) {
                playerKeys++;
                spawnFloatingText(k.x - 15, k.y + 30, "+1 KEY FOUND!", Color.valueOf("FACC15"), 1.3f, 1.2f);
                mapKeys.removeIndex(i);
            }
        }

        if (player.getHealth() <= 0 || player.getBounds().y < -100) {
            gameState = GameState.GAME_OVER;
        }

        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update(delta);
            boolean projectileHit = false;

            if (!activeBoss.isDead() && p.bounds.overlaps(activeBoss.getBounds())) {
                activeBoss.takeDamage(p.damage);
                spawnFloatingText(activeBoss.getBounds().x + activeBoss.getBounds().width / 2f - 15, activeBoss.getBounds().y + activeBoss.getBounds().height / 2f + 10, "-" + (int) p.damage, Color.valueOf("F59E0B"), 0.9f, 1.1f);
                projectiles.removeIndex(i);
                projectileHit = true;
            }

            if (!projectileHit) {
                for (Enemy e : enemies) {
                    if (!e.isDead() && p.bounds.overlaps(e.getBounds())) {
                        e.takeDamage(p.damage);
                        spawnFloatingText(e.getBounds().x + e.getBounds().width / 2f - 10, e.getBounds().y + e.getBounds().height + 10, "-" + (int) p.damage, Color.valueOf("FBBF24"), 0.9f, 1.0f);
                        if (e.isDead()) {
                            playerSouls += 15;
                            spawnFloatingText(e.getBounds().x, e.getBounds().y + 35, "+15 Souls", Color.valueOf("C084FC"), 1.0f, 1.0f);
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

            if (!projectileHit && Math.abs(p.bounds.x - player.getBounds().x) > MAX_PROJECTILE_RANGE) {
                projectiles.removeIndex(i);
            }
        }
    }

    private Rectangle tmpBlockRect = new Rectangle();

    private boolean checkMapCollision(Rectangle rect) {
        for (MapObject object : mapBlocks) {
            Rectangle blockRect = null;

            if (object instanceof RectangleMapObject) {
                blockRect = ((RectangleMapObject) object).getRectangle();
            } else if (object instanceof PolygonMapObject) {
                blockRect = ((PolygonMapObject) object).getPolygon().getBoundingRectangle();
            }

            if (blockRect != null) {
                tmpBlockRect.set(
                    blockRect.x * SCALE_FACTOR,
                    blockRect.y * SCALE_FACTOR,
                    blockRect.width * SCALE_FACTOR,
                    blockRect.height * SCALE_FACTOR
                );

                if (rect.overlaps(tmpBlockRect)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        if (uiStage != null) {
            uiStage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
        if (uiStage != null) {
            uiStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        }
        ((GreenGuardianGame) game).playGameplayMusic();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        player.dispose();
        activeBoss.dispose();
        for (Enemy e : enemies) e.dispose();
        map.dispose();
        mapRenderer.dispose();
        if (uiStage != null) uiStage.dispose();
    }
}
