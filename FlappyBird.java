
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
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        setLayout(null);

        highScoreManager = new HighScoreManager();
        difficultyManager = new DifficultyManager();
        soundManager = new SoundManager();
        themeManager = new ThemeManager();

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
        menuButton = makeStyledButton("Menu",  new Color(255, 215, 0, 220), new Color(30, 30, 30));
        menuButton.setBounds(boardWidth / 2 - 60, 410, 120, 42);
        menuButton.setVisible(false);
        menuButton.addActionListener(e -> { soundManager.playClick(); showMenu(); });
        add(menuButton);

        quitButton = makeStyledButton("Quit",  new Color(220, 50, 50, 220), Color.WHITE);
        quitButton.setBounds(boardWidth / 2 - 60, 490, 120, 42); // Moved down to add spacing below the card
        quitButton.setVisible(false);
        quitButton.addActionListener(e -> { soundManager.shutdown(); HighScoreManager.clearSavedData(); System.exit(0); });
        add(quitButton);
    }

    //  Start the game loop
    private JButton makeStyledButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Hover and pressed effects
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                // Pill shape background
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                
                // Subtle white inner glow/border
                g2.setColor(new Color(255, 255, 255, 100));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, getHeight()-2, getHeight()-2);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("SansSerif", Font.BOLD, 16));
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    //  THEME
    private void applyTheme() {
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
        draw((Graphics2D) g);
    }
    //  Draw everything
    private void draw(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // 1   Background (fills entire panel)  
        if (backgroundImg != null) {
            g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);
        } else {
            g.setColor(new Color(78, 192, 202));
            g.fillRect(0, 0, boardWidth, boardHeight);
        }

        // 2   Pipes (behind ground, behind bird on start screen)  
        for (Pipe p : pipes) {
            g.drawImage(p.img, p.x, p.y, p.width, p.height, null);
        }

        // 3   Ground strip (base.png, scrolling)  
        drawGround(g);

        // 4   Bird (with rotation)   
        drawBird(g);

        // 5   Overlays depending on state   
        if (isEnteringName) {
            // dim everything while the username dialog is up
            g.setColor(new Color(0, 0, 0, 140));
            g.fillRect(0, 0, boardWidth, boardHeight);

        } else if (isMenuVisible) {
            drawMenuScreen(g);

        } else if (gameOver) {
            drawGameOverScreen(g);

        } else if (waitingForStart) {
            drawStartScreen(g);

        } else {
            //   Active gameplay: sprite-digit score at top  
            drawSpriteScore(g, (int) score, boardWidth / 2, 40);
        }
    }

    //   Ground   
    private void drawGround(Graphics2D g) {
        if (baseImg != null) {
            // tile two copies side-by-side so the scroll wraps seamlessly
            g.drawImage(baseImg, groundScrollX, groundY, boardWidth, groundH, null);
            g.drawImage(baseImg, groundScrollX + boardWidth, groundY, boardWidth, groundH, null);
        } else {
            // fallback: simple colored strip
            ThemeManager.Theme t = themeManager.getCurrentTheme();
            g.setColor(t.groundTint);
            g.fillRect(0, groundY, boardWidth, groundH);
            g.setColor(new Color(100, 180, 50));
            g.fillRect(0, groundY, boardWidth, 10);
        }
    }

    //   Bird (rotated + animated)   
    private void drawBird(Graphics2D g) {
        Graphics2D bg = (Graphics2D) g.create();
        double cx = bird.x + bird.width / 2.0;
        double cy = bird.y + bird.height / 2.0;

        double angle;
        if (waitingForStart || isEnteringName) {
            angle = 0;
        } else if (gameOver) {
            angle = Math.toRadians(80);
        } else {
            // Smooth rotation: nose up at -25° when jumping, down to +70° when falling
            angle = Math.toRadians(clamp(velocityY * 4.0, -25, 70));
        }
        bg.rotate(angle, cx, cy);
        bg.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);
        bg.dispose();
    }

    //   Sprite-digit score (like the original Flappy Bird)  
    private void drawSpriteScore(Graphics2D g, int score, int centerX, int y) {
        String s = String.valueOf(score);
        int digitW = 18, digitH = 25;    // rendered size of each digit
        int spacing = 2;
        int totalW = s.length() * (digitW + spacing) - spacing;
        int startX = centerX - totalW / 2;

        ThemeManager.Theme t = themeManager.getCurrentTheme();

        for (int i = 0; i < s.length(); i++) {
            int d = s.charAt(i) - '0';
            int dx = startX + i * (digitW + spacing);
            Image img = (d >= 0 && d <= 9) ? digitImgs[d] : null;
            if (img != null) {
                // Shadow / outline behind digit
                g.drawImage(img, dx + 2, y + 2, digitW, digitH, null);
                g.drawImage(img, dx, y, digitW, digitH, null);
            } else {
                // fallback text digit
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 24));
                g.drawString(String.valueOf(d), dx, y + digitH);
            }
        }
    }


    //  START SCREEN
    //  – The live game world is visible behind
    //  – A soft vignette keeps the title/message readable.
    //  – The palette button, top-right.
    private void drawStartScreen(Graphics2D g) {
        boolean isWelcome = (score == 0 && pipes.isEmpty());

        //   Soft vignette overlay (NOT a harsh black rectangle)  
        // Radial gradient: transparent center -> semi-transparent edges
        int cx = boardWidth / 2, cy = boardHeight / 2;
        float radius = boardHeight * 0.65f;
        RadialGradientPaint vignette = new RadialGradientPaint(
                cx, cy, radius,
                new float[]{0.0f, 0.6f, 1.0f},
                new Color[]{
                    new Color(0, 0, 0, 0),
                    new Color(0, 0, 0, 40),
                    new Color(0, 0, 0, 150)
                }
        );
        g.setPaint(vignette);
        g.fillRect(0, 0, boardWidth, boardHeight);

        //   "message.png" artwork (FlappyBird logo + Get Ready)
        if (messageImg != null && isWelcome) {
            int iw = messageImg.getWidth(null);
            int ih = messageImg.getHeight(null);
            if (iw <= 0) iw = 250;
            if (ih <= 0) ih = 140;

            int maxW = boardWidth - 60;
            double scale = Math.min(1.0, maxW / (double) iw);
            int mw = (int) Math.round(iw * scale);
            int mh = (int) Math.round(ih * scale);
            int mx = (boardWidth - mw) / 2;
            int my = 70;

            g.drawImage(messageImg, mx, my, mw, mh, null);
        } else if (!isWelcome) {
            // Paused title
            drawOutlinedText(g, "PAUSED", new Font("SansSerif", Font.BOLD, 40),
                    boardWidth / 2, 150, Color.WHITE, new Color(0, 0, 0, 180), 3);
        }

        //   Player info badge at bottom   
        int badgeY = 400;
        // frosted glass card
        g.setColor(new Color(0, 0, 0, 100));
        g.fill(new RoundRectangle2D.Float(30, badgeY, boardWidth - 60, 100, 18, 18));
        g.setColor(new Color(255, 255, 255, 50));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new RoundRectangle2D.Float(30, badgeY, boardWidth - 60, 100, 18, 18));

        // player name
        g.setFont(new Font("SansSerif", Font.BOLD, 17));
        g.setColor(new Color(255, 220, 80));
        String name = highScoreManager.getCurrentPlayerName();
        drawCenteredString(g, name, boardWidth / 2, badgeY + 28);

        // best score
        int best = highScoreManager.getPersonalBest(name);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.setColor(new Color(200, 200, 200));
        drawCenteredString(g, "Best Score: " + best, boardWidth / 2, badgeY + 52);

        // action prompt
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.setColor(Color.WHITE);
        String prompt = isWelcome ? "Press SPACE to start" : "Press SPACE to resume";
        drawCenteredString(g, prompt, boardWidth / 2, badgeY + 80);

        //   Theme label (subtle, at very bottom)  
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setColor(new Color(255, 255, 255, 140));
        drawCenteredString(g, themeManager.getThemeName() + "  •  T to change", boardWidth / 2, 535);

        //   Palette widget   
        drawPaletteButton(g);
        if (isPaletteOpen) {
            drawPalettePanel(g);
        }
    }

    //  GAME OVER SCREEN
    private void drawGameOverScreen(Graphics2D g) {
        // dim
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, boardWidth, boardHeight);

        // gameover.png banner
        if (gameoverImg != null) {
            int gw = gameoverImg.getWidth(null);
            int gh = gameoverImg.getHeight(null);
            if (gw <= 0) {
                gw = 192;
                gh = 42;
            }
            g.drawImage(gameoverImg, (boardWidth - gw) / 2, 100, null);
        }

        //   Score card   
        int cardX = 40, cardY = 170, cardW = boardWidth - 80, cardH = 150;
        // card background with border
        g.setColor(new Color(222, 216, 149));  // sandy/parchment
        g.fill(new RoundRectangle2D.Float(cardX, cardY, cardW, cardH, 14, 14));
        g.setColor(new Color(83, 56, 71));
        g.setStroke(new BasicStroke(3f));
        g.draw(new RoundRectangle2D.Float(cardX, cardY, cardW, cardH, 14, 14));
        // inner border
        g.setColor(new Color(245, 235, 200));
        g.setStroke(new BasicStroke(2f));
        g.draw(new RoundRectangle2D.Float(cardX + 6, cardY + 6, cardW - 12, cardH - 12, 10, 10));

        // labels
        int midX = boardWidth / 2;
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(new Color(220, 120, 50));
        g.drawString("SCORE", midX - 80, cardY + 40);
        g.drawString("BEST", midX + 40, cardY + 40);

        // values (use sprite digits)
        drawSpriteScore(g, (int) score, midX - 55, cardY + 55);
        int best = highScoreManager.getPersonalBest(highScoreManager.getCurrentPlayerName());
        drawSpriteScore(g, best, midX + 65, cardY + 55);

        // player name
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.setColor(new Color(83, 56, 71));
        drawCenteredString(g, highScoreManager.getCurrentPlayerName(), midX, cardY + 120);
    }

    //  
    //  MENU SCREEN (Pause)
    //  
    private void drawMenuScreen(Graphics2D g) {
        // Dim the background softly
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, boardWidth, boardHeight);

        int midX = boardWidth / 2;
        int cardW = 280;
        int cardH = 340;
        int cardX = midX - cardW / 2;
        int cardY = (boardHeight - cardH) / 2 - 20;

        //   Frosted glass card   
        g.setColor(new Color(30, 30, 40, 200));
        g.fill(new RoundRectangle2D.Float(cardX, cardY, cardW, cardH, 24, 24));
        // Soft white border for glass effect
        g.setColor(new Color(255, 255, 255, 60));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new RoundRectangle2D.Float(cardX, cardY, cardW, cardH, 24, 24));

        //   Title "PAUSED"   
        drawOutlinedText(g, "PAUSED", new Font("SansSerif", Font.BOLD, 32),
                midX, cardY + 50, new Color(255, 255, 255), new Color(0, 0, 0, 150), 2);

        //   Divider   
        g.setColor(new Color(255, 255, 255, 40));
        g.drawLine(cardX + 30, cardY + 75, cardX + cardW - 30, cardY + 75);

        //   Player Info   
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.setColor(themeManager.getCurrentTheme().uiAccent);
        drawCenteredString(g, highScoreManager.getCurrentPlayerName(), midX, cardY + 115);

        //   Current Score block   
        g.setColor(new Color(255, 255, 255, 120));
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        drawCenteredString(g, "CURRENT SCORE", midX, cardY + 155);
        drawSpriteScore(g, (int) score, midX, cardY + 165);

        //   Best Score block   
        int best = highScoreManager.getPersonalBest(highScoreManager.getCurrentPlayerName());
        g.setColor(new Color(255, 255, 255, 120));
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        drawCenteredString(g, "PERSONAL BEST", midX, cardY + 225);
        drawSpriteScore(g, best, midX, cardY + 235);

        //   Divider   
        g.setColor(new Color(255, 255, 255, 40));
        g.drawLine(cardX + 30, cardY + 285, cardX + cardW - 30, cardY + 285);

        //   Resume prompt   
        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g.setColor(new Color(200, 200, 200));
        drawCenteredString(g, "Press SPACE to return", midX, cardY + 315);
    }

    //  PALETTE WIDGET  (button + panel)
    /**
     * Small round palette button in the top-right corner.
     */
    private void drawPaletteButton(Graphics2D g) {
        int x = PAL_BTN_X, y = PAL_BTN_Y, s = PAL_BTN_SIZE;

        // Glass circle background
        g.setColor(isPaletteBtnHover || isPaletteOpen
                ? new Color(255, 255, 255, 50)
                : new Color(0, 0, 0, 80));
        g.fillOval(x, y, s, s);

        // thin border
        g.setColor(isPaletteBtnHover || isPaletteOpen
                ? new Color(255, 255, 255, 160)
                : new Color(255, 255, 255, 80));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(x, y, s, s);

        // Use a static image asset for the palette icon when available.
        if (paletteIconImg != null) {
            int iconSize = s - 10;
            int iconX = x + (s - iconSize) / 2;
            int iconY = y + (s - iconSize) / 2;
            g.drawImage(paletteIconImg, iconX, iconY, iconSize, iconSize, null);
            return;
        }

        // Fallback drawing if the static asset could not be loaded.
        int pad = 5;
        int innerW = s - pad * 2;
        int innerH = s - pad * 2;
        g.setColor(new Color(255, 255, 255, 210));
        g.fillRoundRect(x + pad, y + pad, innerW, innerH, 6, 6);

        g.setColor(new Color(244, 67, 54));
        g.fillOval(x + 7, y + 7, 7, 7);
        g.setColor(new Color(255, 235, 59));
        g.fillOval(x + 17, y + 7, 7, 7);
        g.setColor(new Color(76, 175, 80));
        g.fillOval(x + 7, y + 17, 7, 7);
        g.setColor(new Color(33, 150, 243));
        g.fillOval(x + 17, y + 17, 7, 7);

        g.setColor(new Color(120, 120, 120, 220));
        g.setStroke(new BasicStroke(1.3f));
        g.drawLine(x + 11, y + 22, x + 22, y + 22);
        g.drawLine(x + 16, y + 10, x + 16, y + 22);
    }

    /**
     * Compact floating panel with circular swatches for each theme.
     */
    private void drawPalettePanel(Graphics2D g) {
        int count = themeManager.getThemeCount();
        int swatchR = 14;  // swatch circle radius
        int gap = 8;
        int pad = 12;
        int rowH = swatchR * 2 + 6;
        int panelW = pad * 2 + count * (swatchR * 2 + gap) - gap;
        int panelH = rowH + pad * 2 + 16;  // room for header text
        int panelX = boardWidth - panelW - 10;
        int panelY = PAL_BTN_Y + PAL_BTN_SIZE + 6;

        //   Panel background (frosted glass)  
        g.setColor(new Color(20, 20, 30, 220));
        g.fill(new RoundRectangle2D.Float(panelX, panelY, panelW, panelH, 14, 14));
        g.setColor(new Color(255, 255, 255, 60));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new RoundRectangle2D.Float(panelX, panelY, panelW, panelH, 14, 14));

        //   Header  
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(new Color(200, 200, 200));
        g.drawString("Themes", panelX + pad, panelY + 16);

        //   Swatches   
        int sy = panelY + 24 + (rowH - swatchR * 2) / 2;
        int currentIdx = themeManager.getCurrentIndex();

        for (int i = 0; i < count; i++) {
            ThemeManager.Theme t = themeManager.getTheme(i);
            int sx = panelX + pad + i * (swatchR * 2 + gap);

            boolean selected = (i == currentIdx);
            boolean hovered = (i == hoveredSwatch);

            // outer ring for selection / hover
            if (selected) {
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(2.5f));
                g.drawOval(sx - 3, sy - 3, swatchR * 2 + 6, swatchR * 2 + 6);
            } else if (hovered) {
                g.setColor(new Color(255, 255, 255, 100));
                g.setStroke(new BasicStroke(1.5f));
                g.drawOval(sx - 2, sy - 2, swatchR * 2 + 4, swatchR * 2 + 4);
            }

            // main swatch circle – filled with theme accent color
            g.setColor(t.uiAccent);
            g.fillOval(sx, sy, swatchR * 2, swatchR * 2);

            // inner half-circle for secondary color (split swatch)
            g.setColor(t.uiAccent2);
            g.fillArc(sx, sy, swatchR * 2, swatchR * 2, 180, 180);

            // subtle border
            g.setColor(new Color(255, 255, 255, 80));
            g.setStroke(new BasicStroke(1f));
            g.drawOval(sx, sy, swatchR * 2, swatchR * 2);
        }

        //   Active theme name under swatches  
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.setColor(new Color(160, 160, 160));
        String label = hoveredSwatch >= 0
                ? themeManager.getTheme(hoveredSwatch).name
                : themeManager.getThemeName();
        int labelW = g.getFontMetrics().stringWidth(label);
        g.drawString(label, panelX + (panelW - labelW) / 2, panelY + panelH - 6);
    }
 
    //  TEXT HELPERS
    private void drawCenteredString(Graphics2D g, String s, int cx, int y) {
        int w = g.getFontMetrics().stringWidth(s);
        g.drawString(s, cx - w / 2, y);
    }

    /**
     * Text with an outline/shadow for readability on busy backgrounds.
     */
    private void drawOutlinedText(Graphics2D g, String s, Font f, int cx, int y,
            Color fill, Color outline, int thickness) {
        g.setFont(f);
        int w = g.getFontMetrics().stringWidth(s);
        int x = cx - w / 2;
        g.setColor(outline);
        for (int dx = -thickness; dx <= thickness; dx++) {
            for (int dy = -thickness; dy <= thickness; dy++) {
                if (dx != 0 || dy != 0) {
                    g.drawString(s, x + dx, y + dy);
                }
            }
        }
        g.setColor(fill);
        g.drawString(s, x, y);
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
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

    //  MENU / PAUSE
    public void showMenu() {
        if (isMenuVisible) {
            return;
        }
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
            bird.y = boardHeight / 2;
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
        menuButton.setVisible(!isMenuVisible && gameOver);
        quitButton.setVisible(isMenuVisible);
    }

    //  INPUT – KEYBOARD
    @Override
    public void keyPressed(KeyEvent e) {
        if (isEnteringName) {
            return;
        }
        int k = e.getKeyCode();

        if (k == KeyEvent.VK_T) {
            changeTheme();
            return;
        }

        if (k == KeyEvent.VK_M) {
            if (isMenuVisible) {
                hideMenu();
            } else {
                showMenu();
            }
            return;
        }

        if (k == KeyEvent.VK_P && !gameOver && !isMenuVisible) {
            if (waitingForStart) {
                resumeGame(); 
            }else {
                waitingForStart = true;
                gameLoop.stop();
                placePipesTimer.stop();
                repaint();
            }
            return;
        }

        if (k == KeyEvent.VK_SPACE) {
            if (isMenuVisible) {
                hideMenu();
                return;
            }

            if (waitingForStart) {
                isPaletteOpen = false;   // close palette on start
                waitingForStart = false;
                velocityY = -9;
                soundManager.playJump();
                gameLoop.start();
                placePipesTimer.start();
                repaint();
                return;
            }

            if (gameOver) {
                bird.y = boardHeight / 2;
                velocityY = 0;
                velocityX = -4;
                pipes.clear();
                score = 0;
                lastDiffStep = 0;
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

    //  INPUT – MOUSE  (palette interaction)
    private Rectangle paletteBtnRect() {
        return new Rectangle(PAL_BTN_X, PAL_BTN_Y, PAL_BTN_SIZE, PAL_BTN_SIZE);
    }

    /**
     * Returns the bounding rect of swatch i in the palette panel, or null if
     * panel closed.
     */
    private Rectangle swatchRect(int i) {
        int swatchR = 14, gap = 8, pad = 12;
        int count = themeManager.getThemeCount();
        int panelW = pad * 2 + count * (swatchR * 2 + gap) - gap;
        int panelX = boardWidth - panelW - 10;
        int panelY = PAL_BTN_Y + PAL_BTN_SIZE + 6;
        int rowH = swatchR * 2 + 6;
        int sy = panelY + 24 + (rowH - swatchR * 2) / 2;
        int sx = panelX + pad + i * (swatchR * 2 + gap);
        return new Rectangle(sx - 3, sy - 3, swatchR * 2 + 6, swatchR * 2 + 6);
    }

    private Rectangle palettePanelRect() {
        int swatchR = 14, gap = 8, pad = 12;
        int count = themeManager.getThemeCount();
        int rowH = swatchR * 2 + 6;
        int panelW = pad * 2 + count * (swatchR * 2 + gap) - gap;
        int panelH = rowH + pad * 2 + 16;
        int panelX = boardWidth - panelW - 10;
        int panelY = PAL_BTN_Y + PAL_BTN_SIZE + 6;
        return new Rectangle(panelX, panelY, panelW, panelH);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();

        // palette button
        if (paletteBtnRect().contains(p) && waitingForStart && !isEnteringName) {
            isPaletteOpen = !isPaletteOpen;
            soundManager.playClick();
            repaint();
            return;
        }

        // swatch click
        if (isPaletteOpen) {
            for (int i = 0; i < themeManager.getThemeCount(); i++) {
                if (swatchRect(i).contains(p)) {
                    themeManager.setThemeIndex(i);
                    applyTheme();
                    soundManager.playMenuSelect();
                    repaint();
                    return;
                }
            }
            // click outside panel → close
            if (!palettePanelRect().contains(p)) {
                isPaletteOpen = false;
                repaint();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();
        boolean prev = isPaletteBtnHover;
        int prevSwatch = hoveredSwatch;

        isPaletteBtnHover = paletteBtnRect().contains(p);

        hoveredSwatch = -1;
        if (isPaletteOpen) {
            for (int i = 0; i < themeManager.getThemeCount(); i++) {
                if (swatchRect(i).contains(p)) {
                    hoveredSwatch = i;
                    break;
                }
            }
        }

        if (prev != isPaletteBtnHover || prevSwatch != hoveredSwatch) {
            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (isPaletteBtnHover || hoveredSwatch >= 0) {
            isPaletteBtnHover = false;
            hoveredSwatch = -1;
            repaint();
        }
    }
}
