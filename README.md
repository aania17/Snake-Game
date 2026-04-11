# 🐍 Viper

A snake-style Java game built with Swing, featuring tile-based maps, A* AI pathfinding, SQLite score persistence, and a clean MVC architecture.

---

## 📋 Table of Contents

- [Gameplay](#gameplay)
- [Controls](#controls)
- [Project Structure](#project-structure)
- [Architecture](#architecture)
- [Design Patterns](#design-patterns)
- [Prerequisites](#prerequisites)
- [Running the Game](#running-the-game)
- [Dependencies](#dependencies)

---

## Gameplay

- Navigate your snake around a tile-based map to collect **food** and earn points
- Each food collected is worth **10 points**
- Every **50 points**, an additional **bomb** spawns on the map
- Hitting a bomb ends the game
- Your **high score is saved** to a local SQLite database (`viper.db`) and persists across sessions

---

## Controls

| Key | Action |
|-----|--------|
| `W` / `↑` | Move Up |
| `S` / `↓` | Move Down |
| `A` / `←` | Move Left |
| `D` / `→` | Move Right |
| `Enter` | Confirm menu selection |

---

## Project Structure

```
src/
└── main/
    ├── java/
    │   ├── ai/
    │   │   ├── AStarStrategy.java      # A* pathfinding implementation
    │   │   └── MoveStrategy.java       # Strategy interface for movement
    │   ├── controller/
    │   │   ├── AssetController.java    # Spawns food and bombs
    │   │   ├── CollisionController.java# Detects object and tile collisions
    │   │   ├── GameController.java     # Core game loop logic
    │   │   ├── InputHandler.java       # Keyboard input capture
    │   │   └── SoundController.java    # Audio playback
    │   ├── database/
    │   │   └── DatabaseManager.java    # SQLite persistence (Singleton)
    │   ├── main/
    │   │   └── Main.java               # Entry point — wires all layers
    │   ├── model/
    │   │   ├── GameState.java          # Central game data (Subject/Observer)
    │   │   ├── entity/
    │   │   │   ├── Entity.java         # Base class for all entities
    │   │   │   └── Player.java         # Player position, movement, sprites
    │   │   └── object/
    │   │       ├── SuperObject.java    # Base class for game objects
    │   │       ├── OBJ_Food.java       # Collectible food item
    │   │       └── OBJ_Bomb.java       # Hazard — ends the game on contact
    │   ├── tile/
    │   │   ├── Tile.java               # Tile data holder (image + collision flag)
    │   │   └── TileManager.java        # Loads map file and draws tile grid
    │   └── view/
    │       ├── GamePanel.java          # Renders game state each frame
    │       └── UI.java                 # Title, HUD, and game over screens
    └── resources/
        └── assets/
            ├── maps/map01              # Tile map definition file
            ├── objects/                # food.png, bomb.png
            ├── player/                 # Snake sprite sheets (8 directional frames)
            ├── sound/                  # .wav audio files
            └── tiles/                  # Tile images (grass, wall, water, etc.)
```

---

## Architecture

Viper follows strict **MVC (Model-View-Controller)** architecture with one-way dependencies:

```
Main
 ├── creates → Model  (GameState, Player, SuperObject)
 ├── creates → Controllers  (GameController, AssetController, ...)
 └── creates → View  (GamePanel, UI)

Controller → reads/writes → Model
View       → reads only  → Model
Controller → never imports → View
```

**Dependency flow is strictly one-directional.** The View never writes to the model, and the Controller never imports anything from the View layer. `Main.java` is the only class that knows about all three layers.

### Audio Events

| Event | Sound file |
|-------|-----------|
| Game start / retry | `BlueBoyAdventure.wav` (loops) |
| Food collected | `eated.wav` |
| Bomb hit | `gameover.wav` |

### Tile Types

| Value in map file | Tile | Walkable |
|---|---|---|
| 0 | Grass | ✅ |
| 1 | Wall | ❌ |
| 2 | Water | ❌ |
| 3 | Earth | ✅ |
| 4 | Sand | ✅ |
| 5 | Tree | ❌ |

---

## Design Patterns

| Pattern | Where used | Purpose |
|---------|-----------|---------|
| **MVC** | Whole project | Separates rendering, logic, and data |
| **Singleton** | `DatabaseManager` | One shared DB connection across the app |
| **Observer** | `GameState` + `DatabaseManager` | Score changes notify listeners without tight coupling |
| **Strategy** | `MoveStrategy` + `AStarStrategy` | Swappable movement algorithms (human/AI) |
| **Factory** | `AssetController` | Centralises object creation (`OBJ_Food`, `OBJ_Bomb`) |

### SOLID Principles Applied

- **SRP** — Each class has one job: `GamePanel` only renders, `InputHandler` only captures keys, `CollisionController` only detects collisions
- **OCP** — New entity types extend `Entity`; new movement strategies implement `MoveStrategy` — no existing classes need modification
- **DIP** — `GameController` depends on `GameState` (abstraction), never on `GamePanel` (concrete View)

---

## Prerequisites

- **Java 17+** (uses records and switch expressions)
- **Maven 3.6+**
- No external server required — SQLite runs embedded

---

## Running the Game

**From IntelliJ IDEA:**
Right-click `Main.java` → Run `'Main.main()'`

**From the terminal:**
```bash
mvn clean compile exec:java -Dexec.mainClass="main.Main"
```

**As a JAR:**
```bash
mvn clean package
java -jar target/viper-1.0.jar
```

---

## Dependencies

Add the following to your `pom.xml` if not already present:

```xml
<dependencies>
    <!-- SQLite JDBC driver for score persistence -->
    <dependency>
        <groupId>org.xerial</groupId>
        <artifactId>sqlite-jdbc</artifactId>
        <version>3.45.1.0</version>
    </dependency>
</dependencies>

<build>
    <resources>
        <resource>
            <directory>src/main/resources</directory>
        </resource>
    </resources>
</build>
```

The high score database (`viper.db`) is created automatically in the project root on first launch.
