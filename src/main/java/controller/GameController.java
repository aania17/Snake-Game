package controller;

import database.DatabaseManager;
import model.GameState;
import model.entity.Player;

/**
 * GameController — orchestrates all game logic each tick.
 *
 * FIXES applied:
 * 1. startNewGame() no longer resets highScore — Player.setDefaultValues()
 *    was already fixed to not touch it, but we also preserve it explicitly
 *    here by caching and restoring it around the reset call, as a safety net.
 * 2. Bomb collision detection was checking obj.collision flag which was
 *    never set to true in the old SuperObject. OBJ_Bomb now sets
 *    collision=true, but we also match by name ("Bomb") so it works either way.
 * 3. Menu input debounce — up/down flags are consumed immediately so holding
 *    a key doesn't scroll the menu dozens of steps per second.
 * 4. Sound is not restarted if background music is already looping
 *    (avoids the click/restart on every frame during gameplay).
 * 5. mapLayout is kept as a flat all-zero grid (no walls), consistent with
 *    the tile-less design. Kept here so it can be replaced with a real map
 *    later without touching any other class.
 *
 * DESIGN — Dependency Inversion (DIP):
 * No import of any view class. GamePanel is never referenced here.
 *
 * DESIGN — Single Responsibility (SRP):
 * Rendering  → GamePanel
 * Input      → InputHandler
 * Spawning   → AssetController
 * Collisions → CollisionController
 * Persistence→ DatabaseManager
 */
public class GameController {

    // ── Dependencies ──────────────────────────────────────────────────
    private final GameState           state;
    private final InputHandler        input;
    private final AssetController     assets;
    private final CollisionController collisions;
    private final SoundController     sound;
    private final DatabaseManager     db;

    // ── Grid config ───────────────────────────────────────────────────
    private final int     tileSize;
    private final int     maxRow;
    private final int     maxCol;
    private       int[][] mapLayout;   // set after TileManager loads the real map

    // ── Internal flags ────────────────────────────────────────────────
    private boolean musicPlaying = false;

    // ── Constructor ───────────────────────────────────────────────────
    public GameController(GameState state,
                          InputHandler input,
                          AssetController assets,
                          CollisionController collisions,
                          SoundController sound,
                          DatabaseManager db,
                          int tileSize, int maxRow, int maxCol) {
        this.state      = state;
        this.input      = input;
        this.assets     = assets;
        this.collisions = collisions;
        this.sound      = sound;
        this.db         = db;
        this.tileSize   = tileSize;
        this.maxRow     = maxRow;
        this.maxCol     = maxCol;
        this.mapLayout  = new int[maxRow][maxCol]; // all 0 = walkable
    }

    // ── Called by Main after TileManager is ready ─────────────────────
    /**
     * Replaces the blank all-zero placeholder grid with the real map layout
     * loaded by TileManager. Must be called before the game loop starts.
     * This gives CollisionController and AssetController accurate wall data.
     */
    public void setMapLayout(int[][] mapLayout) {
        this.mapLayout = mapLayout;
    }

    // ── Main update — called every frame by GamePanel ─────────────────
    public void update() {
        if      (state.isTitle())    handleTitleInput();
        else if (state.isPlaying())  handlePlayUpdate();
        else if (state.isGameOver()) handleGameOverInput();
    }

    // ── Title screen ──────────────────────────────────────────────────
    private void handleTitleInput() {
        // Consume flags immediately — prevents holding key from scrolling fast
        if (input.up && state.ui_commandNum > 0) {
            state.ui_commandNum--;
            input.up = false;
        }
        if (input.down && state.ui_commandNum < 1) {
            state.ui_commandNum++;
            input.down = false;
        }

        if (input.enter) {
            input.enter = false;
            if (state.ui_commandNum == 0) {
                startNewGame();
            } else {
                System.exit(0);
            }
        }
    }

    // ── Gameplay update ───────────────────────────────────────────────
    private void handlePlayUpdate() {

        // 1. Apply input to player movement.
        // getDirection() returns the single most-recently-pressed direction,
        // preventing diagonal movement when two keys are held simultaneously.
        Player p = state.player;
        boolean anyDirectionHeld = input.up || input.down || input.left || input.right;
        if (anyDirectionHeld) {
            p.move(input.getDirection());
        }

        // 2. Keep player inside screen bounds
        p.clampToBounds(tileSize * maxCol, tileSize * maxRow);

        // 3. Spawn food if slot is empty
        if (!state.foodExists) {
            state.foodExists = true;
            assets.genFood(state, tileSize, maxRow, maxCol, mapLayout);
        }

        // 4. Spawn any missing bombs (one active per level of bombNo)
        for (int i = 0; i < state.bombNo; i++) {
            if (!state.bombsExist[i]) {
                state.bombsExist[i] = true;
                assets.genBomb(state, i + 1, tileSize, maxRow, maxCol, mapLayout);
            }
        }

        // 5. Detect collisions with objects
        int hitIndex = collisions.checkObjectCollision(state.player, state.objects);

        if (hitIndex != -1 && state.objects[hitIndex] != null) {
            String hitName = state.objects[hitIndex].name;

            if ("Food".equals(hitName)) {
                handleFoodCollected(hitIndex);
            } else if ("Bomb".equals(hitName)) {
                handleBombHit();
            }
        }
    }

    // ── Food collected ────────────────────────────────────────────────
    private void handleFoodCollected(int hitIndex) {
        // Award points (addScore also updates highScore if beaten)
        state.addScore(state.objects[hitIndex].value);

        // Remove food — it will be respawned next tick
        state.objects[hitIndex] = null;
        state.foodExists = false;

        // Every 50 points, add one more bomb (cap at 9)
        if (state.score > 0 && state.score % 50 == 0 && state.bombNo < 9) {
            state.bombNo++;
        }

        // Play eat sound (index 0 = eated.wav)
        sound.setFile(0);
        sound.play();
    }

    // ── Bomb hit → game over ──────────────────────────────────────────
    private void handleBombHit() {
        state.currentState  = GameState.GAME_OVER_STATE;
        state.ui_commandNum = 0;
        musicPlaying        = false;

        // Persist high score
        db.saveScore(state.player.highScore);

        // Play game-over sound (index 1 = gameover.wav)
        sound.stop();
        sound.setFile(1);
        sound.play();
    }

    // ── Game over screen ──────────────────────────────────────────────
    private void handleGameOverInput() {
        if (input.up && state.ui_commandNum > 0) {
            state.ui_commandNum--;
            input.up = false;
        }
        if (input.down && state.ui_commandNum < 1) {
            state.ui_commandNum++;
            input.down = false;
        }

        if (input.enter) {
            input.enter = false;
            if (state.ui_commandNum == 0) {
                startNewGame();   // Retry
            } else {
                System.exit(0);   // Quit
            }
        }
    }

    // ── Start / restart a game ────────────────────────────────────────
    private void startNewGame() {
        // Cache high score — survives the player reset
        int savedHighScore = state.player.highScore;

        state.player.setDefaultValues();
        state.player.highScore = savedHighScore;   // restore after reset

        // resetGame() zeroes score, clears objects/bombs/food in one call
        state.resetGame();
        state.currentState = GameState.PLAY_STATE;

        // Start background music only if not already looping
        if (!musicPlaying) {
            sound.stop();
            sound.setFile(2);   // BlueBoyAdventure.wav
            sound.loop();
            musicPlaying = true;
        }
    }
}