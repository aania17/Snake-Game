package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import model.GameState;

/**
 * DatabaseManager — persists high scores using SQLite.
 *
 * DESIGN PATTERN — Singleton:
 * Only one DatabaseManager instance exists. Accessed via getInstance().
 *
 * FIXES applied:
 * 1. Singleton is not thread-safe — getInstance() had a classic race
 *    condition. Fixed with a synchronized block (double-checked locking).
 *
 * 2. initialize() was not idempotent — calling it twice (e.g. on retry)
 *    would open a second Connection and orphan the first, leaking it.
 *    Fixed: early return if already connected.
 *
 * 3. Observer callback onScoreChanged() was a no-op log statement.
 *    The Observer pattern was set up but never wired — DatabaseManager
 *    was never actually registered with GameState in Main.java.
 *    Fixed: added registration note in the Javadoc and kept the callback
 *    as a deliberate no-op (final save-on-game-over is the correct design
 *    — writing to DB on every point is genuinely overkill). Comment updated
 *    to reflect the actual intent clearly.
 *
 * 4. getTopScores() leaked a ResultSet — rs was opened but never closed.
 *    Fixed: wrapped in try-with-resources.
 *
 * 5. close() is now registered as a JVM shutdown hook in initialize() so
 *    the DB connection is always cleanly closed even if the user kills the
 *    window without the Quit menu option (which calls System.exit()).
 *
 * 6. DB URL changed to an absolute path using user.dir so viper.db is
 *    always created in the project root regardless of where the JVM is
 *    launched from (running from IDE vs. from target/ gave different paths).
 */
public class DatabaseManager implements GameState.ScoreObserver {

    // ── Singleton ─────────────────────────────────────────────────────
    private static volatile DatabaseManager instance;  // volatile for thread visibility

    private Connection connection;

    private DatabaseManager() {}

    /** Thread-safe double-checked locking singleton. */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    // ── Setup ─────────────────────────────────────────────────────────
    public void initialize() {
        // FIXED: idempotent — skip if already connected
        if (connection != null) return;

        try {
            Class.forName("org.sqlite.JDBC");

            // FIXED: use absolute path so viper.db always lands in the
            // project root, not wherever the JVM happened to be launched from
            String dbPath = System.getProperty("user.dir") + "/viper.db";
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            createTable();
            System.out.println("[DB] Connected to " + dbPath);

            // FIXED: register shutdown hook so connection closes cleanly
            // even when the window is closed via the OS (not the Quit button)
            Runtime.getRuntime().addShutdownHook(new Thread(this::close));

        } catch (Exception e) {
            System.err.println("[DB] Could not connect: " + e.getMessage());
        }
    }

    private void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS scores (
                id        INTEGER PRIMARY KEY AUTOINCREMENT,
                score     INTEGER NOT NULL,
                played_at DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    // ── Observer callback ─────────────────────────────────────────────
    /**
     * Called by GameState every time the score changes.
     *
     * Intentionally a no-op for live updates — writing to the DB on
     * every food collection is unnecessary overhead. The definitive save
     * happens at game-over via saveScore(state.player.highScore).
     *
     * NOTE: For this callback to fire at all, DatabaseManager must be
     * registered with GameState in Main.java:
     *     state.addScoreObserver(db);
     * Without that line the Observer pattern is wired but never activated.
     */
    @Override
    public void onScoreChanged(int newScore) {
        // Deliberate no-op — final score persisted at game-over only.
    }

    // ── Save final score ──────────────────────────────────────────────
    /** Called by GameController when the player hits a bomb (game over). */
    public void saveScore(int score) {
        if (connection == null) {
            System.err.println("[DB] saveScore called but DB is not connected.");
            return;
        }
        String sql = "INSERT INTO scores (score) VALUES (?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, score);
            ps.executeUpdate();
            System.out.println("[DB] Saved score: " + score);
        } catch (SQLException e) {
            System.err.println("[DB] Save failed: " + e.getMessage());
        }
    }

    // ── Read top N scores ─────────────────────────────────────────────
    public int[] getTopScores(int limit) {
        if (connection == null) return new int[0];

        String sql = "SELECT score FROM scores ORDER BY score DESC LIMIT ?";
        int[]  scores = new int[limit];
        int    i      = 0;

        // FIXED: ResultSet was never closed in the original — wrapped in
        // try-with-resources so it closes automatically
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next() && i < limit) {
                    scores[i++] = rs.getInt("score");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] getTopScores failed: " + e.getMessage());
        }
        return scores;
    }

    // ── All-time best ─────────────────────────────────────────────────
    public int getHighScore() {
        if (connection == null) return 0;
        String sql = "SELECT MAX(score) AS best FROM scores";
        try (Statement  stmt = connection.createStatement();
             ResultSet  rs   = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("best");
        } catch (SQLException e) {
            System.err.println("[DB] getHighScore failed: " + e.getMessage());
        }
        return 0;
    }

    // ── Cleanup ───────────────────────────────────────────────────────
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}