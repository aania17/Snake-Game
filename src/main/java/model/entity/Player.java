package model.entity;

import java.awt.Rectangle;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * Player model — stores position, direction, animation state.
 *
 * FIXED:
 * - setDefaultValues() no longer resets highScore to 0.
 *   highScore is loaded from DB once in Main and must survive
 *   across retries. Only a fresh app launch should reset it.
 * - loadImages() uses a null-check on the InputStream so a missing
 *   sprite file prints a clear error instead of a silent NPE.
 *
 * DESIGN PRINCIPLE — Single Responsibility (SRP):
 * Player only manages its own data and movement logic.
 * Rendering → GamePanel (View). Input → InputHandler (Controller).
 */
public class Player extends Entity {

    public Player() {
        solidArea          = new Rectangle(8, 16, 32, 32);
        solidAreaDefaultX  = solidArea.x;
        solidAreaDefaultY  = solidArea.y;

        setDefaultValues();
        loadImages();
    }

    // ── Initialization ────────────────────────────────────────────────
    public void setDefaultValues() {
        x         = 100;
        y         = 100;
        speed     = 4;
        direction = "down";
        hasKey    = 0;
        level     = 1;
        // NOTE: highScore is intentionally NOT reset here.
        // It is loaded from the database once in Main.java and
        // must survive game-over → retry cycles.

        animationState    = 1;
        animationCount    = 0;
        maxAnimationCount = 10;
    }

    private void loadImages() {
        up1    = loadImage("/assets/player/snake_up_1.png");
        up2    = loadImage("/assets/player/snake_up_2.png");
        down1  = loadImage("/assets/player/snake_down_1.png");
        down2  = loadImage("/assets/player/snake_down_2.png");
        left1  = loadImage("/assets/player/snake_left_1.png");
        left2  = loadImage("/assets/player/snake_left_2.png");
        right1 = loadImage("/assets/player/snake_right_1.png");
        right2 = loadImage("/assets/player/snake_right_2.png");
    }

    /** Loads a single image from the classpath; logs clearly if missing. */
    private java.awt.image.BufferedImage loadImage(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.err.println("[Player] ERROR: sprite not found on classpath: " + path);
                return null;
            }
            return ImageIO.read(is);
        } catch (Exception e) {
            System.err.println("[Player] ERROR: failed to read sprite: " + path);
            e.printStackTrace();
            return null;
        }
    }

    // ── Movement ──────────────────────────────────────────────────────
    public void move(String directionInput) {
        this.direction = directionInput;

        switch (direction) {
            case "up"    -> y -= speed;
            case "down"  -> y += speed;
            case "left"  -> x -= speed;
            case "right" -> x += speed;
        }

        updateAnimation();
    }

    private void updateAnimation() {
        animationCount++;
        if (animationCount >= maxAnimationCount) {
            animationState = (animationState == 1) ? 2 : 1;
            animationCount = 0;
        }
    }

    public void resetCollision() {
        collisionOn = false;
    }

    // ── Boundary clamping ─────────────────────────────────────────────
    public void clampToBounds(int screenWidth, int screenHeight) {
        if (x < 0)                 x = 0;
        if (y < 0)                 y = 0;
        if (x + 48 > screenWidth)  x = screenWidth  - 48;
        if (y + 48 > screenHeight) y = screenHeight - 48;
    }
}