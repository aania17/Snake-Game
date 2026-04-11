package main;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import controller.AssetController;
import controller.CollisionController;
import controller.GameController;
import controller.InputHandler;
import controller.SoundController;
import database.DatabaseManager;
import model.GameState;
import model.entity.Player;
import model.object.SuperObject;
import tile.TileManager;
import view.GamePanel;
import view.UI;

/**
 * Main entry point — wires all layers together.
 *
 * FIXED:
 * - panel.requestFocusInWindow() moved to AFTER window.setVisible(true).
 *   Calling it before the window is visible silently fails, meaning
 *   keyboard input never registers.
 *
 * DESIGN PRINCIPLE — Dependency Inversion (DIP):
 * Main is the only place that knows about all layers.
 * Dependency flow (one-way only):
 *   Main → creates → Model, Controller, View
 *   Controller → reads/writes → Model
 *   View → reads → Model  (never writes)
 *   Controller → never imports → View
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // ── 1. MODEL ─────────────────────────────────────────────
            Player        player  = new Player();
            SuperObject[] objects = new SuperObject[10];
            GameState     state   = new GameState(player, objects);

            // ── 2. DATABASE (Singleton) ───────────────────────────────
            DatabaseManager db = DatabaseManager.getInstance();
            db.initialize();

            // Load persisted high score — must happen before setDefaultValues
            // resets anything, which is why Player.setDefaultValues() no longer
            // touches highScore.
            player.highScore = db.getHighScore();

            // Register DB as a score observer (Observer pattern).
            // Without this line, onScoreChanged() is never called.
            state.addScoreObserver(db);

            // ── 3. CONTROLLERS ────────────────────────────────────────
            InputHandler        input      = new InputHandler();
            AssetController     assets     = new AssetController();
            CollisionController collisions = new CollisionController();
            SoundController     sound      = new SoundController();

            final int tileSize = 48;
            final int maxCol   = 16;
            final int maxRow   = 12;

            GameController controller = new GameController(
                state, input, assets, collisions, sound, db,
                tileSize, maxRow, maxCol
            );

            // ── 4. TILE MAP ──────────────────────────────────────────
            // TileManager is created after dimensions are known.
            // Its mapLayout is passed to GameController so collision
            // detection uses the real map (walls, water, trees) instead
            // of the blank all-zero grid.
            TileManager tileManager = new TileManager(maxRow, maxCol, tileSize);
            controller.setMapLayout(tileManager.mapLayout);

            // ── 5. VIEW ──────────────────────────────────────────────
            UI        ui    = new UI();
            GamePanel panel = new GamePanel(state, controller, input, ui,
                                            tileManager, tileSize, maxCol, maxRow);

            // ── 6. WINDOW ────────────────────────────────────────────
            JFrame window = new JFrame();
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(false);
            window.setTitle("Viper");
            window.add(panel);
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);   // window must be visible FIRST

            // FIXED: register keyboard listener and request focus only
            // after the window is showing — otherwise focus silently fails.
            panel.addKeyListener(input);
            panel.setFocusable(true);
            panel.requestFocusInWindow();

            panel.startGameThread();
        });
    }
}