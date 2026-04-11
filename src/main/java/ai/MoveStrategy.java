package ai;

/**
 * MoveStrategy — interface for pluggable movement strategies.
 *
 * DESIGN PATTERN — Strategy:
 * Any class that implements MoveStrategy can be swapped in as the
 * movement algorithm — human input, A* pathfinding, random walk, etc.
 * GameController holds a MoveStrategy reference, not a concrete class.
 * This satisfies the Open/Closed principle: new strategies can be added
 * without changing GameController.
 *
 * Currently implemented strategies:
 *   - AStarStrategy  (AI — finds shortest path to food)
 *   - (HumanInput is handled by InputHandler, not this interface)
 */
public interface MoveStrategy {
    /**
     * @param playerX  current player X (grid units)
     * @param playerY  current player Y (grid units)
     * @param targetX  target X (e.g. food location, grid units)
     * @param targetY  target Y
     * @param grid     walkable map (0 = free, 1 = wall)
     * @return one of: "up", "down", "left", "right"
     */
    String getNextMove(int playerX, int playerY,
                       int targetX, int targetY,
                       int[][] grid);
}