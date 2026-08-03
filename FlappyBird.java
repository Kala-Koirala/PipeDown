
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener {

    //Dimensions 
    final int boardWidth = 360;
    final int boardHeight = 640;
    final int groundY = 580;                   
    final int groundH = boardHeight - groundY;  

    //Images
    Image backgroundImg;
    Image topPipeImg, bottomPipeImg;
    Image gameoverImg;
    Image baseImg;
    Image messageImg;
    Image paletteIconImg;
    Image[] birdFrames;
    Image[] digitImgs = new Image[10];

    //Bird state
    final int birdX = boardWidth / 8;
    int birdY;
    final int birdW = 34, birdH = 24;
    Bird bird;
    int velocityY = 0;
    int velocityX = -4;
    final int gravity = 1;
    int birdFrameIdx = 0;
    int frameTick = 0;

    //   Pipe state  
    final int pipeW = 64, pipeH = 512;
    ArrayList<Pipe> pipes;

    //   Timers   
    Timer gameLoop;
    Timer placePipesTimer;

    //   Game state  
    boolean gameOver = false;
    double score = 0;
    int lastDiffStep = 0;
    boolean isEnteringName = true;
    boolean waitingForStart = true;
    boolean isMenuVisible = false;
    boolean wasGameOverBeforeMenu = false;

    //   Ground scroll   
    int groundScrollX = 0;

    //   Idle bird bob (start screen)  
    long idleTick = 0;

    //   Managers   
    JFrame parentFrame;
    HighScoreManager highScoreManager;
    DifficultyManager difficultyManager;
    SoundManager soundManager;
    ThemeManager themeManager;

    Renderer renderer;
    MenuScreen menuScreen;
    PaletteWidget paletteWidget;
    InputHandler inputHandler;

    //   Swing buttons   
    JButton menuButton;
    JButton quitButton;

    //   Palette widget   
    final int PAL_BTN_SIZE = 32;
    final int PAL_BTN_X = boardWidth - PAL_BTN_SIZE - 10;
    final int PAL_BTN_Y = 10;
    boolean isPaletteOpen = false;
    boolean isPaletteBtnHover = false;
    int hoveredSwatch = -1;

    //  CONSTRUCTOR
    FlappyBird(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(inputHandler);
        addMouseListener(inputHandler);
        addMouseMotionListener(inputHandler);
        setLayout(null);

        highScoreManager = new HighScoreManager();
        difficultyManager = new DifficultyManager();
        soundManager = new SoundManager();
        themeManager = new ThemeManager();

        renderer = new Renderer(this);
        menuScreen = new MenuScreen(this);
        paletteWidget = new PaletteWidget(this);
        inputHandler = new InputHandler(this);

        // Load static assets
        baseImg = ThemeManager.loadImage("assets/base.png");
        messageImg = ThemeManager.loadImage("assets/message.png");
        gameoverImg = ThemeManager.loadImage("assets/gameover.png");
        paletteIconImg = ThemeManager.loadImage("assets/pallete.png");
        for (int i = 0; i < 10; i++) {
            digitImgs[i] = ThemeManager.loadImage("assets/" + i + ".png");
        }

        // Load theme-dependent assets
        applyTheme();

        birdY = boardHeight / 2;
        bird = new Bird(birdX, birdY, birdW, birdH, birdFrames != null ? birdFrames[1] : null);
        pipes = new ArrayList<>();

        placePipesTimer = new Timer(1500, e -> placePipe());
        gameLoop = new Timer(1000 / 60, this);

        //  Buttons
        menuButton = StyledButton.create("Menu",  new Color(255, 215, 0, 220), new Color(30, 30, 30));
        menuButton.setBounds(boardWidth / 2 - 60, 410, 120, 42);
        menuButton.setVisible(false);
        menuButton.addActionListener(e -> { soundManager.playClick(); menuScreen.showMenu(); });
        add(menuButton);

        quitButton = StyledButton.create("Quit",  new Color(220, 50, 50, 220), Color.WHITE);
        quitButton.setBounds(boardWidth / 2 - 60, 490, 120, 42); // Moved down to add spacing below the card
        quitButton.setVisible(false);
        quitButton.addActionListener(e -> { soundManager.shutdown(); HighScoreManager.clearSavedData(); System.exit(0); });
        add(quitButton);
    }

   
    //  THEME
    void applyTheme() {
        backgroundImg = themeManager.getBackgroundImg();
        birdFrames = themeManager.getBirdFrames();
        topPipeImg = themeManager.getTopPipeImg();
        bottomPipeImg = themeManager.getBottomPipeImg();
        if (bird != null && birdFrames != null && birdFrames.length > 1) {
            bird.img = birdFrames[1];
        }
        // Update live pipes
        if (pipes != null) {
            for (Pipe p : pipes) {
                p.img = (p instanceof TopPipe) ? topPipeImg : bottomPipeImg;
            }
        }
    }

    //  Change Theme (called from palette button)
    public void changeTheme() {
        themeManager.nextTheme();
        applyTheme();
        soundManager.playMenuSelect();
        repaint();
    }

    //  EXTERNAL API  (called from Main / UsernameDialog)
    public void setEnteringName(boolean v) {
        isEnteringName = v;
        repaint();
    }

    public void loadCurrentPlayerName() {
        highScoreManager.loadCurrentPlayerName();
        repaint();
    }

    //  PIPE SPAWNING
    public void placePipe() {
        int pipeY = 0;
        int randomPipeY = (int) (pipeY - pipeH / 4 - Math.random() * (pipeH / 2));
        int gap = difficultyManager.getOpeningSpace(score);

        pipes.add(new TopPipe(boardWidth, randomPipeY, pipeW, pipeH, topPipeImg));
        pipes.add(new BottomPipe(boardWidth, randomPipeY + pipeH + gap, pipeW, pipeH, bottomPipeImg));
    }

    //  RENDERING
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.draw((Graphics2D) g);
    }
 

    //  GAME LOGIC  (called at 60 fps)
    public void move() {
        // Ground scrolls even on the start screen
        groundScrollX -= 2;
        if (groundScrollX <= -boardWidth) {
            groundScrollX = 0;
        }

        idleTick++;

        if (waitingForStart) {
            // Idle bird bob (sinusoidal float)
            bird.y = boardHeight / 2 + (int) (8 * Math.sin(idleTick * 0.06));
            // Animate wing flap even while idle
            frameTick++;
            if (birdFrames != null && birdFrames.length > 1 && frameTick % 8 == 0) {
                birdFrameIdx = (birdFrameIdx + 1) % birdFrames.length;
                bird.img = birdFrames[birdFrameIdx];
            }
            return;
        }

        //   Active gameplay   
        // Animate bird wings
        frameTick++;
        if (birdFrames != null && birdFrames.length > 1 && frameTick % 6 == 0) {
            birdFrameIdx = (birdFrameIdx + 1) % birdFrames.length;
            bird.img = birdFrames[birdFrameIdx];
        }

        velocityX = difficultyManager.getVelocityX(score);
        int spawnInterval = difficultyManager.getSpawnInterval(score);
        if (placePipesTimer.getDelay() != spawnInterval) {
            placePipesTimer.setDelay(spawnInterval);
        }

        // ground scrolls with pipe speed
        groundScrollX += velocityX;
        if (groundScrollX <= -boardWidth) {
            groundScrollX = 0;
        }

        int diffStep = (int) (score / 5);
        if (diffStep > lastDiffStep) {
            lastDiffStep = diffStep;
            soundManager.playLevelUp();
        }

        // Bird physics
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        // Pipe movement & scoring
        for (int i = 0; i < pipes.size(); i++) {
            Pipe p = pipes.get(i);
            p.x += velocityX;
            if (!p.passed && bird.x > p.x + p.width) {
                p.passed = true;
                score += 0.5;
            }
            if (collision(bird, p)) {
                gameOver = true;
            }
        }

        // Ground collision
        if (bird.y + bird.height >= groundY) {
            bird.y = groundY - bird.height;
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
            if (highScoreManager.checkAndUpdateHighScore((int) score)) {
                soundManager.playHighScore(); 
            }else {
                soundManager.playGameOver();
            }
            updateButtonVisibility();
        }
    }

    void updateButtonVisibility() {
        menuButton.setVisible(!isMenuVisible && gameOver);
        quitButton.setVisible(isMenuVisible);
    }


    @Override
    public void mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseDragged'");
    }


    @Override
    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseMoved'");
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseClicked'");
    }


    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseEntered'");
    }


    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseExited'");
    }


    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mousePressed'");
    }


    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseReleased'");
    }


    @Override
    public void keyPressed(KeyEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'keyPressed'");
    }


    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'keyReleased'");
    }


    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'keyTyped'");
    }

}