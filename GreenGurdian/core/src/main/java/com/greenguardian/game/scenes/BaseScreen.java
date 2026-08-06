package com.greenguardian.game.scenes;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.greenguardian.game.GreenGuardianGame;

public abstract class BaseScreen extends ScreenAdapter {
    protected GreenGuardianGame game;
    protected SpriteBatch batch;
    protected BitmapFont font;
    protected OrthographicCamera hudCamera;

    protected final float TILE_SIZE = 32f;
    protected final float WORLD_WIDTH = 1280f;
    protected final float WORLD_HEIGHT = 720f;
    protected final float SCALE_FACTOR = 2.5f;

    protected float fadingAlpha = 0.7f;
    protected float fadeSpeed = 3f;
    protected float stateTime = 0f;

    public BaseScreen(GreenGuardianGame game, SpriteBatch batch, BitmapFont font, OrthographicCamera hudCamera) {
        this.game = game;
        this.batch = batch;
        this.font = font;
        this.hudCamera = hudCamera;
    }
}
