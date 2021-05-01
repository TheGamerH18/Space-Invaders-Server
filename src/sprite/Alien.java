package sprite;

import javax.swing.*;

public class Alien extends Sprite {

    private Bomb bomb;

    // Constructor with the Spawn Position of the Alien
    public Alien(int x, int y){
        initAlien(x, y);
    }

    // Hidden Init
    // Creating Vars and selecting Icon
    private void initAlien(int x, int y){
        this.x = x;
        this.y = y;

        bomb = new Bomb(x, y);

        ImageIcon ii = new ImageIcon("src/images/alien.png");
        setImage(ii.getImage());
    }

    // Moving Alien to the Left or Right based on Arg
    public void act(int direction){
        x += direction;
    }

    public Bomb getBomb() {
        return bomb;
    }

    // Bomb Class
    public static class Bomb extends Sprite {

        private boolean destroyed;

        public Bomb(int x, int y) {
            initBomb(x, y);
        }

        // Hidden Init Bomb with Spawn Position, Bomb moved in Board - Sets Image
        private void initBomb(int x, int y) {
            setDestroyed(true);

            this.x = x;
            this.y = y;

            ImageIcon ii = new ImageIcon("src/images/bomb.png");
            setImage(ii.getImage());
        }

        public void setDestroyed(boolean destroyed) {
            this.destroyed = destroyed;
        }

        public boolean isDestroyed() {
            return destroyed;
        }
    }
}
