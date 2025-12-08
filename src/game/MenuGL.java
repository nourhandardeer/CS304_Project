package game;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.j2d.TextRenderer;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;
import com.sun.opengl.util.GLUT;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MenuGL implements GLEventListener, MouseListener, KeyListener {
    private static final int W = 1000;
    private static final int H = 700;

    // GL helpers
    private GLU glu = new GLU();
    private GLUT glut = new GLUT();

    // Resources
    private Texture backgroundTex;
    private TextRenderer textRenderer;


    // Animation objects
    private final List<Star> stars = new ArrayList<>();
    private final List<Bubble> bubbles = new ArrayList<>();

    // Buttons and UI areas
    private final List<Button> buttons = new ArrayList<>();
    private Rect nameBox;
    private Rect levelLeftBox, levelRightBox;

    // state
    private String playerName = "Player";
    private boolean nameActive = false;
    private int level = 1;
    private JFrame frame;
    private Animator animator;

    // Audio
    private static Clip bgClip;

    // Hover tracking
    private int mouseX = 0, mouseY = 0;

    public MenuGL() {
        initObjects();
    }

    private void initObjects() {
        // stars
        for (int i = 0; i < 50; i++) {
            float x = (float) (Math.random() * W);
            float y = (float) (Math.random() * H);
            float s = 0.5f + (float) Math.random() * 2f;
            stars.add(new Star(x, y, s));
        }
        // bubbles (start below screen so they rise)
        for (int i = 0; i < 20; i++) {
            float x = (float) (Math.random() * W);
            float y = (float) (-Math.random() * 400);
            float size = 8 + (float) (Math.random() * 18);
            float speed = 0.3f + (float) (Math.random() * 1.5f);
            bubbles.add(new Bubble(x, y, size, speed));
        }

        // Buttons (positions using screen coords)
        buttons.clear();
        buttons.add(new Button("Start", 420, 260, 160, 46));
        buttons.add(new Button("High Scores", 420, 200, 160, 40));
        buttons.add(new Button("Instructions", 420, 140, 160, 40));

        // name input box
        nameBox = new Rect(400, 380, 200, 50);

        // level boxes
        levelLeftBox = new Rect(400, 320, 30, 35);
        levelRightBox = new Rect(580, 320, 30, 35);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();

        // clear color black
        gl.glClearColor(0f, 0f, 0f, 1f);

        // orthographic 2D
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluOrtho2D(0, W, 0, H);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        textRenderer = new TextRenderer(new Font("Helvetica", Font.BOLD, 30));

        // blending
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        // textures
        try {
            InputStream s = getClass().getResourceAsStream("/assets/background.png");
            if (s != null) {
                backgroundTex = TextureIO.newTexture(s, false, "png");
            } else {
                System.err.println("Warning: /assets/background.png not found in resources.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // start background music (non-blocking)
        playBackgroundMusic("/assets/sounds/background.wav");

    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();

        // clear
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);

        // reset modelview
        gl.glLoadIdentity();

        // draw background
        drawBackground(gl);

        // update & draw stars & bubbles
        updateAndDrawStars(gl);
        updateAndDrawBubbles(gl);

        // draw UI boxes and buttons
        drawUI(gl);

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        // keep logical coords constant (W x H). We don't change viewport scaling here.
        GL gl = drawable.getGL();
        gl.glViewport(0, 0, w, h);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluOrtho2D(0, W, 0, H);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void displayChanged(GLAutoDrawable glAutoDrawable, boolean b, boolean b1) {

    }
    private void drawBackground(GL gl) {
        if (backgroundTex == null) {
            // fallback: plain dark gradient
            gl.glBegin(GL.GL_QUADS);
            gl.glColor3f(0.04f, 0.06f, 0.1f);
            gl.glVertex2f(0, 0);
            gl.glVertex2f(W, 0);
            gl.glColor3f(0.08f, 0.12f, 0.18f);
            gl.glVertex2f(W, H);
            gl.glVertex2f(0, H);
            gl.glEnd();
            gl.glColor3f(1,1,1);
            return;
        }

        backgroundTex.enable();
        backgroundTex.bind();

        gl.glColor3f(1f, 1f, 1f);
        gl.glBegin(GL.GL_QUADS);
        // note: texture coordinates may be top-bottom depending on image; this maps correctly in most cases
        gl.glTexCoord2f(0f, 1f); gl.glVertex2f(0, 0);
        gl.glTexCoord2f(1f, 1f); gl.glVertex2f(W, 0);
        gl.glTexCoord2f(1f, 0f); gl.glVertex2f(W, H);
        gl.glTexCoord2f(0f, 0f); gl.glVertex2f(0, H);
        gl.glEnd();

        backgroundTex.disable();
    }

    private void updateAndDrawStars(GL gl) {
        gl.glPointSize(3f);
        gl.glBegin(GL.GL_POINTS);
        gl.glColor3f(1f, 1f, 1f);
        for (Star s : stars) {
            s.y -= s.speed;
            if (s.y < -2) {
                s.y = H + 2;
                s.x = (float)(Math.random() * W);
            }
            gl.glVertex2f(s.x, s.y);
        }
        gl.glEnd();
    }

    private void updateAndDrawBubbles(GL gl) {
        gl.glColor4f(0.6f, 0.8f, 1f, 0.45f);
        for (Bubble b : bubbles) {
            b.y += b.speed;
            if (b.y - b.size > H + 10) {
                b.y = -20 - (float)(Math.random() * 200);
                b.x = (float)(Math.random() * W);
            }
            drawCircle(gl, b.x, b.y, b.size);
        }
        gl.glColor4f(1f,1f,1f,1f);
    }

    private void drawCircle(GL gl, float cx, float cy, float r) {
        int steps = 24;
        gl.glBegin(GL.GL_TRIANGLE_FAN);
        gl.glVertex2f(cx, cy);
        for (int i = 0; i <= steps; i++) {
            double ang = 2 * Math.PI * i / steps;
            gl.glVertex2f(cx + (float)Math.cos(ang) * r, cy + (float)Math.sin(ang) * r);
        }
        gl.glEnd();
    }

    private void drawRect(GL gl, Rect r, float[] color) {
        gl.glColor4f(color[0], color[1], color[2], color[3]);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(r.x, r.y);
        gl.glVertex2f(r.x + r.w, r.y);
        gl.glVertex2f(r.x + r.w, r.y + r.h);
        gl.glVertex2f(r.x, r.y + r.h);
        gl.glEnd();
    }

    private void drawUI(GL gl) {
        //Game name
        textRenderer.beginRendering(1000, 700); // حجم النافذة
        textRenderer.setColor(1f, 1f, 1f, 1f); // اللون أبيض
        textRenderer.draw("Spot The Difference", 350, 520); // الإحداثيات
        textRenderer.endRendering();

        // Draw name label
        drawTextAt(gl, "Player Name:", 270, 390, GLUT.BITMAP_HELVETICA_18);

        // name box background
        float[] boxColor = nameActive ? new float[]{0.15f, 0.15f, 0.25f, 0.9f} : new float[]{0f,0f,0f,0.45f};
        drawRect(gl, nameBox, boxColor);

        // draw current name inside box (left aligned)
        drawTextAt(gl, playerName + (nameActive ? "_" : ""), nameBox.x + 6, nameBox.y + 20, GLUT.BITMAP_HELVETICA_18);

        // level label and arrows
        drawTextAt(gl, "Select Level:", 270, 330, GLUT.BITMAP_HELVETICA_18);

        // left arrow box
        float[] arrowColor = isHover(levelLeftBox) ? new float[]{0.7f,0.7f,0.9f,0.9f} : new float[]{0.2f,0.2f,0.3f,0.7f};
        drawRect(gl, levelLeftBox, arrowColor);
        drawTextAt(gl, "<", levelLeftBox.x + 8, levelLeftBox.y + 20, GLUT.BITMAP_HELVETICA_18);

        // level value
        drawTextAt(gl, "Level " + level, 470, 330, GLUT.BITMAP_HELVETICA_18);

        // right arrow box
        float[] arrowColorR = isHover(levelRightBox) ? new float[]{0.7f,0.7f,0.9f,0.9f} : new float[]{0.2f,0.2f,0.3f,0.7f};
        drawRect(gl, levelRightBox, arrowColorR);
        drawTextAt(gl, ">", levelRightBox.x + 8, levelRightBox.y + 20, GLUT.BITMAP_HELVETICA_18);

        // Buttons
        for (Button b : buttons) {
            float[] c = b.isHover ? new float[]{0.2f,0.6f,0.9f,0.95f} : new float[]{0.1f,0.2f,0.35f,0.8f};
            drawRect(gl, b.rect, c);
            // text centered
            int textW = glutBitmapWidth(glut, GLUT.BITMAP_HELVETICA_18, b.label);
            int tx = b.rect.x + (b.rect.w - textW) / 2;
            drawTextAt(gl, b.label, tx, b.rect.y + (b.rect.h / 2) + 6, GLUT.BITMAP_HELVETICA_18);
        }
    }

    // Helper: draw text at screen coords (x,y) where origin bottom-left
    private void drawTextAt(GL gl, String text, int x, int y, int font) {
        gl.glColor3f(1f, 1f, 1f);
        gl.glRasterPos2i(x, y);
        glut.glutBitmapString(font, text);
    }
    private void drawText2(GL gl, GLUT glut, String text, float x, float y, float targetSize) {
        gl.glPushMatrix();

        // حرك النص لمكانه
        gl.glTranslatef(x, y, 0);

        // المقياس للتحكم في الحجم
        float scale = targetSize / 100f; // 100f هو تقريبا حجم افتراضي لـ STROKE_ROMAN
        gl.glScalef(scale, scale, 1f);

        // ارسم النص
        glut.glutStrokeString(GLUT.STROKE_ROMAN, text);

        gl.glPopMatrix();
    }
    private int glutBitmapWidth(GLUT glut, int font, String s) {
        int w = 0;
        for (char c : s.toCharArray()) {
            w += glut.glutBitmapWidth(font, c);
        }
        return w;
    }

    private boolean isHover(Rect r) {
        return mouseX >= r.x && mouseX <= r.x + r.w && mouseY >= r.y && mouseY <= r.y + r.h;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // convert window coords (y inverted)
        int wx = e.getX();
        int wy = e.getY();
        // need mapping from component height to logical H
        int ch = ((GLCanvas) e.getComponent()).getHeight();
        int logicalY = ch - wy; // convert to bottom-left origin
        mouseX = wx;
        mouseY = logicalY;

        // check name box
        if (pointInRect(mouseX, mouseY, nameBox)) {
            nameActive = true;
            return;
        } else {
            nameActive = false;
        }

        // level arrows
        if (pointInRect(mouseX, mouseY, levelLeftBox)) {
            level = Math.max(1, level - 1);
            return;
        }
        if (pointInRect(mouseX, mouseY, levelRightBox)) {
            level = Math.min(3, level + 1);
            return;
        }

        // buttons
        for (Button b : buttons) {
            if (pointInRect(mouseX, mouseY, b.rect)) {
                handleButtonClick(b.label);
                return;
            }
        }
    }

    private void handleButtonClick(String label) {
        switch (label) {
            case "Start":
                // launch the game (dispose menu frame)
                if (frame != null) {
                    frame.dispose();
                }
                launchGame(playerName, level);
                break;
            case "High Scores":
                // call existing HighScoresGL static method if present
                try {
                    HighScoresGL.showHighScores(HighScores.loadTop());
                } catch (Throwable t) {
                    System.err.println("HighScoresGL or HighScores not available.");
                }
                break;
            case "Instructions":
                try {
                    InstructionsGL.showInstructions();
                } catch (Throwable t) {
                    System.err.println("InstructionsGL not available.");
                }
                break;
        }
    }
    private void launchGame(String playerName, int level) {
        // This assumes you have a SpotTheDifference class that implements GLEventListener
        try {
            JFrame gframe = new JFrame("Spot The Difference - " + playerName);
            gframe.setSize(W, H);
            gframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gframe.setLocationRelativeTo(null);

            GLCanvas canvas = new GLCanvas();
            SpotTheDifference game = new SpotTheDifference(playerName, level, gframe);
            canvas.addGLEventListener(game);
            canvas.addMouseListener(game);

            gframe.add(canvas);
            Animator a = new Animator(canvas);
            a.start();
            gframe.setVisible(true);
            canvas.requestFocus();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
    private boolean pointInRect(int px, int py, Rect r) {
        return px >= r.x && px <= r.x + r.w && py >= r.y && py <= r.y + r.h;
    }
    @Override
    public void keyTyped(KeyEvent e) {
        if (!nameActive) return;

        char c = e.getKeyChar();
        if (c == KeyEvent.VK_BACK_SPACE) {
            if (playerName.length() > 0) playerName = playerName.substring(0, playerName.length() - 1);
        } else if (c == '\n' || c == '\r') {
            nameActive = false; // confirm
        } else if (Character.isDefined(c) && !Character.isISOControl(c)) {
            // limit length
            if (playerName.length() < 20) playerName += c;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
    private void playBackgroundMusic(String path) {
        try {
            if (bgClip != null && bgClip.isRunning()) {
                bgClip.stop();
                bgClip.close();
            }
            InputStream s = getClass().getResourceAsStream(path);
            if (s == null) {
                System.err.println("Sound resource not found: " + path);
                return;
            }
            // need to use AudioSystem on a stream supporting mark/reset - wrap if necessary
            AudioInputStream ais = AudioSystem.getAudioInputStream(getClass().getResource(path));
            bgClip = AudioSystem.getClip();
            bgClip.open(ais);
            bgClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showMenuGL() {
        JFrame frame = new JFrame("Spot The Difference");
        frame.setSize(W, H);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        GLCanvas canvas = new GLCanvas();
        canvas.setSize(W, H);

        MenuGL menu = new MenuGL();
        menu.frame = frame;

        canvas.addGLEventListener(menu);
        canvas.addMouseListener(menu);

        // add mouse motion listener to update hover states
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int wx = e.getX();
                int wy = e.getY();
                int ch = canvas.getHeight();
                int logicalY = ch - wy;
                menu.mouseX = wx;
                menu.mouseY = logicalY;
                // update button hover states
                for (Button b : menu.buttons) b.isHover = menu.pointInRect(menu.mouseX, menu.mouseY, b.rect);
                menuNameHoverUpdate(menu);
            }
        });

        // key listener for typing
        canvas.addKeyListener(menu);

        // Animator
        Animator animator = new Animator(canvas);
        menu.animator = animator;
        animator.start();

        frame.add(canvas);
        frame.setVisible(true);
        canvas.requestFocus();

    }
    private static void menuNameHoverUpdate(MenuGL menu) {

    }
    private static class Rect {
        int x, y, w, h;
        Rect(int x, int y, int w, int h) { this.x = x; this.y = y; this.w = w; this.h = h; }
    }

    private static class Button {
        String label;
        MenuGL.Rect rect;
        boolean isHover = false;
        Button(String label, int x, int y, int w, int h) {
            this.label = label;
            this.rect = new MenuGL.Rect(x, y, w, h);
        }
    }

    private static class Star { float x,y,speed; Star(float x,float y,float s){this.x=x;this.y=y;this.speed=s;} }
    private static class Bubble { float x,y,size,speed; Bubble(float x,float y,float r,float s){this.x=x;this.y=y;this.size=r;this.speed=s;} }

    // -------------------- main for quick launch --------------------
    public static void main(String[] args) {
        // convenience: call static show
        SwingUtilities.invokeLater(MenuGL::showMenuGL);
    }

}
