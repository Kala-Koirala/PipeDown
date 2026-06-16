
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

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

    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    Class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image Img){
            this.img = img;
        }

        Pipe(Image topImg, Image bottomImg) {
            this.topImg = topImg;
            this.bottomImg = bottomImg;
        }
    }

    Bird bird;
    int velocityX = -4;
    int velocityY = 0;
    int gravity = 1;

    ArrayList<Pipe> pipes;


    Timer gameLoop;
    Timer placePipeTimer;

    FlappyBird(){
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        backgroundImg = new ImageIcon("background-day.png").getImage();
        birdImg = new ImageIcon("yellowbird-upflag.png").getImage();
        topPipeImg = new ImageIcon("pipe-green.png").getImage();
        bottomPipeImg = new ImageIcon("pipe-red.png").getImage();

        bird = new Bird(birdImg);
        pipes = new ArrayList<>();

        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placepipes();
            }
        });

        gameLoop = new Timer(1000/60, this);
        gameLoop.start();

            }

    }

    public void placepipes() {
        Pipe topPipe = new Pipe(topPipeImg);
        pipes.add(topPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {

        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        public void move(){
            velocityY += gravity;
            bird.y += velocityY;
            bird.y = Math.max(bird.y, 0);
            
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            move();
            repaint();
        }


        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                velocityY = -9;
            }
           
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    }

}
