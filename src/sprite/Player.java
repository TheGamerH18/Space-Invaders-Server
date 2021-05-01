package sprite;

import server.Commons;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class Player extends Sprite{

    private int width;

    public Player() {
        initPlayer();
    }

    // Hidden Init Setting start Position and Image
    private void initPlayer() {
        ImageIcon ii = new ImageIcon("src/images/player.png");
        width = ii.getImage().getWidth(null);
        setImage(ii.getImage());

        int START_X = 270;
        setX(START_X);

        int START_Y = 280;
        setY(START_Y);
    }

    // Moving player based on dx var (Changed by Pressing key)
    public void act() {
        x += dx;
        if(x <= 2) x = 2;
        if(x >= Commons.BOARD_WIDTH - 2 * width) x = Commons.BOARD_WIDTH - 2 * width;
    }

    // Called when a key is pressed. Sets dx var for Movement
    public void keyPressed(KeyEvent e){
        int key = e.getKeyCode();
        if(key == KeyEvent.VK_LEFT) dx = -2;
        if(key == KeyEvent.VK_RIGHT) dx = 2;
    }

    // Called when a key is released. Sets dx var to end Movement
    public void keyReleased(KeyEvent e){
        int key = e.getKeyCode();
        if(key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) dx = 0;
    }
}
