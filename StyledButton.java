import java.awt.*;
import javax.swing.*;

public class StyledButton extends JButton {

    public StyledButton(String text, Color bg, Color fg) {
        super(text);
        setFont(new Font("SansSerif", Font.BOLD, 16));
        setForeground(fg);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Inline custom background and border rendering
        addPropertyChangeListener("UI", e -> repaint());
        
        // Custom component painting logic extracted from FlappyBird
        setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                ButtonModel model = getModel();
                if (model.isPressed()) {
                    g2.setColor(bg.darker());
                } else if (model.isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }

                // Pill shape background
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), c.getHeight(), c.getHeight());

                // Subtle white inner glow/border
                g2.setColor(new Color(255, 255, 255, 100));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, c.getWidth() - 3, c.getHeight() - 3, c.getHeight() - 2, c.getHeight() - 2);

                g2.dispose();
                super.paint(g, c);
            }
        });
    }

    public static JButton create(String text, Color bg, Color fg) {
        return new StyledButton(text, bg, fg);
    }
}