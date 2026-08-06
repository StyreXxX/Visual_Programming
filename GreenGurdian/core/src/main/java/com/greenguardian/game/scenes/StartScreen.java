package com.greenguardian.game.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
// import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.greenguardian.game.GreenGuardianGame;

public class StartScreen extends BaseScreen {

    public StartScreen(GreenGuardianGame game, SpriteBatch batch, BitmapFont font, OrthographicCamera hudCamera) {
        super(game, batch, font, hudCamera);
    }

    @Override
    public void render(float delta) {
        stateTime += delta;

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) || Gdx.input.justTouched()) {
            game.setScreen(new MenuScreen(game, batch, font, hudCamera));
        }

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        
        // Calculate a fading alpha value that oscillates smoothly
        float alpha = fadingAlpha + 0.5f * (float)Math.sin(stateTime * fadeSpeed);
        font.setColor(1f, 1f, 1f, alpha);
        
        font.draw(batch, "PRESS ANY KEY TO START", 470, 360);
        batch.end();
    }
}
