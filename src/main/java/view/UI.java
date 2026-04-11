package view;

import java.awt.*;
import model.GameState;

/**
 * UI — renders all overlay screens (title, HUD, game over).
 * Reads GameState only — never modifies it.
 */
public class UI {

    private final Font fontLarge  = new Font("Arial", Font.BOLD,  96);
    private final Font fontMedium = new Font("Arial", Font.BOLD,  48);
    private final Font fontSmall  = new Font("Arial", Font.PLAIN, 28);

    public void draw(Graphics2D g2, GameState state,
                     int screenWidth, int screenHeight, int tileSize) {

        if (state.currentState == GameState.TITLE_STATE) {
            drawTitleScreen(g2, state, screenWidth, tileSize);
        } else if (state.currentState == GameState.PLAY_STATE) {
            drawHUD(g2, state, screenWidth);
        } else if (state.currentState == GameState.GAME_OVER_STATE) {
            drawGameOverScreen(g2, state, screenWidth, screenHeight, tileSize);
        }
    }

    // ── Title screen ─────────────────────────────────────────────────
    private void drawTitleScreen(Graphics2D g2, GameState state,
                                  int screenWidth, int tileSize) {
        g2.setColor(Color.black);
        g2.fillRect(0, 0, screenWidth, tileSize * 12);

        // Title
        g2.setFont(fontLarge);
        String title = "VIPER";
        int x = centeredX(title, g2, screenWidth);
        int y = tileSize * 3;

        g2.setColor(Color.darkGray);
        g2.drawString(title, x + 5, y + 5);
        g2.setColor(new Color(50, 200, 50));
        g2.drawString(title, x, y);

        // Menu options
        g2.setFont(fontMedium);

        String newGame = "NEW GAME";
        x = centeredX(newGame, g2, screenWidth);
        y += tileSize * 3;
        g2.setColor(Color.white);
        g2.drawString(newGame, x, y);
        if (state.ui_commandNum == 0) g2.drawString(">", x - tileSize, y);

        String quit = "QUIT";
        x = centeredX(quit, g2, screenWidth);
        y += tileSize;
        g2.drawString(quit, x, y);
        if (state.ui_commandNum == 1) g2.drawString(">", x - tileSize, y);

        // High score
        g2.setFont(fontSmall);
        g2.setColor(Color.gray);
        String hs = "Best: " + state.player.highScore;
        g2.drawString(hs, centeredX(hs, g2, screenWidth), y + tileSize);
    }

    // ── HUD during gameplay ──────────────────────────────────────────
    private void drawHUD(Graphics2D g2, GameState state, int screenWidth) {
        g2.setFont(fontSmall);
        g2.setColor(Color.white);
        g2.drawString("Score: " + state.score, 16, 36);
        g2.drawString("Best:  " + state.player.highScore, 16, 64);
    }

    // ── Game over screen ─────────────────────────────────────────────
    private void drawGameOverScreen(Graphics2D g2, GameState state,
                                     int screenWidth, int screenHeight,
                                     int tileSize) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setFont(fontLarge);
        String over = "GAME OVER";
        int x = centeredX(over, g2, screenWidth);
        int y = tileSize * 3;
        g2.setColor(Color.red);
        g2.drawString(over, x, y);

        g2.setFont(fontSmall);
        g2.setColor(Color.white);
        String score = "Score: " + state.score + "   Best: " + state.player.highScore;
        g2.drawString(score, centeredX(score, g2, screenWidth), y + 60);

        g2.setFont(fontMedium);

        String retry = "RETRY";
        x = centeredX(retry, g2, screenWidth);
        y += tileSize * 2 + 20;
        g2.drawString(retry, x, y);
        if (state.ui_commandNum == 0) g2.drawString(">", x - 60, y);

        String quit = "QUIT";
        x = centeredX(quit, g2, screenWidth);
        y += tileSize;
        g2.drawString(quit, x, y);
        if (state.ui_commandNum == 1) g2.drawString(">", x - 60, y);
    }

    // ── Helper ───────────────────────────────────────────────────────
    private int centeredX(String text, Graphics2D g2, int screenWidth) {
        int len = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return screenWidth / 2 - len / 2;
    }
}