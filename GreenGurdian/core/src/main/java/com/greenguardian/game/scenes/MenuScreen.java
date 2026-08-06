package com.greenguardian.game.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.greenguardian.game.GreenGuardianGame;

public class MenuScreen extends BaseScreen {
    private int menuIndex = 0;

    public MenuScreen(GreenGuardianGame game, SpriteBatch batch, BitmapFont font, OrthographicCamera hudCamera) {
        super(game, batch, font, hudCamera);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            menuIndex = 0;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            menuIndex = 1;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (menuIndex == 0) {
                game.setScreen(new PlayScreen(game, batch, font, hudCamera));
            } else {
                Gdx.app.exit();
            }
        }

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        GlyphLayout layout = new GlyphLayout();

        String title = "GREEN GUARDIAN";
        layout.setText(font, title);
        font.setColor(Color.GOLD);
        font.draw(batch, title, (1280 - layout.width) / 2, 460);

        String opt1 = (menuIndex == 0) ? "> NEW GAME <" : "  NEW GAME  ";
        String opt2 = (menuIndex == 1) ? "> QUIT <" : "  QUIT  ";

        layout.setText(font, opt1);
        font.setColor(menuIndex == 0 ? Color.GREEN : Color.WHITE);
        font.draw(batch, opt1, (1280 - layout.width) / 2, 360);

        layout.setText(font, opt2);
        font.setColor(menuIndex == 1 ? Color.RED : Color.WHITE);
        font.draw(batch, opt2, (1280 - layout.width) / 2, 310);

        batch.end();
    }
}
