package org.example.dprojectilegame;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

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
    private boolean hasShotThisTurn; // Prevent multiple shots per turn
    private PauseTransition pendingSwitch; // pending delayed switch

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
        2.0,      // mass (kg) - heavier to reduce deceleration from drag
        0.3,      // drag coefficient (lowered to reduce air resistance)
        0.005,    // cross-sectional area (m^2) - smaller to reduce drag
        42.0,     // radius (pixels) - larger projectile, slightly smaller than tank hitbox
        150.0     // initial speed multiplier (increased for greater range)
    );
    
    public GameEngine(Renderer renderer) {
        this.renderer = renderer;
        this.players = new Player[2];
        this.currentPlayerIndex = 0;
        this.gameOver = false;
        this.gravity = 9.81;
        this.pressedKeys = new HashSet<>();
        this.hasShotThisTurn = false;
        this.pendingSwitch = null;

        initializeGame();
    }
    
    private void initializeGame() {
        // Create terrain
        terrain = new Terrain(CANVAS_WIDTH, TERRAIN_BASE_HEIGHT, CANVAS_HEIGHT);
        
        // Create tanks
        // Increase tank model size and hitbox
        Tank leftTank = new Tank(100, 0, 100, 70, true);
        Tank rightTank = new Tank(CANVAS_WIDTH - 200, 0, 100, 70, false);

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
        currentPlayerIndex = 0; // Ensure left tank starts
        activeProjectile = null;
        hasShotThisTurn = false;
    }
    
    public void startTurn() {
        // Reset shot flag when a new turn starts
        hasShotThisTurn = false;
        // Clear any pending delayed switch
        if (pendingSwitch != null) {
            pendingSwitch.stop();
            pendingSwitch = null;
        }
    }

    /**
     * Fires a projectile from the current player's tank.
     */
    public void fireProjectile(double angle, double power, boolean isNuke) {
        if (gameOver) return;
        // If there's an active projectile still in flight, can't fire
        if (activeProjectile != null && activeProjectile.isActive()) return;
        // Ensure the player hasn't already fired this turn
        if (hasShotThisTurn) return;

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
        
        // Compute quadratic drag constant k from the drag equation: F = 0.5 * rho * C_d * A * v^2
        // Projectile expects dragCoefficientK where F_drag = k * v^2, so k = 0.5 * rho * C_d * A
        double airDensity = 1.225; // kg/m^3 (sea level standard)
        double dragCoefficientK = 0.5 * airDensity * type.dragCoefficient * type.crossSectionalArea;

        // Derive projectile radius from tank size so projectile is slightly smaller than tank hitbox
        double tankRadius = Math.max(tank.getWidth(), tank.getHeight()) / 2.0;
        double projectileRadius = Math.max(4.0, tankRadius * 0.85); // at least 4 px

        activeProjectile = new Projectile(
            tank.getCannonTipX(),
            tank.getCannonTipY(),
            vx, vy,
            type.mass,
            dragCoefficientK,
            projectileRadius
        );
        // Mark as nuke if applicable
        if (isNuke) {
            activeProjectile.setNuke(true);
        }
        // Mark that the current player has fired this turn
        hasShotThisTurn = true;
    }

    /**
     * Schedule a delayed switch of turns after a given delay in seconds.
     */
    private void scheduleSwitchTurn(double delaySeconds) {
        // Cancel existing pending switch if any
        if (pendingSwitch != null) {
            pendingSwitch.stop();
            pendingSwitch = null;
        }

        pendingSwitch = new PauseTransition(Duration.seconds(delaySeconds));
        pendingSwitch.setOnFinished(evt -> {
            // End current projectile and switch turn
            activeProjectile = null;
            switchTurn();
            pendingSwitch = null;
        });
        pendingSwitch.play();
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
                scheduleSwitchTurn(1.0); // delay 1 second before switching
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
            // Delay switching to the other player's turn by 1 second
            scheduleSwitchTurn(1.0);
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
                    // Delay switching turns so explosion/impact can be seen
                    scheduleSwitchTurn(1.0);
                    return;
                }
            }
        }
    }

    public void switchTurn() {
        // Only switch if there's no active projectile
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
        // Controls: Left tank uses WASD + SPACE, Right tank uses Arrow keys + P
        if (tank.isLeftTank()) {
            // Left tank controls: WASD for move/angle, SPACE to fire
            if (code == KeyCode.A) {
                tank.moveLeft();
                terrain.adjustTankToTerrain(tank);
            } else if (code == KeyCode.D) {
                tank.moveRight();
                terrain.adjustTankToTerrain(tank);
            } else if (code == KeyCode.W) {
                tank.increaseAngle();
            } else if (code == KeyCode.S) {
                tank.decreaseAngle();
            } else if (code == KeyCode.SPACE) {
                boolean isNuke = currentPlayer.hasNukeAvailable() && pressedKeys.contains(KeyCode.SHIFT);
                fireProjectile(tank.getAngle(), tank.getPower(), isNuke);
                if (isNuke) {
                    currentPlayer.recordHit();
                }
            }
        } else {
            // Right tank controls: Arrow keys for move/angle, P to fire
            if (code == KeyCode.LEFT) {
                tank.moveLeft();
                terrain.adjustTankToTerrain(tank);
            } else if (code == KeyCode.RIGHT) {
                tank.moveRight();
                terrain.adjustTankToTerrain(tank);
            } else if (code == KeyCode.UP) {
                tank.increaseAngle();
            } else if (code == KeyCode.DOWN) {
                tank.decreaseAngle();
            } else if (code == KeyCode.P) {
                boolean isNuke = currentPlayer.hasNukeAvailable() && pressedKeys.contains(KeyCode.SHIFT);
                fireProjectile(tank.getAngle(), tank.getPower(), isNuke);
                if (isNuke) {
                    currentPlayer.recordHit();
                }
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
                    winner = player.getName();
                    break;
                }
            }
            renderer.drawGameOver(winner);
        }
    }

    // Getters and setters
    public void setWind(double wind) {
        if (physicsEngine != null) physicsEngine.setWind(wind);
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
        if (physicsEngine != null) physicsEngine.setGravity(gravity);
    }

    /**
     * Set the power value for all tanks (used by GameApp to apply a constant power).
     */
    public void setPowerForAll(double power) {
        for (Player p : players) {
            if (p != null && p.getTank() != null) {
                p.getTank().setPower(power);
            }
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }
}
