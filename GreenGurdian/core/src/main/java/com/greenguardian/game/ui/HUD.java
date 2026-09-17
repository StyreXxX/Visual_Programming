package com.greenguardian.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.greenguardian.game.assets.AssetLoader;
import com.greenguardian.game.entities.Boss;
import com.greenguardian.game.entities.Enemy;
import com.greenguardian.game.entities.Player;

public class HUD {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private AssetLoader assets;
    private float playerGhostHealth = 1000f;
    private TextureRegion playerPortraitRegion;
    private com.badlogic.gdx.graphics.g2d.GlyphLayout glyphLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();

    public HUD(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font, AssetLoader assets) {
        this.batch = batch;
        this.shapeRenderer = shapeRenderer;
        this.font = font;
        this.assets = assets;

        if (font != null && font.getRegion() != null && font.getRegion().getTexture() != null) {
            font.getRegion().getTexture().setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.Linear, com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
        }

        if (assets.playerStandSheet != null) {
            // Perfectly centered high-res bust of hero (hood, face, chest, shoulders centered)
            this.playerPortraitRegion = new TextureRegion(assets.playerStandSheet, 414, 4, 150, 150);
        }
    }

    public void drawHealthBars(Player player, Boss boss) {
        drawHealthBars(player, boss, 1);
    }

    public void drawHealthBars(Player player, Boss boss, int levelIndex) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        float currentHP = player.getHealth();
        float maxHP = player.getMaxHealth();

        // Ghost health bar catch-up interpolation
        if (currentHP < playerGhostHealth) {
            playerGhostHealth = Math.max(currentHP, playerGhostHealth - 220f * Gdx.graphics.getDeltaTime());
        } else {
            playerGhostHealth = currentHP;
        }

        // ================= UNIFIED RPG STATUS FRAME (Master Chassis) =================
        float frameY = 630f;
        float frameX = 18f, frameW = 330f, frameH = 70f;

        // Soft Frame Outer Drop Shadow
        shapeRenderer.setColor(0f, 0f, 0f, 0.55f);
        shapeRenderer.rect(frameX - 3, frameY - 3, frameW + 6, frameH + 6);

        // Deep Gunmetal/Obsidian Plate Base
        shapeRenderer.setColor(0.08f, 0.10f, 0.13f, 0.94f);
        shapeRenderer.rect(frameX, frameY, frameW, frameH);

        // Antique Gold Frame Border
        shapeRenderer.setColor(0.72f, 0.58f, 0.25f, 1f);
        shapeRenderer.rectLine(frameX, frameY, frameX + frameW, frameY, 1.8f);
        shapeRenderer.rectLine(frameX, frameY + frameH, frameX + frameW, frameY + frameH, 1.8f);
        shapeRenderer.rectLine(frameX, frameY, frameX, frameY + frameH, 1.8f);
        shapeRenderer.rectLine(frameX + frameW, frameY, frameX + frameW, frameY + frameH, 1.8f);

        // Subtle Inner Chamfer Accent Line
        shapeRenderer.setColor(0.25f, 0.30f, 0.38f, 0.45f);
        shapeRenderer.rectLine(frameX + 2, frameY + 2, frameX + frameW - 2, frameY + 2, 1f);
        shapeRenderer.rectLine(frameX + 2, frameY + frameH - 2, frameX + frameW - 2, frameY + frameH - 2, 1f);
        shapeRenderer.rectLine(frameX + 2, frameY + 2, frameX + 2, frameY + frameH - 2, 1f);
        shapeRenderer.rectLine(frameX + frameW - 2, frameY + 2, frameX + frameW - 2, frameY + frameH - 2, 1f);

        // ================= INTEGRATED PORTRAIT SOCKET =================
        float portX = 24f, portY = frameY + 5f, portW = 60f, portH = 60f;

        // Recessed Dark Well Interior
        shapeRenderer.setColor(0.04f, 0.05f, 0.07f, 1f);
        shapeRenderer.rect(portX, portY, portW, portH);

        // Polished Gold Inner Rim
        shapeRenderer.setColor(0.85f, 0.72f, 0.30f, 1f);
        shapeRenderer.rectLine(portX, portY, portX + portW, portY, 1.5f);
        shapeRenderer.rectLine(portX, portY + portH, portX + portW, portY + portH, 1.5f);
        shapeRenderer.rectLine(portX, portY, portX, portY + portH, 1.5f);
        shapeRenderer.rectLine(portX + portW, portY, portX + portW, portY + portH, 1.5f);

        // ================= PLAYER HEALTH BAR =================
        float pX = 94f, pY = frameY + 24f, pW = 244f, pH = 14f;

        // Health Bar Recessed Shadow
        shapeRenderer.setColor(0f, 0f, 0f, 0.6f);
        shapeRenderer.rect(pX - 2, pY - 2, pW + 4, pH + 4);

        // Dark Background (Empty Health Trough)
        shapeRenderer.setColor(0.18f, 0.05f, 0.05f, 0.95f);
        shapeRenderer.rect(pX, pY, pW, pH);

        // Catch-up Ghost Health Bar (Warm Amber linger bar)
        if (playerGhostHealth > currentHP) {
            float ghostRatio = Math.min(1.0f, Math.max(0f, playerGhostHealth / maxHP));
            shapeRenderer.setColor(0.95f, 0.62f, 0.16f, 0.90f);
            shapeRenderer.rect(pX, pY, pW * ghostRatio, pH);
        }

        // Active Health Bar (Rich Emerald Green)
        float pRatio = Math.min(1.0f, Math.max(0f, currentHP / maxHP));
        shapeRenderer.setColor(0.12f, 0.78f, 0.34f, 1f);
        shapeRenderer.rect(pX, pY, pW * pRatio, pH);

        // Soft Translucent Gloss Highlight (Top 45%)
        shapeRenderer.setColor(1f, 1f, 1f, 0.18f);
        shapeRenderer.rect(pX, pY + pH * 0.55f, pW * pRatio, pH * 0.45f);

        // Clean Golden Trim Frame
        shapeRenderer.setColor(0.65f, 0.52f, 0.20f, 1f);
        shapeRenderer.rectLine(pX, pY, pX + pW, pY, 1.5f);
        shapeRenderer.rectLine(pX, pY + pH, pX + pW, pY + pH, 1.5f);
        shapeRenderer.rectLine(pX, pY, pX, pY + pH, 1.5f);
        shapeRenderer.rectLine(pX + pW, pY, pX + pW, pY + pH, 1.5f);

        // ================= STATUS EFFECT BADGES (Row 3, Underneath HP) =================
        float badgeX = 94f;
        float badgeY = frameY + 5f;
        float badgeH = 14f;

        if (player.isWaterDebuffed()) {
            float bWidth = 90f;
            shapeRenderer.setColor(0.08f, 0.18f, 0.32f, 0.90f);
            shapeRenderer.rect(badgeX, badgeY, bWidth, badgeH);
            shapeRenderer.setColor(0.25f, 0.65f, 0.95f, 0.90f);
            shapeRenderer.rectLine(badgeX, badgeY, badgeX + bWidth, badgeY, 1.2f);
            shapeRenderer.rectLine(badgeX, badgeY + badgeH, badgeX + bWidth, badgeY + badgeH, 1.2f);
            shapeRenderer.rectLine(badgeX, badgeY, badgeX, badgeY + badgeH, 1.2f);
            shapeRenderer.rectLine(badgeX + bWidth, badgeY, badgeX + bWidth, badgeY + badgeH, 1.2f);
            badgeX += bWidth + 6f;
        }

        if (player.getDamageStoneCount() > 0) {
            float bWidth = 80f;
            shapeRenderer.setColor(0.28f, 0.20f, 0.08f, 0.90f);
            shapeRenderer.rect(badgeX, badgeY, bWidth, badgeH);
            shapeRenderer.setColor(0.95f, 0.75f, 0.20f, 0.90f);
            shapeRenderer.rectLine(badgeX, badgeY, badgeX + bWidth, badgeY, 1.2f);
            shapeRenderer.rectLine(badgeX, badgeY + badgeH, badgeX + bWidth, badgeY + badgeH, 1.2f);
            shapeRenderer.rectLine(badgeX, badgeY, badgeX, badgeY + badgeH, 1.2f);
            shapeRenderer.rectLine(badgeX + bWidth, badgeY, badgeX + bWidth, badgeY + badgeH, 1.2f);
            badgeX += bWidth + 6f;
        }

        if (player.hasSoulMagnet()) {
            float bWidth = 68f;
            shapeRenderer.setColor(0.22f, 0.10f, 0.30f, 0.90f);
            shapeRenderer.rect(badgeX, badgeY, bWidth, badgeH);
            shapeRenderer.setColor(0.75f, 0.45f, 0.95f, 0.90f);
            shapeRenderer.rectLine(badgeX, badgeY, badgeX + bWidth, badgeY, 1.2f);
            shapeRenderer.rectLine(badgeX, badgeY + badgeH, badgeX + bWidth, badgeY + badgeH, 1.2f);
            shapeRenderer.rectLine(badgeX, badgeY, badgeX, badgeY + badgeH, 1.2f);
            shapeRenderer.rectLine(badgeX + bWidth, badgeY, badgeX + bWidth, badgeY + badgeH, 1.2f);
        }

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

    public void drawMinimap(ShapeRenderer shapeRenderer, float mapW, float mapH, Player player, Array<Enemy> enemies, Boss boss, Array<Rectangle> souls, Array<Rectangle> keys, OrthographicCamera camera, MapObjects mapBlocks, float scaleFactor, boolean isHidden) {
        if (isHidden) return;
        if (mapW <= 0 || mapH <= 0 || player == null) return;

        // Minimap Top-Right Position (200w x 92h - Landscape 1:1 Aspect Ratio)
        float mx = 1060f;
        float my = 608f;
        float mw = 200f;
        float mh = 92f;

        // Soft Outer Drop Shadow
        shapeRenderer.setColor(0f, 0f, 0f, 0.55f);
        shapeRenderer.rect(mx - 3, my - 3, mw + 6, mh + 6);

        // Dark Gunmetal / Obsidian Radar Plate Base
        shapeRenderer.setColor(0.06f, 0.08f, 0.11f, 0.94f);
        shapeRenderer.rect(mx, my, mw, mh);

        // Radar Active Viewport Area (Within inner margins)
        float rx = mx + 4f;
        float ry = my + 14f; // 12px at bottom reserved for Level Progression Track
        float rw = mw - 8f;  // 192f
        float rh = mh - 26f; // 66f

        // Recessed Dark Radar Well
        shapeRenderer.setColor(0.04f, 0.05f, 0.07f, 1f);
        shapeRenderer.rect(rx, ry, rw, rh);

        // Subtle Grid Lines
        shapeRenderer.setColor(0.18f, 0.22f, 0.28f, 0.40f);
        shapeRenderer.rectLine(rx + rw / 2f, ry, rx + rw / 2f, ry + rh, 1f);
        shapeRenderer.rectLine(rx, ry + rh / 2f, rx + rw, ry + rh / 2f, 1f);

        // World view range: 1700 horizontal world pixels centered around player/camera
        float camX = (camera != null) ? camera.position.x : player.getBounds().x;
        float camY = (camera != null) ? camera.position.y : player.getBounds().y;
        float viewRangeX = 1700f;
        float viewRangeY = (viewRangeX / rw) * rh; // ~584 world pixels vertically

        float minWorldX = camX - (viewRangeX / 2f);
        float maxWorldX = camX + (viewRangeX / 2f);
        float minWorldY = camY - (viewRangeY / 2f);
        float maxWorldY = camY + (viewRangeY / 2f);

        // ================= DRAW TERRAIN PLATFORMS =================
        if (mapBlocks != null) {
            for (MapObject object : mapBlocks) {
                Rectangle bRect = null;
                if (object instanceof RectangleMapObject) {
                    bRect = ((RectangleMapObject) object).getRectangle();
                } else if (object instanceof PolygonMapObject) {
                    bRect = ((PolygonMapObject) object).getPolygon().getBoundingRectangle();
                }
                if (bRect == null) continue;

                float bx = bRect.x * scaleFactor;
                float by = bRect.y * scaleFactor;
                float bw = bRect.width * scaleFactor;
                float bh = bRect.height * scaleFactor;

                // Quick horizontal and vertical cull
                if (bx + bw < minWorldX || bx > maxWorldX) continue;
                if (by + bh < minWorldY || by > maxWorldY) continue;

                // Clip to visible window
                float clipLeft = Math.max(minWorldX, bx);
                float clipRight = Math.min(maxWorldX, bx + bw);
                float clipBottom = Math.max(minWorldY, by);
                float clipTop = Math.min(maxWorldY, by + bh);

                if (clipRight > clipLeft && clipTop > clipBottom) {
                    float normX1 = (clipLeft - minWorldX) / viewRangeX;
                    float normX2 = (clipRight - minWorldX) / viewRangeX;
                    float normY1 = (clipBottom - minWorldY) / viewRangeY;
                    float normY2 = (clipTop - minWorldY) / viewRangeY;

                    float dx = rx + normX1 * rw;
                    float dy = ry + normY1 * rh;
                    float dw = (normX2 - normX1) * rw;
                    float dh = (normY2 - normY1) * rh;

                    // Platform Stone/Earth body
                    shapeRenderer.setColor(0.18f, 0.24f, 0.22f, 0.90f);
                    shapeRenderer.rect(dx, dy, dw, dh);

                    // Platform Top Moss / Edge Highlight Line
                    if (by + bh <= maxWorldY) {
                        shapeRenderer.setColor(0.35f, 0.65f, 0.40f, 0.95f);
                        shapeRenderer.rect(dx, dy + dh - 1.2f, dw, 1.2f);
                    }
                }
            }
        }

        // ================= SOULS PICKUPS =================
        if (souls != null) {
            shapeRenderer.setColor(0.75f, 0.40f, 0.95f, 0.90f);
            for (Rectangle s : souls) {
                if (s.x + s.width >= minWorldX && s.x <= maxWorldX && s.y + s.height >= minWorldY && s.y <= maxWorldY) {
                    float sx = rx + ((s.x - minWorldX) / viewRangeX) * rw;
                    float sy = ry + ((s.y - minWorldY) / viewRangeY) * rh;
                    shapeRenderer.rect(sx - 1.5f, sy - 1.5f, 3f, 3f);
                }
            }
        }

        // ================= KEYS PICKUPS =================
        if (keys != null) {
            shapeRenderer.setColor(1.0f, 0.85f, 0.20f, 1f);
            for (Rectangle k : keys) {
                if (k.x + k.width >= minWorldX && k.x <= maxWorldX && k.y + k.height >= minWorldY && k.y <= maxWorldY) {
                    float kx = rx + ((k.x - minWorldX) / viewRangeX) * rw;
                    float ky = ry + ((k.y - minWorldY) / viewRangeY) * rh;
                    shapeRenderer.rect(kx - 2f, ky - 2f, 4f, 4f);
                }
            }
        }

        // ================= ENEMIES =================
        if (enemies != null) {
            shapeRenderer.setColor(0.95f, 0.22f, 0.22f, 0.95f);
            for (Enemy e : enemies) {
                if (e != null && !e.isDead()) {
                    Rectangle eb = e.getBounds();
                    if (eb.x + eb.width >= minWorldX && eb.x <= maxWorldX && eb.y + eb.height >= minWorldY && eb.y <= maxWorldY) {
                        float ex = rx + ((eb.x - minWorldX) / viewRangeX) * rw;
                        float ey = ry + ((eb.y - minWorldY) / viewRangeY) * rh;
                        shapeRenderer.rect(ex - 2f, ey - 2f, 4f, 4f);
                    }
                }
            }
        }

        // ================= BOSS TRACKER =================
        if (boss != null && !boss.isDead()) {
            Rectangle bb = boss.getBounds();
            if (bb.x + bb.width >= minWorldX && bb.x <= maxWorldX && bb.y + bb.height >= minWorldY && bb.y <= maxWorldY) {
                float bx = rx + ((bb.x - minWorldX) / viewRangeX) * rw;
                float by = ry + ((bb.y - minWorldY) / viewRangeY) * rh;
                shapeRenderer.setColor(0.95f, 0.15f, 0.15f, 1f);
                shapeRenderer.rect(bx - 3.5f, by - 3.5f, 7f, 7f);
                shapeRenderer.setColor(1.0f, 0.80f, 0.10f, 1f);
                shapeRenderer.rect(bx - 2f, by - 2f, 4f, 4f);
            } else if (bb.x > maxWorldX) {
                // Boss is ahead down the level: Draw indicator arrow on right edge of radar
                float indX = rx + rw - 6f;
                float indY = ry + rh / 2f;
                shapeRenderer.setColor(1.0f, 0.70f, 0.15f, 1f);
                shapeRenderer.rect(indX, indY - 4f, 4f, 8f);
            }
        }

        // ================= PLAYER BLIP (Center Beacon) =================
        float pWorldX = player.getBounds().x + player.getBounds().width / 2f;
        float pWorldY = player.getBounds().y + player.getBounds().height / 2f;
        float px = rx + ((pWorldX - minWorldX) / viewRangeX) * rw;
        float py = ry + ((pWorldY - minWorldY) / viewRangeY) * rh;

        // Player Outer Emerald Aura
        shapeRenderer.setColor(0.10f, 0.85f, 0.35f, 1f);
        shapeRenderer.rect(px - 3.5f, py - 3.5f, 7f, 7f);
        // Player Glowing Core
        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        shapeRenderer.rect(px - 1.5f, py - 1.5f, 3f, 3f);

        // ================= LEVEL PROGRESSION STRIP (Bottom of Frame) =================
        float trackX = mx + 6f;
        float trackY = my + 4f;
        float trackW = mw - 12f;
        float trackH = 4f;

        // Track Background
        shapeRenderer.setColor(0.12f, 0.14f, 0.18f, 0.95f);
        shapeRenderer.rect(trackX, trackY, trackW, trackH);

        // Boss Target Marker at right end of track
        shapeRenderer.setColor(0.95f, 0.25f, 0.25f, 1f);
        shapeRenderer.rect(trackX + trackW - 3f, trackY - 1f, 3f, 6f);

        // Traversed Progress Fill (Emerald)
        float progressRatio = Math.min(1.0f, Math.max(0f, pWorldX / mapW));
        shapeRenderer.setColor(0.15f, 0.75f, 0.35f, 0.90f);
        shapeRenderer.rect(trackX, trackY, trackW * progressRatio, trackH);

        // Current Player Progress Pip
        float pipX = trackX + trackW * progressRatio;
        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        shapeRenderer.rect(pipX - 1.5f, trackY - 1.5f, 3f, 7f);

        // ================= FRAME BORDERS =================
        // Outer Gold Border
        shapeRenderer.setColor(0.75f, 0.60f, 0.22f, 1f);
        shapeRenderer.rectLine(mx, my, mx + mw, my, 1.8f);
        shapeRenderer.rectLine(mx, my + mh, mx + mw, my + mh, 1.8f);
        shapeRenderer.rectLine(mx, my, mx, my + mh, 1.8f);
        shapeRenderer.rectLine(mx + mw, my, mx + mw, my + mh, 1.8f);

        // Inner Chamfer Line
        shapeRenderer.setColor(0.25f, 0.30f, 0.38f, 0.45f);
        shapeRenderer.rectLine(mx + 2, my + 2, mx + mw - 2, my + 2, 1f);
        shapeRenderer.rectLine(mx + 2, my + mh - 2, mx + mw - 2, my + mh - 2, 1f);
        shapeRenderer.rectLine(mx + 2, my + 2, mx + 2, my + mh - 2, 1f);
        shapeRenderer.rectLine(mx + mw - 2, my + 2, mx + mw - 2, my + mh - 2, 1f);
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
            // Full background cinematic darken
            shapeRenderer.setColor(0f, 0f, 0f, 0.82f);
            shapeRenderer.rect(0, 0, 1280, 720);

            // Center subtle dark crimson aura
            shapeRenderer.setColor(0.35f, 0.04f, 0.06f, 0.25f);
            shapeRenderer.rect(340, 180, 600, 360);

            // Modal dimensions (Centered at x=360, y=200, 560x320)
            float mX = 360f, mY = 200f, mW = 560f, mH = 320f;

            // Modal drop shadow
            shapeRenderer.setColor(0f, 0f, 0f, 0.65f);
            shapeRenderer.rect(mX - 4, mY - 4, mW + 8, mH + 8);

            // Modal body (Dark Obsidian plate)
            shapeRenderer.setColor(0.08f, 0.08f, 0.11f, 0.97f);
            shapeRenderer.rect(mX, mY, mW, mH);

            // Header Banner (Deep Crimson plate)
            shapeRenderer.setColor(0.25f, 0.05f, 0.07f, 0.98f);
            shapeRenderer.rect(mX, mY + mH - 80f, mW, 80f);

            // Top Header Glow Stripe
            shapeRenderer.setColor(0.95f, 0.25f, 0.25f, 1f);
            shapeRenderer.rect(mX, mY + mH - 3f, mW, 3f);

            // Outer Antique Gold Frame Border
            shapeRenderer.setColor(0.82f, 0.68f, 0.25f, 1f);
            shapeRenderer.rectLine(mX, mY, mX + mW, mY, 2.2f);
            shapeRenderer.rectLine(mX, mY + mH, mX + mW, mY + mH, 2.2f);
            shapeRenderer.rectLine(mX, mY, mX, mY + mH, 2.2f);
            shapeRenderer.rectLine(mX + mW, mY, mX + mW, mY + mH, 2.2f);

            // Inner Ruby Accent Frame Line
            shapeRenderer.setColor(0.55f, 0.12f, 0.15f, 0.70f);
            shapeRenderer.rectLine(mX + 4, mY + 4, mX + mW - 4, mY + 4, 1.2f);
            shapeRenderer.rectLine(mX + 4, mY + mH - 4, mX + mW - 4, mY + mH - 4, 1.2f);
            shapeRenderer.rectLine(mX + 4, mY + 4, mX + 4, mY + mH - 4, 1.2f);
            shapeRenderer.rectLine(mX + mW - 4, mY + 4, mX + mW - 4, mY + mH - 4, 1.2f);

            // Header Separator Gold Line
            shapeRenderer.setColor(0.82f, 0.68f, 0.25f, 0.85f);
            shapeRenderer.rectLine(mX + 15, mY + mH - 80f, mX + mW - 15, mY + mH - 80f, 1.5f);

            // Interactive Button Pill Box (380x54)
            float btnW = 380f, btnH = 54f;
            float btnX = 640f - (btnW / 2f);
            float btnY = mY + 40f;

            // Button Shadow
            shapeRenderer.setColor(0f, 0f, 0f, 0.50f);
            shapeRenderer.rect(btnX - 2, btnY - 2, btnW + 4, btnH + 4);

            // Button Body
            shapeRenderer.setColor(0.15f, 0.17f, 0.23f, 0.98f);
            shapeRenderer.rect(btnX, btnY, btnW, btnH);

            // Button Gold Glow Border
            shapeRenderer.setColor(0.92f, 0.76f, 0.25f, 1f);
            shapeRenderer.rectLine(btnX, btnY, btnX + btnW, btnY, 2f);
            shapeRenderer.rectLine(btnX, btnY + btnH, btnX + btnW, btnY + btnH, 2f);
            shapeRenderer.rectLine(btnX, btnY, btnX, btnY + btnH, 2f);
            shapeRenderer.rectLine(btnX + btnW, btnY, btnX + btnW, btnY + btnH, 2f);
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
            // Minimap Header Label & Boss Distance
            drawShadowedText("RADAR", 1066f, 694f, Color.valueOf("FCD34D"), 0.50f);
            if (boss != null && !boss.isDead()) {
                int dist = (int) Math.max(0, (boss.getBounds().x - player.getBounds().x) / 10f);
                if (dist > 0) {
                    drawShadowedText("BOSS: " + dist + "m", 1192f, 694f, Color.valueOf("F87171"), 0.48f);
                }
            }

            // Mystic Shop Logo (Positioned directly below the minimap, horizontally centered)
            float shopX = 1118f;
            float shopY = 502f;
            float shopW = 84f;
            float shopH = 84f;
            batch.draw(assets.shopTexture, shopX, shopY, shopW, shopH);
            drawShadowedText("[B] Shop", shopX + 11f, shopY - 4f, Color.valueOf("FCD34D"), 0.58f);

            batch.draw(assets.soulTexture, 1120, 30, 50, 50);
            font.setColor(Color.valueOf("B47EE5"));
            font.draw(batch, "x " + playerSouls, 1180, 68);
            font.setColor(Color.valueOf("80E892"));
            font.draw(batch, "[H] Heal (50)", 1120, 20);

            if (levelIndex == 1) {
                batch.draw(assets.keyTexture, 1105, 75, 90, 90);
                font.setColor(Color.LIGHT_GRAY);
                font.draw(batch, "x " + playerKeys + "/3", 1180, 130);
            }

            // ================= PLAYER PORTRAIT & TEXT (Top-Left) =================
            float frameY = 630f;
            if (playerPortraitRegion != null) {
                batch.draw(playerPortraitRegion, 25f, frameY + 6f, 58f, 58f);
            }

            // Header Row: Character Title (left) & Numeric HP (right) above the bar
            drawShadowedText("GREEN GUARDIAN", 94f, frameY + 52f, Color.valueOf("FCD34D"), 0.60f);

            // Right-aligned clean HP readout: "HP" in mint emerald + numbers in crisp white
            float barRightX = 94f + 244f;
            String hpNumText = (int) player.getHealth() + " / " + (int) player.getMaxHealth();
            font.getData().setScale(0.58f);
            glyphLayout.setText(font, "HP " + hpNumText);
            float totalHpWidth = glyphLayout.width;
            glyphLayout.setText(font, "HP ");
            float hpTagWidth = glyphLayout.width;
            font.getData().setScale(1.0f);

            float hpStartX = barRightX - totalHpWidth;
            drawShadowedText("HP ", hpStartX, frameY + 52f, Color.valueOf("34D399"), 0.58f);
            drawShadowedText(hpNumText, hpStartX + hpTagWidth, frameY + 52f, Color.WHITE, 0.58f);

            // Status Badges Text with shadow (Row 3)
            float textBadgeX = 94f;
            if (player.isWaterDebuffed()) {
                drawShadowedText("SLOW (" + (int) Math.ceil(player.getWaterDebuffTimer()) + "s)", textBadgeX + 6f, frameY + 16f, Color.valueOf("67E8F9"), 0.46f);
                textBadgeX += 90f + 6f;
            }

            if (player.getDamageStoneCount() > 0) {
                batch.draw(assets.swordIconTexture, textBadgeX + 4f, frameY + 5f, 12f, 12f);
                drawShadowedText("+" + (player.getDamageStoneCount() * 20) + "% ATK", textBadgeX + 18f, frameY + 16f, Color.valueOf("FCD34D"), 0.46f);
                textBadgeX += 80f + 6f;
            }

            if (player.hasSoulMagnet()) {
                batch.draw(assets.soulTexture, textBadgeX + 4f, frameY + 5f, 12f, 12f);
                drawShadowedText("MAGNET", textBadgeX + 18f, frameY + 16f, Color.valueOf("C084FC"), 0.46f);
            }

            if (boss.isAwake()) {
                font.setColor(Color.GOLD);
                String bossName = (levelIndex == 2) ? "TREE KNIGHT" : "SKELETON KING";
                glyphLayout.setText(font, bossName);
                float textWidth = glyphLayout.width;
                font.draw(batch, bossName, 640f - (textWidth / 2f), 712f);
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
            float mX = 360f, mY = 200f, mW = 560f, mH = 320f;

            // Grand Cinematic "GAME OVER"
            String title = "GAME OVER";
            font.getData().setScale(1.85f);
            glyphLayout.setText(font, title);
            float titleX = 640f - (glyphLayout.width / 2f);
            float titleY = mY + mH - 26f;

            // Soft dark drop shadow
            font.setColor(0f, 0f, 0f, 0.90f);
            font.draw(batch, title, titleX + 2.5f, titleY - 2.5f);
            // High-visibility radiant crimson
            font.setColor(Color.valueOf("FF4D4D"));
            font.draw(batch, title, titleX, titleY);

            // Large, clear, high-contrast Lore Subtitle
            String subtitle = "The Guardian's light has faded in the ancient grove...";
            font.getData().setScale(0.85f);
            glyphLayout.setText(font, subtitle);
            float subX = 640f - (glyphLayout.width / 2f);
            float subY = mY + mH - 118f;
            drawShadowedText(subtitle, subX, subY, Color.valueOf("F3F4F6"), 0.85f);

            // Large, clear Encouraging Hint
            String tip = "Death is not the end. Rise again to reclaim the sacred grove!";
            font.getData().setScale(0.78f);
            glyphLayout.setText(font, tip);
            float tipX = 640f - (glyphLayout.width / 2f);
            float tipY = subY - 38f;
            drawShadowedText(tip, tipX, tipY, Color.valueOf("FCD34D"), 0.78f);

            // Prominent Interactive Button Prompt
            float btnY = mY + 40f;
            String btnText = "[ ENTER ]  REVIVE & RESTART";
            font.getData().setScale(0.92f);
            glyphLayout.setText(font, btnText);
            float btnTextX = 640f - (glyphLayout.width / 2f);
            float btnTextY = btnY + 37f;
            drawShadowedText(btnText, btnTextX, btnTextY, Color.valueOf("FEF08A"), 0.92f);
            font.getData().setScale(1.0f);
        } else if (boss.isDead()) {
            if (levelIndex == 1) {
                String titleText = playerKeys >= 3 ? "ALL KEYS FOUND!" : "LEVEL 1 CLEARED!";
                font.getData().setScale(1.2f);
                glyphLayout.setText(font, titleText);
                float titleX = 640f - (glyphLayout.width / 2f);
                font.setColor(Color.GOLD);
                font.draw(batch, titleText, titleX, 430);

                String btnText = "Enter Level 2";
                font.getData().setScale(1.2f);
                glyphLayout.setText(font, btnText);
                float btnX = 640f - (glyphLayout.width / 2f);
                float btnY = 320f + (glyphLayout.height / 2f);
                font.setColor(Color.WHITE);
                font.draw(batch, btnText, btnX, btnY);
                font.getData().setScale(1.0f);
            } else {
                String titleText = "VICTORY ACHIEVED!";
                font.getData().setScale(1.2f);
                glyphLayout.setText(font, titleText);
                float titleX = 640f - (glyphLayout.width / 2f);
                font.setColor(Color.GOLD);
                font.draw(batch, titleText, titleX, 430);

                String btnText = "Main Menu";
                font.getData().setScale(1.2f);
                glyphLayout.setText(font, btnText);
                float btnX = 640f - (glyphLayout.width / 2f);
                float btnY = 320f + (glyphLayout.height / 2f);
                font.setColor(Color.WHITE);
                font.draw(batch, btnText, btnX, btnY);
                font.getData().setScale(1.0f);
            }
        }
        batch.end();
    }

    private void drawShadowedText(String text, float x, float y, Color color, float scale) {
        font.getData().setScale(scale);
        font.setColor(0f, 0f, 0f, 0.85f);
        font.draw(batch, text, x + 1.2f, y - 1.2f);
        font.setColor(color);
        font.draw(batch, text, x, y);
        font.getData().setScale(1.0f);
    }
}

