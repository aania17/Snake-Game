package ai;

import java.util.*;

/**
 * AStarStrategy — grid-based A* pathfinding.
 *
 * DESIGN PATTERN — Strategy (implements MoveStrategy):
 * This is a concrete strategy. It can be passed to any component
 * that accepts a MoveStrategy, making the AI swappable at runtime.
 *
 * The original AStar.java worked on a flat graph (adjacency matrix).
 * This version works on a 2D grid, which matches the snake game's map.
 */
public class AStarStrategy implements MoveStrategy {

    @Override
    public String getNextMove(int playerX, int playerY,
                              int targetX, int targetY,
                              int[][] grid) {

        int rows = grid.length;
        int cols = grid[0].length;

        // Node: grid position + cost
        record Node(int row, int col, int g, double f, Node parent) {}

        PriorityQueue<Node> open   = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f()));
        boolean[][]         closed = new boolean[rows][cols];

        open.add(new Node(playerY, playerX, 0,
                 heuristic(playerY, playerX, targetY, targetX), null));

        int[][] dr = {{-1,0},{1,0},{0,-1},{0,1}};   // up, down, left, right
        String[] dirNames = {"up","down","left","right"};

        while (!open.isEmpty()) {
            Node current = open.poll();

            if (current.row() == targetY && current.col() == targetX) {
                // Trace back to first step
                Node step = current;
                while (step.parent() != null &&
                       !(step.parent().row() == playerY &&
                         step.parent().col() == playerX)) {
                    step = step.parent();
                }
                // Determine direction from player to first step
                int dRow = step.row() - playerY;
                int dCol = step.col() - playerX;
                if (dRow == -1) return "up";
                if (dRow ==  1) return "down";
                if (dCol == -1) return "left";
                if (dCol ==  1) return "right";
            }

            closed[current.row()][current.col()] = true;

            for (int i = 0; i < 4; i++) {
                int nr = current.row() + dr[i][0];
                int nc = current.col() + dr[i][1];

                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;
                if (closed[nr][nc]) continue;
                if (grid[nr][nc] == 1) continue;  // wall

                int    g = current.g() + 1;
                double f = g + heuristic(nr, nc, targetY, targetX);
                open.add(new Node(nr, nc, g, f, current));
            }
        }

        return "down";  // fallback if no path found
    }

    private double heuristic(int r1, int c1, int r2, int c2) {
        return Math.abs(r1 - r2) + Math.abs(c1 - c2);  // Manhattan distance
    }
}