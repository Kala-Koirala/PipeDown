
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel{

    int boardWidth = 360;
    int boardHeight = 640;

    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;
//bird

    int birdX = boardWidth/8;
    int birdY =boardHeight/2;
    int birdWidth =34;
    int birdHeight=24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }
    FlappyBird(){
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.blue);

        backgroundImg = new ImageIcon("src/background-day.png").getImage();
        birdImg = new ImageIcon("src/yellowbird-upflag.png").getImage();
        topPipeImg = new ImageIcon("src/pipe-green.png").getImage();
        bottomPipeImg = new ImageIcon("src/pipe-red.png").getImage();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

    }

}
