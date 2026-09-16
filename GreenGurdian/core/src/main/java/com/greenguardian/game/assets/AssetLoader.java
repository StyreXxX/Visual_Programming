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
    public Texture keyTexture;

    // Player Textures
    public Texture playerWalkSheet;
    public Texture playerAttackSheet;
    public Texture playerDeathSheet;
    public Texture playerStandSheet;
    public Texture playerIdleSheet;
    public Texture playerStaffWalkSheet;
    public Texture playerStaffAttackSheet;
    // public Texture playerStaffIdleSheet;

    // Boss 1 Textures (Level 1)
    public Texture bossWalkSheet;
    public Texture bossAttackSheet;
    public Texture bossSpecialAttackSheet;
    public Texture bossDashSheet;
    public Texture bossDeathSheet;
    public Texture bossChargeSheet;

    // Boss 2 Textures (Level 2 - Tree Knight)
    public Texture boss2WalkSheet;
    public Texture boss2AttackSheet;
    public Texture boss2SmashSheet;
    public Texture boss2ChargeSheet;
    public Texture boss2ShieldSheet;
    public Texture boss2DeathSheet;

    // Backwards-compatible aliases for Level 2 Boss
    public Texture centaurWalkSheet;
    public Texture centaurBasicAttackSheet;
    public Texture centaurSmashAttackSheet;
    public Texture centaurChargeAttackSheet;
    public Texture centaurDeathSheet;

    // Enemy Textures
    public Texture enemyWalkSheet;
    public Texture enemyAttackSheet;
    public Texture enemyDeathSheet;

    public void load() {
        shopTexture = new Texture(Gdx.files.internal("ui/shop.png"));
        soulTexture = new Texture(Gdx.files.internal("ui/soulCurrency.png"));
        staffDisplayTexture = new Texture(Gdx.files.internal("ui/magicStaff.png"));

        // Player
        playerWalkSheet = new Texture(Gdx.files.internal("characters/player/MainCharacterWalking.png"));
        playerAttackSheet = new Texture(Gdx.files.internal("characters/player/MainCharacterAttack.png"));
        playerDeathSheet = new Texture(Gdx.files.internal("characters/player/deathanimation.png"));
        playerStandSheet = new Texture(Gdx.files.internal("characters/player/MainCharacterStanding.png"));
        playerIdleSheet = new Texture(Gdx.files.internal("characters/player/idle-animation.png"));
        playerStaffWalkSheet = new Texture(Gdx.files.internal("characters/player/mainCharacterStaffWalking.png"));
        playerStaffAttackSheet = new Texture(Gdx.files.internal("characters/player/mainCharacterStaffAttack.png"));

        // Boss 1 (Level 1)
        bossWalkSheet = new Texture(Gdx.files.internal("characters/bosses/boss1/EnemyWalking.png"));
        bossAttackSheet = new Texture(Gdx.files.internal("characters/bosses/boss1/EnemyAttack.png"));
        bossSpecialAttackSheet = new Texture(Gdx.files.internal("characters/bosses/boss1/FirstBossAttack.png"));
        bossDashSheet = new Texture(Gdx.files.internal("characters/bosses/boss1/bossDash.png"));
        bossDeathSheet = new Texture(Gdx.files.internal("characters/bosses/boss1/EnemyDeath.png"));
        bossChargeSheet = new Texture(Gdx.files.internal("characters/bosses/boss1/bossCharge.png"));

        // Boss 2 (Level 2 - Tree Knight)
        boss2WalkSheet = new Texture(Gdx.files.internal("characters/bosses/boss2/boss2Walking.png"));
        boss2AttackSheet = new Texture(Gdx.files.internal("characters/bosses/boss2/boss2Attack.png"));
        boss2SmashSheet = new Texture(Gdx.files.internal("characters/bosses/boss2/boss2Smash.png"));
        boss2ChargeSheet = new Texture(Gdx.files.internal("characters/bosses/boss2/boss2Charge.png"));
        boss2ShieldSheet = new Texture(Gdx.files.internal("characters/bosses/boss2/boss2Shield.png"));
        boss2DeathSheet = new Texture(Gdx.files.internal("characters/bosses/boss2/boss2Death.png"));

        // Alias for compatibility
        centaurWalkSheet = boss2WalkSheet;
        centaurBasicAttackSheet = boss2AttackSheet;
        centaurSmashAttackSheet = boss2SmashSheet;
        centaurChargeAttackSheet = boss2ChargeSheet;
        centaurDeathSheet = boss2DeathSheet;

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

        keyTexture = new Texture(Gdx.files.internal("ui/key-white.gif"));
    }

    public void dispose() {
        if (shopTexture != null) shopTexture.dispose();
        if (soulTexture != null) soulTexture.dispose();
        if (staffDisplayTexture != null) staffDisplayTexture.dispose();
        if (swordProjectileTexture != null) swordProjectileTexture.dispose();
        if (staffProjectileTexture != null) staffProjectileTexture.dispose();
        if (keyTexture != null) keyTexture.dispose();

        if (playerWalkSheet != null) playerWalkSheet.dispose();
        if (playerAttackSheet != null) playerAttackSheet.dispose();
        if (playerDeathSheet != null) playerDeathSheet.dispose();
        if (playerStandSheet != null) playerStandSheet.dispose();
        if (playerIdleSheet != null) playerIdleSheet.dispose();
        if (playerStaffWalkSheet != null) playerStaffWalkSheet.dispose();
        if (playerStaffAttackSheet != null) playerStaffAttackSheet.dispose();

        if (bossWalkSheet != null) bossWalkSheet.dispose();
        if (bossAttackSheet != null) bossAttackSheet.dispose();
        if (bossSpecialAttackSheet != null) bossSpecialAttackSheet.dispose();
        if (bossDashSheet != null) bossDashSheet.dispose();
        if (bossDeathSheet != null) bossDeathSheet.dispose();
        if (bossChargeSheet != null) bossChargeSheet.dispose();

        if (boss2WalkSheet != null) boss2WalkSheet.dispose();
        if (boss2AttackSheet != null) boss2AttackSheet.dispose();
        if (boss2SmashSheet != null) boss2SmashSheet.dispose();
        if (boss2ChargeSheet != null) boss2ChargeSheet.dispose();
        if (boss2ShieldSheet != null) boss2ShieldSheet.dispose();
        if (boss2DeathSheet != null) boss2DeathSheet.dispose();

        if (enemyWalkSheet != null) enemyWalkSheet.dispose();
        if (enemyAttackSheet != null) enemyAttackSheet.dispose();
        if (enemyDeathSheet != null) enemyDeathSheet.dispose();
    }
}
