package game;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import com.sun.opengl.util.GLUT;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;
import javax.sound.sampled.*;

import com.sun.opengl.util.j2d.TextRenderer;
import game.texture.TextureReader;

public class SpotTheDifference implements GLEventListener, MouseListener {


    private GLU glu = new GLU();
    private GLUT glut = new GLUT();
    private GameModel model;
    private GameController controller;
    private int lastLevel = -1;


    private Clip correctClip, wrongClip, winClip, loseClip;
    private TextRenderer messageRenderer;

    private List<Point> foundClickPositions = new ArrayList<>();


    // Textures
    private String[] textureNames = {
            "level1_left.png","level1_right.png",
            "level2_left.png","level2_right.png",
            "level3_left.png","level3_right.png",
            "background.png"
    };
    private TextureReader.Texture[] texture = new TextureReader.Texture[textureNames.length];
    private int[] textures = new int[textureNames.length];

    // For stars and bubbles
    private List<Star> stars = new ArrayList<>();
    private List<Bubble> bubbles = new ArrayList<>();

    private boolean gameWon = false;
    private boolean gameLost = false;
    private boolean paused = false;
    private JFrame parentFrame;

    public SpotTheDifference(String playerName, int level,JFrame frame) {
        this.parentFrame = frame;
        this.controller = new GameController(playerName, level);
        this.model = new GameModel(playerName, level, controller);

        initStarsBubbles();
        loadSounds();
    }

    private void initStarsBubbles() {
        stars.clear();
        bubbles.clear();
        // 50 stars
        for(int i=0;i<50;i++) {
            float x = (float)(Math.random() * 1000);
            float y = (float)(Math.random() * 700);
            float speed = 0.5f + (float)Math.random() * 2f;
            stars.add(new Star(x, y, speed));
        }
        // 20 bubbles
        for(int i=0;i<20;i++) {
            float x = (float)(Math.random() * 1000);
            float y = (float)(-Math.random() * 400);
            float size = 8 + (float)(Math.random() * 18);
            float speed = 0.3f + (float)(Math.random() * 1.5f);
            bubbles.add(new Bubble(x, y, size, speed));
        }
    }


    private void loadSounds() {
        try {
            correctClip = loadClip("/assets/sounds/correct.wav");
            wrongClip = loadClip("/assets/sounds/out.wav");
            winClip = loadClip("/assets/sounds/win.wav");
            loseClip = loadClip("/assets/sounds/lose.wav");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Clip loadClip(String path) throws Exception {
        URL url = getClass().getResource(path);
        AudioInputStream ais = AudioSystem.getAudioInputStream(url);
        Clip clip = AudioSystem.getClip();
        clip.open(ais);
        return clip;
    }



    @Override
    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glClearColor(0f,0f,0f,1f);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluOrtho2D(0,1000,0,700);
        gl.glMatrixMode(GL.GL_MODELVIEW);

        messageRenderer = new TextRenderer(new Font("Helvetica", Font.BOLD, 28));


        try {
            for(int i=0;i<textureNames.length;i++) {
                texture[i] = TextureReader.readTexture("assets/"+textureNames[i], true);
            }
            gl.glGenTextures(textureNames.length,textures,0);
            for(int i=0;i<textureNames.length;i++) {
                gl.glBindTexture(GL.GL_TEXTURE_2D,textures[i]);
                gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MIN_FILTER,GL.GL_LINEAR);
                gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MAG_FILTER,GL.GL_LINEAR);
                ByteBuffer pixels = texture[i].getPixels();
                gl.glTexImage2D(GL.GL_TEXTURE_2D,0,GL.GL_RGBA,texture[i].getWidth(),texture[i].getHeight(),0,GL.GL_RGBA,GL.GL_UNSIGNED_BYTE,pixels);
            }
        } catch (Exception e) { e.printStackTrace(); }


    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        if (model.level != lastLevel) {
            foundClickPositions.clear();
            lastLevel = model.level;
        }


        gl.glBindTexture(GL.GL_TEXTURE_2D, 0); // unbind any texture
        gl.glColor3f(0.5f, 0.5f, 0.5f); // gray background
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, 0);
        gl.glVertex2f(1000, 0);
        gl.glVertex2f(1000, 700);
        gl.glVertex2f(0, 700);
        gl.glEnd();


        // Draw Stars
        gl.glPointSize(3f);
        gl.glBegin(GL.GL_POINTS);
        gl.glColor3f(1f, 1f, 1f); // white stars
        for (Star s : stars) {
            s.update();
            gl.glVertex2f(s.x, s.y);
        }
        gl.glEnd();

// Draw Bubbles
        gl.glColor4f(0.8f, 0.8f, 0.8f, 0.45f); // light gray semi-transparent
        for (Bubble b : bubbles) {
            b.update();
            drawCircle2(gl, b.x, b.y, b.size);
        }
        gl.glColor4f(1f,1f,1f,1f); // reset color



        if(!gameWon){
            if (model.paused) {
                gl.glColor3f(1f,1f,0f);
                gl.glRasterPos2f(450,350);
                glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18,"PAUSED");
            }
            else {
                int margin = 100;  // المسافة من كل جهة
                int space = 30;    // المسافة بين الصورتين
                int imgW = (1000 - 2*margin - space)/2;
                int imgH = 700 - 2*margin;

                int leftIndex=(model.level-1)*2;
                int rightIndex=leftIndex+1;

                gl.glBindTexture(GL.GL_TEXTURE_2D,textures[leftIndex]); drawQuad(gl,margin,margin,margin+imgW,margin+imgH);
                gl.glBindTexture(GL.GL_TEXTURE_2D,textures[rightIndex]); drawQuad(gl,margin+imgW+space,margin,margin+imgW+space+imgW,margin+imgH);

                gl.glColor3f(0f,1f,0f);
                gl.glColor3f(0f,1f,0f);
                for(Point p : foundClickPositions) {
                    drawCircle(gl, p.x, p.y, 12);
                }



            }
            // بعد رسم الصور والنقاط
            if(model.isShowMessage()){
                String msg = model.getMessageText();
                messageRenderer.beginRendering(1000, 700); // حجم النافذة
                if(msg.contains("LOSE") || msg.contains("Game Over")) {
                    messageRenderer.setColor(1f, 0f, 0f, 1f); // أحمر للخسارة
                } else {
                    messageRenderer.setColor(1f, 1f, 0f, 1f); // أصفر للنجاح
                }
                // حساب منتصف النص تقريبا
                int textWidth = messageRenderer.getBounds(msg).getBounds().width;
                int x = (1000 - textWidth)/2;
                int y = 350; // نفس الارتفاع القديم
                messageRenderer.draw(msg, x, y);
                messageRenderer.endRendering();
            }


        }


        controller.setScore(model.score);
        controller.setLives(model.lives);
        controller.setTimer(model.timeLeft);
        controller.setLevel(model.level);
        controller.draw(gl, glu, 1000, 700);

    }

    private void drawQuad(GL gl,float x1,float y1,float x2,float y2){
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0f,0f); gl.glVertex2f(x1,y1);
        gl.glTexCoord2f(1f,0f); gl.glVertex2f(x2,y1);
        gl.glTexCoord2f(1f,1f); gl.glVertex2f(x2,y2);
        gl.glTexCoord2f(0f,1f); gl.glVertex2f(x1,y2);
        gl.glEnd();
    }

    private void drawCircle(GL gl,float cx,float cy,float r){
        gl.glLineWidth(4.0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        for(int i=0;i<360;i++){
            double theta=Math.toRadians(i);
            float x=(float)(r*Math.cos(theta))+cx;
            float y=(float)(r*Math.sin(theta))+cy;
            gl.glVertex2f(x,y);
        }
        gl.glEnd();
    }
    private void drawCircle2(GL gl, float cx, float cy, float r) {
        int steps = 24;
        gl.glBegin(GL.GL_TRIANGLE_FAN);
        gl.glVertex2f(cx, cy);
        for(int i=0;i<=steps;i++){
            double angle = 2*Math.PI*i/steps;
            gl.glVertex2f(cx + (float)Math.cos(angle)*r, cy + (float)Math.sin(angle)*r);
        }
        gl.glEnd();
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) {

    }

    @Override
    public void displayChanged(GLAutoDrawable glAutoDrawable, boolean b, boolean b1) {

    }
    @Override
    public void mouseClicked(MouseEvent e){
        int mx=e.getX(),my=e.getY();




        // First let controller handle clicks on Pause/Menu
        controller.handleClick(mx, my, 1000, 700);
        // update paused state from controller
        if (controller.isPaused()) {
            model.pauseGame();
        } else {
            model.resumeGame();
        }

        paused = model.paused;

        if (controller.isBackToMenu()) {
            parentFrame.dispose();   // close the game window only
            MenuGL.showMenuGL(); // open menu again
            return;
        }

        if(paused||gameWon||gameLost) return;

        int margin = 100;  // المسافة من كل جهة
        int space = 30;    // المسافة بين الصورتين
        int imgW = (1000 - 2*margin - space)/2;
        int imgH = 700 - 2*margin;

        int leftX1=margin,leftY1=margin,rightX1=margin+imgW+space,rightY1=margin;

        boolean insideLeft=mx>=leftX1&&mx<=leftX1+imgW&&my>=leftY1&&my<=leftY1+imgH;
        boolean insideRight=mx>=rightX1&&mx<=rightX1+imgW&&my>=rightY1&&my<=rightY1+imgH;
        if(!insideLeft&&!insideRight) return;

        int clickX,clickY;
        if(mx <= leftX1 + imgW){
            // Left image
            clickX = (int)((mx - leftX1) * 800f / imgW);
            clickY = (int)((my - leftY1) * 400f / imgH);
        } else {
            // Right image
            clickX = (int)((mx - rightX1) * 800f / imgW + 400);
            clickY = (int)((my - rightY1) * 400f / imgH);
        }


        System.out.println("Original X = " + clickX + " | Original Y = " + clickY);
        boolean found = model.checkClick(clickX, clickY);


        if(found){ playClip(correctClip);
            int h = parentFrame.getContentPane().getHeight();
            foundClickPositions.add(new Point(mx, h - my));
        }
        else {  playClip(wrongClip); if(model.lives<=0) gameLost=true; }
        controller.setScore(model.score);
        controller.setLives(model.lives);
        controller.setTimer(model.timeLeft);
        controller.setLevel(model.level);
    }

    private void playClip(Clip clip){
        if(clip==null) return;
        if(clip.isRunning()) clip.stop();
        clip.setFramePosition(0);
        clip.start();
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

    private static class Star {
        float x, y, speed;
        Star(float x, float y, float speed) { this.x = x; this.y = y; this.speed = speed; }
        void update() {
            y -= speed; // move down
            if (y < 0) {
                y = 700 + 2; // reset top
                x = (float)(Math.random() * 1000);
            }
        }
    }


    private static class Bubble {
        float x, y, size, speed;
        Bubble(float x, float y, float size, float speed) { this.x = x; this.y = y; this.size = size; this.speed = speed; }
        void update() {
            y += speed; // move up
            if (y - size > 700) {
                y = -20 - (float)(Math.random() * 200);
                x = (float)(Math.random() * 1000);
            }
        }
    }
}
