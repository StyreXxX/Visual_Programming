package com.greenguardian.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.greenguardian.game.assets.AssetLoader;
import com.greenguardian.game.scenes.StartScreen;

public class GreenGuardianGame extends Game {
    public SpriteBatch batch;
    public BitmapFont font;
    public AssetLoader assets;

    public OrthographicCamera hudCamera;
    public Viewport hudViewport;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.8f);

        assets = new AssetLoader();
        assets.load();

        hudCamera = new OrthographicCamera();
        hudViewport = new StretchViewport(1280, 720, hudCamera);

        setScreen(new StartScreen(this, batch, font, hudCamera));
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        hudViewport.update(width, height, true);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        font.dispose();
        assets.dispose();
    }
}
