package com.greenguardian.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class FloatingText {
    public float x;
    public float y;
    public String text;
    public Color color;
    public float duration;
    public float elapsedTime = 0f;
    public float vy = 55f;
    public float vx = 0f;
    public float scale = 1.0f;

    public FloatingText(float x, float y, String text, Color color) {
        this(x, y, text, color, 1.0f, 1.0f);
    }

    public FloatingText(float x, float y, String text, Color color, float duration, float scale) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = new Color(color);
        this.duration = duration;
        this.scale = scale;
    }

    public boolean update(float delta) {
        elapsedTime += delta;
        y += vy * delta;
        x += vx * delta;
        vy = Math.max(10f, vy - 25f * delta);
        return elapsedTime >= duration;
    }

    public void draw(SpriteBatch batch, BitmapFont font) {
        float alpha = Math.max(0f, 1f - (elapsedTime / duration));
        Color oldColor = font.getColor();
        font.setColor(color.r, color.g, color.b, alpha);
        font.getData().setScale(scale);
        font.draw(batch, text, x, y);
        font.getData().setScale(1.0f);
        font.setColor(oldColor);
    }
}
