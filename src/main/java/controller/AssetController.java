package controller;

import model.GameState;
import model.object.OBJ_Bomb;
import model.object.OBJ_Food;

/**
 * AssetController — responsible for spawning game objects into GameState.
 *
 * FIXED:
 * - Imports now point to model.object.* (not the old object.* package)
 * - generate() call matches the updated SuperObject.generate() signature:
 *     (int maxRow, int maxCol, int[][] mapLayout, Entity entity, int tileSize)
 *   No GamePanel is passed — controller stays out of the View layer.
 *
 * DESIGN PATTERN — Factory (simple):
 * Centralises object creation so no other class calls new OBJ_Food() directly.
 */
public class AssetController {

    public void genFood(GameState state, int tileSize,
                        int maxRow, int maxCol, int[][] mapLayout) {
        state.objects[0] = new OBJ_Food();
        state.objects[0].generate(maxRow, maxCol, mapLayout, state.player, tileSize);
    }

    public void genBomb(GameState state, int id, int tileSize,
                        int maxRow, int maxCol, int[][] mapLayout) {
        // slot 0 is always food; bombs occupy slots 1..9
        int slot = Math.max(1, Math.min(id, state.objects.length - 1));
        state.objects[slot] = new OBJ_Bomb();
        state.objects[slot].generate(maxRow, maxCol, mapLayout, state.player, tileSize);
    }
}