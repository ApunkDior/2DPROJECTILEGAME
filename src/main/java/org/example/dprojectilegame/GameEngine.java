package org.example.dprojectilegame;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameEngine {
    //Two players stored in the array
    private Player[] players;
    private int currentPlayerIndex;
    //Bullets that currently in the air
    private Projectile activeProjectile;
    // systme so for gravity wind , applying physics step, checking collision, explosion animations
    private PhysicsEngine physicsEngine;
    private PhysicsUpdater physicsUpdater;
    private CollisionDetector collisionDetector;
    private ExplosionManager explosionManager;
    private Renderer renderer;
    private Terrain terrain;
    private boolean gameOver;
    private double gravity;
    
    private static final double CANVAS_WIDTH = 1200;
    private static final double CANVAS_HEIGHT = 700;
    private static final double TERRAIN_BASE_HEIGHT = 150;

    //Stores currently pressed keys
    private Set<KeyCode> pressedKeys;
    private long lastTime;
    private boolean hasShotThisTurn; // prevent multiple shots per turn
    private PauseTransition pendingSwitch; // pending delayed switch

    private Integer shooterPlayerIndexWhenFired;
    //How long projectile has been flying
    private double flightElapsedTime;
    /* Next whole second to record (1, 2, …). */
    private int nextWholeSecondToLog;
    private final List<ShotSample> currentFlightSamples = new ArrayList<>();

    private Label flightTimerLabel;
    private Label flightTableTitleLabel;
    private Label flightTableBodyLabel;
    /* Telemetry is what collect data from remote or inaccessible sources and transmitting */
    private Label telemetryFormulasLabel;
    private XYChart.Series<Number, Number> trajectoryVxSeries;
    private XYChart.Series<Number, Number> trajectoryVySeries;
    private XYChart.Series<Number, Number> trajectoryParabolaSeries;
    private ImageView telemetryTankIcon;
    private Image telemetryTankImageLeft;
    private Image telemetryTankImageRight;
    /* Velocity at impact (before deactivate); flight time at impact */
    private double lastImpactVx;
    private double lastImpactVy;
    private double lastFlightTimeAtImpact;
    private String lastShotTableTitle = "";
    private String lastShotTableBody = "";
    private String lastShotFormulasText = "";

    private double lastLaunchVx0;
    private double lastLaunchVy0;
    private double trajSampleAccum;

    private static final double TRAJECTORY_SAMPLE_INTERVAL_SEC = 0.05;

    private static final class ShotSample {
        final int second;
        final double vx;
        final double vy;

        ShotSample(int second, double vx, double vy) {
            this.second = second;
            this.vx = vx;
            this.vy = vy;
        }
    }

    public GameEngine(Renderer renderer) {
        this.renderer = renderer;
        this.players = new Player[2];
        this.currentPlayerIndex = 0;
        this.gameOver = false;
        this.gravity = 9.81;
        this.pressedKeys = new HashSet<>();
        this.hasShotThisTurn = false;
        this.pendingSwitch = null;
        this.shooterPlayerIndexWhenFired = null;
        this.flightElapsedTime = 0;
        this.nextWholeSecondToLog = 1;

        initializeGame();
    }

    public void setTelemetrySidebarLabels(Label timerLabel, Label tableTitleLabel, Label tableBodyLabel) {
        this.flightTimerLabel = timerLabel;
        this.flightTableTitleLabel = tableTitleLabel;
        this.flightTableBodyLabel = tableBodyLabel;
    }

    //CREATES BOTH CHARTS
    public void setTelemetryExtension(Label formulasLabel,
                                      LineChart<Number, Number> chartVxVy,
                                      LineChart<Number, Number> chartParabola,
                                      Slider windControl,
                                      Slider powerControl) {
        this.telemetryFormulasLabel = formulasLabel;
        if (chartVxVy != null) {
            NumberAxis xAxis = (NumberAxis) chartVxVy.getXAxis();
            NumberAxis yAxis = (NumberAxis) chartVxVy.getYAxis();
            xAxis.setLabel("Time (s)");
            yAxis.setLabel("Velocity (px/s)");
            xAxis.setAutoRanging(true);
            yAxis.setAutoRanging(true);
            trajectoryVxSeries = new XYChart.Series<>();
            trajectoryVxSeries.setName("Vx");
            trajectoryVySeries = new XYChart.Series<>();
            trajectoryVySeries.setName("Vy");
            chartVxVy.setTitle(null);
            chartVxVy.setLegendVisible(true);
            chartVxVy.getData().clear();
            chartVxVy.getData().add(trajectoryVxSeries);
            chartVxVy.getData().add(trajectoryVySeries);
        }
        if (chartParabola != null) {
            NumberAxis px = (NumberAxis) chartParabola.getXAxis();
            NumberAxis py = (NumberAxis) chartParabola.getYAxis();
            px.setLabel("x (px)");
            py.setLabel("Height from bottom (px)");
            px.setAutoRanging(true);  //enables automatic axis scaling based on data
            py.setAutoRanging(true);
            trajectoryParabolaSeries = new XYChart.Series<>();
            trajectoryParabolaSeries.setName("Path");
            chartParabola.setTitle(null);
            chartParabola.setLegendVisible(false);
            chartParabola.getData().clear();
            chartParabola.getData().add(trajectoryParabolaSeries);
        }
        if (windControl != null) {
            windControl.valueProperty().addListener((obs, o, n) -> setWind(n.doubleValue()));
            setWind(windControl.getValue());
            blockArrowKeysOnSlider(windControl);
        }
        if (powerControl != null) {
            powerControl.valueProperty().addListener((obs, o, n) -> setPowerForAll(n.doubleValue()));
            setPowerForAll(powerControl.getValue());
            blockArrowKeysOnSlider(powerControl);
        }
    }

    /*0° tank sprites for telemetry (left vs right shooter)*/
    public void setTelemetryTankIcon(ImageView imageView, Image leftTank0Deg, Image rightTank0Deg) {
        this.telemetryTankIcon = imageView;
        this.telemetryTankImageLeft = leftTank0Deg;
        this.telemetryTankImageRight = rightTank0Deg;
    }

    private static void blockArrowKeysOnSlider(Slider slider) {
        slider.setFocusTraversable(false);
        slider.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.RIGHT) {
                e.consume();
            }
        });
    }
    
    private void initializeGame() {
        shooterPlayerIndexWhenFired = null;
        flightElapsedTime = 0;
        nextWholeSecondToLog = 1;
        currentFlightSamples.clear();

        // Create terrain
        terrain = new Terrain(CANVAS_WIDTH, TERRAIN_BASE_HEIGHT, CANVAS_HEIGHT);
        
        // Create tanks
        // Increase tank model size and hitbox
        Tank leftTank = new Tank(100, 0, 100*2.3, 70*1.3, true);
        Tank rightTank = new Tank(CANVAS_WIDTH - 200, 0, 100*2.3, 70*1.3, false);

        // Position tanks on terrain
        terrain.adjustTankToTerrain(leftTank);
        terrain.adjustTankToTerrain(rightTank);
        
        // Create players
        players[0] = new Player("Left", leftTank);
        players[1] = new Player("Right", rightTank);
        
        physicsEngine = new PhysicsEngine(gravity, 0.0);
        physicsUpdater = new PhysicsUpdater(physicsEngine);
        collisionDetector = new CollisionDetector(terrain);
        explosionManager = new ExplosionManager();
        
        startGame();
        clearPersistedShotTable();
    }

    private void clearPersistedShotTable() {
        lastShotTableTitle = "";
        lastShotTableBody = "";
        lastShotFormulasText = "";
        if (flightTableTitleLabel != null) {
            flightTableTitleLabel.setText("");
            flightTableTitleLabel.setVisible(false);
        }
        if (flightTableBodyLabel != null) {
            flightTableBodyLabel.setText("");
            flightTableBodyLabel.setVisible(false);
        }
        if (telemetryFormulasLabel != null) {
            telemetryFormulasLabel.setText("");
            telemetryFormulasLabel.setVisible(false);
        }
        if (telemetryTankIcon != null) {
            telemetryTankIcon.setVisible(false);
        }
        resetTrajectorySeries();
    }
    
    public void startGame() {
        gameOver = false;
        currentPlayerIndex = 0; // Ensure left tank starts
        activeProjectile = null;
        hasShotThisTurn = false;
        if (explosionManager != null) {
            explosionManager.clear();
        }
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

    public void fireProjectile(double angle, double power, boolean isNuke) {
        //it doesnt allow firing if the game has ended
        if (gameOver) return;
        // If there's an active projectile still in flight, can't fire
        if (activeProjectile != null && activeProjectile.isActive()) return;
        // Ensure the player hasn't already fired this turn
        if (hasShotThisTurn) return;

        Player currentPlayer = getCurrentPlayer();
        Tank tank = currentPlayer.getTank();
        
        ProjectileType projectileKind = isNuke ? ProjectileType.EXPLOSIVE : ProjectileType.STANDARD;
        //convert power
        double initialSpeed = power * projectileKind.getInitialSpeedMultiplier() / 100.0;

        double[] muzzle = new double[2];
        double[] vel = new double[2];
        tank.getMuzzleWorldPosition(muzzle);
        tank.getLaunchVelocityWorld(angle, initialSpeed, vel);
        //Store initial velocity for the display
        lastLaunchVx0 = vel[0];
        lastLaunchVy0 = vel[1];
        //make the projectile closer to the canon
        activeProjectile = ProjectileType.createProjectileAt(
                //detects a mismatch between the agent's expeccted librairies and the user's application dependencies
                muzzle[0], muzzle[1], vel[0], vel[1], tank, projectileKind);
        //reset the graphs
        resetTrajectorySeries();
        appendTrajectorySample(0.0, vel[0], vel[1], muzzle[0], muzzle[1]);
        trajSampleAccum = 0.0;
        // Mark that the current player has fired this turn
        hasShotThisTurn = true;

        shooterPlayerIndexWhenFired = currentPlayerIndex;
        refreshTelemetryTankIcon();
        flightElapsedTime = 0;
        nextWholeSecondToLog = 1;
        currentFlightSamples.clear();
    }

    //Schedule a delayed switch of turns after a given delay in seconds.
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
    

    public void update(double deltaTime) {
        if (gameOver) {
            return;
        }

        explosionManager.update(deltaTime);
        updateActiveProjectileFlight(deltaTime);
        syncAllTanksToTerrain();
        checkWinCondition();
    }

    //Updates the current acitive porjectile's physic
    private void updateActiveProjectileFlight(double deltaTime) {
        //Exit if there's no projectile or nothing is happening
        if (activeProjectile == null || !activeProjectile.isActive()) {
            return;
        }
        //Track total flight time
        flightElapsedTime += deltaTime;
        // Advance projectile physics (position, velocity,collision)
        physicsUpdater.step(activeProjectile, deltaTime, terrain);

        trajSampleAccum += deltaTime;
        while (trajSampleAccum >= TRAJECTORY_SAMPLE_INTERVAL_SEC && activeProjectile.isActive()) {
            trajSampleAccum -= TRAJECTORY_SAMPLE_INTERVAL_SEC;
            appendTrajectorySample(
                    flightElapsedTime,
                    activeProjectile.getVx(),
                    activeProjectile.getVy(),
                    activeProjectile.getX(),
                    activeProjectile.getY());
        }

        while (activeProjectile.isActive() && flightElapsedTime >= nextWholeSecondToLog) {
            currentFlightSamples.add(new ShotSample(
                    nextWholeSecondToLog,
                    activeProjectile.getVx(),
                    activeProjectile.getVy()));
            nextWholeSecondToLog++;
        }

        checkCollisions();
    }

    private void syncAllTanksToTerrain() {
        for (Player player : players) {
            if (player != null) {
                terrain.adjustTankToTerrain(player.getTank());
            }
        }
        for (Player player : players) {
            if (player != null && player.getTank() != null) {
                player.getTank().stepBodyRotationTowardTarget();
            }
        }
    }
    
    private void checkCollisions() {
        if (activeProjectile == null || !activeProjectile.isActive()) {
            return;
        }

        if (collisionDetector.checkGroundCollision(activeProjectile)) {
            double sx = activeProjectile.getX();
            double sy = terrain.getYAt(sx);
            lastImpactVx = activeProjectile.getVx();
            lastImpactVy = activeProjectile.getVy();
            lastFlightTimeAtImpact = flightElapsedTime;
            explosionManager.spawn(sx, sy, activeProjectile.getExplosionSpawnRadius());
            activeProjectile.deactivate();
            finalizeShotTelemetry();
            getCurrentPlayer().resetConsecutiveHits();
            scheduleSwitchTurn(0.5);
            return;
        }

        for (int i = 0; i < players.length; i++) {
            Player player = players[i];
            if (player != null && player != getCurrentPlayer()) {
                Tank tank = player.getTank();
                if (collisionDetector.checkTankCollision(activeProjectile, tank)) {
                    double ax = activeProjectile.getX();
                    double ay = terrain.getYAt(ax);
                    lastImpactVx = activeProjectile.getVx();
                    lastImpactVy = activeProjectile.getVy();
                    lastFlightTimeAtImpact = flightElapsedTime;
                    explosionManager.spawn(ax, ay, activeProjectile.getExplosionSpawnRadius());
                    int damage = activeProjectile.getProjectileType().getImpactDamage();
                    tank.takeDamage(damage);

                    if (!activeProjectile.isNuke()) {
                        getCurrentPlayer().recordHit();
                    }
                    activeProjectile.deactivate();
                    finalizeShotTelemetry();
                    scheduleSwitchTurn(1.0);
                    return;
                }
            }
        }
    }

    private void finalizeShotTelemetry() {
        if (shooterPlayerIndexWhenFired == null || players[shooterPlayerIndexWhenFired] == null) {
            return;
        }
        Player shooter = players[shooterPlayerIndexWhenFired];
        boolean left = shooter.getTank().isLeftTank();
        lastShotTableTitle = (left ? "Left Tank" : "Right Tank") + " — Last Shot";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-8s %-12s %-12s%n", "Time", "Vy", "Vx"));
        for (ShotSample s : currentFlightSamples) {
            sb.append(String.format("%-8d %-12.2f %-12.2f%n", s.second, s.vy, s.vx));
        }
        lastShotTableBody = sb.toString();

        double viMag = Math.hypot(lastLaunchVx0, lastLaunchVy0);
        double vfMag = Math.hypot(lastImpactVx, lastImpactVy);
        StringBuilder fb = new StringBuilder();
        fb.append(String.format("v⃗ᵢ = (%.2f, %.2f)   |v⃗ᵢ| = %.2f px/s%n", lastLaunchVx0, lastLaunchVy0, viMag));
        fb.append(String.format("v⃗f = (%.2f, %.2f)   |v⃗f| = %.2f px/s%n", lastImpactVx, lastImpactVy, vfMag));
        fb.append(String.format("t = %.2f s%n", lastFlightTimeAtImpact));
        lastShotFormulasText = fb.toString();

        if (flightTableTitleLabel != null) {
            flightTableTitleLabel.setText(lastShotTableTitle);
            flightTableTitleLabel.setVisible(true);
        }
        if (flightTableBodyLabel != null) {
            flightTableBodyLabel.setText(lastShotTableBody);
            flightTableBodyLabel.setVisible(true);
        }
        if (telemetryFormulasLabel != null) {
            telemetryFormulasLabel.setText(lastShotFormulasText);
            telemetryFormulasLabel.setVisible(true);
        }
    }

    private void resetTrajectorySeries() {
        if (trajectoryVxSeries != null) {
            trajectoryVxSeries.getData().clear();
        }
        if (trajectoryVySeries != null) {
            trajectoryVySeries.getData().clear();
        }
        if (trajectoryParabolaSeries != null) {
            trajectoryParabolaSeries.getData().clear();
        }
    }

    /* Vx/Vy vs time, and parabola: x vs height from bottom. */
    private void appendTrajectorySample(double timeSec, double vx, double vy, double x, double screenY) {
        if (trajectoryVxSeries != null && trajectoryVySeries != null) {
            trajectoryVxSeries.getData().add(new XYChart.Data<>(timeSec, vx));
            trajectoryVySeries.getData().add(new XYChart.Data<>(timeSec, vy));
        }
        if (trajectoryParabolaSeries != null) {
            double heightFromBottom = CANVAS_HEIGHT - screenY;
            trajectoryParabolaSeries.getData().add(new XYChart.Data<>(x, heightFromBottom));
        }
    }

    private void refreshTelemetryTankIcon() {
        if (telemetryTankIcon == null || telemetryTankImageLeft == null || shooterPlayerIndexWhenFired == null) {
            return;
        }
        Player shooter = players[shooterPlayerIndexWhenFired];
        if (shooter == null || shooter.getTank() == null) {
            return;
        }
        telemetryTankIcon.setImage(shooter.getTank().isLeftTank() ? telemetryTankImageLeft : telemetryTankImageRight);
        telemetryTankIcon.setVisible(true);
    }

    private void updateTelemetrySidebar() {
        if (flightTimerLabel != null) {
            if (activeProjectile != null && activeProjectile.isActive()) {
                flightTimerLabel.setVisible(true);
                flightTimerLabel.setText(String.format("t = %.1fs", flightElapsedTime));
            } else {
                flightTimerLabel.setVisible(false);
                flightTimerLabel.setText("");
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
        if (tank.isLeftTank()) {
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
            } else if (code == KeyCode.R) {
                boolean isNuke = currentPlayer.hasNukeAvailable() && pressedKeys.contains(KeyCode.SHIFT);
                fireProjectile(tank.getAngle(), tank.getPower(), isNuke);
                if (isNuke) {
                    currentPlayer.recordHit();
                }
            }
        } else {
            if (code == KeyCode.J) {
                tank.moveLeft();
                terrain.adjustTankToTerrain(tank);
            } else if (code == KeyCode.L) {
                tank.moveRight();
                terrain.adjustTankToTerrain(tank);
            } else if (code == KeyCode.I) {
                tank.increaseAngle();
            } else if (code == KeyCode.K) {
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
    
    //Starts the game loop.
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
        Integer overlayShooter = (activeProjectile != null && activeProjectile.isActive())
                ? shooterPlayerIndexWhenFired : null;
        renderer.render(players, activeProjectile, terrain, overlayShooter, explosionManager);
        updateTelemetrySidebar();

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
