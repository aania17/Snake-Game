package controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * InputHandler — captures raw keyboard events only.
 *
 * FIXES applied:
 * 1. Direction locking: in a snake-style game only one direction should be
 *    active at a time. Previously, holding two keys simultaneously (e.g. UP
 *    and RIGHT) would move the player diagonally because GameController
 *    checked all four flags independently. InputHandler now tracks a single
 *    lastDirection so GameController can call getDirection() and get exactly
 *    one value per tick. The raw booleans are kept for backward compatibility
 *    (GameController still reads them for menu navigation).
 *
 * 2. keyTyped() is a no-op — no change needed, kept for interface compliance.
 */
public class InputHandler implements KeyListener {

    // Raw booleans — still used by GameController for menu up/down/enter
    public boolean up, down, left, right;
    public boolean enter;

    // Last movement direction pressed — used by GameController during gameplay
    // to ensure only one axis moves at a time (no diagonal movement).
    private String lastDirection = "down";

    /** Returns the most recently pressed movement direction. */
    public String getDirection() {
        return lastDirection;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
            up            = true;
            lastDirection = "up";
        }
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            down          = true;
            lastDirection = "down";
        }
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
            left          = true;
            lastDirection = "left";
        }
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
            right         = true;
            lastDirection = "right";
        }
        if (code == KeyEvent.VK_ENTER) {
            enter = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP)    up    = false;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN)  down  = false;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT)  left  = false;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) right = false;
        if (code == KeyEvent.VK_ENTER)                           enter = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}