package org.example.dprojectilegame;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.HashSet;
import java.util.Set;

/**
 * Control class that manages the overall game state and coordinates
 * between PhysicsEngine, CollisionDetector, and Renderer.
 */
public class GameEngine {
    private Player[] players;
    private int currentPlayerIndex;
    private Projectile activeProjectile;
    private PhysicsEngine physicsEngine;
    private CollisionDetector collisionDetector;
    private Renderer renderer;
    private Terrain terrain;
    private boolean gameOver;
    private double gravity;
    
    private static final double CANVAS_WIDTH = 1200;
    private static final double CANVAS_HEIGHT = 700;
    private static final double TERRAIN_BASE_HEIGHT = 150;
    
    private Set<KeyCode> pressedKeys;
    private long lastTime;
    
    // Projectile type configurations
    private static class ProjectileType {
        double mass;
        double dragCoefficient;
        double crossSectionalArea;
        double radius;
        double initialSpeed;
        
        ProjectileType(double mass, double dragCoefficient, double crossSectionalArea, 
                      double radius, double initialSpeed) {
            this.mass = mass;
            this.dragCoefficient = dragCoefficient;
            this.crossSectionalArea = crossSectionalArea;
            this.radius = radius;
            this.initialSpeed = initialSpeed;
        }
    }
    
    // Default projectile type (standard shell)
    private static final ProjectileType DEFAULT_PROJECTILE = new ProjectileType(
        1.0,      // mass (kg)
        0.47,     // drag coefficient (round projectile)
        0.01,     // cross-sectional area (m^2)
        4.0,      // radius (pixels)
        50.0      // initial speed multiplier
    );
    
    public GameEngine(Renderer renderer) {
        this.renderer = renderer;
        this.players = new Player[2];
        this.currentPlayerIndex = 0;
        this.gameOver = false;
        this.gravity = 9.81;
        this.pressedKeys = new HashSet<>();
        
        initializeGame();
    }
    
    private void initializeGame() {
        // Create terrain
        terrain = new Terrain(CANVAS_WIDTH, TERRAIN_BASE_HEIGHT, CANVAS_HEIGHT);
        
        // Create tanks
        Tank leftTank = new Tank(100, 0, 50, 30, true);
        Tank rightTank = new Tank(CANVAS_WIDTH - 150, 0, 50, 30, false);
        
        // Position tanks on terrain
        terrain.adjustTankToTerrain(leftTank);
        terrain.adjustTankToTerrain(rightTank);
        
        // Create players
        players[0] = new Player("Left", leftTank);
        players[1] = new Player("Right", rightTank);
        
        // Initialize physics and collision systems
        physicsEngine = new PhysicsEngine(gravity, 0.0);
        collisionDetector = new CollisionDetector(terrain);
        
        startGame();
    }
    
    public void startGame() {
        gameOver = false;
        currentPlayerIndex = 0;
        activeProjectile = null;
    }
    
    public void startTurn() {
        // Reset any active projectiles from previous turn
        if (activeProjectile != null && activeProjectile.isActive()) {
            // Wait for projectile to finish
            return;
        }
        activeProjectile = null;
    }
    
    /**
     * Fires a projectile from the current player's tank.
     */
    public void fireProjectile(double angle, double power, boolean isNuke) {
        if (gameOver) return;
        if (activeProjectile != null && activeProjectile.isActive()) {
            return; // Wait for current projectile to finish
        }
        
        Player currentPlayer = getCurrentPlayer();
        Tank tank = currentPlayer.getTank();
        
        // Calculate initial velocity from angle and power
        double angleRad = Math.toRadians(angle);
        double initialSpeed = power * DEFAULT_PROJECTILE.initialSpeed / 100.0;
        
        double vx = initialSpeed * Math.cos(angleRad);
        double vy = -initialSpeed * Math.sin(angleRad); // Negative because y increases downward
        
        // Adjust for right tank (reverse direction)
        if (!tank.isLeftTank()) {
            vx = -vx;
        }
        
        // Create projectile
        ProjectileType type = isNuke ? 
            new ProjectileType(DEFAULT_PROJECTILE.mass * 2, DEFAULT_PROJECTILE.dragCoefficient,
                              DEFAULT_PROJECTILE.crossSectionalArea, DEFAULT_PROJECTILE.radius * 2,
                              DEFAULT_PROJECTILE.initialSpeed) : DEFAULT_PROJECTILE;
        
        activeProjectile = new Projectile(
            tank.getCannonTipX(),
            tank.getCannonTipY(),
            vx, vy,
            type.radius,
            type.mass,
            type.dragCoefficient,
            type.crossSectionalArea,
        );
    }
    
    /**
     * Updates game state each frame.
     */
    public void update(double deltaTime) {
        if (gameOver) return;
        
        // Update active projectile
        if (activeProjectile != null && activeProjectile.isActive()) {
            physicsEngine.updateProjectile(activeProjectile, deltaTime);
            
            // Check collisions
            checkCollisions();
            
            // Check if projectile is out of bounds
            if (activeProjectile.getX() < 0 || activeProjectile.getX() > CANVAS_WIDTH ||
                activeProjectile.getY() < 0 || activeProjectile.getY() > CANVAS_HEIGHT) {
                activeProjectile.deactivate();
                getCurrentPlayer().resetConsecutiveHits();
                switchTurn();
            }
        }
        
        // Adjust tanks to terrain (in case terrain was destroyed)
        for (Player player : players) {
            if (player != null) {
                terrain.adjustTankToTerrain(player.getTank());
            }
        }
        
        checkWinCondition();
    }
    
    private void checkCollisions() {
        if (activeProjectile == null || !activeProjectile.isActive()) return;
        
        // Check ground collision
        if (collisionDetector.checkGroundCollision(activeProjectile)) {
            activeProjectile.deactivate();
            getCurrentPlayer().resetConsecutiveHits();
            switchTurn();
            return;
        }
        
        // Check tank collisions
        for (int i = 0; i < players.length; i++) {
            Player player = players[i];
            if (player != null && player != getCurrentPlayer()) {
                Tank tank = player.getTank();
                if (collisionDetector.checkTankCollision(activeProjectile, tank)) {
                    // Calculate damage
                    int damage = activeProjectile.isNuke() ? 3 : 1;
                    tank.takeDamage(damage);
                    
                    // Record hit for nuke system
                    if (!activeProjectile.isNuke()) {
                        getCurrentPlayer().recordHit();
                    }
                    
                    activeProjectile.deactivate();
                    switchTurn();
                    return;
                }
            }
        }
    }
    
    public void switchTurn() {
        if (activeProjectile == null || !activeProjectile.isActive()) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.length;
            startTurn();
        }
    }
    
    public void checkWinCondition() {
        for (Player player : players) {
            if (player != null && player.getTank().isDestroyed()) {
                gameOver = true;
                return;
            }
        }
    }
    
    public Player getCurrentPlayer() {
        return players[currentPlayerIndex];
    }
    
    /**
     * Sets up input handling for the game.
     */
    public void setupInputHandling(Scene scene) {
        scene.setOnKeyPressed(event -> {
            pressedKeys.add(event.getCode());
            handleKeyPress(event.getCode());
        });
        
        scene.setOnKeyReleased(event -> {
            pressedKeys.remove(event.getCode());
        });
    }
    
    private void handleKeyPress(KeyCode code) {
        if (gameOver) {
            if (code == KeyCode.R) {
                initializeGame();
            }
            return;
        }
        
        Player currentPlayer = getCurrentPlayer();
        Tank tank = currentPlayer.getTank();
        boolean isLeftTank = tank.isLeftTank();
        
        // Movement controls
        if ((code == KeyCode.A && isLeftTank) || (code == KeyCode.LEFT && !isLeftTank)) {
            tank.moveLeft();
            terrain.adjustTankToTerrain(tank);
        } else if ((code == KeyCode.D && isLeftTank) || (code == KeyCode.RIGHT && !isLeftTank)) {
            tank.moveRight();
            terrain.adjustTankToTerrain(tank);
        }
        
        // Cannon angle controls
        if ((code == KeyCode.W && isLeftTank) || (code == KeyCode.UP && !isLeftTank)) {
            tank.increaseAngle();
        } else if ((code == KeyCode.S && isLeftTank) || (code == KeyCode.DOWN && !isLeftTank)) {
            tank.decreaseAngle();
        }
        
        // Fire projectile
        if (code == KeyCode.SPACE) {
            boolean isNuke = currentPlayer.hasNukeAvailable() && pressedKeys.contains(KeyCode.SHIFT);
            fireProjectile(tank.getAngle(), tank.getPower(), isNuke);
            if (isNuke) {
                currentPlayer.recordHit(); // This resets the counter
            }
        }
    }
    
    /**
     * Starts the game loop.
     */
    public void startGameLoop() {
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                
                double deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                
                update(deltaTime);
                render();
            }
        };
        
        gameLoop.start();
    }
    
    private void render() {
        // Render game elements
        renderer.render(players, activeProjectile, terrain);
        
        // Render UI info
        if (!gameOver) {
            Player currentPlayer = getCurrentPlayer();
            renderer.drawGameInfo(
                currentPlayer.getName(),
                physicsEngine.getWind(),
                currentPlayer.getTank().getPower(),
                currentPlayer.getConsecutiveHits()
            );
        } else {
            // Find winner
            String winner = "";
            for (Player player : players) {
                if (player != null && !player.getTank().isDestroyed()) {
                    winner = player.getName() + " Wins!";
                    break;
                }
            }
            renderer.drawGameOver(winner);
        }
    }
    
    // Getters and setters
    public void setWind(double wind) {
        physicsEngine.setWind(wind);
    }
    
    public void setGravity(double gravity) {
        this.gravity = gravity;
        physicsEngine.setGravity(gravity);
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
}
