package model;

import java.util.ArrayList;
import java.util.List;

import model.entity.Player;
import model.object.SuperObject;

/**
 * Central model — holds ALL game data.
 * No rendering, no input, no controller logic here.
 *
 * DESIGN PATTERN — Observer:
 * GameState is the Subject. ScoreObserver implementors are notified
 * whenever the score changes (add or reset), without GameState knowing
 * who is listening.
 *
 * DESIGN PRINCIPLE — Single Responsibility (SRP):
 * GameState only stores and exposes game data.
 * It does not update itself — that is GameController's job.
 *
 * FIXES:
 * 1. resetGame() added — resets all mutable gameplay fields in one place
 *    so GameController doesn't scatter field assignments across startNewGame().
 *    highScore is intentionally NOT touched here (it survives retries).
 * 2. Observer notification made consistent — both addScore() and resetScore()
 *    notify after the state change is complete.
 * 3. object array is reset via resetGame() rather than being replaced with
 *    a naked 'new SuperObject[10]' from outside, which bypassed encapsulation.
 */
public class GameState {

    // ── State constants ───────────────────────────────────────────────
    public static final int TITLE_STATE     = 0;
    public static final int PLAY_STATE      = 1;
    public static final int GAME_OVER_STATE = 2;

    // ── Core data (public for read access by View; mutated via methods) ─
    public Player        player;
    public SuperObject[] objects;

    public int     currentState = TITLE_STATE;
    public int     score        = 0;
    public int     bombNo       = 1;
    public boolean foodExists   = false;
    public boolean[] bombsExist = new boolean[10];

    // Menu cursor — written by GameController, read by UI
    public int ui_commandNum = 0;

    // ── Observer list ─────────────────────────────────────────────────
    private final List<ScoreObserver> observers = new ArrayList<>();

    // ── Constructor ───────────────────────────────────────────────────
    public GameState(Player player, SuperObject[] objects) {
        this.player  = player;
        this.objects = objects;
    }

    // ── Observer registration ─────────────────────────────────────────
    public void addScoreObserver(ScoreObserver observer) {
        observers.add(observer);
    }

    // ── Score mutation (always go through these — never set score directly) ─

    /**
     * Adds points to the current score and updates highScore if beaten.
     * Notifies observers after both values are updated.
     */
    public void addScore(int points) {
        score += points;
        if (score > player.highScore) {
            player.highScore = score;   // highScore updated before notify
        }
        notifyObservers();
    }

    /**
     * Zeroes the score for a new game.
     * Does NOT reset highScore — that persists across retries.
     * Notifies observers after the reset so the UI reflects 0 immediately.
     */
    public void resetScore() {
        score = 0;
        notifyObservers();
    }

    // ── Full game reset (called by GameController on new game / retry) ─
    /**
     * Resets all mutable gameplay fields to their start-of-game values.
     * highScore and currentState are NOT touched here — the caller sets
     * currentState after this returns.
     *
     * Centralising the reset here means GameController doesn't need to
     * know about every individual field — it just calls resetGame().
     */
    public void resetGame() {
        resetScore();
        foodExists   = false;
        bombNo       = 1;
        bombsExist   = new boolean[10];
        objects      = new SuperObject[10];
        ui_commandNum = 0;
    }

    // ── State helpers ─────────────────────────────────────────────────
    public boolean isPlaying()  { return currentState == PLAY_STATE;      }
    public boolean isGameOver() { return currentState == GAME_OVER_STATE; }
    public boolean isTitle()    { return currentState == TITLE_STATE;     }

    // ── Observer notification ─────────────────────────────────────────
    private void notifyObservers() {
        for (ScoreObserver o : observers) {
            o.onScoreChanged(score);
        }
    }

    // ── Observer interface ────────────────────────────────────────────
    public interface ScoreObserver {
        void onScoreChanged(int newScore);
    }
}