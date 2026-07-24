package com.greenguardian.game;

import com.badlogic.gdx.math.Rectangle;

public class Projectile {
    public Rectangle bounds;
    public boolean facingRight;
    public int damage;
    public int type; // 1 = Sword Projectile, 2 = Staff Magic Wave

    public Projectile(float x, float y, boolean facingRight, int type) {
        this.facingRight = facingRight;
        this.type = type;

        if (type == 2) {
            // Staff Projectile: Bigger hitbox & higher damage
            this.bounds = new Rectangle(x, y, 28, 16);
            this.damage = 2;
        } else {
            // Sword Projectile: Standard settings
            this.bounds = new Rectangle(x, y, 16, 8);
            this.damage = 1;
        }
    }

    public void update(float delta) {
        // Staff projectile moves faster (600 speed vs 400 speed)
        float speed = (type == 2) ? 600f : 400f;
        bounds.x += (facingRight ? speed : -speed) * delta;
    }
}
