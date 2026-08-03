import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class MenuScreen {

    private final FlappyBird panel;

    public MenuScreen(FlappyBird panel) {
        this.panel = panel;
    }

    public void showMenu() {
        if (panel.isMenuVisible) return;
        panel.isMenuVisible = true;
        panel.wasGameOverBeforeMenu = panel.gameOver;
        panel.gameLoop.stop();
        panel.placePipesTimer.stop();
        panel.updateButtonVisibility();
        panel.repaint();
    }

    public void hideMenu() {
        panel.isMenuVisible = false;
        panel.waitingForStart = true;
        panel.updateButtonVisibility();
        panel.repaint();
    }

    public void resumeGame() {
        panel.waitingForStart = false;
        if (panel.wasGameOverBeforeMenu) {
            panel.bird.y = panel.boardHeight / 2;
            panel.velocityY = 0;
            panel.velocityX = -4;
            panel.pipes.clear();
            panel.score = 0;
            panel.gameOver = false;
        }
        panel.gameLoop.start();
        panel.placePipesTimer.start();
        if (!panel.wasGameOverBeforeMenu) {
            panel.velocityY = -9;
            panel.soundManager.playJump();
        }
        panel.updateButtonVisibility();
    }

    public void drawMenuScreen(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, panel.boardWidth, panel.boardHeight);

        int midX = panel.boardWidth / 2;
        int cardW = 280;
        int cardH = 340;
        int cardX = midX - cardW / 2;
        int cardY = (panel.boardHeight - cardH) / 2 - 20;

        g.setColor(new Color(30, 30, 40, 200));
        g.fill(new RoundRectangle2D.Float(cardX, cardY, cardW, cardH, 24, 24));
        g.setColor(new Color(255, 255, 255, 60));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new RoundRectangle2D.Float(cardX, cardY, cardW, cardH, 24, 24));

        panel.renderer.drawOutlinedText(g, "PAUSED", new Font("SansSerif", Font.BOLD, 32),
                midX, cardY + 50, new Color(255, 255, 255), new Color(0, 0, 0, 150), 2);

        g.setColor(new Color(255, 255, 255, 40));
        g.drawLine(cardX + 30, cardY + 75, cardX + cardW - 30, cardY + 75);

        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.setColor(panel.themeManager.getCurrentTheme().uiAccent);
        panel.renderer.drawCenteredString(g, panel.highScoreManager.getCurrentPlayerName(), midX, cardY + 115);

        g.setColor(new Color(255, 255, 255, 120));
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.renderer.drawCenteredString(g, "CURRENT SCORE", midX, cardY + 155);
        panel.renderer.drawSpriteScore(g, (int) panel.score, midX, cardY + 165);

        int best = panel.highScoreManager.getPersonalBest(panel.highScoreManager.getCurrentPlayerName());
        g.setColor(new Color(255, 255, 255, 120));
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.renderer.drawCenteredString(g, "PERSONAL BEST", midX, cardY + 225);
        panel.renderer.drawSpriteScore(g, best, midX, cardY + 235);

        g.setColor(new Color(255, 255, 255, 40));
        g.drawLine(cardX + 30, cardY + 285, cardX + cardW - 30, cardY + 285);

        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g.setColor(new Color(200, 200, 200));
        panel.renderer.drawCenteredString(g, "Press SPACE to return", midX, cardY + 315);
    }
}