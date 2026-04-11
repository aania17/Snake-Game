package model.entity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Base class for all game entities (Player, future enemies).
 *
 * DESIGN PRINCIPLE — Open/Closed (OCP):
 * Entity is open for extension (Player extends it, AI enemies can too)
 * but closed for modification — we don't touch Entity when adding
 * new entity types.
 *
 * Pure model — no Graphics2D, no rendering of any kind.
 */
public class Entity {

    public int x, y;
    public int speed;
    public int hasKey;
    public int level;
    public int highScore;

    public String direction = "down";

    // Sprite images — stored here so GamePanel can read them for rendering
    public BufferedImage up1, up2, down1, down2;
    public BufferedImage left1, left2, right1, right2;

    // Animation state
    public int animationState = 1;
    public int animationCount = 0;
    public int maxAnimationCount = 10;

    // Collision
    public Rectangle solidArea;
    public int solidAreaDefaultX, solidAreaDefaultY;
    public boolean collisionOn = false;
}