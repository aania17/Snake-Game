package controller;

import java.awt.Rectangle;

import model.entity.Entity;
import model.object.SuperObject;

/**
 * CollisionController — pure collision logic, no rendering, no input.
 *
 * FIXES applied:
 * 1. Object collision rectangle was built using obj.solidArea.x/y as offsets
 *    on top of obj.worldX/worldY — but SuperObject.solidArea was already being
 *    set to the absolute world position in generate(). This caused a double-
 *    offset bug where the collision box was placed far from the visible sprite.
 *    Fixed: object rectangle is built from worldX/worldY only, with solidArea
 *    width/height for size. solidArea.x/y offsets are only meaningful for the
 *    player entity which has a deliberate inset hit-box.
 *
 * 2. checkTileCollision() is unchanged — it was already correct.
 *    It is still included so the full controller is self-contained and ready
 *    for use if tile-based walls are added to the map later.
 */
public class CollisionController {

    /**
     * Checks whether the entity's solid area overlaps any object.
     *
     * @return index of the first hit object, or -1 if none.
     */
    public int checkObjectCollision(Entity entity, SuperObject[] objects) {
        // Build the player's actual collision rectangle (with its solidArea inset)
        Rectangle entityRect = new Rectangle(
            entity.x + entity.solidArea.x,
            entity.y + entity.solidArea.y,
            entity.solidArea.width,
            entity.solidArea.height
        );

        for (int i = 0; i < objects.length; i++) {
            SuperObject obj = objects[i];
            if (obj == null) continue;

            // FIXED: object rectangle starts at worldX/worldY (pixel position
            // of the tile the object was spawned on). solidArea.x/y offsets are
            // NOT applied here because SuperObject doesn't have an inset hit-box
            // — its solidArea covers the full tile. Applying the offset a second
            // time was pushing the detection rectangle off-screen.
            Rectangle objRect = new Rectangle(
                obj.worldX,
                obj.worldY,
                obj.solidArea.width,
                obj.solidArea.height
            );

            if (entityRect.intersects(objRect)) {
                entity.collisionOn = obj.collision;
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns true if the entity is out of bounds or standing on a wall tile.
     * Wall tiles have value 1 in mapLayout. Value 0 = walkable.
     */
    public boolean checkTileCollision(Entity entity, int[][] mapLayout, int tileSize) {
        int left   = entity.x + entity.solidArea.x;
        int right  = entity.x + entity.solidArea.x + entity.solidArea.width  - 1;
        int top    = entity.y + entity.solidArea.y;
        int bottom = entity.y + entity.solidArea.y + entity.solidArea.height - 1;

        int leftCol   = left   / tileSize;
        int rightCol  = right  / tileSize;
        int topRow    = top    / tileSize;
        int bottomRow = bottom / tileSize;

        // Out of bounds check
        if (topRow    < 0 || bottomRow >= mapLayout.length    ||
            leftCol   < 0 || rightCol  >= mapLayout[0].length) {
            return true;
        }

        // Wall tile check (all four corners of the solid area)
        return mapLayout[topRow][leftCol]    == 1 ||
               mapLayout[topRow][rightCol]   == 1 ||
               mapLayout[bottomRow][leftCol] == 1 ||
               mapLayout[bottomRow][rightCol] == 1;
    }
}