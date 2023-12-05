package entities;
import java.util.Random;

public class Enemy extends Entity implements Collectable, Scrollable {
    Random rand = new Random();

    //Location of image file to be drawn for an Avoid
    //Dimensions of the Avoid    
    public static final int AVOID_WIDTH = 75;
    public static final int AVOID_HEIGHT = 75;
    //Speed that the avoid moves each time the game scrolls
    public static final int AVOID_SCROLL_SPEED = 5;

    public Enemy(int x, int y, String imageFile){
        super(x, y, AVOID_WIDTH, AVOID_HEIGHT, imageFile);
        
    }
    
    
    public int getScrollSpeed(){
        return AVOID_SCROLL_SPEED;
    }
    
    //Move the avoid left by the scroll speed
    public void scroll(){
        setX(getX() - AVOID_SCROLL_SPEED);
    }
    public void followPlayer(int currPlayerX, int currPlayerY, int ticks){
        double value = 0.015;
        if (this.x != currPlayerX || this.y != currPlayerY) {
            int interpolatedValueX = (int) Math.floor(this.x + value * (currPlayerX - this.x));
            int interpolatedValueY = (int) Math.floor(this.y + value * (currPlayerY - this.y));
            setX(interpolatedValueX);
            setY(interpolatedValueY);

        }
    }
    
    //Colliding with an Avoid does not affect the player's score
    public int getPoints(){
       return 10;
    }
    
    //Colliding with an Avoid Reduces players HP by 1
    public int getDamage(){
        return -1;
    }
    
}
