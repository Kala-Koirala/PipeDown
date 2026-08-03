import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class Renderer {

    private final FlappyBird panel;

    public Renderer(FlappyBird panel) {
        this.panel = panel;
    }

    public void draw(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (panel.backgroundImg != null) {
            g.drawImage(panel.backgroundImg, 0, 0, panel.boardWidth, panel.boardHeight, null);
        } else {
            g.setColor(new Color(78, 192, 202));
            g.fillRect(0, 0, panel.boardWidth, panel.boardHeight);
        }

        for (Pipe p : panel.pipes) {
            g.drawImage(p.img, p.x, p.y, p.width, p.height, null);
        }

        drawGround(g);
        drawBird(g);

        if (panel.isEnteringName) {
            g.setColor(new Color(0, 0, 0, 140));
            g.fillRect(0, 0, panel.boardWidth, panel.boardHeight);
        } else if (panel.isMenuVisible) {
            panel.menuScreen.drawMenuScreen(g);
        } else if (panel.gameOver) {
            drawGameOverScreen(g);
        } else if (panel.waitingForStart) {
            drawStartScreen(g);
        } else {
            drawSpriteScore(g, (int) panel.score, panel.boardWidth / 2, 40);
        }
    }

    public void drawGround(Graphics2D g) {
        if (panel.baseImg != null) {
            g.drawImage(panel.baseImg, panel.groundScrollX, panel.groundY, panel.boardWidth, panel.groundH, null);
            g.drawImage(panel.baseImg, panel.groundScrollX + panel.boardWidth, panel.groundY, panel.boardWidth, panel.groundH, null);
        } else {
            ThemeManager.Theme t = panel.themeManager.getCurrentTheme();
            g.setColor(t.groundTint);
            g.fillRect(0, panel.groundY, panel.boardWidth, panel.groundH);
            g.setColor(new Color(100, 180, 50));
            g.fillRect(0, panel.groundY, panel.boardWidth, 10);
        }
    }

    public void drawBird(Graphics2D g) {
        Graphics2D bg = (Graphics2D) g.create();
        double cx = panel.bird.x + panel.bird.width / 2.0;
        double cy = panel.bird.y + panel.bird.height / 2.0;

        double angle;
        if (panel.waitingForStart || panel.isEnteringName) {
            angle = 0;
        } else if (panel.gameOver) {
            angle = Math.toRadians(80);
        } else {
            angle = Math.toRadians(clamp(panel.velocityY * 4.0, -25, 70));
        }
        bg.rotate(angle, cx, cy);
        bg.drawImage(panel.bird.img, panel.bird.x, panel.bird.y, panel.bird.width, panel.bird.height, null);
        bg.dispose();
    }

    public void drawSpriteScore(Graphics2D g, int score, int centerX, int y) {
        String s = String.valueOf(score);
        int digitW = 18, digitH = 25;
        int spacing = 2;
        int totalW = s.length() * (digitW + spacing) - spacing;
        int startX = centerX - totalW / 2;

        for (int i = 0; i < s.length(); i++) {
            int d = s.charAt(i) - '0';
            int dx = startX + i * (digitW + spacing);
            Image img = (d >= 0 && d <= 9) ? panel.digitImgs[d] : null;
            if (img != null) {
                g.drawImage(img, dx + 2, y + 2, digitW, digitH, null);
                g.drawImage(img, dx, y, digitW, digitH, null);
            } else {
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 24));
                g.drawString(String.valueOf(d), dx, y + digitH);
            }
        }
    }

    public void drawStartScreen(Graphics2D g) {
        boolean isWelcome = (panel.score == 0 && panel.pipes.isEmpty());

        int cx = panel.boardWidth / 2, cy = panel.boardHeight / 2;
        float radius = panel.boardHeight * 0.65f;
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
        g.fillRect(0, 0, panel.boardWidth, panel.boardHeight);

        if (panel.messageImg != null && isWelcome) {
            int iw = panel.messageImg.getWidth(null);
            int ih = panel.messageImg.getHeight(null);
            if (iw <= 0) iw = 250;
            if (ih <= 0) ih = 140;

            int maxW = panel.boardWidth - 60;
            double scale = Math.min(1.0, maxW / (double) iw);
            int mw = (int) Math.round(iw * scale);
            int mh = (int) Math.round(ih * scale);
            int mx = (panel.boardWidth - mw) / 2;
            int my = 70;

            g.drawImage(panel.messageImg, mx, my, mw, mh, null);
        } else if (!isWelcome) {
            drawOutlinedText(g, "PAUSED", new Font("SansSerif", Font.BOLD, 40),
                    panel.boardWidth / 2, 150, Color.WHITE, new Color(0, 0, 0, 180), 3);
        }

        int badgeY = 400;
        g.setColor(new Color(0, 0, 0, 100));
        g.fill(new RoundRectangle2D.Float(30, badgeY, panel.boardWidth - 60, 100, 18, 18));
        g.setColor(new Color(255, 255, 255, 50));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new RoundRectangle2D.Float(30, badgeY, panel.boardWidth - 60, 100, 18, 18));

        g.setFont(new Font("SansSerif", Font.BOLD, 17));
        g.setColor(new Color(255, 220, 80));
        String name = panel.highScoreManager.getCurrentPlayerName();
        drawCenteredString(g, name, panel.boardWidth / 2, badgeY + 28);

        int best = panel.highScoreManager.getPersonalBest(name);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.setColor(new Color(200, 200, 200));
        drawCenteredString(g, "Best Score: " + best, panel.boardWidth / 2, badgeY + 52);

        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.setColor(Color.WHITE);
        String prompt = isWelcome ? "Press SPACE to start" : "Press SPACE to resume";
        drawCenteredString(g, prompt, panel.boardWidth / 2, badgeY + 80);

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setColor(new Color(255, 255, 255, 140));
        drawCenteredString(g, panel.themeManager.getThemeName() + "  •  T to change", panel.boardWidth / 2, 535);

        panel.paletteWidget.drawPaletteButton(g);
        if (panel.isPaletteOpen) {
            panel.paletteWidget.drawPalettePanel(g);
        }
    }

    public void drawGameOverScreen(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, panel.boardWidth, panel.boardHeight);

        if (panel.gameoverImg != null) {
            int gw = panel.gameoverImg.getWidth(null);
            int gh = panel.gameoverImg.getHeight(null);
            if (gw <= 0) {
                gw = 192;
                gh = 42;
            }
            g.drawImage(panel.gameoverImg, (panel.boardWidth - gw) / 2, 100, null);
        }

        int cardX = 40, cardY = 170, cardW = panel.boardWidth - 80, cardH = 150;
        g.setColor(new Color(222, 216, 149));
        g.fill(new RoundRectangle2D.Float(cardX, cardY, cardW, cardH, 14, 14));
        g.setColor(new Color(83, 56, 71));
        g.setStroke(new BasicStroke(3f));
        g.draw(new RoundRectangle2D.Float(cardX, cardY, cardW, cardH, 14, 14));
        g.setColor(new Color(245, 235, 200));
        g.setStroke(new BasicStroke(2f));
        g.draw(new RoundRectangle2D.Float(cardX + 6, cardY + 6, cardW - 12, cardH - 12, 10, 10));

        int midX = panel.boardWidth / 2;
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(new Color(220, 120, 50));
        g.drawString("SCORE", midX - 80, cardY + 40);
        g.drawString("BEST", midX + 40, cardY + 40);

        drawSpriteScore(g, (int) panel.score, midX - 55, cardY + 55);
        int best = panel.highScoreManager.getPersonalBest(panel.highScoreManager.getCurrentPlayerName());
        drawSpriteScore(g, best, midX + 65, cardY + 55);

        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.setColor(new Color(83, 56, 71));
        drawCenteredString(g, panel.highScoreManager.getCurrentPlayerName(), midX, cardY + 120);

        drawMedal(g, (int) panel.score, cardX + 35, cardY + 40);

        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        drawCenteredString(g, "Press SPACE to restart", midX, 370);
    }

    public void drawMedal(Graphics2D g, int score, int x, int y) {
        if (score < 5) return;

        Color medalColor;
        String label;
        if (score >= 40) {
            medalColor = new Color(255, 215, 0);
            label = "G";
        } else if (score >= 20) {
            medalColor = new Color(192, 192, 192);
            label = "S";
        } else {
            medalColor = new Color(205, 127, 50);
            label = "B";
        }

        int r = 22;
        g.setColor(medalColor);
        g.fillOval(x, y, r * 2, r * 2);
        g.setColor(medalColor.darker());
        g.setStroke(new BasicStroke(2.5f));
        g.drawOval(x, y, r * 2, r * 2);

        g.setColor(new Color(255, 255, 255, 90));
        g.fillOval(x + 5, y + 3, r, r - 2);

        g.setColor(new Color(80, 50, 20));
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, x + r - fm.stringWidth(label) / 2, y + r + fm.getAscent() / 2 - 2);
    }

    public void drawCenteredString(Graphics2D g, String s, int cx, int y) {
        int w = g.getFontMetrics().stringWidth(s);
        g.drawString(s, cx - w / 2, y);
    }

    public void drawOutlinedText(Graphics2D g, String s, Font f, int cx, int y,
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
}