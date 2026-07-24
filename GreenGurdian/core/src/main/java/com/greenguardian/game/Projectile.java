package com.greenguardian.game;

import com.badlogic.gdx.math.Rectangle;

public class Projectile {
    public Rectangle bounds;
    public boolean facingRight;

    public Projectile(float x, float y, boolean facingRight) {
        this.bounds = new Rectangle(x, y, 16, 8);
        this.facingRight = facingRight;
    }

    public void update(float delta) {
        // Move the projectile forward based on the direction it was fired
        bounds.x += (facingRight ? 400 : -400) * delta;
    }
}
