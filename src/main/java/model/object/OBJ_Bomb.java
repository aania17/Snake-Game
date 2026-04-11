package model.object;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * OBJ_Bomb — hazard object that ends the game on contact.
 *
 * FIXED (MVC refactor):
 * - Package changed from 'object' → 'model.object'
 * - Uses getResourceAsStream with null-check so a missing
 *   asset fails with a clear message instead of a silent NPE.
 */
public class OBJ_Bomb extends SuperObject {

    public OBJ_Bomb() {
        name      = "Bomb";
        collision = true;   // touching this triggers game over

        try {
            InputStream is = getClass().getResourceAsStream("/assets/objects/bomb.png");
            if (is == null) {
                System.err.println("[OBJ_Bomb] ERROR: /assets/objects/bomb.png not found on classpath.");
            } else {
                image = ImageIO.read(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}