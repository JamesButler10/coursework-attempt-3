/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bradford.spacegame;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.IntStream;
import static uk.ac.bradford.spacegame.Asteroid.Direction.DOWN;
import static uk.ac.bradford.spacegame.Asteroid.Direction.LEFT;
import static uk.ac.bradford.spacegame.Asteroid.Direction.RIGHT;
import static uk.ac.bradford.spacegame.Asteroid.Direction.UP;
import static uk.ac.bradford.spacegame.GameEngine.TileType.BLACK_HOLE;
import static uk.ac.bradford.spacegame.GameEngine.TileType.PULSAR_ACTIVE;
import static uk.ac.bradford.spacegame.GameEngine.TileType.PULSAR_INACTIVE;
import static uk.ac.bradford.spacegame.GameEngine.TileType.SPACE;

/**
 * The GameEngine class is responsible for managing information about the game,
 * creating levels, the player, aliens and asteroids, as well as updating
 * information when a key is pressed while the game is running.
 *
 * @author prtrundl
 */
public class GameEngine {

    /**
     * An enumeration type to represent different types of tiles that make up
     * the level. Each type has a corresponding image file that is used to draw
     * the right tile to the screen for each tile in a level. Space is open for
     * the player and asteroids to move into, black holes will kill the player
     * if they move into the tile and destroy asteroids that move into them,
     * pulsars will damage the player if they are in or adjacent to a pulsar
     * tile while it is active.
     */
    public enum TileType {
        SPACE, BLACK_HOLE, PULSAR_ACTIVE, PULSAR_INACTIVE
    }

    /**
     * The width of the level, measured in tiles. Changing this may cause the
     * display to draw incorrectly, and as a minimum the size of the GUI would
     * need to be adjusted.
     */
    public static final int GRID_WIDTH = 25;

    /**
     * The height of the level, measured in tiles. Changing this may cause the
     * display to draw incorrectly, and as a minimum the size of the GUI would
     * need to be adjusted.
     */
    public static final int GRID_HEIGHT = 18;

    /**
     * The chance of a black hole being generated instead of open space when
     * generating the level. 1.0 is 100% chance, 0.0 is 0% chance. This can be
     * changed to affect the difficulty.
     */
    private static final double BLACK_HOLE_CHANCE = 0.07;

    /**
     * The chance of a pulsar being created instead of open space when
     * generating the level. 1.0 is 100% chance, 0.0 is 0% chance. This can be
     * changed to affect the difficulty.
     */
    private static final double PULSAR_CHANCE = 0.03;

    /**
     * A random number generator that can be used to include randomised choices
     * in the creation of levels, in choosing places to spawn the player, aliens
     * and asteroids, and to randomise movement or other factors.
     */
    private Random rng = new Random(911);

    /**
     * The number of levels cleared by the player in this game. Can be used to
     * generate harder games as the player clears levels.
     */
    private int cleared = 0;

    /**
     * The number of points the player has gained this level. Used to track when
     * the current level is won and a new one should be generated.
     */
    private int points = 0;

    /**
     * Tracks the current turn number. Used to control pulsar activation and
     * asteroid movement.
     */
    private int turnNumber = 1;

    /**
     * The GUI associated with a GameEngine object. THis link allows the engine
     * to pass level (tiles) and entity information to the GUI to be drawn.
     */
    private GameGUI gui;

    /**
     * The 2 dimensional array of tiles the represent the current level. The
     * size of this array should use the GRID_HEIGHT and GRID_WIDTH attributes
     * when it is created.
     */
    private TileType[][] tiles;

    /**
     * An ArrayList of Point objects used to create and track possible locations
     * to spawn the player, aliens and asteroids.
     */
    private ArrayList<Point> spawns;

    /**
     * A Player object that is the current player. This object stores the state
     * information for the player, including hull strength and the current
     * position (which is a pair of co-ordinates that corresponds to a tile in
     * the current level)
     */
    private Player player;

    /**
     * An array of Alien objects that represents the aliens in the current
     * level. Elements in this array should be of the type Alien, meaning that
     * an alien is alive and needs to be drawn or moved, or should be null which
     * means nothing is drawn or processed for movement. Null values in this
     * array are skipped during drawing and movement processing.
     */
    private Alien[] aliens;

    /**
     * An array of Asteroid objects that represents the asteroids in the current
     * level. Elements in this array should be of the type Asteroid, meaning
     * that an asteroid exists and needs to be drawn or moved, or should be null
     * which means nothing is drawn or processed for movement. Null values in
     * this array are skipped during drawing and movement processing.
     */
    private Asteroid[] asteroids;

    /**
     * Constructor that creates a GameEngine object and connects it with a
     * GameGUI object.
     *
     * @param gui The GameGUI object that this engine will pass information to
     * in order to draw levels and entities to the screen.
     */
    public GameEngine(GameGUI gui) {
        this.gui = gui;
        startGame();
    }

    /**
     * Generates a level by creating a 2D array tiles using the given GRID_WIDTH
     * and GRID_HEIGHT to set the size of the array. Initially all the positions
     * are set to the enum value SPACE before the elements BLACK_HOLE and
     * PULSAR_INACTIVE are added using the random number generator rng and if
     * this value is smaller than set chance chance for that element it will add
     * the element in the specified position.
     */
    private TileType[][] generateLevel() {
//  creates 2D array with previously set sizing
        tiles = new TileType[GRID_WIDTH][GRID_HEIGHT];
//  works through all the positions in the 2D array and sets them to the tile type space
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                tiles[i][j] = TileType.SPACE;
            }
        }
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
//creates a double r and sets it too a random double value
                double r = rng.nextDouble();
//if r is less than or equal to the spawn chance of black holes it sets the tile value to black hole                
                if (r <= BLACK_HOLE_CHANCE) {
                    tiles[i][j] = TileType.BLACK_HOLE;
                }

            }
        }
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                double r = rng.nextDouble();
//if r is less than or equal to the spawn chance of pulsars it sets the tile value to black hole                
                if (r <= PULSAR_CHANCE) {
                    tiles[i][j] = TileType.PULSAR_INACTIVE;
                }

            }
        }
        return tiles;
    }

    /**
     * creates an array list spawns which contains point values. It first
     * empties the array list so that each time it is called it resets the
     * values which stops the array list just adding the new values to the list
     * each time its called. Using a for loop it then works through every
     * position in the 2d array and if its value is SPACE it adds the position
     * to the spawns array list.
     */
    private ArrayList<Point> getSpawns() {
        ArrayList<Point> spawns = new ArrayList<Point>();
//removes all values from the array list.        
        spawns.removeAll(spawns);
//checks if each position in tiles has the value SPACE        
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                if (tiles[i][j] == SPACE) {
//if the value is SPACE it adds the position to the array list.                    
                    int x = i;
                    int y = j;
                    Point position = new Point(x, y);
                    spawns.add(position);

                }

            }
        }
//returns the arraylist        
        return spawns;
    }

    /**
     * creates the array aliens with 3 positions. Generates a random value
     * within the range of the spawns array list. It then takes the point value
     * stored at this location and splits point to separate x and y values and
     * creates an alien with strength 5 in the given position.
     */
    private Alien[] spawnAliens() {
        aliens = new Alien[3];
        for (int i = 0; i < 3; i++) {
//generates a random value within the spawns array list             
            int r = (int) (Math.random() * spawns.size() + 1);
//splits the point value at the location r into usable coordinates            
            Point t = spawns.get(r);
            int a = (int) t.getX();
            int b = (int) t.getY();
//generates an alien and adds it to the alien array            
            Alien ali = new Alien(5, a, b);
            aliens[i] = ali;
        }
//returns the aliens array        
        return aliens;
    }

    /**
     * generates a player by taking a point value from the array list spawns and
     * then taking the x and y values from this point value. finally it
     * generates a player with hull strength and the set position before
     * returning the player.
     */
    private Player spawnPlayer() {
        int r = (int) (Math.random() * spawns.size() + 1);
//generates a random value within the spawns array list         
        Point t = spawns.get(r);
//splits the point value at the location r into usable coordinates 
        int a = (int) t.getX();
        int b = (int) t.getY();
//creates a player with 10 hull strenght and given coordinates and returns the player        
        player = new Player(10, a, b);
        return player;
    }

    /**
     * when the method is called it reduces the value of the players x
     * coordinate by 1. If moving left would take the player out of the array it
     * places the player in the highest x coordinate with the same y coordinate.
     * Before moving it checks if the target position is empty by checking if it
     * has the value SPACE and calling the asteroidCheck method. If the position
     * is empty it sets the players position to the new position. If there is an
     * asteroid in the position it still moves but the asteroidCheck method also
     * removes the asteroid.
     */
    public void movePlayerLeft() {
//reduces the x coordinate by 1        
        int i = player.getX() - 1;
        int j = player.getY();
//if the new position is out of the array it places the player in the position with the greatest possible x coordinate       
        if (i < 0) {
            player.setPosition(GRID_WIDTH - 1, j);
        } else {
//if the target tile is empty the player moves in to the new position otherwise it stays where it is            
            if (tiles[i][j] == SPACE && asteroidCheck() == true) {
                player.setPosition(i, j);
            } else {
                if (tiles[i][j] == SPACE) {
                    player.setPosition(i, j);
                }
            }
        }
    }

    /**
     * when the method is called it increases the value of the players x
     * coordinate by 1. If moving right would take the player out of the array
     * it places the player in the lowest x coordinate with the same y
     * coordinate. Before moving it checks if the target position is empty by
     * checking if it has the value SPACE and calling the asteroidCheck method.
     * If the position is empty it sets the players position to the new
     * position. If there is an asteroid in the position it still moves but the
     * asteroidCheck method also removes the asteroid.
     */
    public void movePlayerRight() {
//increases the x coordinate by 1  
        int i = player.getX() + 1;
        int j = player.getY();
//if the new position is out of the array it places the player in the position with the smallest possible x coordinate        
        if (i > 24) {
            player.setPosition(0, j);
        } else {
//if the target tile is empty the player moves in to the new position otherwise it stays where it is            
            if (tiles[i][j] == SPACE && asteroidCheck() == true) {
                player.setPosition(i, j);
            } else {
                if (tiles[i][j] == SPACE) {
                    player.setPosition(i, j);
                }
            }
        }
    }

    /**
     * when the method is called it decreases the value of the players y
     * coordinate by 1. If moving up would take the player out of the array it
     * places the player in the greatest y coordinate with the same x
     * coordinate. Before moving it checks if the target position is empty by
     * checking if it has the value SPACE and calling the asteroidCheck method.
     * If the position is empty it sets the players position to the new
     * position. If there is an asteroid in the position it still moves but the
     * asteroidCheck method also removes the asteroid.
     */
    public void movePlayerUp() {
//reduces the y coordinate by 1        
        int i = player.getX();
        int j = player.getY() - 1;
//if the new position is out of the array it places the player in the position with the greatest possible y coordinate        
        if (j < 0) {
            player.setPosition(i, GRID_HEIGHT - 1);
        } else {
//if the target tile is empty the player moves in to the new position otherwise it stays where it is            
            if (tiles[i][j] == SPACE && asteroidCheck() == true) {
                player.setPosition(i, j);
            } else {
                if (tiles[i][j] == SPACE) {
                    player.setPosition(i, j);
                }
            }
        }
    }

    /**
     * when the method is called it increases the value of the players y
     * coordinate by 1. If moving down would take the player out of the array it
     * places the player in the smallest y coordinate with the same x
     * coordinate. Before moving it checks if the target position is empty by
     * checking if it has the value SPACE and calling the asteroidCheck method.
     * If the position is empty it sets the players position to the new
     * position. If there is an asteroid in the position it still moves but the
     * asteroidCheck method also removes the asteroid.
     */
    public void movePlayerDown() {
//increases the y coordinate by 1 
        int i = player.getX();
        int j = player.getY() + 1;
//if the new position is out of the array it places the player in the position with the smallest possible y coordinate
        if (j > 17) {
            player.setPosition(i, 0);
        } else {
//if the target tile is empty the player moves in to the new position otherwise it stays where it is            
            if (tiles[i][j] == SPACE && asteroidCheck() == true) {
                player.setPosition(i, j);
            }
            if (tiles[i][j] == SPACE) {
                player.setPosition(i, j);
            }
        }
    }

    /**
     * Checks if the player is on the same tile as an asteroid. it gets the
     * players coordinates and for each asteroid that doesn't have a value null
     * checks if they are equal to the asteroids coordinates. if they are it
     * removes the asteroid and increases the players score. if the score
     * reaches 5 the newLevel method is called. if there is an asteroid in the
     * position it returns the value true and if not it returns the value false.
     */
    private boolean asteroidCheck() {
//gets the players coordinates        
        int i = player.getX();
        int j = player.getY();
//gets each asteroids coordinates one at a time        
        for (int k = 0; k < 10; k++) {
            if (asteroids[k] != null) {
                int x = asteroids[k].getX();
                int y = asteroids[k].getY();
//if the player is in the same position as the asteroid it sets the asteroids value to null
                if (i == x & j == y) {
                    asteroids[k] = null;
//increases the value of points by 1 and if its value is 5 calls the new level mehtod                    
                    points++;
                    if (points == 5) {
                        newLevel();
                    }
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Moves the asteroid in the given direction. If the asteroid is trying to
     * move out of the array or into a pulsar or black hole it will respawn the
     * asteroid in a different position.
     */
    private void moveAsteroids() {
        for (int k = 0; k < 10; k++) {
            int t = rng.nextInt(spawns.size());
//for each asteroid that is live it gets it x and y coordinates             
            if (asteroids[k] != null) {
                int x = asteroids[k].getX();
                int y = asteroids[k].getY();
//gets the movement direction of the asteroid from the asteroid class and changes it position in the given direction                
                if (asteroids[k].getMovementDirection() == UP) {
                    y--;
                }
                if (asteroids[k].getMovementDirection() == DOWN) {
                    y++;
                }
                if (asteroids[k].getMovementDirection() == LEFT) {
                    x--;
                }
                if (asteroids[k].getMovementDirection() == RIGHT) {
                    y++;
                }
//if the asteroid is trying to move out of the 2d array it respawns the asteroid in a new position form the spawns array list                
                if (x < 0 || y < 0 || x > GRID_WIDTH - 1 || y > GRID_HEIGHT - 1 || tiles[x][y] == PULSAR_ACTIVE || tiles[x][y] == PULSAR_INACTIVE || tiles[x][y] == BLACK_HOLE) {
                    Point s = spawns.get(t);
                    x = (int) s.getX();
                    y = (int) s.getY();
                }
//if the asteroid is trying to move in to a pulsar or black hole it respawns the asteroid in a new position form the spawns array list                
                if (tiles[x][y] == PULSAR_ACTIVE || tiles[x][y] == PULSAR_INACTIVE || tiles[x][y] == BLACK_HOLE) {
                    Point s = spawns.get(t);
                    x = (int) s.getX();
                    y = (int) s.getY();
                }
//places the asteroid in the new position if possible.                
                asteroids[k].setPosition(x, y);
            }
        }
    }

    /**
     * for each alien that is live it calls the moveAlien method and passes the
     * current alien to it
     */
    private void moveAliens() {
//checks how many aliens there are        
        int t = aliens.length;
        for (int a = 0; a < t; a++) {
//if the alien is live the moveAlien method is called.            
            if (aliens[a] != null) {
                moveAlien(aliens[a]);
            }
        }
    }

    /**
     * Gets the coordinates of the specific alien. a number between 0 and 3 is
     * randomly generated. Depending on the value of the random number it moves
     * in a set direction. if the alien is trying to move out of the map it
     * wraps round to the other side. It then gets the players coordinates and
     * if they are in the same position it damages the player removing 2 from
     * the hull strength. it then checks if the alien is in the same position as
     * an asteroid. If they are in the same position it minuses one from the
     * players score and respawns the asteroid in a different position.
     */
    private void moveAlien(Alien a) {
        int x = a.getX();
        int y = a.getY();
//sets t to a random value between 0 and 3        
        int t = rng.nextInt(4);
//if t is 0 and the position is clear it moves the alien one place right         
        if (t == 0 && x < GRID_WIDTH - 1) {
            if (tiles[x + 1][y] == SPACE) {
                x++;
                a.setPosition(x, y);
            }
        }
//if its trying to move right of the map it moves the alien to the opposite position        
        if (x + 1 > 24) {
            a.setPosition(0, y);
        }
        if (t == 1 && x > 0) {
//if t is 1 and the position is clear it moves the alien one place left             
            if (tiles[x - 1][y] == SPACE) {
                x--;
                a.setPosition(x, y);
            }
        }
//if its trying to move right of the map it moves the alien to the opposite position        
        if (x - 1 < 0) {
            a.setPosition(GRID_WIDTH - 1, y);

        }
        if (t == 2 && y < GRID_HEIGHT - 1) {
//if t is 2 and the position is clear it moves the alien one place up              
            if (tiles[x][y + 1] == SPACE) {
                y++;
                a.setPosition(x, y);
            }
        }
//if its trying to move up of the map it moves the alien to the opposite position         
        if (y + 1 > 17) {
            a.setPosition(x, 0);
        }
        if (t == 3 && y > 0) {
//if t is 3 and the position is clear it moves the alien one place down              
            if (tiles[x][y - 1] == SPACE) {
                y--;
                a.setPosition(x, y);
            }
        }
//if its trying to move down of the map it moves the alien to the opposite position        
        if (y - 1 < 0) {
            a.setPosition(x, GRID_HEIGHT - 1);
        }
//gets the players position and if they're in the same position reduces the player hull strength by 2         
        int b = player.getX();
        int c = player.getY();
        if (x == b && y == c) {
            player.hullStrength = player.hullStrength - 2;
        }
//gets all the asteroids position one at a time        
        for (int k = 0; k < 10; k++) {
            if (asteroids[k] != null) {
                int r = asteroids[k].getX();
                int s = asteroids[k].getY();
//if the asteroid and alien are in the same position it removes one from the players score                
                if (x == r && y == s) {
                    points--;
//if the asteroid and alien are in the same position it moves the asteroid to a new valid position                    
                    int d = (int) (Math.random() * spawns.size() + 1);
                    Point e = spawns.get(d);
                    int f = (int) e.getX();
                    int g = (int) e.getY();
                    asteroids[k].setPosition(f, g);
                }
            }
        }

    }

    /**
     * spawns 10 asteroids and places them in the array asteroids. It randomly
     * chooses a position in the spawns array list splints the point value in to
     * coordinates and creates an asteroid using these coordinates and adds it
     * to the array. it then removes the position form the spawns array list.
     * finally it returns the asteroids array.
     */
    private Asteroid[] spawnAsteroids() {
//creates the array of Asteroid objects called asteroids of size 10        
        asteroids = new Asteroid[10];
        for (int i = 0; i < 10; i++) {
//chooses a random position in the spawns array list and gets the coordinates of the point            
            int r = (int) (Math.random() * spawns.size() + 1);
            Point t = spawns.get(r);
            int a = (int) t.getX();
            int b = (int) t.getY();
//creates an asteroid in the position of the coordinates and adds it to the array.             
            Asteroid ast = new Asteroid(a, b);
            asteroids[i] = ast;
//removes the position from the spawns array list            
            spawns.remove(r);
        }
        return asteroids;
    }

    /**
     * When called it searches through the tiles 2D array and sets the value of
     * any position that is currently an inactive pulsar to an active pulsar
     */
    private void activatePulsars() {
//works through the tiles array        
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
//if a position is equal to PULSAR_INACTIVE it sets it to PULSAR_ACTIVE instead                
                if (tiles[i][j] == PULSAR_INACTIVE) {
                    tiles[i][j] = PULSAR_ACTIVE;
                }
            }

        }
    }

    /**
     * When called it searches through the tiles 2D array and sets the value of
     * any position that is currently an active pulsar to an inactive pulsar
     */
    private void deactivatePulsars() {
//works through the tiles array          
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
//if a position is equal to PULSAR_INACTIVE it sets it to PULSAR_ACTIVE instead                 
                if (tiles[i][j] == PULSAR_ACTIVE) {
                    tiles[i][j] = PULSAR_INACTIVE;
                }
            }

        }
    }

    /**
     * Checks if the player is in a position next to an active pulsar. Gets the
     * coordinates of the player and removes 1 from both the x and y coordinate.
     * if this position has a coordinate less than 0 it adds one to that value.
     * and removes 1 form either a or b. if x or y, + 2 is greater than the
     * range of the array it removes 1 from either a or b. using the generated
     * values it searches around the player to check if there is an active
     * pulsar and if there is it removes, 2 + the number of cleared levels, from
     * the players hull strength
     */
    private void pulsarDamage() {
//takes the coordinates of the player and removes one from each        
        int x = player.getX() - 1;
        int y = player.getY() - 1;
        int a = 3;
        int b = 3;
        if (x < 0) {
//if x is less than 0 it adds 1 and removes 1 from a            
            x++;
            a--;
        }
        if (y < 0) {
//if y is less than 0 it adds 1 and removes 1 from b            
            y++;
            b--;
        }
        if (x + 2 > GRID_WIDTH - 1) {
//if x+2(one to the right of the player) is greater than the grid width it removes 1 from a            
            a--;
        }
        if (y + 2 > GRID_HEIGHT - 1) {
//if y+2(one above the player) is greater than the grid height it removes 1 from b             
            b--;
        }
//searches the positions around the player within the tiles 2D array         
        for (int s = x; s < x + a; s++) {
            for (int t = y; t < y + b; t++) {
//if any of these positions contain an active pulsar it removes points from the players hull strenght               
                if (tiles[s][t] == PULSAR_ACTIVE) {
                    player.hullStrength = player.hullStrength - 2 - cleared;
                }
            }

        }
    }

    /**
     * When called adds one to the value of cleared and resets the value of
     * points it then calls all the methods needed to generate a new level
     */
    private void newLevel() {
        cleared++;
        points = 0;
        generateLevel();
        getSpawns();
        spawnAsteroids();
        spawnAliens();
        placePlayer();
    }

    /**
     * Gets a random position from the spawns array list. It then sets the
     * players position to this value on the newly generated level. it then
     * removes that position from the array list
     */
    private void placePlayer() {
//chooses a random value from the spawns array list        
        int r = (int) (Math.random() * spawns.size() + 1);
        Point t = spawns.get(r);
        int a = (int) t.getX();
        int b = (int) t.getY();
//places the player in the set position        
        player.setPosition(a, b);
//removes the position from spawns        
        spawns.remove(r);
    }

    /**
     * Performs a single turn of the game when the user presses a key on the
     * keyboard. This method activates or deactivates pulsars periodically by
     * using the turn attribute, moves any aliens and asteroids and then checks
     * if the player is dead, exiting the game or resetting it. It checks if the
     * player has collected enough asteroids to win the level and calls the
     * method if it does. Finally it requests the GUI to redraw the game level
     * by passing it the tiles, player, aliens and asteroids for the current
     * level.
     */
    public void doTurn() {
        if (turnNumber % 20 == 0) {
            activatePulsars();
        }
        if (turnNumber % 20 == 5) {
            deactivatePulsars();
        }
        if (turnNumber % 10 == 5) {
            moveAsteroids();
        }
        moveAliens();
        pulsarDamage();
        if (player.getHullStrength() < 1) {
            System.exit(0);
        }
        if (points >= 5) {
            newLevel();
        }
        gui.updateDisplay(tiles, player, aliens, asteroids);
        turnNumber++;
    }

    /**
     * Starts a game. This method generates a level, finds spawn positions in
     * the level, spawns aliens, asteroids and the player and then requests the
     * GUI to update the level on screen using the information on tiles, player,
     * asteroids and aliens.
     */
    public void startGame() {
        tiles = generateLevel();
        spawns = getSpawns();
        asteroids = spawnAsteroids();
        aliens = spawnAliens();
        player = spawnPlayer();
        gui.updateDisplay(tiles, player, aliens, asteroids);
    }
}
