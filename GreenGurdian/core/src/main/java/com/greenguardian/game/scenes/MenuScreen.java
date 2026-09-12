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

public class MenuScreen extends BaseScreen {
    private Stage stage;
    private Texture background;
    private Texture newGameTexture;
    private Texture quitTexture;

    public MenuScreen(GreenGuardianGame game, SpriteBatch batch, BitmapFont font, OrthographicCamera hudCamera) {
        super(game, batch, font, hudCamera);
        
        background = new Texture("ui/screen-bg-without-start.jpg");
        stage = new Stage(new StretchViewport(1280, 720), batch);
        Gdx.input.setInputProcessor(stage);
        
        game.playStartMusic();

        newGameTexture = new Texture("ui/btn_new_game.png");
        Texture newGameHoverTexture = new Texture("ui/btn_new_game_hover.png");
        quitTexture = new Texture("ui/btn_quit.png");
        Texture quitHoverTexture = new Texture("ui/btn_quit_hover.png");

        ImageButton.ImageButtonStyle newGameStyle = new ImageButton.ImageButtonStyle();
        newGameStyle.imageUp = new TextureRegionDrawable(new TextureRegion(newGameTexture));
        newGameStyle.imageOver = new TextureRegionDrawable(new TextureRegion(newGameHoverTexture));

        ImageButton.ImageButtonStyle quitStyle = new ImageButton.ImageButtonStyle();
        quitStyle.imageUp = new TextureRegionDrawable(new TextureRegion(quitTexture));
        quitStyle.imageOver = new TextureRegionDrawable(new TextureRegion(quitHoverTexture));

        ImageButton newGameBtn = new ImageButton(newGameStyle);
        newGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.playButtonSound();
                game.setScreen(new PlayScreen(game, batch, font, hudCamera, 1));
            }
        });

        ImageButton quitBtn = new ImageButton(quitStyle);
        quitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.playButtonSound();
                Gdx.app.exit();
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.bottom().padBottom(150);
        // The original buttons are around 1100x250, we scale them down slightly to fit 1280x720 neatly
        table.add(newGameBtn).width(450).height(100).padBottom(20).row();
        table.add(quitBtn).width(450).height(100);

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(stage.getViewport().getCamera().combined);
        batch.begin();
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
        if (newGameTexture != null) newGameTexture.dispose();
        if (quitTexture != null) quitTexture.dispose();
    }
}

