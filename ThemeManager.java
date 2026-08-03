
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ThemeManager {

    //  Theme definition 
    public static class Theme {

        public final String name;
        public final String backgroundPath;
        public final String[] birdFrames;       // downflap, midflap, upflap
        public final String bottomPipePath;
        public final String topPipePath;         // null → auto-flip bottomPipe
        public final Color groundTint;          // tint color for the base strip
        public final Color scoreOutline;        // shadow/outline color for score digits
        public final Color uiAccent;            // accent used in UI chrome
        public final Color uiAccent2;           // secondary accent

        public Theme(String name, String bg, String[] birdFrames,
                String bottomPipe, String topPipe,
                Color groundTint, Color scoreOutline,
                Color uiAccent, Color uiAccent2) {
            this.name = name;
            this.backgroundPath = bg;
            this.birdFrames = birdFrames;
            this.bottomPipePath = bottomPipe;
            this.topPipePath = topPipe;
            this.groundTint = groundTint;
            this.scoreOutline = scoreOutline;
            this.uiAccent = uiAccent;
            this.uiAccent2 = uiAccent2;
        }
    }

    //  All themes 
    private static final Theme[] ALL_THEMES = {
        // 0  Classic Day  –  the original Flappy Bird look
        new Theme("Classic Day",
        "assets/background-day.png",
        new String[]{
            "assets/yellowbird-downflap.png",
            "assets/yellowbird-midflap.png",
            "assets/yellowbird-upflap.png"
        },
        "assets/pipe-green.png",
        "assets/pipe-green-unpside.png",
        new Color(222, 216, 149), // sandy ground
        new Color(83, 56, 71), // dark score outline
        new Color(255, 215, 0), // gold accent
        new Color(76, 175, 80) // green accent
        ),
        // 1  Sunset City
        new Theme("Sunset City",
        "assets/Background/Background1.png",
        new String[]{
            "assets/redbird-downflap.png",
            "assets/redbird-midflap.png",
            "assets/redbird-upflap.png"
        },
        "assets/pipe-red.png",
        null,
        new Color(190, 130, 70),
        new Color(80, 40, 20),
        new Color(255, 152, 0),
        new Color(255, 235, 59)
        ),
        // 2  City Lights
        new Theme("City Lights",
        "assets/Background/Background3.png",
        new String[]{
            "assets/bluebird-downflap.png",
            "assets/bluebird-midflap.png",
            "assets/bluebird-upflap.png"
        },
        "assets/pipe-green.png",
        "assets/pipe-green-unpside.png",
        new Color(50, 90, 140),
        new Color(10, 30, 60),
        new Color(0, 229, 255),
        new Color(130, 180, 255)
        ),
        // 3  Starlit Sky
        new Theme("Starlit Sky",
        "assets/Background/Background5.png",
        new String[]{
            "assets/redbird-downflap.png",
            "assets/redbird-midflap.png",
            "assets/redbird-upflap.png"
        },
        "assets/pipe-red.png",
        null,
        new Color(40, 70, 120),
        new Color(10, 20, 50),
        new Color(224, 64, 251),
        new Color(156, 39, 176)
        ),
        // 4  Neon Noir
        new Theme("Neon Noir",
        "assets/Background/Background6.png",
        new String[]{
            "assets/yellowbird-downflap.png",
            "assets/yellowbird-midflap.png",
            "assets/yellowbird-upflap.png"
        },
        "assets/pipe-green.png",
        "assets/pipe-green-unpside.png",
        new Color(30, 30, 50),
        new Color(0, 0, 0),
        new Color(0, 255, 204),
        new Color(255, 0, 128)
        ),
        // 5  Pixel Bat
        new Theme("Pixel Bat",
        "assets/BatTheme/Background.png",
        new String[]{
            "assets/BatTheme/Bat.png",
            "assets/BatTheme/Bat.png",
            "assets/BatTheme/Bat.png"
        },
        "assets/BatTheme/Ostacolo_Up.png",
        "assets/BatTheme/Obstacle_Down.png",
        new Color(60, 60, 60), // dark grey ground tint
        new Color(20, 20, 20), // dark score outline
        new Color(138, 43, 226), // purple accent
        new Color(147, 112, 219) // lighter purple accent
        )
    };

    private int currentIndex = 0;

    //  Public API 
    public int getThemeCount() {
        return ALL_THEMES.length;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public Theme getCurrentTheme() {
        return ALL_THEMES[currentIndex];
    }

    public String getThemeName() {
        return getCurrentTheme().name;
    }

    public Theme getTheme(int i) {
        return ALL_THEMES[i];
    }

    public void setThemeIndex(int i) {
        if (i >= 0 && i < ALL_THEMES.length) {
            currentIndex = i;
        }
    }

    public void nextTheme() {
        currentIndex = (currentIndex + 1) % ALL_THEMES.length;
    }

    //  Asset loaders 
    public Image getBackgroundImg() {
        return safeLoad(getCurrentTheme().backgroundPath, "assets/background-day.png");
    }

    public Image getBirdImg() {
        Image[] f = getBirdFrames();
        return (f != null && f.length > 1) ? f[1] : f[0];   // midflap as default
    }

    /**
     * Returns 3-frame animation [down, mid, up].
     */
    public Image[] getBirdFrames() {
        Theme t = getCurrentTheme();
        Image[] frames = new Image[t.birdFrames.length];
        for (int i = 0; i < t.birdFrames.length; i++) {
            frames[i] = loadImage(t.birdFrames[i]);
        }
        if (frames[0] == null) {
            frames = new Image[]{loadImage("assets/yellowbird-midflap.png")};
        }
        return frames;
    }

    public Image getBottomPipeImg() {
        return safeLoad(getCurrentTheme().bottomPipePath, "assets/pipe-green.png");
    }

    public Image getTopPipeImg() {
        Theme t = getCurrentTheme();
        if (t.topPipePath != null) {
            Image img = loadImage(t.topPipePath);
            if (img != null) {
                return img;
            }
        }
        return flipVertically(getBottomPipeImg());
    }

    //  Utility 
    private Image safeLoad(String primary, String fallback) {
        Image img = loadImage(primary);
        return (img != null) ? img : loadImage(fallback);
    }

    public static Image flipVertically(Image img) {
        if (img == null) {
            return null;
        }
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        if (w <= 0 || h <= 0) {
            return img;
        }
        BufferedImage buf = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buf.createGraphics();
        g.drawImage(img, 0, h, w, -h, null);
        g.dispose();
        return buf;
    }

    public static Image loadImage(String path) {
        if (path == null) {
            return null;
        }
        // Try classpath resource first
        try {
            URL url = ThemeManager.class.getResource(path);
            if (url != null) {
                return new ImageIcon(url).getImage();
            }
        } catch (Exception ignored) {
        }
        // Try filesystem
        try {
            File f = new File(path);
            if (f.exists()) {
                return ImageIO.read(f);
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
