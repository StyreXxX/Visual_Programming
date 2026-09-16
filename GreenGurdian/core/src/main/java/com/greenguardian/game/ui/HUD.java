package com.greenguardian.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.greenguardian.game.assets.AssetLoader;
import com.greenguardian.game.entities.Boss;
import com.greenguardian.game.entities.Player;

public class HUD {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private AssetLoader assets;

    public HUD(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font, AssetLoader assets) {
        this.batch = batch;
        this.shapeRenderer = shapeRenderer;
        this.font = font;
        this.assets = assets;
    }

    public void drawHealthBars(Player player, Boss boss) {
        float pX = 20f, pY = 665f, pW = 250f, pH = 24f;

        shapeRenderer.setColor(0.08f, 0.08f, 0.1f, 0.9f);
        shapeRenderer.rect(pX - 4, pY - 4, pW + 8, pH + 8);

        shapeRenderer.setColor(0.85f, 0.7f, 0.2f, 1f);
        shapeRenderer.rectLine(pX - 4, pY - 4, pX + pW + 4, pY - 4, 2);
        shapeRenderer.rectLine(pX - 4, pY + pH + 4, pX + pW + 4, pY + pH + 4, 2);
        shapeRenderer.rectLine(pX - 4, pY - 4, pX - 4, pY + pH + 4, 2);
        shapeRenderer.rectLine(pX + pW + 4, pY - 4, pX + pW + 4, pY + pH + 4, 2);

        shapeRenderer.setColor(0.3f, 0.05f, 0.05f, 1f);
        shapeRenderer.rect(pX, pY, pW, pH);

        float pRatio = (float) player.getHealth() / player.getMaxHealth();
        shapeRenderer.setColor(0.1f, 0.85f, 0.25f, 1f);
        shapeRenderer.rect(pX, pY, Math.max(0, pW * pRatio), pH);

        shapeRenderer.setColor(1f, 1f, 1f, 0.2f);
        shapeRenderer.rect(pX, pY + pH - 4, Math.max(0, pW * pRatio), 4);

        if (!boss.isDead() && boss.isAwake()) {
            float bW = 500f, bH = 22f;
            float bX = 640f - (bW / 2f);
            float bY = 665f;

            shapeRenderer.setColor(0.08f, 0.08f, 0.1f, 0.95f);
            shapeRenderer.rect(bX - 5, bY - 5, bW + 10, bH + 10);

            shapeRenderer.setColor(0.75f, 0.55f, 0.15f, 1f);
            shapeRenderer.rectLine(bX - 5, bY - 5, bX + bW + 5, bY - 5, 2);
            shapeRenderer.rectLine(bX - 5, bY + bH + 5, bX + bW + 5, bY + bH + 5, 2);
            shapeRenderer.rectLine(bX - 5, bY - 5, bX - 5, bY + bH + 5, 2);
            shapeRenderer.rectLine(bX + bW + 5, bY - 5, bX + bW + 5, bY + bH + 5, 2);

            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            shapeRenderer.rect(bX, bY, bW, bH);

            float bRatio = (float) boss.getHealth() / boss.getMaxHealth();
            shapeRenderer.setColor(0.95f, 0.4f, 0.05f, 1f);
            shapeRenderer.rect(bX, bY, Math.max(0, bW * bRatio), bH);

            shapeRenderer.setColor(1f, 1f, 1f, 0.25f);
            shapeRenderer.rect(bX, bY + bH - 4, Math.max(0, bW * bRatio), 4);
        }
    }

    public void drawWeaponHotbarSlots(Player player, boolean isGameOver, boolean isShopOpen, Boss boss) {
        if (isGameOver || isShopOpen || boss.isDead()) return;

        float slotSize = 60f;
        float gap = 8f;
        float totalW = slotSize * 2 + gap;
        float startX = (1280f - totalW) / 2f;
        float startY = 18f;

        int equipped = player.getEquippedWeapon();

        for (int i = 0; i < 2; i++) {
            float sx = startX + i * (slotSize + gap);
            float sy = startY;
            boolean isSelected = (equipped == (i + 1));

            // Outer drop shadow / dark outline
            shapeRenderer.setColor(0.06f, 0.06f, 0.08f, 0.95f);
            shapeRenderer.rect(sx - 4, sy - 4, slotSize + 8, slotSize + 8);

            // Beveled metal frame
            shapeRenderer.setColor(0.40f, 0.40f, 0.44f, 1f);
            shapeRenderer.rect(sx - 2, sy - 2, slotSize + 4, slotSize + 4);

            // Top & Left highlight edge
            shapeRenderer.setColor(0.70f, 0.70f, 0.74f, 1f);
            shapeRenderer.rectLine(sx - 2, sy + slotSize + 1, sx + slotSize + 2, sy + slotSize + 1, 2);
            shapeRenderer.rectLine(sx - 2, sy - 2, sx - 2, sy + slotSize + 1, 2);

            // Bottom & Right shadow edge
            shapeRenderer.setColor(0.20f, 0.20f, 0.24f, 1f);
            shapeRenderer.rectLine(sx - 2, sy - 2, sx + slotSize + 2, sy - 2, 2);
            shapeRenderer.rectLine(sx + slotSize + 1, sy - 2, sx + slotSize + 1, sy + slotSize + 1, 2);

            // Inset dark slot interior
            shapeRenderer.setColor(0.12f, 0.13f, 0.10f, 0.95f);
            shapeRenderer.rect(sx, sy, slotSize, slotSize);

            // Active weapon selection highlight border
            if (isSelected) {
                shapeRenderer.setColor(1f, 0.95f, 0.55f, 1f);
                shapeRenderer.rectLine(sx - 3, sy - 3, sx + slotSize + 3, sy - 3, 3);
                shapeRenderer.rectLine(sx - 3, sy + slotSize + 3, sx + slotSize + 3, sy + slotSize + 3, 3);
                shapeRenderer.rectLine(sx - 3, sy - 3, sx - 3, sy + slotSize + 3, 3);
                shapeRenderer.rectLine(sx + slotSize + 3, sy - 3, sx + slotSize + 3, sy + slotSize + 3, 3);

                // Subtle inner glow
                shapeRenderer.setColor(1f, 1f, 1f, 0.12f);
                shapeRenderer.rect(sx, sy, slotSize, slotSize);
            }
        }
    }

    public void drawOverlays(boolean isGameOver, boolean isShopOpen, Boss boss) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (isGameOver) {
            shapeRenderer.setColor(0f, 0f, 0f, 0.65f);
            shapeRenderer.rect(0, 0, 1280, 720);

            shapeRenderer.setColor(0.1f, 0.1f, 0.12f, 0.9f);
            shapeRenderer.rect(440, 260, 400, 200);
            shapeRenderer.setColor(0.8f, 0.1f, 0.1f, 1f);
            shapeRenderer.rect(440, 455, 400, 5);
        } else if (isShopOpen) {
            shapeRenderer.setColor(0f, 0f, 0f, 0.75f);
            shapeRenderer.rect(0, 0, 1280, 720);

            shapeRenderer.setColor(0.12f, 0.12f, 0.16f, 0.95f);
            shapeRenderer.rect(340, 160, 600, 400);

            shapeRenderer.setColor(0.85f, 0.7f, 0.2f, 1f);
            shapeRenderer.rectLine(340, 160, 940, 160, 3);
            shapeRenderer.rectLine(340, 560, 940, 560, 3);
            shapeRenderer.rectLine(340, 160, 340, 560, 3);
            shapeRenderer.rectLine(940, 160, 940, 560, 3);
        } else if (boss.isDead()) {
            shapeRenderer.setColor(0f, 0f, 0f, 0.5f);
            shapeRenderer.rect(0, 0, 1280, 720);

            shapeRenderer.setColor(0f, 0f, 0f, 0.5f);
            shapeRenderer.rect(488, 293, 304, 54);

            shapeRenderer.setColor(0.18f, 0.18f, 0.22f, 0.95f);
            shapeRenderer.rect(490, 295, 300, 50);

            shapeRenderer.setColor(0.9f, 0.75f, 0.2f, 1f);
            shapeRenderer.rectLine(490, 295, 790, 295, 2);
            shapeRenderer.rectLine(490, 345, 790, 345, 2);
            shapeRenderer.rectLine(490, 295, 490, 345, 2);
            shapeRenderer.rectLine(790, 295, 790, 345, 2);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void drawTextAndIcons(boolean isGameOver, boolean isShopOpen, Boss boss, int playerSouls, int playerKeys, int staffCost, String shopMessage, int levelIndex, Player player) {
        batch.begin();

        if (!isGameOver && !boss.isDead() && !isShopOpen) {
            batch.draw(assets.shopTexture, 1150, 580, 100, 100);

            batch.draw(assets.soulTexture, 1120, 30, 50, 50);
            font.setColor(Color.valueOf("B47EE5"));
            font.draw(batch, "x " + playerSouls, 1180, 68);

            if (levelIndex == 1) {
                batch.draw(assets.keyTexture, 1105, 75, 90, 90);
                font.setColor(Color.LIGHT_GRAY);
                font.draw(batch, "x " + playerKeys + "/3", 1180, 130);
            }

            font.setColor(Color.WHITE);
            font.draw(batch, "PLAYER HP", 25, 712);

            if (boss.isAwake()) {
                font.setColor(Color.GOLD);
                if (levelIndex == 2) {
                    font.draw(batch, "TREE KNIGHT", 565, 712);
                } else {
                    font.draw(batch, "THE GUARDIAN", 555, 712);
                }
            }

            // Weapon Hotbar icons and numbers
            float slotSize = 60f;
            float gap = 8f;
            float totalW = slotSize * 2 + gap;
            float startX = (1280f - totalW) / 2f;
            float startY = 18f;

            // Slot 1: Sword
            batch.draw(assets.swordIconTexture, startX + 6, startY + 6, 48, 48);
            font.setColor(player.getEquippedWeapon() == 1 ? Color.GOLD : Color.LIGHT_GRAY);
            font.draw(batch, "1", startX + 8, startY + 54);

            // Slot 2: Staff
            float slot2X = startX + slotSize + gap;
            if (player.hasUnlockedStaff()) {
                batch.draw(assets.staffSlotTexture, slot2X + 6, startY + 6, 48, 48);
                font.setColor(player.getEquippedWeapon() == 2 ? Color.GOLD : Color.LIGHT_GRAY);
                font.draw(batch, "2", slot2X + 8, startY + 54);
            } else {
                font.setColor(0.4f, 0.4f, 0.45f, 0.5f);
                font.draw(batch, "2", slot2X + 8, startY + 54);
            }
        }

        if (isShopOpen) {
            font.setColor(Color.GOLD);
            font.draw(batch, "MYSTIC SHOP (Press B to Close)", 420, 530);

            batch.draw(assets.staffDisplayTexture, 380, 340, 100, 100);
            font.setColor(Color.WHITE);
            font.draw(batch, "Magic Staff", 500, 420);
            font.setColor(Color.GREEN);
            font.draw(batch, "Damage: 2 | Fast Speed", 500, 380);

            font.setColor(Color.valueOf("B47EE5"));
            font.draw(batch, "Cost: " + staffCost + " Souls", 500, 340);

            font.setColor(Color.GOLD);
            font.draw(batch, "[Press ENTER to Buy]", 500, 280);

            if (!shopMessage.isEmpty()) {
                if (shopMessage.contains("SUCCESSFUL")) font.setColor(Color.GREEN);
                else font.setColor(Color.RED);
                font.draw(batch, shopMessage, 500, 230);
            }
        } else if (isGameOver) {
            font.setColor(Color.RED);
            font.draw(batch, "GAME OVER", 550, 410);
            font.setColor(Color.WHITE);
            font.draw(batch, "Press ENTER to Restart", 485, 340);
        } else if (boss.isDead()) {
            if (levelIndex == 1) {
                font.setColor(Color.GOLD);
                if (playerKeys >= 3) {
                    font.draw(batch, "ALL KEYS FOUND!", 520, 430);
                } else {
                    font.draw(batch, "LEVEL 1 CLEARED!", 520, 430);
                }
                font.setColor(Color.WHITE);
                font.draw(batch, "Enter Level 2", 555, 328);
            } else {
                font.setColor(Color.GOLD);
                font.draw(batch, "VICTORY ACHIEVED!", 500, 430);
                font.setColor(Color.WHITE);
                font.draw(batch, "Main Menu", 585, 328);
            }
        }
        batch.end();
    }
}
