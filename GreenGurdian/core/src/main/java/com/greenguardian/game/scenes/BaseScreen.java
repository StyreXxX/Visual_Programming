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

    public BaseScreen(GreenGuardianGame game, SpriteBatch batch, BitmapFont font, OrthographicCamera hudCamera) {
        this.game = game;
        this.batch = batch;
        this.font = font;
        this.hudCamera = hudCamera;
    }
}
