package game;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import com.sun.opengl.util.GLUT;
public class GameController {

    private String playerName;
    private int score;
    private int level;
    private int lives;
    private int timer; // seconds

    private boolean paused = false;
    private boolean backToMenu = false;

    private GLUT glut = new GLUT();

    public GameController(String playerName, int level) {
        this.playerName = playerName;
        this.level = level;
        this.score = 0;
        this.lives = 3;      // 3 lives per level
        this.timer = 60;     // 60 sec per level
    }
    public void updateScore(int points) { score += points; }
    public void loseLife() { lives--; }
    public void resetTimer() { timer = 60; }
    public void decrementTimer() { if(!paused && timer>0) timer--; }
    public void togglePause() { paused = !paused; }

    public boolean isPaused() { return paused; }
    public boolean isBackToMenu() { return backToMenu; }
    public boolean isGameOver() { return lives <= 0 || timer <= 0; }

    public void nextLevel() {
        level++;
        lives = 3;
        timer = 60;

    }
    public void draw(GL gl, GLU glu, int windowWidth, int windowHeight) {
        // set projection for 2D overlay
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D(0, windowWidth, windowHeight, 0); // top-left origin
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glColor3f(0f, 0f, 0f); // white text

        String status = String.format("Player: %s   Score: %d   Lives: %d   Timer: %d   Level: %d",
                playerName, score, lives, timer, level);

        // draw text
        gl.glRasterPos2f(20, 30);
        for (char c : status.toCharArray()) {
            glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, c);
        }

}}
