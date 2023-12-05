package entities;
import java.awt.geom.*;

public class Bomb extends Entity {
    static int initialBombWidth = 25;
    static int initialBombHeight = 25;
    static int attackingSizeIncrease = 100;
    static String inactiveImage = "assets/weapon.png";
    static String activeImage = "assets/weapon.png";
    public int ticksElapsedFromAttack;

    public boolean attacking = false;
    int playerW; 
    int playerH;
    public Bomb(int playerX, int playerY, int playerW, int playerH){
        super(playerX, playerY, initialBombWidth, initialBombHeight, inactiveImage);  
        this.playerH = playerH;
        this.playerW = playerW;
    }
    public void updateLocation(int playerX, int playerY){
        this.x = ((playerW / 2) + playerX) - (this.getWidth() / 2);
        this.y = ((playerH / 2) + playerY) - (this.getHeight() / 2);
    }
    public void attack (int playerX, int playerY){
        attacking = true;
        this.setImageName(activeImage);
        this.setHeight(initialBombHeight + attackingSizeIncrease);
        this.setWidth(initialBombWidth + attackingSizeIncrease);
        this.x = ((playerW / 2) + playerX) - (this.getWidth() / 2);
        this.y = ((playerH / 2) + playerY) - (this.getHeight() / 2);
    }
    public void deattack (int playerX, int playerY){
        attacking = false;
        this.setImageName(inactiveImage);
        this.setHeight(initialBombHeight);
        this.setWidth(initialBombWidth);
        this.x = ((playerW / 2) + playerX) - (this.getWidth() / 2);
        this.y = ((playerH / 2) + playerY) - (this.getHeight() / 2);
    }
}


