package org.example.dprojectilegame;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Boundary class - Application entry point and UI setup.
 */
public class GameApp extends Application {
    private Stage stage;
    private Scene scene;
    private static final double WINDOW_WIDTH = 1400;
    private static final double WINDOW_HEIGHT = 800;
    private static final double CANVAS_WIDTH = 1200;
    private static final double CANVAS_HEIGHT = 700;
    
    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        
        // Create canvas and renderer
        Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        Renderer renderer = new Renderer(canvas);
        
        // Create game engine
        GameEngine gameEngine = new GameEngine(renderer);
        
        // Create main layout
        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        
        // Create side panel with controls
        VBox sidePanel = createSidePanel(gameEngine);
        root.setRight(sidePanel);
        
        // Create scene
        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // Setup input handling
        gameEngine.setupInputHandling(scene);
        
        // Start game loop
        gameEngine.startGameLoop();
        
        // Setup stage
        stage.setTitle("2D Projectile Tank Game");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    
    private VBox createSidePanel(GameEngine gameEngine) {
        VBox sidePanel = new VBox(20);
        sidePanel.setPadding(new Insets(20));
        sidePanel.setStyle("-fx-background-color: #2b2b2b;");
        sidePanel.setPrefWidth(200);
        
        // Power slider
        Label powerLabel = new Label("Power:");
        powerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        Slider powerSlider = new Slider(10, 100, 50);
        powerSlider.setShowTickLabels(true);
        powerSlider.setShowTickMarks(true);
        powerSlider.setMajorTickUnit(25);
        powerSlider.setMinorTickCount(5);
        powerSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Update current player's tank power
            if (gameEngine.getCurrentPlayer() != null) {
                gameEngine.getCurrentPlayer().getTank().setPower(newVal.doubleValue());
            }
        });
        
        // Wind slider
        Label windLabel = new Label("Wind Force:");
        windLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        Slider windSlider = new Slider(-50, 50, 0);
        windSlider.setShowTickLabels(true);
        windSlider.setShowTickMarks(true);
        windSlider.setMajorTickUnit(25);
        windSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            gameEngine.setWind(newVal.doubleValue());
        });
        
        // Instructions
        Label instructionsLabel = new Label("Controls:\n\n" +
                "Left Tank:\n" +
                "A/D - Move\n" +
                "W/S - Angle\n" +
                "Space - Fire\n" +
                "Shift+Space - Nuke\n\n" +
                "Right Tank:\n" +
                "Arrow Keys - Move/Angle\n" +
                "Space - Fire\n" +
                "Shift+Space - Nuke\n\n" +
                "R - Restart");
        instructionsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        instructionsLabel.setWrapText(true);
        
        sidePanel.getChildren().addAll(
                powerLabel, powerSlider,
                windLabel, windSlider,
                instructionsLabel
        );
        
        return sidePanel;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
