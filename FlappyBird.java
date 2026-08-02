
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    int boardWidth = 360;
    int boardHeight = 640;

    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    int birdX = boardWidth / 8;
    int birdY = boardHeight / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    Bird bird;
    int velocityX = -4;
    int velocityY = 0;
    int gravity = 1;

    ArrayList<Pipe> pipes;

    Timer gameLoop;
    Timer placePipesTimer;
    boolean gameOver = false;
    double score = 0;
    int lastDifficultyStep = 0; // tracks difficulty level so playLevelUp() fires once per tier

    JFrame parentFrame;
    HighScoreManager highScoreManager;
    DifficultyManager difficultyManager;
    boolean isEnteringName = true;
    boolean waitingForStart = true;
    SoundManager soundManager;

    FlappyBird(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        highScoreManager = new HighScoreManager();
        difficultyManager = new DifficultyManager();
        soundManager = new SoundManager();

        backgroundImg = new ImageIcon(getClass().getResource("assets/background-day.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("assets/yellowbird-upflap.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("assets/pipe-green-unpside.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("assets/pipe-green.png")).getImage();

        bird = new Bird(birdX, birdY, birdWidth, birdHeight, birdImg);
        pipes = new ArrayList<>();

        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipe();
            }
        });

        gameLoop = new Timer(1000 / 60, this);
    }

    public void setEnteringName(boolean enteringName) {
        this.isEnteringName = enteringName;
        repaint();
    }

    public void loadCurrentPlayerName() {
        highScoreManager.loadCurrentPlayerName();
        repaint();
    }

    public void placePipe() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = difficultyManager.getOpeningSpace(score);

        Pipe topPipe = new TopPipe(pipeX, randomPipeY, pipeWidth, pipeHeight, topPipeImg);
        pipes.add(topPipe);

        Pipe bottomPipe = new BottomPipe(pipeX, topPipe.y + pipeHeight + openingSpace, pipeWidth, pipeHeight, bottomPipeImg);
        pipes.add(bottomPipe);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        if (gameOver) {
            g.drawString("Game Over: " + (int) score, 10, 35);
        } else {
            g.drawString("Score: " + (int) score, 10, 35);
        }

        if (waitingForStart && !isEnteringName && !gameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            String msg = "Press SPACE to Start";
            int msgWidth = g.getFontMetrics().stringWidth(msg);
            g.drawString(msg, (boardWidth - msgWidth) / 2, boardHeight / 2);
        }

        if (isEnteringName) {
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRect(0, 0, boardWidth, boardHeight);
        }
    }

    public void move() {
        if (waitingForStart) {
            return;
        }

        velocityX = difficultyManager.getVelocityX(score);
        int currentSpawnInterval = difficultyManager.getSpawnInterval(score);
        if (placePipesTimer.getDelay() != currentSpawnInterval) {
            placePipesTimer.setDelay(currentSpawnInterval);
        }

        int currentDifficultyStep = (int) (score / 5);
        if (currentDifficultyStep > lastDifficultyStep) {
            lastDifficultyStep = currentDifficultyStep;
            soundManager.playLevelUp();
        }

        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    public boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width
                && a.x + a.width > b.x
                && a.y < b.y + b.height
                && a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
            placePipesTimer.stop();
            boolean isNewHighScore = highScoreManager.checkAndUpdateHighScore((int) score);
            if (isNewHighScore) {
                soundManager.playHighScore();
            } else {
                soundManager.playGameOver();
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (isEnteringName) {
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (waitingForStart) {
                waitingForStart = false;
                velocityY = -9;
                gameLoop.start();
                placePipesTimer.start();
                repaint();
                return;
            }

            if (gameOver) {
                bird.y = birdY;
                velocityY = 0;
                velocityX = -4;
                pipes.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                placePipesTimer.start();
            } else {
                velocityY = -9;
                soundManager.playJump();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
