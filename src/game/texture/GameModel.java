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
}
