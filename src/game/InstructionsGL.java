package game;

import com.sun.opengl.util.GLUT;
import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import javax.swing.*;

public class InstructionsGL implements GLEventListener {

    private GLU glu = new GLU();
    private GLUT glut = new GLUT();
    private String[] instructions = {
            "Welcome to Spot The Difference!",
            "",
            "1. Click on differences between the left and right images.",
            "2. You have limited lives and time.",
            "3. Find all differences to complete the level.",
            "4. Use pause button to pause the game.",
            "",
            "Good luck!"
    };

    @Override
    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glClearColor(0f, 0f, 0f, 1f); // خلفية سوداء
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluOrtho2D(0, 600, 0, 400); // أبعاد نافذة التعليمات
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);

        gl.glColor3f(1f, 1f, 1f); // اللون أبيض
        gl.glLoadIdentity();

        int y = 350;
        for (String line : instructions) {
            gl.glRasterPos2i(20, y);
            glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, line);
            y -= 30; // مسافة بين السطور
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    }

    @Override
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }
}