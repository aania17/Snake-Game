package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import controller.GameController;
import controller.InputHandler;
import model.GameState;
import model.entity.Player;
import model.object.SuperObject;
import tile.TileManager;

/**
 * GamePanel — the View.
 *
 * FIXED:
 * - obj.draw(g2, tileSize) — matches the updated SuperObject.draw()
 *   signature which no longer takes a GamePanel argument.
 *
 * DESIGN PRINCIPLE — Single Responsibility (SRP):
 * GamePanel ONLY renders. It reads GameState and draws it.
 * It does NOT update state, spawn objects, or handle logic.
 *
 * DESIGN PRINCIPLE — Dependency Inversion (DIP):
 * GamePanel depends on GameState (model abstraction), not on
 * specific controller internals. GameController is injected
 * in — GamePanel never calls 'new GameController(...)'.
 */
public class GamePanel extends JPanel implements Runnable {

    // ── Screen config ─────────────────────────────────────────────────
    public final int tileSize;
    public final int maxScreenCol;
    public final int maxScreenRow;
    public final int screenWidth;
    public final int screenHeight;

    // ── Dependencies (injected, never created here) ───────────────────
    private final GameState      state;
    private final GameController controller;
    private final UI             ui;
    private final TileManager    tileManager;

    private Thread gameThread;
    private static final int FPS = 60;

    public GamePanel(GameState state,
                     GameController controller,
                     InputHandler input,
                     UI ui,
                     TileManager tileManager,
                     int tileSize, int maxCol, int maxRow) {

        this.state        = state;
        this.controller   = controller;
        this.ui           = ui;
        this.tileManager  = tileManager;
        this.tileSize     = tileSize;
        this.maxScreenCol = maxCol;
        this.maxScreenRow = maxRow;
        this.screenWidth  = tileSize * maxCol;
        this.screenHeight = tileSize * maxRow;

        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    // ── Game loop ─────────────────────────────────────────────────────
    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        double delta        = 0;
        long   lastTime     = System.nanoTime();

        while (gameThread != null) {
            long currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                controller.update();
                repaint();
                delta--;
            }
        }
    }

    // ── Rendering ─────────────────────────────────────────────────────
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Draw tile map (replaces the plain grid background)
        tileManager.draw(g2);

        // Draw objects (food, bombs)
        // FIXED: pass tileSize instead of GamePanel instance
        for (SuperObject obj : state.objects) {
            if (obj != null) obj.draw(g2, tileSize);
        }

        // Draw player (rendering logic lives here, not in Player model)
        drawPlayer(g2, state.player);

        // Draw UI overlay (score, title screen, game over)
        ui.draw(g2, state, screenWidth, screenHeight, tileSize);

        g2.dispose();
    }

    // ── Player rendering ──────────────────────────────────────────────
    private void drawPlayer(Graphics2D g2, Player player) {
        BufferedImage image = switch (player.direction) {
            case "up"    -> (player.animationState == 1) ? player.up1    : player.up2;
            case "down"  -> (player.animationState == 1) ? player.down1  : player.down2;
            case "left"  -> (player.animationState == 1) ? player.left1  : player.left2;
            case "right" -> (player.animationState == 1) ? player.right1 : player.right2;
            default      -> player.down1;
        };

        if (image != null) {
            g2.drawImage(image, player.x, player.y, tileSize, tileSize, null);
        } else {
            // Fallback: green square if sprite not loaded
            g2.setColor(Color.green);
            g2.fillRect(player.x, player.y, tileSize, tileSize);
        }
    }

}