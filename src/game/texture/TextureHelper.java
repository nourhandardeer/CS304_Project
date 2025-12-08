// Placeholder texture helper (JOGL-specific code can be added here later)
package game.texture;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

import javax.media.opengl.GL;
import java.io.File;
import java.io.IOException;

public class TextureHelper {

    public static Texture loadTexture(GL gl, String filePath) {
        try {
            File file = new File(filePath);
            Texture texture = TextureIO.newTexture(file, true);
            return texture;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void bindTexture(GL gl, Texture texture) {
        if (texture != null) {
            texture.enable();
            texture.bind();
        }
    }
}

