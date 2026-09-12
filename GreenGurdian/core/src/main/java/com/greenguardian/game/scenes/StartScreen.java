package com.greenguardian.game.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.greenguardian.game.GreenGuardianGame;

public class StartScreen extends BaseScreen {
    private Stage stage;
    private Texture background;
    private Texture startButtonTexture;

    public StartScreen(GreenGuardianGame game, SpriteBatch batch, BitmapFont font, OrthographicCamera hudCamera) {
        super(game, batch, font, hudCamera);
        background = new Texture("ui/screen-bg-without-start.jpg");
        
        stage = new Stage(new StretchViewport(1280, 720), batch);
        Gdx.input.setInputProcessor(stage);

        startButtonTexture = new Texture("ui/btn_start.png");
        Texture startButtonHoverTexture = new Texture("ui/btn_start_hover.png");

        ImageButton.ImageButtonStyle startStyle = new ImageButton.ImageButtonStyle();
        startStyle.imageUp = new TextureRegionDrawable(new TextureRegion(startButtonTexture));
        startStyle.imageOver = new TextureRegionDrawable(new TextureRegion(startButtonHoverTexture));

        ImageButton startBtn = new ImageButton(startStyle);
        startBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.playButtonSound();
                game.setScreen(new MenuScreen(game, batch, font, hudCamera));
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.center().padTop(200);
        // Using the same consistent 450x100 sizing as the Menu Screen
        table.add(startBtn).width(450).height(100);

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        stateTime += delta;

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(stage.getViewport().getCamera().combined);
        batch.begin();
        // Draw background
        batch.draw(background, 0, 0, 1280, 720);
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (stage != null) stage.dispose();
        if (background != null) background.dispose();
        if (startButtonTexture != null) startButtonTexture.dispose();
    }
}

