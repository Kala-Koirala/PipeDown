import java.awt.Point;
import java.awt.event.*;

public class InputHandler implements KeyListener, MouseListener, MouseMotionListener {

    private final FlappyBird panel;

    public InputHandler(FlappyBird panel) {
        this.panel = panel;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (panel.isEnteringName) return;
        int k = e.getKeyCode();

        if (k == KeyEvent.VK_T) {
            panel.changeTheme();
            return;
        }

        if (k == KeyEvent.VK_M) {
            if (panel.isMenuVisible) {
                panel.menuScreen.hideMenu();
            } else {
                panel.menuScreen.showMenu();
            }
            return;
        }

        if (k == KeyEvent.VK_P && !panel.gameOver && !panel.isMenuVisible) {
            if (panel.waitingForStart) {
                panel.menuScreen.resumeGame();
            } else {
                panel.waitingForStart = true;
                panel.gameLoop.stop();
                panel.placePipesTimer.stop();
                panel.repaint();
            }
            return;
        }

        if (k == KeyEvent.VK_SPACE) {
            if (panel.isMenuVisible) {
                panel.menuScreen.hideMenu();
                return;
            }

            if (panel.waitingForStart) {
                panel.isPaletteOpen = false;
                panel.waitingForStart = false;
                panel.velocityY = -9;
                panel.soundManager.playJump();
                panel.gameLoop.start();
                panel.placePipesTimer.start();
                panel.repaint();
                return;
            }

            if (panel.gameOver) {
                panel.bird.y = panel.boardHeight / 2;
                panel.velocityY = 0;
                panel.velocityX = -4;
                panel.pipes.clear();
                panel.score = 0;
                panel.lastDiffStep = 0;
                panel.gameOver = false;
                panel.gameLoop.start();
                panel.placePipesTimer.start();
                panel.updateButtonVisibility();
            } else {
                panel.velocityY = -9;
                panel.soundManager.playJump();
            }
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();

        if (panel.paletteWidget.paletteBtnRect().contains(p) && panel.waitingForStart && !panel.isEnteringName) {
            panel.isPaletteOpen = !panel.isPaletteOpen;
            panel.soundManager.playClick();
            panel.repaint();
            return;
        }

        if (panel.isPaletteOpen) {
            for (int i = 0; i < panel.themeManager.getThemeCount(); i++) {
                if (panel.paletteWidget.swatchRect(i).contains(p)) {
                    panel.themeManager.setThemeIndex(i);
                    panel.applyTheme();
                    panel.soundManager.playMenuSelect();
                    panel.repaint();
                    return;
                }
            }
            if (!panel.paletteWidget.palettePanelRect().contains(p)) {
                panel.isPaletteOpen = false;
                panel.repaint();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();
        boolean prev = panel.isPaletteBtnHover;
        int prevSwatch = panel.hoveredSwatch;

        panel.isPaletteBtnHover = panel.paletteWidget.paletteBtnRect().contains(p);

        panel.hoveredSwatch = -1;
        if (panel.isPaletteOpen) {
            for (int i = 0; i < panel.themeManager.getThemeCount(); i++) {
                if (panel.paletteWidget.swatchRect(i).contains(p)) {
                    panel.hoveredSwatch = i;
                    break;
                }
            }
        }

        if (prev != panel.isPaletteBtnHover || prevSwatch != panel.hoveredSwatch) {
            panel.repaint();
        }
    }

    @Override public void mouseDragged(MouseEvent e) { mouseMoved(e); }
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {
        if (panel.isPaletteBtnHover || panel.hoveredSwatch >= 0) {
            panel.isPaletteBtnHover = false;
            panel.hoveredSwatch = -1;
            panel.repaint();
        }
    }
}