package sprite;

import java.awt.*;

public class Sprite {
    private boolean visible;
    private Image image;
    private boolean dying;

    int x;
    int y;
    int dx;

    public Sprite() {
        visible = true;
    }

    public void die() {
        visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    protected void setVisible(boolean visible){
        this.visible = visible;
    }

    // Sets Image of Sprite
    public void setImage(Image image) {
        this.image = image;
    }

    // Returns Image of Sprite
    public Image getImage() {
        return image;
    }

    // Sets X Coordinate of Sprite
    public void setX(int x){
        this.x = x;
    }

    // Sets Y Coordinate of Sprite
    public void setY(int y) {
        this.y = y;
    }

    // Returns Y Coordinate of Sprite
    public int getY() {
        return y;
    }

    // Returns X Coordinate of Sprite
    public int getX() {
        return x;
    }

    // Sets Dying var
    public void setDying(boolean dying) {
        this.dying = dying;
    }

    // Returns if Sprite is Dying
    public boolean isDying() {
        return dying;
    }
}
