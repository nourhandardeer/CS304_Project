package game;

import com.sun.opengl.util.GLUT;
import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import javax.swing.*;
import java.util.List;

public class HighScoresGL implements GLEventListener {

    private GLU glu = new GLU();
    private GLUT glut = new GLUT();
    private List<String> scores;

    public HighScoresGL(List<String> scores) {
        this.scores = scores;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glClearColor(0f, 0f, 0f, 1f); // خلفية سوداء
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluOrtho2D(0, 400, 0, 500); // أبعاد نافذة High Scores
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);

        gl.glColor3f(1f, 1f, 0f); // اللون أصفر
        gl.glLoadIdentity();

        int y = 450;
        for (String s : scores) {
            gl.glRasterPos2i(50, y); // مكان النص
            glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, s);
            y -= 30; // مسافة بين كل سطر وسطر
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
    @Override
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}

    // لتشغيل الـ High Scores
    public static void showHighScores(List<String> scores) {
        JFrame frame = new JFrame("High Scores");
        frame.setSize(400, 500);
        frame.setLocationRelativeTo(null);

        GLCanvas canvas = new GLCanvas();
        canvas.addGLEventListener(new HighScoresGL(scores));

        frame.getContentPane().add(canvas);
        frame.setVisible(true);
    }
}
