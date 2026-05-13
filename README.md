# 🐍 Snake Game

A snake-style arcade game built entirely in Java using Swing. You control a snake navigating a tile-based map, collecting food to grow your score while dodging bombs that multiply as you get better. The game saves your high score locally using SQLite so your best run is always remembered across sessions.

The project was built as a learning exercise in software architecture — specifically refactoring a working but monolithic game into a clean **MVC (Model-View-Controller)** structure, applying SOLID principles, and implementing classic design patterns in a real, running application.

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
- Each food item collected is worth **10 points**
- Every **50 points**, an additional **bomb** spawns on the map — up to 9 at once
- Hitting any bomb ends the game immediately
- The map is made up of different tile types — some are walls or water that constrain where objects can spawn
- Your **high score persists** across sessions, saved automatically to a local SQLite database (`viper.db`) on game over
- On the title screen your all-time best is displayed so you always know what you're chasing

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
    │   ├── controller/
    │   │   ├── AssetController.java    # Spawns and places food and bombs
    │   │   ├── CollisionController.java# Detects object and tile collisions
    │   │   ├── GameController.java     # Core game loop logic and state transitions
    │   │   ├── InputHandler.java       # Captures keyboard input as boolean flags
    │   │   └── SoundController.java    # Loads and plays .wav audio clips
    │   ├── database/
    │   │   └── DatabaseManager.java    # SQLite persistence — Singleton pattern
    │   ├── main/
    │   │   └── Main.java               # Entry point — wires all layers together
    │   ├── model/
    │   │   ├── GameState.java          # Central game data — Observer pattern (Subject)
    │   │   ├── entity/
    │   │   │   ├── Entity.java         # Base class for all game entities
    │   │   │   └── Player.java         # Player position, speed, direction, sprites
    │   │   └── object/
    │   │       ├── SuperObject.java    # Base class for all world objects
    │   │       ├── OBJ_Food.java       # Collectible food — awards points on touch
    │   │       └── OBJ_Bomb.java       # Hazard — ends the game on contact
    │   ├── tile/
    │   │   ├── Tile.java               # Data holder: tile image + collision flag
    │   │   └── TileManager.java        # Loads map file, draws tile grid each frame
    │   └── view/
    │       ├── GamePanel.java          # Game loop + renders all game state each frame
    │       └── UI.java                 # Title screen, HUD overlay, game over screen
    └── resources/
        └── assets/
            ├── maps/map01              # Tile map — space-separated grid of tile indices
            ├── objects/                # bomb.png, food.png
            ├── player/                 # Snake sprite sheets (8 directional frames × 2)
            ├── sound/                  # .wav audio files for music and effects
            └── tiles/                  # Tile images (grass, wall, water, earth, sand, tree)
```

---

## Architecture

Viper follows strict **MVC (Model-View-Controller)** architecture with one-way, non-circular dependencies. This was the primary goal of the refactor — the original codebase had the View, Controller, and Model all importing each other freely, which caused compilation errors and made the code impossible to reason about.

```
Main
 ├── creates → Model      (GameState, Player, SuperObject[])
 ├── creates → Controllers (GameController, AssetController, CollisionController, ...)
 ├── creates → TileManager (tile layer — feeds mapLayout to GameController)
 └── creates → View       (GamePanel, UI)

Controller  →  reads/writes  →  Model
View        →  reads only    →  Model
Controller  →  never imports →  View
```

`Main.java` is the only class that knows about all layers. It constructs everything and injects dependencies downward. No class ever reaches upward or sideways outside its own layer.

### Layer Responsibilities

| Layer | Classes | Responsibility |
|-------|---------|----------------|
| **Model** | `GameState`, `Player`, `Entity`, `SuperObject`, `OBJ_Food`, `OBJ_Bomb` | Store all game data. No rendering, no input, no logic. |
| **Controller** | `GameController`, `AssetController`, `CollisionController`, `InputHandler`, `SoundController` | Update the model each tick. Never touch the View. |
| **View** | `GamePanel`, `UI` | Read the model and draw it. Never write to the model. |
| **Tile** | `TileManager`, `Tile` | Load and render the tile map. Provides `mapLayout` to controllers. |
| **Database** | `DatabaseManager` | Persist and retrieve high scores via SQLite. |

### Audio Events

| Event | File |
|-------|------|
| New game / retry | `BlueBoyAdventure.wav` (loops continuously) |
| Food collected | `eated.wav` |
| Bomb hit | `gameover.wav` |

### Tile Types

| Map value | Tile | Walkable | Objects spawn here |
|-----------|------|----------|--------------------|
| 0 | Grass | ✅ | ✅ |
| 1 | Wall | ❌ | ❌ |
| 2 | Water | ❌ | ❌ |
| 3 | Earth | ✅ | ✅ |
| 4 | Sand | ✅ | ✅ |
| 5 | Tree | ❌ | ❌ |

---

## Design Patterns

| Pattern | Where used | Purpose |
|---------|-----------|---------|
| **MVC** | Whole project | Clean separation of rendering, logic, and data |
| **Singleton** | `DatabaseManager` | One shared DB connection; prevents duplicate writes |
| **Observer** | `GameState` (Subject) + `DatabaseManager` (Observer) | Score changes automatically notify listeners — no manual calls needed |
| **Strategy** | `MoveStrategy` interface + `AStarStrategy` | Movement algorithm is swappable without changing any other class |
| **Factory** | `AssetController` | Centralises object creation — no other class calls `new OBJ_Food()` directly |

### SOLID Principles Applied

| Principle | How |
|-----------|-----|
| **SRP** — Single Responsibility | Each class has exactly one job. `GamePanel` only renders. `InputHandler` only captures keys. `CollisionController` only checks collisions. `SoundController` only manages audio. |
| **OCP** — Open/Closed | New entity types extend `Entity` without modifying it. New movement algorithms implement `MoveStrategy` without touching `GameController`. |
| **DIP** — Dependency Inversion | `GameController` depends on `GameState` (the model abstraction), never on `GamePanel` (a concrete View class). All dependencies are injected via constructors in `Main`. |

---

## Prerequisites

- **Java 17+** — the codebase uses switch expressions and records
- **Maven 3.6+** — for dependency management and building
- No external server required — SQLite runs fully embedded

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

The high score database (`viper.db`) is created automatically in the project root on first launch and updated on every game over.

---

## Dependencies

Add the following to your `pom.xml` if not already present:

```xml
<dependencies>
    <!-- SQLite JDBC driver for high score persistence -->
    <dependency>
        <groupId>org.xerial</groupId>
        <artifactId>sqlite-jdbc</artifactId>
        <version>3.45.1.0</version>
    </dependency>
</dependencies>

<build>
    <resources>
        <resource>
            <!-- Ensures assets (images, sounds, maps) are copied to classpath -->
            <directory>src/main/resources</directory>
        </resource>
    </resources>
</build>
```
