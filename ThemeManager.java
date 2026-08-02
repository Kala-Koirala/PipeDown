import java.awt.Image;
import javax.swing.ImageIcon;

public class ThemeManager {

    private String[] themeNames = { "Day", "Night", "Blue" };
    private String[] backgroundPaths = {
            "assets/background-day.png",
            "assets/background-night.png",
            "assets/background-night.png"
    };
    private String[] birdPaths = {
            "assets/yellowbird-upflap.png",
            "assets/redbird-upflap.png",
            "assets/bluebird-upflap.png"
    };

    private int currentIndex = 0;

    public void nextTheme() {
        currentIndex = (currentIndex + 1) % themeNames.length;
    }

    public String getThemeName() {
        return themeNames[currentIndex];
    }

    public Image getBackgroundImg() {
        return loadImage(backgroundPaths[currentIndex]);
    }

    public Image getBirdImg() {
        return loadImage(birdPaths[currentIndex]);
    }

    private Image loadImage(String path) {
        return new ImageIcon(getClass().getResource(path)).getImage();
    }
}
