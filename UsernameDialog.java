import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class UsernameDialog extends JDialog {
    private JTextField textField;
    private String username = "Player";
    private Runnable onSubmitCallback;
    private BufferedImage bgImage;

    public UsernameDialog(Frame owner, Runnable onSubmitCallback) {
        super(owner, "User Name", false); // modeless
        this.onSubmitCallback = onSubmitCallback;
        setUndecorated(true); // no title bar, no minimize/close buttons
        setLayout(null);
        setSize(300, 300); // matches textbox_bg.jpeg dimensions
        setLocationRelativeTo(owner);
        setResizable(false);

        // Load background image (bird/sky/grass asset)
        try {
            bgImage = ImageIO.read(new File("textbox_bg.jpeg"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Content panel draws the background image instead of a black overlay
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        contentPanel.setOpaque(false);
        contentPanel.setLayout(null);
        setContentPane(contentPanel);

        // "Player Name:" label, placed in the empty space above the textfield
        JLabel nameLabel = new JLabel("Player Name:", JLabel.CENTER);
        nameLabel.setBounds(70, 108, 160, 24);
        nameLabel.setFont(new Font("Monospaced", Font.BOLD, 15));
        nameLabel.setForeground(new Color(0, 51, 102)); // dark navy, contrasts against light sky
        contentPanel.add(nameLabel);

        textField = new JTextField();
        textField.setBounds(65, 135, 170, 28);
        textField.setFont(new Font("Monospaced", Font.BOLD, 16));
        textField.setForeground(Color.DARK_GRAY);
        textField.setBackground(new Color(245, 222, 179)); // wheat background
        textField.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
        textField.setHorizontalAlignment(JTextField.CENTER);
        contentPanel.add(textField);

        JButton btn = new JButton("Enter");
        btn.setFont(new Font("Monospaced", Font.BOLD, 16));
        btn.setBackground(new Color(222, 184, 135));
        btn.setForeground(Color.DARK_GRAY);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        btn.setBounds(40, 195, 100, 40);
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitName();
            }
        });
        contentPanel.add(btn);

        JButton quitBtn = new JButton("Quit");
        quitBtn.setFont(new Font("Monospaced", Font.BOLD, 16));
        quitBtn.setBackground(new Color(222, 184, 135));
        quitBtn.setForeground(Color.DARK_GRAY);
        quitBtn.setFocusPainted(false);
        quitBtn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        quitBtn.setBounds(160, 195, 100, 40);
        quitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HighScoreManager.clearSavedData();
                System.exit(0);
            }
        });
        contentPanel.add(quitBtn);

        // Allow pressing Enter in text field to submit
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitName();
            }
        });
    }

    private void submitName() {
        String input = textField.getText().trim();
        if (!input.isEmpty()) {
            username = input;
        }
        // Save to file
        try (PrintWriter writer = new PrintWriter(new FileWriter("player_name.txt"))) {
            writer.println(username);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (onSubmitCallback != null) {
            onSubmitCallback.run();
        }
        dispose();
    }

    public String getUsername() {
        return username;
    }
}