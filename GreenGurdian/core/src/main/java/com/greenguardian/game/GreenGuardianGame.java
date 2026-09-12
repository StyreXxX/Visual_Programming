package com.greenguardian.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
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

    public Music startMusic;
    public Music gameplayMusic;
    public Sound buttonSound;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.8f);

        assets = new AssetLoader();
        assets.load();

        hudCamera = new OrthographicCamera();
        hudViewport = new StretchViewport(1280, 720, hudCamera);

        startMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/start-music.mp3"));
        startMusic.setLooping(true);
        startMusic.setVolume(0.5f);

        gameplayMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/gameplay-music.mp3"));
        gameplayMusic.setLooping(true);
        gameplayMusic.setVolume(0.5f);

        buttonSound = Gdx.audio.newSound(Gdx.files.internal("audio/button-click-sound.mp3"));

        playStartMusic();

        setScreen(new StartScreen(this, batch, font, hudCamera));
    }

    public void playButtonSound() {
        if (buttonSound != null) {
            buttonSound.play(1.0f);
        }
    }

    public void playStartMusic() {
        if (gameplayMusic.isPlaying()) {
            gameplayMusic.stop();
        }
        if (!startMusic.isPlaying()) {
            startMusic.play();
        }
    }

    public void playGameplayMusic() {
        if (startMusic.isPlaying()) {
            startMusic.stop();
        }
        if (!gameplayMusic.isPlaying()) {
            gameplayMusic.play();
        }
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
        if (startMusic != null) startMusic.dispose();
        if (gameplayMusic != null) gameplayMusic.dispose();
        if (buttonSound != null) buttonSound.dispose();
    }
}
