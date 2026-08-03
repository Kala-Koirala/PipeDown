import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class PaletteWidget {

    private final FlappyBird panel;

    public PaletteWidget(FlappyBird panel) {
        this.panel = panel;
    }

    public Rectangle paletteBtnRect() {
        return new Rectangle(panel.PAL_BTN_X, panel.PAL_BTN_Y, panel.PAL_BTN_SIZE, panel.PAL_BTN_SIZE);
    }

    public Rectangle swatchRect(int i) {
        int swatchR = 14, gap = 8, pad = 12;
        int count = panel.themeManager.getThemeCount();
        int panelW = pad * 2 + count * (swatchR * 2 + gap) - gap;
        int panelX = panel.boardWidth - panelW - 10;
        int panelY = panel.PAL_BTN_Y + panel.PAL_BTN_SIZE + 6;
        int rowH = swatchR * 2 + 6;
        int sy = panelY + 24 + (rowH - swatchR * 2) / 2;
        int sx = panelX + pad + i * (swatchR * 2 + gap);
        return new Rectangle(sx - 3, sy - 3, swatchR * 2 + 6, swatchR * 2 + 6);
    }

    public Rectangle palettePanelRect() {
        int swatchR = 14, gap = 8, pad = 12;
        int count = panel.themeManager.getThemeCount();
        int rowH = swatchR * 2 + 6;
        int panelW = pad * 2 + count * (swatchR * 2 + gap) - gap;
        int panelH = rowH + pad * 2 + 16;
        int panelX = panel.boardWidth - panelW - 10;
        int panelY = panel.PAL_BTN_Y + panel.PAL_BTN_SIZE + 6;
        return new Rectangle(panelX, panelY, panelW, panelH);
    }

    public void drawPaletteButton(Graphics2D g) {
        int x = panel.PAL_BTN_X, y = panel.PAL_BTN_Y, s = panel.PAL_BTN_SIZE;

        g.setColor(panel.isPaletteBtnHover || panel.isPaletteOpen
                ? new Color(255, 255, 255, 50)
                : new Color(0, 0, 0, 80));
        g.fillOval(x, y, s, s);

        g.setColor(panel.isPaletteBtnHover || panel.isPaletteOpen
                ? new Color(255, 255, 255, 160)
                : new Color(255, 255, 255, 80));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(x, y, s, s);

        if (panel.paletteIconImg != null) {
            int iconSize = s - 10;
            int iconX = x + (s - iconSize) / 2;
            int iconY = y + (s - iconSize) / 2;
            g.drawImage(panel.paletteIconImg, iconX, iconY, iconSize, iconSize, null);
            return;
        }

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

    public void drawPalettePanel(Graphics2D g) {
        int count = panel.themeManager.getThemeCount();
        int swatchR = 14;
        int gap = 8;
        int pad = 12;
        int rowH = swatchR * 2 + 6;
        int panelW = pad * 2 + count * (swatchR * 2 + gap) - gap;
        int panelH = rowH + pad * 2 + 16;
        int panelX = panel.boardWidth - panelW - 10;
        int panelY = panel.PAL_BTN_Y + panel.PAL_BTN_SIZE + 6;

        g.setColor(new Color(20, 20, 30, 220));
        g.fill(new RoundRectangle2D.Float(panelX, panelY, panelW, panelH, 14, 14));
        g.setColor(new Color(255, 255, 255, 60));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new RoundRectangle2D.Float(panelX, panelY, panelW, panelH, 14, 14));

        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(new Color(200, 200, 200));
        g.drawString("Themes", panelX + pad, panelY + 16);

        int sy = panelY + 24 + (rowH - swatchR * 2) / 2;
        int currentIdx = panel.themeManager.getCurrentIndex();

        for (int i = 0; i < count; i++) {
            ThemeManager.Theme t = panel.themeManager.getTheme(i);
            int sx = panelX + pad + i * (swatchR * 2 + gap);

            boolean selected = (i == currentIdx);
            boolean hovered = (i == panel.hoveredSwatch);

            if (selected) {
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(2.5f));
                g.drawOval(sx - 3, sy - 3, swatchR * 2 + 6, swatchR * 2 + 6);
            } else if (hovered) {
                g.setColor(new Color(255, 255, 255, 100));
                g.setStroke(new BasicStroke(1.5f));
                g.drawOval(sx - 2, sy - 2, swatchR * 2 + 4, swatchR * 2 + 4);
            }

            g.setColor(t.uiAccent);
            g.fillOval(sx, sy, swatchR * 2, swatchR * 2);

            g.setColor(t.uiAccent2);
            g.fillArc(sx, sy, swatchR * 2, swatchR * 2, 180, 180);

            g.setColor(new Color(255, 255, 255, 80));
            g.setStroke(new BasicStroke(1f));
            g.drawOval(sx, sy, swatchR * 2, swatchR * 2);
        }

        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.setColor(new Color(160, 160, 160));
        String label = panel.hoveredSwatch >= 0
                ? panel.themeManager.getTheme(panel.hoveredSwatch).name
                : panel.themeManager.getThemeName();
        int labelW = g.getFontMetrics().stringWidth(label);
        g.drawString(label, panelX + (panelW - labelW) / 2, panelY + panelH - 6);
    }
}