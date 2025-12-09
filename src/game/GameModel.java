package game;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.Timer;

public class GameModel {
    public String username;
    public int level;
    public int score = 0;
    public int lives = 3;
    public int timeLeft; // seconds
    public List<Point> differences;
    private GameController  controller;

    private Timer timer;
    private boolean showMessage = false;
    private String messageText = "";
    private float messageAlpha = 0f;
    private int messageY = -50;
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

                    checkEndMessage();
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
        stopTimer(); // stop timer


        level++;          // new level
        timeLeft = 60;    // constant time for every level
        loadLevel(level);
        foundPoints.clear();
        gameWon = false;
        gameLost = false;

        if (controller != null) {
            controller.setLevel(level);
            controller.setScore(score);
            controller.setLives(lives);
            controller.setTimer(timeLeft);
        }

        startTimer();
    }


    public void showFinalMessage(String text) {
        messageText = text;
        messageAlpha = 1f;
        messageY = targetY; // or set wherever you want the text
        showMessage = true;

    }

    public void pauseGame() {
        paused = true;
        if (timer != null) timer.stop();
    }

    public void resumeGame() {
        paused = false;
        if (timer != null) timer.start();
    }



    public int getOriginalWidth() {
        return levelSizes[(level - 1) * 2][0];
    }

    public int getOriginalHeight() {
        return levelSizes[(level - 1) * 2][1];
    }


    public boolean checkClick(int x, int y) {
        int tol = 45;

        Iterator<Point> it = differences.iterator();
        while (it.hasNext()) {
            Point p = it.next();


            int dx1 = Math.abs(p.x - x);
            int dx2 = Math.abs(p.x - (x >= 400 ? x - 400 : x + 400));
            int dx = Math.min(dx1, dx2);

            int dy = Math.abs(p.y - y);

            if (dx <= tol && dy <= tol) {

                System.out.println("Matched difference at stored=(" + p.x + "," + p.y + ") with click=(" + x + "," + y + "), dx=" + dx + ", dy=" + dy);

                it.remove();
                foundPoints.add(p);
                score += 100;

                checkEndMessage();
                return true;
            }
        }

        // لو مفيش فرق صح
        lives--;
        if (lives <= 0) {
            gameLost = true;
            stopTimer();
        }
        checkEndMessage();
        return false;
    }



    public List<Point> differencesFound() {
        return foundPoints;
    }
    private void checkEndMessage() {
        if (!showMessage) {
            if (differences.isEmpty()) { // if the player achieved all goals
                if (level >= 3) {
                    // fainal level
                    messageText = "CONGRATULATIONS! You finished all levels!";
                    messageAlpha = 1f;
                    messageY = targetY;
                    showMessage = true;
                    gameWon = true;
                    HighScores.save(username, score);
                    playWinSound(); // access the win`s sound
                } else {

                    messageText = "LEVEL COMPLETE!";
                    messageAlpha = 1f;
                    messageY = targetY;
                    showMessage = true;
                    playWinSound(); // access the win`s sound


                    new Timer(2500, e -> {
                        ((Timer) e.getSource()).stop();
                        startNextLevel();
                        showMessage = false; // hide the message when begin new level
                    }).start();
                }

            } else if (lives <= 0 || timeLeft <= 0) { // if lose
                messageText = "YOU LOSE - Game Over";
                messageAlpha = 1f;
                messageY = targetY;
                showMessage = true;
                gameLost = true;
                playLoseSound(); // access the lose`s sound
            }
        }
    }


    public void playWinSound() {
        if (winClip != null) {
            if (winClip.isRunning()) winClip.stop();
            winClip.setFramePosition(0);
            winClip.start();
        }
    }


    public void playLoseSound() {
        if (loseClip != null) {
            if (loseClip.isRunning()) loseClip.stop();
            loseClip.setFramePosition(0);
            loseClip.start();
        }
    }


    private void loadEndSounds() {
        try {
            AudioInputStream aisWin = AudioSystem.getAudioInputStream(getClass().getResource("/assets/sounds/win.wav"));
            winClip = AudioSystem.getClip();
            winClip.open(aisWin);

            AudioInputStream aisLose = AudioSystem.getAudioInputStream(getClass().getResource("/assets/sounds/lose.wav"));
            loseClip = AudioSystem.getClip();
            loseClip.open(aisLose);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public boolean isShowMessage() {
        return showMessage;
    }

    public String getMessageText() {
        return messageText;
    }
    public int[][] getLevelSizes() {
        return levelSizes;
    }



}
