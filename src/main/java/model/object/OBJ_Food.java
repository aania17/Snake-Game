package model.object;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * OBJ_Food — collectible food object.
 *
 * FIXED (MVC refactor):
 * - Package changed from 'object' → 'model.object'
 * - Uses getResourceAsStream with null-check so a missing
 *   asset fails with a clear message instead of a silent NPE.
 */
public class OBJ_Food extends SuperObject {

    public OBJ_Food() {
        name  = "Food";
        value = 10;   // points awarded on collection

        try {
            InputStream is = getClass().getResourceAsStream("/assets/objects/food.png");
            if (is == null) {
                System.err.println("[OBJ_Food] ERROR: /assets/objects/food.png not found on classpath.");
            } else {
                image = ImageIO.read(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}