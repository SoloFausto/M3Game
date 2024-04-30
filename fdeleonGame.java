import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.io.*; 

import entities.Bomb;
import entities.Collectable;
import entities.Enemy;
import entities.Entity;
import entities.Get;
import entities.Player;
import entities.SpecialGet;

//The basic ScrollingGame, featuring Avoids, Gets, and SpecialGets
//Players must reach a score threshold to win
//If player runs out of HP (via too many Avoid collisions) they lose
public class fdeleonGame extends GameEngine {

    // Starting Player coordinates
    protected static final int STARTING_PLAYER_X = 0;
    protected static final int STARTING_PLAYER_Y = 100;

    // Score needed to win the game
    protected static final int SCORE_TO_WIN = 300;

    // Maximum that the game speed can be increased to
    // (a percentage, ex: a value of 300 = 300% speed, or 3x regular speed)
    protected static final int MAX_GAME_SPEED = 300;
    // Interval that the speed changes when pressing speed up/down keys
    protected static final int SPEED_CHANGE = 20;

    protected static final String INTRO_SPLASH_FILE = "assets/background.png";
    // Key pressed to advance past the splash screen
    public static final int ADVANCE_SPLASH_KEY = KeyEvent.VK_ENTER;

    // Interval that Entities get spawned in the game window
    // ie: once every how many ticks does the game attempt to spawn new Entities
    protected static final int SPAWN_INTERVAL = 90;

    // A Random object for all your random number generation needs!
    protected static final Random rand = new Random();

    // Player's current score
    protected int score;
    protected int beforeKey;
    // Stores a reference to game's Player object for quick reference
    // (This Player will also be in the displayList)
    protected Player player;
    protected Bomb bomb;
    volatile String selectedTaskName = null;
    private volatile CompletableFuture<ProcessHandle> selectedTask;

    public fdeleonGame() {
        super();
    }

    public fdeleonGame(int gameWidth, int gameHeight) {
        super(gameWidth, gameHeight);
    }

    // Performs all of the initialization operations that need to be done before the
    // game starts
    protected void pregame() {
        if(!(System.getProperty("os.name").toLowerCase().indexOf("win") <= 0)){
            System.out.println("This game only works on a Windows OS.");
            System.exit(0);
        }
        super.setSplashImage("assets/playBackground.gif");
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        TaskPicker taskPicker = new TaskPicker();
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
            if(taskPicker.getTextFieldInput() != null){
                selectedTaskName = taskPicker.getTextFieldInput();
                break;
            }
            
        }
        // System.out.println( selectedTaskName);
        selectedTask = CompletableFuture.supplyAsync(() -> TaskPicker.getWindowProcessHandler(selectedTaskName));
        super.setSplashImage(INTRO_SPLASH_FILE);
        player = new Player(STARTING_PLAYER_X, STARTING_PLAYER_Y);
        displayList.add(player);
        bomb = new Bomb(STARTING_PLAYER_X, STARTING_PLAYER_Y, player.getWidth(), player.getHeight());
        displayList.add(bomb);
        score = 0;
        this.setBackgroundImage("assets/playBackground.gif");
    }
    // Called on each game tick
    protected void updateGame() {
        if (bomb.attacking && bomb.ticksElapsedFromAttack == 35) {
            bomb.deattack(player.x, player.y);
            bomb.ticksElapsedFromAttack = 0;
        } else {
            bomb.ticksElapsedFromAttack += 1;
        }
        handleOffScreen();
        // move all movable Entities on the game board
        moveEntities();
        // Spawn new entities only at a certain interval
        if (super.getTicksElapsed() % SPAWN_INTERVAL == 0) {
            spawnEntities();
        }
        for (int i = 0; i < displayList.size(); i++) {
            if (displayList.get(i).isCollidingWith(displayList.get(0))) {
                if (displayList.get(i) instanceof Player || bomb.attacking) {
                    continue;
                } else if (displayList.get(i) instanceof Bomb) {
                    Bomb bomb = (Bomb) displayList.get(i);
                    bomb.updateLocation(player.x, player.y);
                    continue;
                }
                handlePlayerCollision((Collectable) displayList.get(i), i);
            } else if (displayList.get(i).isCollidingWith(displayList.get(1))) {
                if (displayList.get(i) instanceof Player || displayList.get(i) instanceof Bomb) {
                    continue;
                }
                displayList.get(i).setImageName("/assets/death.gif");
                handleBombCollision(displayList.get(i), i);
            } else if (displayList.get(i) instanceof Enemy) {
                for (Entity collidingEntity : displayList) {
                    Entity sourceEntity = displayList.get(i);
                    if (sourceEntity.isCollidingWith(collidingEntity) && sourceEntity != collidingEntity) {
                        sourceEntity.preventSuperposition(collidingEntity);
                    }
                }
            }
        }
        gcOutOfWindowEntities();
        // Update the title text on the top of the window
        setTitleText(String.format("HP: %d, Score: %d", player.getHP(), score));
        if (determineIfGameIsOver()) {
            if (player.getHP() <= 0) {
                super.setTitleText("GAME OVER - You Lose!");
            } else if (this.score >= 300) {
                
                 try {selectedTask.get().destroyForcibly();} catch (InterruptedException | ExecutionException e) {e.printStackTrace();}
                    
                
                super.setTitleText("GAME OVER - You Won!");
            }
        }
    }

    // Scroll all scrollable entities per their respective scroll speeds
    protected void moveEntities() {
        for (int i = 0; i < displayList.size(); i++) {
            if (displayList.get(i) instanceof Enemy) {
                Enemy currentScrollable = (Enemy) displayList.get(i);
                currentScrollable.followPlayer(player.x, player.y, super.getTicksElapsed());
            } else if (displayList.get(i) instanceof Get) {
                Get currentScrollable = (Get) displayList.get(i);
                currentScrollable.setX(currentScrollable.getX() - currentScrollable.getScrollSpeed());
            }
        }
    }

    // Handles "garbage collection" of the displayList
    // Removes entities from the displayList that have scrolled offscreen
    // (i.e. will no longer need to be drawn in the game window).
    protected void gcOutOfWindowEntities() {
        ArrayList<Integer> entitiesToRemove = new ArrayList<>();
        for (int i = 0; i < displayList.size(); i++) {
            Entity currEntity = displayList.get(i);
            if (currEntity.getX() + currEntity.getWidth() < 0 && !(currEntity instanceof Player)) {
                entitiesToRemove.add(i);
            }
        }
        for (Integer entityIndex : entitiesToRemove) {
            displayList.remove(entityIndex.intValue());
        }

    }

    // Called whenever it has been determined that the Player collided with a
    // collectable
    protected void handlePlayerCollision(Collectable collidedWith, int i) {
        Player player = (Player) displayList.get(0);
            displayList.remove(i);
            score += collidedWith.getPoints();
            player.modifyHP(collidedWith.getDamage());
    }

    protected void handleBombCollision(Entity collidedWith, int i) {
        if (collidedWith instanceof Enemy) {
            displayList.remove(i);
            int pointValue = ((Enemy)collidedWith).getPoints();
            score += pointValue;
        }
    }

    // Spawn new Entities on the right edge of the game window
    protected void spawnEntities() {
        // From the depths of the unknown, they emerge with a cryptic call,
        // The unseen and enigmatic entities, shadows on the wall.
        int ammountAvoid = rand.nextInt(3 - 2) + 2;
        for (int i = 0; i < ammountAvoid; i++) {
            int randY = rand.nextInt(getWindowHeight());
            int randomImageNumber = rand.nextInt(21 - 1 + 1) + 1;

            String AVOID_IMAGE_FILE = "assets/monsterIcons/" + randomImageNumber + ".png";
            int randomSpawn = rand.nextInt(2 - 1 + 1) + 1 == 2 ?  getWindowWidth() : -10;
            Enemy getAvoid = new Enemy(randomSpawn, randY, AVOID_IMAGE_FILE);

            // Lost in a web of paradox, they seek their place in time,
            // In the dance of creation, a purpose that's sublime.
            while (collidingWithAll(getAvoid)) {
                getAvoid.setY(rand.nextInt(getWindowHeight()));
            }
            displayList.add(getAvoid);
        }

        // And from the darkest corners, where dreams and desires intertwine,
        // The Collectibles arise, their enigma as their sign.

        int ammountGetSpecial = rand.nextInt(2 - 1) + 1;
        for (int i = 0; i < ammountGetSpecial; i++) {
            // From the void's hidden depths, emerges a rare, mystic kind,
            // SpecialGet, an enigma, in the shadows they unwind.
            int randY = rand.nextInt(getWindowHeight());
            SpecialGet getSpecial = new SpecialGet(getWindowWidth(), randY);

            // In the tangled web of existence, they evade the common eye,
            // A dance with destiny, beneath a shrouded sky.
            while (collidingWithAll(getSpecial)) {
                getSpecial.setY(rand.nextInt(getWindowHeight()));
            }
            displayList.add(getSpecial);
        }
        // A tapestry of destiny, woven with threads of the obscure,
        // As entities manifest, in this realm, ever impure.
    }

    boolean collidingWithAll(Entity ent) {
        for (int i = 0; i < displayList.size(); i++) {
            if (ent.isCollidingWith(displayList.get(i)) || (ent.getY() + ent.getHeight()) > getWindowHeight()
                    || (ent.getY() < 0)) {
                return true;
            }
        }
        return false;
    }

    // Called once the game is over, performs any end-of-game operations
    protected void postgame() {
        if(player.getHP() <= 0){
            displayList.clear();
            this.setBackgroundImage("/assets/bsod.png");
        }
        else if(score >= SCORE_TO_WIN){
            displayList.clear();
            this.setBackgroundImage("/assets/winbg.png");
        }
       
    }

    // Determines if the game is over or not
    // Game can be over due to either a win or lose state
    protected boolean determineIfGameIsOver() {
        Player player = (Player) displayList.get(0);
        if (player.getHP() <= 0 || this.score >= 300) {
            return true;
        }
        return false;
    }

    public void handleOffScreen() {
        Player player = (Player) displayList.get(0);
        if (player.getX() < 0) {
            player.setX(0);
        }
        if (player.getY() < 0) {
            player.setY(0);
        }
        if (player.getY() + player.getHeight() > this.getWindowHeight()) {
            player.setY(this.getWindowHeight() - player.getHeight());
        }
        if (player.getX() + player.getWidth() > this.getWindowWidth()) {
            player.setX(this.getWindowWidth() - player.getWidth());
        }
    }

    // Reacts to a single key press on the keyboard
    protected void reactToKey(int key) {
        setDebugText("Key Pressed!: " + KeyEvent.getKeyText(key) + ",  DisplayList size: " + displayList.size());
        if (key == KEY_PAUSE_GAME) {
            this.isPaused = !this.isPaused;
        } else if (key == KEY_QUIT_GAME) {
            System.exit(0);
        } else if (!isPaused) {
            switch (key) {
                case ADVANCE_SPLASH_KEY: // if a splash screen is active, only react to the "advance splash" key...
                                         // nothing else!
                    if (getSplashImage() != null) {
                        super.setSplashImage(null);
                    }
                    break;
                case RIGHT_KEY:
                    player.setX(player.getX() + player.getMovementSpeed());
                    break;
                case LEFT_KEY:
                    player.setX(player.getX() - player.getMovementSpeed());
                    break;
                case UP_KEY:
                    player.setY(player.getY() - player.getMovementSpeed());
                    break;
                case DOWN_KEY:
                    player.setY(player.getY() + player.getMovementSpeed());
                    break;
                case SPEED_UP_KEY:
                    super.setGameSpeed(super.getGameSpeed() + 10);
                case SPEED_DOWN_KEY:
                    super.setGameSpeed(super.getGameSpeed() - 10 <= 0 ? 10 : super.getGameSpeed() - 10);
                case KeyEvent.VK_SPACE:
                    if (beforeKey == KeyEvent.VK_SPACE) {
                        break;
                    }
                    bomb.attack(player.x, player.y);
                    bomb.ticksElapsedFromAttack = 0;

                default:
                    break;
            }
            beforeKey = key;
        }
    }

    // Handles reacting to a single mouse click in the game window
    // Won't be used in Milestone #2... you could use it in Creative Game though!
    protected MouseEvent reactToMouseClick(MouseEvent click) {
        if (click != null) { // ensure a mouse click occurred
            int clickX = click.getX();
            int clickY = click.getY();
            setDebugText("Click at: " + clickX + ", " + clickY);
        }
        return click;// returns the mouse event for any child classes overriding this method
    }

}
