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
    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

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
}
