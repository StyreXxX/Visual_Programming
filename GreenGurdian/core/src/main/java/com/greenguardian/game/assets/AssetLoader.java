package com.greenguardian.game.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class AssetLoader {
    public Texture shopTexture;
    public Texture soulTexture;
    public Texture staffDisplayTexture;
    public Texture swordProjectileTexture;
    public Texture staffProjectileTexture;

    // Player Textures
    public Texture playerWalkSheet;
    public Texture playerAttackSheet;
    public Texture playerDeathSheet;
    public Texture playerStaffWalkSheet;
    public Texture playerStaffAttackSheet;

    // Boss Textures
    public Texture bossWalkSheet;
    public Texture bossAttackSheet;
    public Texture bossSpecialAttackSheet;
    public Texture bossDashSheet;
    public Texture bossDeathSheet;
    public Texture bossChargeSheet;

    // Enemy Textures
    public Texture enemyWalkSheet;
    public Texture enemyAttackSheet;
    public Texture enemyDeathSheet;

    public void load() {
        shopTexture = new Texture(Gdx.files.internal("ui/shop.png"));
        soulTexture = new Texture(Gdx.files.internal("ui/soulCurrency.png"));
        staffDisplayTexture = new Texture(Gdx.files.internal("ui/magicStaff.png"));

        // Player
        playerWalkSheet = new Texture(Gdx.files.internal("characters/player/MCWalking.png"));
        playerAttackSheet = new Texture(Gdx.files.internal("characters/player/MainCharacterAttack.png"));
        playerDeathSheet = new Texture(Gdx.files.internal("characters/player/deathanimation.png"));
        playerStaffWalkSheet = new Texture(Gdx.files.internal("characters/player/mainCharacterStaffWalking.png"));
        playerStaffAttackSheet = new Texture(Gdx.files.internal("characters/player/mainCharacterStaffAttack.png"));

        // Boss
        bossWalkSheet = new Texture(Gdx.files.internal("characters/boss/EnemyWalking.png"));
        bossAttackSheet = new Texture(Gdx.files.internal("characters/boss/EnemyAttack.png"));
        bossSpecialAttackSheet = new Texture(Gdx.files.internal("characters/boss/FirstBossAttack.png"));
        bossDashSheet = new Texture(Gdx.files.internal("characters/boss/bossDash.png"));
        bossDeathSheet = new Texture(Gdx.files.internal("characters/boss/EnemyDeath.png"));
        bossChargeSheet = new Texture(Gdx.files.internal("characters/boss/bossCharge.png"));

        // Enemy
        enemyWalkSheet = new Texture(Gdx.files.internal("characters/enemy/normalEnemyWalking.png"));
        enemyAttackSheet = new Texture(Gdx.files.internal("characters/enemy/normalEnemyAttacking.png"));
        enemyDeathSheet = new Texture(Gdx.files.internal("characters/enemy/normalEnemyDeath.png"));

        Pixmap p1 = new Pixmap(16, 8, Pixmap.Format.RGBA8888);
        p1.setColor(Color.GREEN);
        p1.fill();
        swordProjectileTexture = new Texture(p1);
        p1.dispose();

        Pixmap p2 = new Pixmap(28, 16, Pixmap.Format.RGBA8888);
        p2.setColor(Color.CYAN);
        p2.fill();
        staffProjectileTexture = new Texture(p2);
        p2.dispose();
    }

    public void dispose() {
        if (shopTexture != null) shopTexture.dispose();
        if (soulTexture != null) soulTexture.dispose();
        if (staffDisplayTexture != null) staffDisplayTexture.dispose();
        if (swordProjectileTexture != null) swordProjectileTexture.dispose();
        if (staffProjectileTexture != null) staffProjectileTexture.dispose();

        if (playerWalkSheet != null) playerWalkSheet.dispose();
        if (playerAttackSheet != null) playerAttackSheet.dispose();
        if (playerDeathSheet != null) playerDeathSheet.dispose();
        if (playerStaffWalkSheet != null) playerStaffWalkSheet.dispose();
        if (playerStaffAttackSheet != null) playerStaffAttackSheet.dispose();

        if (bossWalkSheet != null) bossWalkSheet.dispose();
        if (bossAttackSheet != null) bossAttackSheet.dispose();
        if (bossSpecialAttackSheet != null) bossSpecialAttackSheet.dispose();
        if (bossDashSheet != null) bossDashSheet.dispose();
        if (bossDeathSheet != null) bossDeathSheet.dispose();
        if (bossChargeSheet != null) bossChargeSheet.dispose();

        if (enemyWalkSheet != null) enemyWalkSheet.dispose();
        if (enemyAttackSheet != null) enemyAttackSheet.dispose();
        if (enemyDeathSheet != null) enemyDeathSheet.dispose();
    }
}
