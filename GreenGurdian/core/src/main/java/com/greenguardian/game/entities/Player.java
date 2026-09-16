package com.greenguardian.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.greenguardian.game.assets.AssetLoader;

public class Player extends Entity {
    private Texture staffWalkSheet, staffAttackSheet;
    private Texture healSheet;
    private Animation<TextureRegion> staffWalkAnim, staffAttackAnim, idleAnim, healAnim;
    private boolean isHealing = false;
    private float healStateTime = 0f;
    // private TextureRegion staffIdleFrame;

    private boolean hasUnlockedStaff = false;
    private float bonusDamageMultiplier = 1.0f;
    private int damageStoneCount = 0;
    private boolean hasSoulMagnet = false;
    private int equippedWeapon = 1; // 1 = Sword, 2 = Staff
    private boolean inWater = false;
    private boolean wasInWater = false;
    private float waterDebuffTimer = 0f;
    private boolean hasDealtMeleeDamage = false;

    private final float JUMP_SPEED = 1000f;
    private final float PLAYER_SPEED = 500f;
    private static final int INITIAL_MAX_HEALTH = 1000;

    private float groundedTimer = 0f;
    private static final float COYOTE_TIME = 0.15f;

    public Player(float startX, float startY, AssetLoader assets) {
        super(startX, startY, 30, 70);
        this.maxHealth = INITIAL_MAX_HEALTH;
        this.health = INITIAL_MAX_HEALTH;

        walkSheet = assets.playerWalkSheet;
        attackSheet = assets.playerAttackSheet;
        deathSheet = assets.playerDeathSheet;
        standSheet = assets.playerStandSheet;

        staffWalkSheet = assets.playerStaffWalkSheet;
        staffAttackSheet = assets.playerStaffAttackSheet;

        walkAnim = createAnimation(walkSheet, 4, 0.15f);
        attackAnim = createAnimation(attackSheet, 3, 0.1f);
        deathAnim = createAnimation(deathSheet, 4, 0.2f);
        idleAnim = createAnimation(assets.playerIdleSheet, 8, 0.12f);

        staffWalkAnim = createAnimation(staffWalkSheet, 4, 0.15f);
        staffAttackAnim = createAnimation(staffAttackSheet, 3, 0.1f);

        healSheet = assets.playerHealSheet;
        healAnim = createAnimation(healSheet, 8, 0.11f);

        idleFrame = new TextureRegion(standSheet);
        // staffIdleFrame = new TextureRegion(staffWalkSheet, 0, 0, staffWalkSheet.getWidth() / 4, staffWalkSheet.getHeight());
    }

    public void equipStaff() {
        unlockStaff();
    }

    public void unlockStaff() {
        this.hasUnlockedStaff = true;
        this.equippedWeapon = 2;
    }

    public boolean hasStaff() {
        return hasUnlockedStaff;
    }

    public boolean hasUnlockedStaff() {
        return hasUnlockedStaff;
    }

    public int getEquippedWeapon() {
        return equippedWeapon;
    }

    public void setEquippedWeapon(int slot) {
        if (slot == 1) {
            this.equippedWeapon = 1;
        } else if (slot == 2 && hasUnlockedStaff) {
            this.equippedWeapon = 2;
        }
    }

    public boolean isStaffEquipped() {
        return equippedWeapon == 2;
    }

    public boolean hasDealtMeleeDamage() {
        return hasDealtMeleeDamage;
    }

    public void setDealtMeleeDamage(boolean dealt) {
        this.hasDealtMeleeDamage = dealt;
    }

    public boolean isWaterDebuffed() {
        return inWater || waterDebuffTimer > 0f;
    }

    public float getWaterDebuffTimer() {
        return waterDebuffTimer;
    }

    private float lastDamageTaken = 0f;
    private boolean justTookDamage = false;

    @Override
    public void takeDamage(float amount) {
        super.takeDamage(amount);
        this.lastDamageTaken = amount;
        this.justTookDamage = true;
    }

    public boolean pollJustTookDamage() {
        if (justTookDamage) {
            justTookDamage = false;
            return true;
        }
        return false;
    }

    public float getLastDamageTaken() {
        return lastDamageTaken;
    }

    public float getAttackDamageMultiplier() {
        return isWaterDebuffed() ? 0.5f : 1.0f;
    }

    public float getMeleeDamage() {
        return 1.0f * bonusDamageMultiplier * getAttackDamageMultiplier();
    }

    public void addMaxHealth(float amount) {
        this.maxHealth += amount;
        this.health = Math.min(this.maxHealth, this.health + amount);
    }

    public void addDamageStone() {
        this.damageStoneCount++;
        this.bonusDamageMultiplier += 0.20f;
    }

    public float getBonusDamageMultiplier() {
        return bonusDamageMultiplier;
    }

    public int getDamageStoneCount() {
        return damageStoneCount;
    }

    public void unlockSoulMagnet() {
        this.hasSoulMagnet = true;
    }

    public boolean hasSoulMagnet() {
        return hasSoulMagnet;
    }

    public boolean canDealMeleeDamage() {
        return isAttacking && !isStaffEquipped() && !hasDealtMeleeDamage && stateTime >= 0.08f && stateTime <= 0.45f;
    }

    public boolean canHeal() {
        return !isDead && !isHealing && health < maxHealth;
    }

    public boolean isHealing() {
        return isHealing;
    }

    public void heal() {
        if (!canHeal()) return;
        isHealing = true;
        healStateTime = 0f;
        health = Math.min(maxHealth, health + maxHealth * 0.5f);
    }

    public Rectangle getMeleeHitbox() {
        float reachWidth = 70f;
        float rx = facingRight ? bounds.x : bounds.x - reachWidth;
        float rw = bounds.width + reachWidth;
        return new Rectangle(rx, bounds.y - 10f, rw, bounds.height + 20f);
    }

    public void update(float delta, Array<Projectile> projectiles, MapObjects blocks, MapObjects waterZones, float scale) {
        if (health <= 0 && !isDead) {
            isDead = true;
            stateTime = 0;
        }

        if (isAttacking && isWaterDebuffed()) {
            stateTime += delta * 0.5f; // 50% slower attack speed
        } else {
            stateTime += delta;
        }
        if (isDead) return;

        if (isHealing) {
            healStateTime += delta;
            if (healAnim.isAnimationFinished(healStateTime)) {
                isHealing = false;
                healStateTime = 0f;
            }
        }

        float oldY = bounds.y;
        velocityY += GRAVITY * delta;
        bounds.y += velocityY * delta;

        boolean isGrounded = false;
        if (checkCollision(bounds, blocks, scale)) {
            bounds.y = oldY;
            if (velocityY < 0) isGrounded = true;
            velocityY = 0;
        }

        if (isGrounded) {
            groundedTimer = COYOTE_TIME;
        } else {
            groundedTimer -= delta;
        }

        // Check water collision
        inWater = false;
        if (waterZones != null) {
            inWater = checkCollision(bounds, waterZones, scale);
        }

        // When player touches water or comes out of water, trigger/refresh debuff for 5 seconds
        if (inWater) {
            if (!wasInWater || waterDebuffTimer <= 0f) {
                waterDebuffTimer = 5.0f;
            }
        } else if (wasInWater) {
            // Player just came out of water: debuff stays for 5 seconds
            waterDebuffTimer = 5.0f;
        }
        wasInWater = inWater;

        // Process water debuff (duration is exactly 5 seconds, reducing health slowly by 10%)
        if (waterDebuffTimer > 0f) {
            waterDebuffTimer -= delta;

            // Reduce health slowly by 10% of max health over 5 seconds (2% per second)
            float damageThisFrame = (maxHealth * 0.10f / 5.0f) * delta;
            health -= damageThisFrame;
            if (health <= 0f) {
                health = 0f;
                isDead = true;
                stateTime = 0f;
            }

            if (waterDebuffTimer < 0f) {
                waterDebuffTimer = 0f;
            }
        }

        // Reduce jump height to half (50%) when in water or water debuffed (persists for 5s out of water)
        float currentJumpSpeed = isWaterDebuffed() ? JUMP_SPEED * 0.5f : JUMP_SPEED;
        if (groundedTimer > 0 && !isAttacking && !isHealing && (Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.UP))) {
            velocityY = currentJumpSpeed;
            groundedTimer = 0;
        }

        float oldX = bounds.x;

        // Reduce movement speed by 50% when in water or water debuffed (persists for 5s out of water)
        float currentSpeed = isWaterDebuffed() ? PLAYER_SPEED * 0.5f : PLAYER_SPEED;

        if (!isAttacking && !isHealing) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) {
                equippedWeapon = 1;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) {
                if (hasUnlockedStaff) {
                    equippedWeapon = 2;
                }
            }

            if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                bounds.x -= currentSpeed * delta;
                facingRight = false;
            } else if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                bounds.x += currentSpeed * delta;
                facingRight = true;
            }

            if (checkCollision(bounds, blocks, scale)) {
                bounds.x = oldX;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                isAttacking = true;
                stateTime = 0;
                hasDealtMeleeDamage = false;
                if (isStaffEquipped()) {
                    spawnProjectile(projectiles);
                }
            }
        }

        Animation<TextureRegion> currentAttackAnim = isStaffEquipped() ? staffAttackAnim : attackAnim;
        if (isAttacking && currentAttackAnim.isAnimationFinished(stateTime)) {
            isAttacking = false;
            hasDealtMeleeDamage = false;
        }
    }

    private void spawnProjectile(Array<Projectile> projectiles) {
        float px = facingRight ? bounds.x + 50 : bounds.x - 20;
        float py = bounds.y + 30;

        float staffDamage = 2.0f * bonusDamageMultiplier * getAttackDamageMultiplier();
        projectiles.add(new Projectile(px, py, facingRight, 2, staffDamage));
    }

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = idleFrame;

        if (isDead) {
            currentFrame = deathAnim.getKeyFrame(stateTime, false);
        } else if (isHealing) {
            currentFrame = healAnim.getKeyFrame(healStateTime, false);
        } else if (isAttacking) {
            currentFrame = (isStaffEquipped() ? staffAttackAnim : attackAnim).getKeyFrame(stateTime, false);
        } else if (velocityY != 0 && !inWater) {
            currentFrame = (isStaffEquipped() ? staffWalkAnim : walkAnim).getKeyFrame(0);
        } else if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            currentFrame = (isStaffEquipped() ? staffWalkAnim : walkAnim).getKeyFrame(stateTime, true);
        } else {
            currentFrame = isStaffEquipped() ? staffWalkAnim.getKeyFrame(0) : idleAnim.getKeyFrame(stateTime, true);
        }

        // Submerge logic: slice the bottom 40% of the sprite
        float fullDrawHeight = 100f;
        float drawHeight = inWater ? 60f : fullDrawHeight;

        TextureRegion renderFrame = currentFrame;
        if (inWater) {
            // Create a temporary region capturing only the top 60% of the texture
            renderFrame = new TextureRegion(currentFrame, 0, 0,
                currentFrame.getRegionWidth(),
                (int)(currentFrame.getRegionHeight() * 0.6f));
        }

        float aspect = (float) currentFrame.getRegionWidth() / currentFrame.getRegionHeight();
        float drawWidth = fullDrawHeight * aspect;
        float drawX = bounds.x + (bounds.width / 2f) - (drawWidth / 2f);

        // Shift the Y rendering position up by 40 units in water, or down by 8 units to compensate for sprite bottom padding
        float drawY = inWater ? bounds.y + 40f : bounds.y - 8f;

        drawFlipped(batch, renderFrame, drawX, drawY, drawWidth, drawHeight, facingRight);
    }

    @Override
    public void dispose() {
        // Textures are managed by AssetLoader
    }
}
