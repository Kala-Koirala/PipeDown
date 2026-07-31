import java.awt.Image;

public class Pipe {

    int x;
    int y;
    int width;
    int height;
    Image img;
    boolean passed = false;

    Pipe(int x, int y, int width, int height, Image img) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.img = img;
    }
}