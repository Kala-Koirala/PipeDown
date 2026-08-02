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
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
    Image gameoverImg;

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
    int lastDifficultyStep = 0;

    JFrame parentFrame;
    HighScoreManager highScoreManager;
    DifficultyManager difficultyManager;
    boolean isEnteringName = true;
    boolean waitingForStart = true;
    SoundManager soundManager;
    ThemeManager themeManager;

    JButton menuButton;
    JButton quitButton;
    boolean isMenuVisible = false;
    boolean wasGameOverBeforeMenu = false;

    FlappyBird(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);
        setLayout(null);

        highScoreManager = new HighScoreManager();
        difficultyManager = new DifficultyManager();
        soundManager = new SoundManager();
        themeManager = new ThemeManager();

        backgroundImg = themeManager.getBackgroundImg();
        birdImg = themeManager.getBirdImg();
        topPipeImg = new ImageIcon(getClass().getResource("assets/pipe-green-unpside.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("assets/pipe-green.png")).getImage();

        java.net.URL goUrl = getClass().getResource("assets/gameover.png");
        if (goUrl != null) {
            gameoverImg = new ImageIcon(goUrl).getImage();
        }

        bird = new Bird(birdX, birdY, birdWidth, birdHeight, birdImg);
        pipes = new ArrayList<>();

        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipe();
            }
        });

        gameLoop = new Timer(1000 / 60, this);

        menuButton = new JButton("Menu");
        menuButton.setBounds(130, 410, 100, 40);
        menuButton.setFont(new Font("Monospaced", Font.BOLD, 18));
        menuButton.setBackground(new Color(222, 184, 135));
        menuButton.setForeground(Color.DARK_GRAY);
        menuButton.setFocusPainted(false);
        menuButton.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        menuButton.setFocusable(false);
        menuButton.setVisible(false);
        menuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMenu();
            }
        });
        add(menuButton);

        quitButton = new JButton("Quit");
        quitButton.setBounds(130, 450, 100, 40);
        quitButton.setFont(new Font("Monospaced", Font.BOLD, 18));
        quitButton.setBackground(new Color(222, 184, 135));
        quitButton.setForeground(Color.DARK_GRAY);
        quitButton.setFocusPainted(false);
        quitButton.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        quitButton.setFocusable(false);
        quitButton.setVisible(false);
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                soundManager.shutdown();
                HighScoreManager.clearSavedData();
                System.exit(0);
            }
        });
        add(quitButton);
    }

    public void setEnteringName(boolean enteringName) {
        this.isEnteringName = enteringName;
        repaint();
    }

    public void loadCurrentPlayerName() {
        highScoreManager.loadCurrentPlayerName();
        repaint();
    }

    public void changeTheme() {
        themeManager.nextTheme();
        backgroundImg = themeManager.getBackgroundImg();
        birdImg = themeManager.getBirdImg();
        bird.img = birdImg;
        repaint();
    }

    public void placePipe() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = difficultyManager.getOpeningSpace(score);

        Pipe topPipe = new TopPipe(pipeX, randomPipeY, pipeWidth, pipeHeight, topPipeImg);
        pipes.add(topPipe);

        Pipe bottomPipe = new BottomPipe(pipeX, topPipe.y + pipeHeight + openingSpace, pipeWidth, pipeHeight,
                bottomPipeImg);
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
        if (!gameOver && !isMenuVisible && !waitingForStart) {
            g.drawString("Score: " + (int) score, 10, 35);
        }

        if (isEnteringName) {
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRect(0, 0, boardWidth, boardHeight);

        } else if (isMenuVisible) {
            g.setColor(new Color(0, 0, 0, 220));
            g.fillRect(0, 0, boardWidth, boardHeight);

            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String menuTitle = "MENU";
            int titleWidth = g.getFontMetrics().stringWidth(menuTitle);
            g.drawString(menuTitle, (boardWidth - titleWidth) / 2, 100);

            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 22));
            String playerStr = "Player: " + highScoreManager.getCurrentPlayerName();
            int playerWidth = g.getFontMetrics().stringWidth(playerStr);
            g.drawString(playerStr, (boardWidth - playerWidth) / 2, 150);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            String scoreStr = "Current Score: " + (int) score;
            int scoreWidth = g.getFontMetrics().stringWidth(scoreStr);
            g.drawString(scoreStr, (boardWidth - scoreWidth) / 2, 190);

            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            int pb = highScoreManager.getPersonalBest(highScoreManager.getCurrentPlayerName());
            String pbStr = "Best Score: " + pb;
            int pbWidth = g.getFontMetrics().stringWidth(pbStr);
            g.drawString(pbStr, (boardWidth - pbWidth) / 2, 230);

            g.setColor(Color.LIGHT_GRAY);
            g.setFont(new Font("Arial", Font.ITALIC, 16));
            String instStr = "Press SPACE/M to return to game";
            int instWidth = g.getFontMetrics().stringWidth(instStr);
            g.drawString(instStr, (boardWidth - instWidth) / 2, 300);

        } else if (gameOver) {
            int goWidth = 188;
            int goHeight = 42;
            if (gameoverImg != null) {
                int w = gameoverImg.getWidth(null);
                int h = gameoverImg.getHeight(null);
                if (w > 0) goWidth = w;
                if (h > 0) goHeight = h;
                g.drawImage(gameoverImg, (boardWidth - goWidth) / 2, 100, null);
            } else {
                g.setColor(Color.RED);
                g.setFont(new Font("Arial", Font.BOLD, 30));
                g.drawString("GAME OVER", (boardWidth - 140) / 2, 100);
            }

            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 22));
            String playerStr = "Player: " + highScoreManager.getCurrentPlayerName();
            int playerWidth = g.getFontMetrics().stringWidth(playerStr);
            g.drawString(playerStr, (boardWidth - playerWidth) / 2, 190);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            String scoreStr = "Score: " + (int) score;
            int scoreWidth = g.getFontMetrics().stringWidth(scoreStr);
            g.drawString(scoreStr, (boardWidth - scoreWidth) / 2, 230);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            String restartStr = "Press Space to Restart";
            int restartWidth = g.getFontMetrics().stringWidth(restartStr);
            g.drawString(restartStr, (boardWidth - restartWidth) / 2, 290);

        } else if (waitingForStart) {
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRect(0, 0, boardWidth, boardHeight);

            boolean isWelcome = (score == 0 && pipes.isEmpty());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            String titleStr = isWelcome ? "WELCOME" : "PAUSED";
            int titleWidth = g.getFontMetrics().stringWidth(titleStr);
            g.drawString(titleStr, (boardWidth - titleWidth) / 2, 100);

            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.setColor(Color.YELLOW);
            String nameStr = "Player: " + highScoreManager.getCurrentPlayerName();
            int nameWidth = g.getFontMetrics().stringWidth(nameStr);
            g.drawString(nameStr, (boardWidth - nameWidth) / 2, 140);

            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            String actionStr = isWelcome ? "Press SPACE to Start!" : "Press SPACE to Resume!";
            int actionWidth = g.getFontMetrics().stringWidth(actionStr);
            g.drawString(actionStr, (boardWidth - actionWidth) / 2, 220);

            g.setFont(new Font("Arial", Font.PLAIN, 14));
            String themeMsg = "Theme: " + themeManager.getThemeName() + " (Press T to change)";
            int themeMsgWidth = g.getFontMetrics().stringWidth(themeMsg);
            g.drawString(themeMsg, (boardWidth - themeMsgWidth) / 2, 250);
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
            updateButtonVisibility();
        }
    }

    public void showMenu() {
        if (isMenuVisible) return;
        isMenuVisible = true;
        wasGameOverBeforeMenu = gameOver;
        gameLoop.stop();
        placePipesTimer.stop();
        updateButtonVisibility();
        repaint();
    }

    public void hideMenu() {
        isMenuVisible = false;
        waitingForStart = true;
        updateButtonVisibility();
        repaint();
    }

    public void resumeGame() {
        waitingForStart = false;
        if (wasGameOverBeforeMenu) {
            bird.y = birdY;
            velocityY = 0;
            velocityX = -4;
            pipes.clear();
            score = 0;
            gameOver = false;
        }
        gameLoop.start();
        placePipesTimer.start();
        if (!wasGameOverBeforeMenu) {
            velocityY = -9;
            soundManager.playJump();
        }
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        if (isMenuVisible) {
            menuButton.setVisible(false);
            quitButton.setVisible(true);
        } else if (gameOver) {
            menuButton.setVisible(true);
            quitButton.setVisible(false);
        } else {
            menuButton.setVisible(false);
            quitButton.setVisible(false);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (isEnteringName) {
            return;
        }

        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_T) {
            changeTheme();
            return;
        }

        if (keyCode == KeyEvent.VK_M) {
            if (isMenuVisible) {
                hideMenu();
            } else {
                showMenu();
            }
            return;
        }

        if (keyCode == KeyEvent.VK_P) {
            if (!gameOver && !isMenuVisible) {
                if (waitingForStart) {
                    resumeGame();
                } else {
                    waitingForStart = true;
                    gameLoop.stop();
                    placePipesTimer.stop();
                    repaint();
                }
            }
            return;
        }

        if (keyCode == KeyEvent.VK_SPACE) {
            if (isMenuVisible) {
                hideMenu();
                return;
            }

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
                updateButtonVisibility();
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
