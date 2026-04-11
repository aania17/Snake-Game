package tile;

import java.awt.Graphics2D;   // java.awt.Graphics2D — NOT tile.Graphics2D (delete that file!)
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

/**
 * TileManager — loads tile images and the map layout, then draws the tile grid.
 *
 * FIXES applied:
 * 1. Removed GamePanel dependency entirely.
 *    Old code: TileManager(GamePanel gp) — stored gp and read gp.maxScreenRow,
 *    gp.tileSize etc. on every call. This created a View → tile layer coupling
 *    that broke MVC. Now all dimensions are passed as plain ints in the
 *    constructor, so TileManager has zero knowledge of any View class.
 *
 * 2. Removed import of main.GamePanel (was causing a cross-layer dependency).
 *
 * 3. tile/Graphics2D.java MUST BE DELETED from your project. That empty class
 *    shadows java.awt.Graphics2D. This file correctly imports java.awt.Graphics2D.
 *
 * 4. Map resource path fixed: /maps/map01 → /assets/maps/map01
 *    Your map file lives at src/main/resources/assets/maps/map01, so the
 *    classpath path must include /assets/.
 *
 * 5. Null guards added to getTileImage() — if a tile image fails to load
 *    (wrong path, missing file) it logs a clear error instead of a silent NPE
 *    that only crashes when draw() tries to paint that tile.
 *
 * 6. draw() now uses the injected maxRow/maxCol/tileSize fields instead of
 *    hard-coded literals (was: y<12, x<16 — breaks if grid size ever changes).
 *
 * 7. mapLayout is public so GameController/CollisionController can read it
 *    for collision checks, consistent with how the rest of the codebase works.
 */
public class TileManager {

    private final int maxScreenRow;
    private final int maxScreenCol;
    private final int tileSize;

    public  Tile[]  tile;           // tile type definitions (indexed by map value)
    public  int[][] mapLayout;      // grid of tile indices, [row][col]

    // ── Constructor ───────────────────────────────────────────────────
    /**
     * @param maxScreenRow  number of tile rows on screen
     * @param maxScreenCol  number of tile columns on screen
     * @param tileSize      pixel width/height of one tile
     */
    public TileManager(int maxScreenRow, int maxScreenCol, int tileSize) {
        this.maxScreenRow = maxScreenRow;
        this.maxScreenCol = maxScreenCol;
        this.tileSize     = tileSize;

        tile      = new Tile[10];
        mapLayout = new int[maxScreenRow][maxScreenCol];

        getTileImage();
        loadMap();
    }

    // ── Tile image loading ────────────────────────────────────────────
    private void getTileImage() {
        // Tile indices must match the values used in the map file:
        //   0 = grass   (walkable)
        //   1 = wall    (collision)
        //   2 = water   (collision)
        //   3 = earth   (walkable)
        //   4 = sand    (walkable)
        //   5 = tree    (collision)
        tile[0] = loadTile("/assets/tiles/grass.png", false);
        tile[1] = loadTile("/assets/tiles/wall.png",  true);
        tile[2] = loadTile("/assets/tiles/water.png", true);
        tile[3] = loadTile("/assets/tiles/earth.png", false);
        tile[4] = loadTile("/assets/tiles/sand.png",  false);
        tile[5] = loadTile("/assets/tiles/tree.png",  true);
    }

    /**
     * Loads a single tile image from the classpath.
     * Returns a Tile with a null image (and logs an error) if the file
     * is missing — draw() handles null images gracefully.
     */
    private Tile loadTile(String path, boolean collision) {
        Tile t = new Tile();
        t.collision = collision;
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.err.println("[TileManager] ERROR: tile image not found: " + path
                        + " — check src/main/resources" + path);
            } else {
                t.image = ImageIO.read(is);
            }
        } catch (IOException e) {
            System.err.println("[TileManager] ERROR: failed to read tile image: " + path);
            e.printStackTrace();
        }
        return t;
    }

    // ── Map loading ───────────────────────────────────────────────────
    private void loadMap() {
        // FIXED path: was "/maps/map01" — must be "/assets/maps/map01"
        // because the file lives at src/main/resources/assets/maps/map01
        String mapPath = "/assets/maps/map01";
        try {
            InputStream is = getClass().getResourceAsStream(mapPath);
            if (is == null) {
                System.err.println("[TileManager] ERROR: map file not found: " + mapPath
                        + " — check src/main/resources" + mapPath);
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            for (int row = 0; row < maxScreenRow; row++) {
                String line = br.readLine();
                if (line == null) {
                    System.err.println("[TileManager] ERROR: map file has fewer rows than expected "
                            + "(expected " + maxScreenRow + ", ran out at row " + row + ")");
                    break;
                }
                String[] nums = line.trim().split("\\s+");  // handles multiple spaces/tabs
                for (int col = 0; col < maxScreenCol; col++) {
                    if (col < nums.length) {
                        mapLayout[row][col] = Integer.parseInt(nums[col]);
                    } else {
                        System.err.println("[TileManager] WARNING: row " + row
                                + " has fewer columns than expected — defaulting to 0");
                        mapLayout[row][col] = 0;
                    }
                }
            }
            br.close();

        } catch (Exception e) {
            System.err.println("[TileManager] ERROR: failed to load map: " + mapPath);
            e.printStackTrace();
        }
    }

    // ── Rendering ─────────────────────────────────────────────────────
    /**
     * Draws the full tile grid. Called by GamePanel each frame before
     * drawing objects and the player.
     */
    public void draw(Graphics2D g2) {
        for (int row = 0; row < maxScreenRow; row++) {
            for (int col = 0; col < maxScreenCol; col++) {
                int tileIndex = mapLayout[row][col];

                // Bounds-check the index so a bad map value can't crash draw()
                if (tileIndex < 0 || tileIndex >= tile.length || tile[tileIndex] == null) {
                    continue;
                }

                BufferedImage img = tile[tileIndex].image;
                if (img != null) {
                    g2.drawImage(img, col * tileSize, row * tileSize, tileSize, tileSize, null);
                }
            }
        }
    }
}