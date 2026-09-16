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
        drawOverlays(isGameOver, isShopOpen, boss, 0);
    }

    public void drawOverlays(boolean isGameOver, boolean isShopOpen, Boss boss, int shopSelectedIndex) {
        if (!isGameOver && !isShopOpen && !boss.isDead()) return;

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
            // Full background darken
            shapeRenderer.setColor(0f, 0f, 0f, 0.80f);
            shapeRenderer.rect(0, 0, 1280, 720);

            // Main Shop Modal Box (820 x 530)
            float modalX = 230f;
            float modalY = 95f;
            float modalW = 820f;
            float modalH = 530f;

            shapeRenderer.setColor(0.10f, 0.10f, 0.14f, 0.96f);
            shapeRenderer.rect(modalX, modalY, modalW, modalH);

            // Outer Gold Frame
            shapeRenderer.setColor(0.85f, 0.70f, 0.20f, 1f);
            shapeRenderer.rectLine(modalX, modalY, modalX + modalW, modalY, 3);
            shapeRenderer.rectLine(modalX, modalY + modalH, modalX + modalW, modalY + modalH, 3);
            shapeRenderer.rectLine(modalX, modalY, modalX, modalY + modalH, 3);
            shapeRenderer.rectLine(modalX + modalW, modalY, modalX + modalW, modalY + modalH, 3);

            // Top Header Line
            shapeRenderer.setColor(0.85f, 0.70f, 0.20f, 0.5f);
            shapeRenderer.rectLine(modalX + 15, modalY + modalH - 50, modalX + modalW - 15, modalY + modalH - 50, 2);

            // 4 Upgrade Item Cards
            float cardX = modalX + 30f;
            float cardW = modalW - 60f;
            float cardH = 80f;
            float startY = modalY + modalH - 145f;
            float cardGap = 14f;

            for (int i = 0; i < 4; i++) {
                float cy = startY - i * (cardH + cardGap);
                boolean isSelected = (i == shopSelectedIndex);

                if (isSelected) {
                    // Highlighted card background
                    shapeRenderer.setColor(0.18f, 0.22f, 0.32f, 0.98f);
                    shapeRenderer.rect(cardX, cy, cardW, cardH);

                    // Glowing Gold Border
                    shapeRenderer.setColor(1.0f, 0.85f, 0.30f, 1f);
                    shapeRenderer.rectLine(cardX, cy, cardX + cardW, cy, 3);
                    shapeRenderer.rectLine(cardX, cy + cardH, cardX + cardW, cy + cardH, 3);
                    shapeRenderer.rectLine(cardX, cy, cardX, cy + cardH, 3);
                    shapeRenderer.rectLine(cardX + cardW, cy, cardX + cardW, cy + cardH, 3);
                } else {
                    // Regular card background
                    shapeRenderer.setColor(0.14f, 0.14f, 0.18f, 0.85f);
                    shapeRenderer.rect(cardX, cy, cardW, cardH);

                    // Muted Border
                    shapeRenderer.setColor(0.32f, 0.32f, 0.38f, 0.8f);
                    shapeRenderer.rectLine(cardX, cy, cardX + cardW, cy, 1.5f);
                    shapeRenderer.rectLine(cardX, cy + cardH, cardX + cardW, cy + cardH, 1.5f);
                    shapeRenderer.rectLine(cardX, cy, cardX, cy + cardH, 1.5f);
                    shapeRenderer.rectLine(cardX + cardW, cy, cardX + cardW, cy + cardH, 1.5f);
                }

                // Icon Box
                float iconBoxX = cardX + 12f;
                float iconBoxY = cy + 10f;
                float iconBoxSize = 60f;
                shapeRenderer.setColor(0.08f, 0.08f, 0.10f, 0.9f);
                shapeRenderer.rect(iconBoxX, iconBoxY, iconBoxSize, iconBoxSize);

                // Health Elixir Cross Icon
                if (i == 1) {
                    shapeRenderer.setColor(0.15f, 0.85f, 0.30f, 1f);
                    // Vertical bar
                    shapeRenderer.rect(iconBoxX + 24f, iconBoxY + 12f, 12f, 36f);
                    // Horizontal bar
                    shapeRenderer.rect(iconBoxX + 12f, iconBoxY + 24f, 36f, 12f);
                }
            }
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
        drawTextAndIcons(isGameOver, isShopOpen, boss, playerSouls, playerKeys, staffCost, shopMessage, levelIndex, player, 0);
    }

    public void drawTextAndIcons(boolean isGameOver, boolean isShopOpen, Boss boss, int playerSouls, int playerKeys, int staffCost, String shopMessage, int levelIndex, Player player, int shopSelectedIndex) {
        batch.begin();

        if (!isGameOver && !boss.isDead() && !isShopOpen) {
            batch.draw(assets.shopTexture, 1150, 580, 100, 100);

            batch.draw(assets.soulTexture, 1120, 30, 50, 50);
            font.setColor(Color.valueOf("B47EE5"));
            font.draw(batch, "x " + playerSouls, 1180, 68);
            font.setColor(Color.valueOf("80E892"));
            font.draw(batch, "[H] Heal (50)", 1115, 20);

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
            float modalX = 230f;
            float modalY = 95f;
            float modalW = 820f;
            float modalH = 530f;

            // Header Title
            font.setColor(Color.GOLD);
            font.draw(batch, "MYSTIC SHRINE & SHOP", modalX + 250f, modalY + modalH - 18f);

            // Subtitle
            font.setColor(Color.LIGHT_GRAY);
            font.draw(batch, "[Press B to Close]", modalX + modalW - 200f, modalY + modalH - 18f);

            float cardX = modalX + 30f;
            float cardW = modalW - 60f;
            float cardH = 80f;
            float startY = modalY + modalH - 145f;
            float cardGap = 14f;

            for (int i = 0; i < 4; i++) {
                float cy = startY - i * (cardH + cardGap);
                float iconBoxX = cardX + 12f;
                float iconBoxY = cy + 10f;
                boolean isSelected = (i == shopSelectedIndex);

                // Draw Item Icon
                if (i == 0) {
                    batch.draw(assets.staffDisplayTexture, iconBoxX + 6f, iconBoxY + 6f, 48f, 48f);
                } else if (i == 2) {
                    batch.draw(assets.swordIconTexture, iconBoxX + 8f, iconBoxY + 8f, 44f, 44f);
                } else if (i == 3) {
                    batch.draw(assets.soulTexture, iconBoxX + 8f, iconBoxY + 8f, 44f, 44f);
                }

                // Item Details
                float textX = cardX + 86f;
                if (i == 0) {
                    font.setColor(isSelected ? Color.GOLD : Color.WHITE);
                    font.draw(batch, "[1] Magic Staff", textX, cy + 62f);
                    font.setColor(Color.LIGHT_GRAY);
                    font.draw(batch, "Unlocks ranged magic attack projectile. Press [2] to equip.", textX, cy + 34f);

                    if (player.hasUnlockedStaff()) {
                        font.setColor(Color.GREEN);
                        font.draw(batch, "[OWNED]", cardX + cardW - 140f, cy + 48f);
                    } else {
                        font.setColor(Color.valueOf("B47EE5"));
                        font.draw(batch, "Cost: 100 Souls", cardX + cardW - 170f, cy + 48f);
                    }
                } else if (i == 1) {
                    font.setColor(isSelected ? Color.GOLD : Color.WHITE);
                    font.draw(batch, "[2] Max Health Elixir", textX, cy + 62f);
                    font.setColor(Color.LIGHT_GRAY);
                    font.draw(batch, "Permanently increases Max HP by +200 and restores +200 health.", textX, cy + 34f);

                    font.setColor(Color.valueOf("B47EE5"));
                    font.draw(batch, "Cost: 75 Souls", cardX + cardW - 170f, cy + 56f);
                    font.setColor(Color.valueOf("80E892"));
                    font.draw(batch, "Max: " + (int) player.getMaxHealth() + " HP", cardX + cardW - 170f, cy + 30f);
                } else if (i == 2) {
                    font.setColor(isSelected ? Color.GOLD : Color.WHITE);
                    font.draw(batch, "[3] Damage Stone", textX, cy + 62f);
                    font.setColor(Color.LIGHT_GRAY);
                    font.draw(batch, "Permanently increases Sword & Staff damage by +20%.", textX, cy + 34f);

                    font.setColor(Color.valueOf("B47EE5"));
                    font.draw(batch, "Cost: 60 Souls", cardX + cardW - 170f, cy + 56f);
                    int bonusPercent = (int) ((player.getBonusDamageMultiplier() - 1.0f) * 100f + 0.5f);
                    font.setColor(Color.GOLD);
                    font.draw(batch, "Bonus: +" + bonusPercent + "%", cardX + cardW - 170f, cy + 30f);
                } else if (i == 3) {
                    font.setColor(isSelected ? Color.GOLD : Color.WHITE);
                    font.draw(batch, "[4] Soul Magnet Relic", textX, cy + 62f);
                    font.setColor(Color.LIGHT_GRAY);
                    font.draw(batch, "Passively pulls all nearby souls toward the player from afar.", textX, cy + 34f);

                    if (player.hasSoulMagnet()) {
                        font.setColor(Color.GREEN);
                        font.draw(batch, "[ACTIVE]", cardX + cardW - 140f, cy + 48f);
                    } else {
                        font.setColor(Color.valueOf("B47EE5"));
                        font.draw(batch, "Cost: 80 Souls", cardX + cardW - 170f, cy + 48f);
                    }
                }
            }

            // Message & Footer navigation
            if (!shopMessage.isEmpty()) {
                if (shopMessage.contains("SUCCESSFUL")) font.setColor(Color.GREEN);
                else font.setColor(Color.RED);
                font.draw(batch, shopMessage, modalX + 40f, modalY + 68f);
            }

            font.setColor(Color.LIGHT_GRAY);
            font.draw(batch, "Use [W/S] or [UP/DOWN] to select | Press [ENTER] or [1-4] to buy", modalX + 110f, modalY + 34f);
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
