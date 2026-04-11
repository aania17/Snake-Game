package model.object;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import model.entity.Entity;

/**
 * SuperObject — base class for all game objects (Food, Bomb).
 *
 * FIXED (MVC refactor):
 * - Package changed from 'object' → 'model.object'
 * - draw()     now takes (Graphics2D, int tileSize)     — no GamePanel
 * - generate() now takes (int maxRow, int maxCol,
 *                         int[][] mapLayout, Entity entity, int tileSize)
 *                                                        — no GamePanel
 * - Removed all imports of main.GamePanel and entity.Entity (old paths)
 */
public class SuperObject {

    public BufferedImage image;
    public String  name;
    public boolean collision = false;
    public int     worldX, worldY;
    public int     value = 10;   // score awarded on collect (Food overrides)

    public Rectangle solidArea        = new Rectangle(0, 0, 48, 48);
    public int       solidAreaDefaultX = 0;
    public int       solidAreaDefaultY = 0;

    // ── Rendering (called by GamePanel — View responsibility) ────────
    public void draw(Graphics2D g2, int tileSize) {
        if (image != null) {
            g2.drawImage(image, worldX, worldY, tileSize, tileSize, null);
        }
    }

    // ── Spawning (called by AssetController) ─────────────────────────
    /**
     * Places this object at a random walkable tile,
     * guaranteed not to overlap the player's current tile.
     *
     * @param maxRow    number of tile rows on screen
     * @param maxCol    number of tile columns on screen
     * @param mapLayout tile map (0 = walkable, 1 = wall)
     * @param entity    player entity — used to avoid spawning on top of player
     * @param tileSize  pixel size of one tile
     */
    public void generate(int maxRow, int maxCol,
                         int[][] mapLayout, Entity entity, int tileSize) {

        int col, row;
        int playerCol = entity.x / tileSize;
        int playerRow = entity.y / tileSize;

        do {
            col = (int) (Math.random() * maxCol);
            row = (int) (Math.random() * maxRow);
        } while (
            // Don't spawn on a wall tile
            mapLayout[row][col] == 1
            // Don't spawn exactly on the player
            || (col == playerCol && row == playerRow)
        );

        worldX = col * tileSize;
        worldY = row * tileSize;

        // Keep solidArea in sync with world position for collision checks
        solidArea.x = worldX;
        solidArea.y = worldY;
    }
}