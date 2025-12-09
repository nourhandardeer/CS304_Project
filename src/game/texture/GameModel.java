package game.texture;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.Timer;

public class GameModel {
    public String username;
    public int level;
    public int score = 0;
    public int lives = 3;
    public int timeLeft; // seconds
    public List<Point> differences; // إحداثيات الاختلاف الأصلية
    private GameController controller;

    private Timer timer;
    private boolean showMessage = false;
    private String messageText = "";
    private float messageAlpha = 0f;
    private int messageY = -50; // يبدأ من فوق الشاشة
    private final int targetY = 350;
    private Clip winClip;
    private Clip loseClip;
    private List<Point> foundPoints = new ArrayList<>();
    // أبعاد الصور الأصلية لكل ليفل
    private final int[][] levelSizes = {
            {397, 264}, {397, 265}, // Level 1
            {400, 300}, {400, 300}, // Level 2
            {400, 400}, {400, 400}  // Level 3
    };

    // لإظهار النتائج في الـ GUI أو OpenGL
    public boolean gameWon = false;
    public boolean gameLost = false;
    public boolean paused = false;



    public GameModel(String username, int level, GameController c) {
        this.username = username;
        this.level = level;
        this.controller = c;
        this.timeLeft = 60 + (level-1)*30;
        loadEndSounds();
        loadLevel(level);
        startTimer();
    }


    private void startTimer() {
        timer = new Timer(1000, e -> {
            if (!paused && !gameWon && !gameLost) {
                timeLeft--;
                if (timeLeft <= 0) {
                    lives = 0;
                    gameLost = true;
                    stopTimer();
                }
            }
        });
        timer.start();
    }


    public void stopTimer() {
        if (timer != null) timer.stop();
    }


    private void loadLevel(int lvl) {
        differences = new ArrayList<>();
        foundPoints.clear();
        gameWon = false;
        gameLost = false;

        if (lvl == 1) {
            differences.add(new Point(91, 284));
            differences.add(new Point(379, 175));
            differences.add(new Point(527, 121));
        } else if (lvl == 2) {
            differences.add(new Point(195,173));
            differences.add(new Point(344,56));
            differences.add(new Point(611,153));
        } else {
            differences.add(new Point(427,302));
            differences.add(new Point(659,326));
            differences.add(new Point(358,49));
        }
    }


    public void startNextLevel() {
        stopTimer(); // وقف التايمر الحالي


        level++;          // المستوى الجديد
        timeLeft = 60;    // وقت ثابت لكل مستوى
        loadLevel(level); // تحميل الاختلافات
        foundPoints.clear();
        gameWon = false;
        gameLost = false;

        if (controller != null) {
            controller.setLevel(level);
            controller.setScore(score);
            controller.setLives(lives);
            controller.setTimer(timeLeft);
        }

        startTimer(); // شغل التايمر للمستوى الجديد
    }


    public void showFinalMessage(String text) {
        messageText = text;
        messageAlpha = 1f;
        messageY = targetY; // or set wherever you want the text
        showMessage = true;

    }


    public int getOriginalWidth() {
        return levelSizes[(level - 1) * 2][0];
    }

    public int getOriginalHeight() {
        return levelSizes[(level - 1) * 2][1];
    }
}
