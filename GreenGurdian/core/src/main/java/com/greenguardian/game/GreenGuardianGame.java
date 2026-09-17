package com.greenguardian.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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

    // Default volume at 50%
    public float globalVolume = 0.5f;

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
        startMusic.setVolume(globalVolume);

        gameplayMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/gameplay-music.mp3"));
        gameplayMusic.setLooping(true);
        gameplayMusic.setVolume(globalVolume);

        buttonSound = Gdx.audio.newSound(Gdx.files.internal("audio/button-click-sound.mp3"));

        playStartMusic();
        setScreen(new StartScreen(this, batch, font, hudCamera));
    }

    public void setGlobalVolume(float volume) {
        this.globalVolume = Math.max(0f, Math.min(1f, volume));
        if (startMusic != null) startMusic.setVolume(globalVolume);
        if (gameplayMusic != null) gameplayMusic.setVolume(globalVolume);
    }

    public void playButtonSound() {
        if (buttonSound != null) {
            // buttonSound.play() accepts volume as a parameter
            buttonSound.play(globalVolume);
        }
    }

    public void playStartMusic() {
        if (gameplayMusic != null && gameplayMusic.isPlaying()) gameplayMusic.stop();
        if (startMusic != null && !startMusic.isPlaying()) {
            startMusic.setVolume(globalVolume);
            startMusic.play();
        }
    }

    public void playGameplayMusic() {
        if (startMusic != null && startMusic.isPlaying()) startMusic.stop();
        if (gameplayMusic != null && !gameplayMusic.isPlaying()) {
            gameplayMusic.setVolume(globalVolume);
            gameplayMusic.play();
        }
    }

    @Override
    public void setScreen(Screen nextScreen) {
        Screen previousScreen = getScreen();
        super.setScreen(nextScreen);
        if (previousScreen != null && previousScreen != nextScreen) {
            previousScreen.dispose();
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
        if (getScreen() != null) {
            getScreen().dispose();
        }
        super.dispose();
        batch.dispose();
        font.dispose();
        assets.dispose();
        if (startMusic != null) startMusic.dispose();
        if (gameplayMusic != null) gameplayMusic.dispose();
        if (buttonSound != null) buttonSound.dispose();
    }
}
