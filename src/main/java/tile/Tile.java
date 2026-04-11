package tile;

import java.awt.image.BufferedImage;

/**
 * Tile — data holder for a single tile type.
 *
 * Pure model — no rendering, no logic.
 * TileManager holds an array of these and draws them via Graphics2D.
 */
public class Tile {
    public BufferedImage image;
    public boolean collision = false;
}