package org.example.dprojectilegame;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
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
    // Constant game parameters (replace sliders)
    private static final double DEFAULT_WIND = 0.0;   // constant wind force
    private static final double DEFAULT_POWER = 50.0; // constant tank power

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        
        // Create canvas and renderer
        Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        Renderer renderer = new Renderer(canvas);
        
        // Create game engine
        GameEngine gameEngine = new GameEngine(renderer);
        // Apply constant parameters
        gameEngine.setWind(DEFAULT_WIND);
        gameEngine.setPowerForAll(DEFAULT_POWER);

        // Create main layout
        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        
        // Create side panel with controls
        VBox sidePanel = createSidePanel();
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

    private VBox createSidePanel() {
         VBox sidePanel = new VBox(20);
         sidePanel.setPadding(new Insets(20));
         sidePanel.setStyle("-fx-background-color: #2b2b2b;");
         sidePanel.setPrefWidth(200);

         // Show constant parameter info instead of interactive sliders/instructions
         String params = "Wind: " + DEFAULT_WIND + " (constant)\nPower: " + ((int)DEFAULT_POWER) + " (constant)";
         Label paramsLabel = new Label(params);
         paramsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
         sidePanel.getChildren().add(paramsLabel);

          return sidePanel;
     }

    public static void main(String[] args) {
        launch(args);
    }
}
