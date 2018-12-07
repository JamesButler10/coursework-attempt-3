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
     * Generates a new level. The method builds a 2D array of TileTypes that
     * will be used to draw tiles to the screen and to add a variety of elements
     * into each level. Tiles can be space, black holes, active pulsars or
     * inactive pulsars. This method should contain the implementation of an
     * algorithm to create an interesting and varied level each time it is
     * called.
     *
     * @return A 2D array of TileTypes representing the tiles in the current
     * level of the dungeon. The size of this array should use the width and
     * height attributes of the level specified by GRID_WIDTH and GRID_HEIGHT.
     */
    private TileType[][] generateLevel() {
        tiles = new TileType[GRID_WIDTH][GRID_HEIGHT];
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                tiles[i][j] = TileType.SPACE;
            }
        }
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                double r = rng.nextDouble();
                if (r <= BLACK_HOLE_CHANCE) {
                    tiles[i][j] = TileType.BLACK_HOLE;
                }

            }
        }
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                double r = rng.nextDouble();
                if (r <= PULSAR_CHANCE) {
                    tiles[i][j] = TileType.PULSAR_INACTIVE;
                }

            }
        }
        return tiles;
    }

    /**
     * Generates spawn points for entities. The method processes the tiles array
     * and finds tiles that are suitable for spawning, i.e. space tiles.
     * Suitable tiles should be added to the ArrayList that will be returned as
     * Point objects - Points are a simple kind of object that contain an X and
     * a Y co-ordinate stored using the int primitive type.
     *
     * @return An ArrayList containing Point objects representing suitable X and
     * Y co-ordinates in the current level that entities can be spawned in.
     */
    private ArrayList<Point> getSpawns() {
        ArrayList<Point> spawns = new ArrayList<Point>();
        spawns.removeAll(spawns);
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                if (tiles[i][j] == SPACE) {
                    int x = i;
                    int y = j;
                    Point position = new Point(x, y);
                    spawns.add(position);

                }

            }
        }
        //add code here to generate Point objects witrh suitable X and Y co-ordinates
        return spawns;
    }

    /**
     * Spawns aliens in suitable locations in the current level. The method uses
     * the spawns ArrayList to pick suitable positions to add aliens, removing
     * these positions from the spawns ArrayList as they are used (using the
     * remove() method) to avoid multiple entities spawning in the same
     * location. The method creates aliens by instantiating the Alien class,
     * setting health and the X and Y position for the alien using the Point
     * object removed from the spawns ArrayList.
     *
     * @return An array of Alien objects representing the aliens for the current
     * level
     */
    private Alien[] spawnAliens() {
        aliens = new Alien[3];
        for (int i = 0; i < 3; i++) {
            int r = (int) (Math.random() * spawns.size() + 1);
            Point t = spawns.get(r);
            int a = (int) t.getX();
            int b = (int) t.getY();
            Alien ali = new Alien(5, a, b);
            aliens[i] = ali;
        }
        return aliens;            //modify to return array of Alien objects
    }

    /**
     * Spawns a Player entity in the game. The method uses the spawns ArrayList
     * to select a suitable location to spawn the player and removes the Point
     * from the spawns ArrayList. The method instantiates the Player class and
     * assigns values for the health and position of the player.
     *
     * @return A Player object representing the player in the game
     */
    private Player spawnPlayer() {
        int r = (int) (Math.random() * spawns.size() + 1);
        Point t = spawns.get(r);
        int a = (int) t.getX();
        int b = (int) t.getY();
        player = new Player(10, a, b);
        return player;
    }

    /**
     * Handles the movement of the player when attempting to move left in the
     * game. This method is called by the InputHandler class when the user has
     * pressed the left arrow key on the keyboard. The method checks whether the
     * tile to the left of the player is empty for movement and if it is updates
     * the player object's X and Y locations with the new position. If the tile
     * to the left of the player is not empty the method will not update the
     * player position, but could make other changes to the game.
     */
    public void movePlayerLeft() {
        int i = player.getX() - 1;
        int j = player.getY();
        if (i < 0) {
            i = i++;
        } else {
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
     * Handles the movement of the player when attempting to move right in the
     * game. This method is called by the InputHandler class when the user has
     * pressed the right arrow key on the keyboard. The method checks whether
     * the tile to the right of the player is empty for movement and if it is
     * updates the player object's X and Y locations with the new position. If
     * the tile to the right of the player is not empty the method will not
     * update the player position, but could make other changes to the game.
     */
    public void movePlayerRight() {
        int i = player.getX() + 1;
        int j = player.getY();
        if (i > 24) {
            i = i - 1;
        } else {
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
     * Handles the movement of the player when attempting to move up in the
     * game. This method is called by the InputHandler class when the user has
     * pressed the up arrow key on the keyboard. The method checks whether the
     * tile above the player is empty for movement and if it is updates the
     * player object's X and Y locations with the new position. If the tile
     * above the player is not empty the method will not update the player
     * position, but could make other changes to the game.
     */
    public void movePlayerUp() {
        int i = player.getX();
        int j = player.getY() - 1;
        if (j < 0) {
            i++;
        } else {
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
     * Handles the movement of the player when attempting to move right in the
     * game. This method is called by the InputHandler class when the user has
     * pressed the down arrow key on the keyboard. The method checks whether the
     * tile below the player is empty for movement and if it is updates the
     * player object's X and Y locations with the new position. If the tile
     * below the player is not empty the method will not update the player
     * position, but could make other changes to the game.
     */
    public void movePlayerDown() {
        int i = player.getX();
        int j = player.getY() + 1;
        if (j > 17) {
            j = j - 1;
        } else {
            if (tiles[i][j] == SPACE && asteroidCheck() == true) {
                player.setPosition(i, j);
            }
            if (tiles[i][j] == SPACE) {
                player.setPosition(i, j);
            }
        }
    }

    private boolean asteroidCheck() {
        int i = player.getX();
        int j = player.getY();
        for (int k = 0; k < 10; k++) {
            if (asteroids[k] != null) {
                int x = asteroids[k].getX();
                int y = asteroids[k].getY();

                if (i == x & j == y) {
                    asteroids[k] = null;
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
     * Updates the position of Asteroid objects by altering their X and Y
     * co-ordinates according to their moveDirection attribute value. This
     * iterates over the asteroids array one element at a time, checks if the
     * current element is null (skipping it if it is null) and finding the
     * moveDirection value for the current asteroid object. Asteroids with a
     * moveDirection value other than NONE should have their position updated
     * accordingly, and if their new position puts them outside the map or
     * inside a black hole they are "destroyed". Destroyed asteroids should be
     * replaced by creating a new, randomly positioned asteroid in the same
     * index of the asteroids array that the destroyed asteroid used to occupy.
     */
    private void moveAsteroids() {
        for (int k = 0; k < 10; k++) {
            int t = rng.nextInt(spawns.size());
            if (asteroids[k] != null) {
                int x = asteroids[k].getX();
                int y = asteroids[k].getY();
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
                if (x < 0 || y < 0 || x > GRID_WIDTH - 1 || y > GRID_HEIGHT - 1) {
                    Point s = spawns.get(t);
                    x = (int) s.getX();
                    y = (int) s.getY();
                }
                if (tiles[x][y] == PULSAR_ACTIVE || tiles[x][y] == PULSAR_INACTIVE || tiles[x][y] == BLACK_HOLE) {
                    Point s = spawns.get(t);
                    x = (int) s.getX();
                    y = (int) s.getY();

                }
                asteroids[k].setPosition(x, y);
            }
        }
    }

    /**
     * Moves all aliens on the current level. The method checks for non-null
     * elements in the aliens array and calls the moveAlien method for each one
     * that is not null.
     */
    private void moveAliens() {
        int t = aliens.length;
        for (int a = 0; a < t; a++) {
            if (aliens[a] != null) {
                moveAlien(aliens[a]);
            }
        }
    }

    /**
     * Moves a specific alien in the game. The method updates the X and Y
     * attributes of the alien to reflect its new position.
     *
     * @param a The Alien that needs to be moved
     */
    private void moveAlien(Alien a) {
        int x = a.getX();
        int y = a.getY();
        int t = rng.nextInt(3);
        if (t == 0 && x < GRID_WIDTH-1) {
            if (tiles[x + 1][y] == SPACE) {
                a.setPosition(x + 1, y);
            }
        }
        if (t == 1 && x > 0) {
            if (tiles[x - 1][y] == SPACE) {
                a.setPosition(x - 1, y);
            }

        }
        if (t == 2 && y < GRID_HEIGHT -1) {
            if (tiles[x][y + 1] == SPACE) {
                a.setPosition(x, y + 1);
            }

        }
        if (t == 3 && y > 0) {
            if (tiles[x][y - 1] == SPACE) {
                a.setPosition(x, y - 1);
            }
        }
    }

    /**
     * Spawns asteroids in suitable locations in the current level. The method
     * uses the spawns ArrayList to pick suitable positions to add asteroids,
     * removing these positions from the spawns ArrayList as they are used
     * (using the remove() method) to avoid multiple entities spawning in the
     * same location. The method creates asteroids by repeatedly instantiating
     * the Asteroid class and setting the X and Y position for the asteroid
     * using the Point object removed from the spawns ArrayList.
     *
     * @return An array of Asteroid objects representing the asteroids for the
     * current level
     */
    private Asteroid[] spawnAsteroids() {

        asteroids = new Asteroid[10];
        for (int i = 0; i < 10; i++) {
            int r = (int) (Math.random() * spawns.size() + 1);
            Point t = spawns.get(r);
            int a = (int) t.getX();
            int b = (int) t.getY();
            Asteroid ast = new Asteroid(a, b);
            asteroids[i] = ast;
            spawns.remove(r);
        }
        return asteroids;
    }

    /**
     * Processes the tiles array to find inactive pulsars and change them to
     * active pulsars. When a tile is found of the correct type, that tile is
     * set to PULSAR_ACTIVE. When the map is drawn to the screen next the
     * inactive pulsar will now be an active pulsar.
     */
    private void activatePulsars() {
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                if (tiles[i][j] == PULSAR_INACTIVE) {
                    tiles[i][j] = PULSAR_ACTIVE;
                }
            }

        }
    }

    /**
     * Processes the tiles array to find active pulsars and change them to
     * inactive pulsars. When a tile is found of the correct type, that tile is
     * set to PULSAR_INACTIVE. When the map is drawn to the screen next the
     * active pulsar will now be an inactive pulsar.
     */
    private void deactivatePulsars() {
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                if (tiles[i][j] == PULSAR_ACTIVE) {
                    tiles[i][j] = PULSAR_INACTIVE;
                }
            }

        }
    }

    /**
     * Damages the player if the player is in an active pulsar tile, or any of
     * the eight tiles adjacent to the active pulsar, when this method is
     * called. The method uses the player's current x and y position and
     * searches around the player looking for pulsar tiles. Any pulsar tiles
     * found this way result in a call to the changeHullStrength method for the
     * player object to damage the player.
     */
    private void pulsarDamage() {
        int x = player.getX() - 1;
        int y = player.getY() - 1;
        int a = 3;
        int b = 3;
        if (x < 0) {
            x++;
            a--;
        }
        if (y < 0) {
            y++;
            b--;
        }
        if (x + 2 > GRID_WIDTH) {
            a--;
        }
        if (y + 2 > GRID_HEIGHT) {
            b--;
        }
        for (int s = x; s < x + a; s++) {
            for (int t = y; t < y + b; t++) {
                if (tiles[s][t] == PULSAR_ACTIVE) {
                    player.hullStrength = player.hullStrength - 2 - cleared;
                }
            }

        }
    }

    /**
     * Called in response to the player collecting enough points win the current
     * level. The method increases the valued of cleared by one, resets the
     * value of points to zero, generates a new level by calling the
     * generateLevel method, fills the spawns ArrayList with suitable spawn
     * locations, then spawns aliens and asteroids. Finally it places the player
     * in the new level by calling the placePlayer() method. Note that a new
     * player object should not be created here as this will reset the player's
     * health to maximum.
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
     * Places the player in a level by choosing a spawn location from the spawns
     * ArrayList, removing the spawn position as it is used. The method sets the
     * players position in the level by calling its setPosition method with the
     * x and y values of the Point taken from the spawns ArrayList.
     */
    private void placePlayer() {
        int r = (int) (Math.random() * spawns.size() + 1);
        Point t = spawns.get(r);
        int a = (int) t.getX();
        int b = (int) t.getY();
        player.setPosition(a, b);
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
