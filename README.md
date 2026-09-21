# Green Guardian

Youtube Link: https://www.youtube.com/watch?v=UMCAUNgvF3w

## Overview
Green Guardian is an immersive 2D action-platformer built from the ground up using Java and the LibGDX framework. Designed as a comprehensive showcase of strong Object-Oriented Programming (OOP) principles, the game offers a rich, dynamic world where players battle challenging bosses, collect currency, and upgrade their arsenal. From exploring the grassy plains of Level 1 to navigating the complex water physics of Level 2, players must master diverse combat styles, manage their resources, and utilize an interactive HUD to survive.

## Core Features
* **Dynamic Combat & Economy:** Switch between distinct weapons, including a standard Sword and an upgraded Magic Staff. Defeat enemies to harvest Souls, which can be spent at the in-game mystic shop to purchase tactical upgrades.
* **Advanced Boss Encounters:** Face off against multi-phased bosses equipped with unique AI behaviors, specialized attack patterns, and escalating difficulty mechanics based on health thresholds.
* **Interactive Environments:** Traverse meticulously designed Tiled maps featuring custom collision mechanics, dynamic water physics, and distinct level themes.
* **Robust UI/UX:** Navigate seamlessly through custom Start and Menu screens, utilize a real-time minimap radar for exploration, and manage inventory via a dedicated hotbar. Settings include quality-of-life features like adjustable audio sliders.

## Team Contributions

Rayhan:
* Game startup.
* Introduced Tiled map design
* Made Level 1 Map in Tiled
* Made Level 1 Boss
* Made Level 1 npcs
* Introduced the Shop in the game
* Introduced the Soul for shop
* Introduced different weapons(Staff and Sword)
* Introduced the Fast Travel to different level(Developers Option)
* Queued Audio in Level 2
* Made water mechanics in level 2
* Quality of life changes: introducing sound slidebar in settings

Nafi:
* Initialized core game logic, screens, player entity, assets, and level maps.
* Restructured entire codebase to follow OOP approach
* Introduced Audio for different interfaces
* Improved Boss mechanics and encounters.
* Implemented and organized player animations (stand animation, fading animation, etc.).
* Replaced hardcoded values and used static polygons for less memory usage.
* Performed multiple bug fixes and optimizations across the codebase.
* 8Created the foundational MenuScreen and StartScreen for the game's entry flow.
* Resolved multiple bugs related to the rendering and behavior of menus. write this in compact points

Afraz:
* Improved combat and weapon mechanics
* Introduced weapon slot and hotbar
* Introduced healing and rpgb mechanics
* Made Level 2 Map
* Made level 2 boss and enemy mechanics and design
* Introduced hud overhaul
* Introduced minimap radar
* Improved water mechanics in level 2
* map fixes(some bugs in level 1 map)
* animation fixes

## Technical Stack
* **Language:** Java
* **Framework:** LibGDX, LWJGL3 (Desktop Backend)
* **Level Design:** Tiled Map Editor (.tmx)
* **Build System:** Gradle
* **IDE Workflow:** IntelliJ IDEA

## Installation & Execution
To run Green Guardian locally, ensure you have a Java JDK installed on your machine. The project uses Gradle to manage dependencies and build the LWJGL3 desktop application.

1. Clone this repository to your local machine using Git.
2. Open IntelliJ IDEA and select **Open Project**, navigating to the root `build.gradle` file.
3. Allow Gradle to sync completely. This will download all required LibGDX libraries, LWJGL native files, and asset dependencies.
4. Open the **Gradle Tool Window** on the right side of the IDE.
5. Navigate through the task tree: `GreenGuardian > lwjgl3 > Tasks > application`.
6. Double-click the `run` task to compile and launch the game window.

* **E:** Attack / Fire Projectile
* **B:** Open / Close Mystic Shop
* **Enter:** Confirm Shop Purchase / Restart Game
