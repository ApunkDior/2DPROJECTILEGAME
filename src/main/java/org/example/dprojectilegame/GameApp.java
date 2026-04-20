package org.example.dprojectilegame;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GameApp extends Application {

    private static final double WINDOW_WIDTH = 1540;
    private static final double WINDOW_HEIGHT = 800;
    private static final double CANVAS_WIDTH = 1200;
    private static final double CANVAS_HEIGHT = 700;
    private static final double DEFAULT_WIND = 0.0;
    private static final double DEFAULT_POWER = 50.0;

    private static final Path MUSIC_DIR =
            Paths.get("/Users/shakinatoussaint/Downloads/Graphics/MUSIC");

    private static final String TXT_MONO = "-fx-text-fill: white; -fx-font-family: monospace; -fx-font-size: 11px;";
    private static final String HDR = "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;";
    private static final String SUB = "-fx-text-fill: white; -fx-font-size: 12px;";
    private static final String CHART_VX_VY = ""
            + ".chart-plot-background { -fx-background-color: transparent; }"
            + ".chart-vertical-grid-lines { -fx-stroke: #555555; }"
            + ".chart-horizontal-grid-lines { -fx-stroke: #555555; }"
            + ".chart-legend-item { -fx-text-fill: white; }"
            + ".default-color0.chart-series-line { -fx-stroke: white; -fx-stroke-width: 2px; -fx-stroke-dash-array: 8 6; }"
            + ".default-color1.chart-series-line { -fx-stroke: white; -fx-stroke-width: 2px; }";
    private static final String CHART_PARA = ""
            + ".chart-plot-background { -fx-background-color: transparent; }"
            + ".chart-vertical-grid-lines { -fx-stroke: #555555; }"
            + ".chart-horizontal-grid-lines { -fx-stroke: #555555; }"
            + ".default-color0.chart-series-line { -fx-stroke: #cccccc; -fx-stroke-width: 2px; }";

    private final BackgroundMusic backgroundMusic = new BackgroundMusic();

    @Override
    public void start(Stage primaryStage) throws Exception {
        Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        Renderer renderer = new Renderer(canvas);

        GameEngine gameEngine = new GameEngine(renderer);

        Label flightTableTitleLabel = new Label();
        flightTableTitleLabel.setVisible(false);
        flightTableTitleLabel.setWrapText(true);
        flightTableTitleLabel.setStyle(SUB);

        Label flightTableBodyLabel = new Label();
        flightTableBodyLabel.setVisible(false);
        flightTableBodyLabel.setWrapText(false);
        flightTableBodyLabel.setMaxWidth(Double.MAX_VALUE);
        flightTableBodyLabel.setTextOverrun(OverrunStyle.CLIP);
        flightTableBodyLabel.setStyle(TXT_MONO);

        Label telemetryFormulasLabel = new Label();
        telemetryFormulasLabel.setVisible(false);
        telemetryFormulasLabel.setWrapText(true);
        telemetryFormulasLabel.setMaxWidth(280);
        telemetryFormulasLabel.setStyle(SUB);

        Image tankL = loadTelemetryTankImage("/org/example/dprojectilegame/telemetry/TANKWAR_L0D.png");
        Image tankR = loadTelemetryTankImage("/org/example/dprojectilegame/telemetry/TANKWAR_R0D.png");
        ImageView tankIconView = new ImageView();
        tankIconView.setPreserveRatio(true);
        tankIconView.setFitWidth(100);
        tankIconView.setVisible(false);

        Slider windSlider = new Slider(-50, 50, DEFAULT_WIND);
        windSlider.setShowTickMarks(true);
        windSlider.setShowTickLabels(true);
        windSlider.setMajorTickUnit(25);
        windSlider.setBlockIncrement(1);
        windSlider.setPrefWidth(200);
        windSlider.setStyle("-fx-control-inner-background: #3a3a3a;");

        Label windValueLabel = new Label(String.format("u = %.1f", windSlider.getValue()));
        windValueLabel.setStyle(SUB + " -fx-min-width: 72;");

        Slider powerSlider = new Slider(1, 100, DEFAULT_POWER);
        powerSlider.setShowTickMarks(true);
        powerSlider.setShowTickLabels(false);
        powerSlider.setBlockIncrement(1);
        powerSlider.setPrefWidth(200);
        powerSlider.setStyle("-fx-control-inner-background: #3a3a3a;");

        Label powerValueLabel = new Label(String.format("power = %.0f", powerSlider.getValue()));
        powerValueLabel.setStyle(SUB + " -fx-min-width: 72;");
        powerSlider.valueProperty().addListener((obs, o, n) ->
                powerValueLabel.setText(String.format("power = %.0f", n.doubleValue())));

        windSlider.valueProperty().addListener((obs, o, n) ->
                windValueLabel.setText(String.format("u = %.1f", n.doubleValue())));

        NumberAxis trajVxT = new NumberAxis();
        NumberAxis trajVxV = new NumberAxis();
        trajVxT.setAutoRanging(true);
        trajVxV.setAutoRanging(true);
        trajVxT.setTickLabelFill(Color.WHITE);
        trajVxV.setTickLabelFill(Color.WHITE);
        trajVxT.setStyle("-fx-tick-label-fill: white;");
        trajVxV.setStyle("-fx-tick-label-fill: white;");

        LineChart<Number, Number> chartVxVy = new LineChart<>(trajVxT, trajVxV);
        chartVxVy.setAnimated(false);
        chartVxVy.setPrefHeight(200);
        chartVxVy.setMinHeight(160);
        chartVxVy.setMaxHeight(240);
        chartVxVy.setLegendVisible(true);
        chartVxVy.setStyle("-fx-background-color: transparent;" + CHART_VX_VY);
        chartVxVy.setFocusTraversable(false);
        chartVxVy.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.RIGHT) {
                e.consume();
            }
        });

        NumberAxis paraX = new NumberAxis();
        NumberAxis paraY = new NumberAxis();
        paraX.setAutoRanging(true);
        paraY.setAutoRanging(true);
        paraX.setTickLabelFill(Color.WHITE);
        paraY.setTickLabelFill(Color.WHITE);
        paraX.setStyle("-fx-tick-label-fill: white;");
        paraY.setStyle("-fx-tick-label-fill: white;");

        LineChart<Number, Number> chartParabola = new LineChart<>(paraX, paraY);
        chartParabola.setAnimated(false);
        chartParabola.setPrefHeight(200);
        chartParabola.setMinHeight(160);
        chartParabola.setMaxHeight(240);
        chartParabola.setLegendVisible(false);
        chartParabola.setStyle("-fx-background-color: transparent;" + CHART_PARA);
        chartParabola.setFocusTraversable(false);
        chartParabola.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.RIGHT) {
                e.consume();
            }
        });

        gameEngine.setTelemetrySidebarLabels(null, flightTableTitleLabel, flightTableBodyLabel);
        gameEngine.setTelemetryExtension(
                telemetryFormulasLabel, chartVxVy, chartParabola, windSlider, powerSlider);
        if (tankL != null && tankR != null) {
            gameEngine.setTelemetryTankIcon(tankIconView, tankL, tankR);
        }

        BorderPane root = new BorderPane();
        root.setCenter(canvas);

        VBox calibrationPanel = buildCalibrationPanel(
                windSlider,
                windValueLabel,
                powerSlider,
                powerValueLabel,
                tankIconView,
                flightTableTitleLabel,
                flightTableBodyLabel,
                telemetryFormulasLabel,
                chartVxVy,
                chartParabola);

        ScrollPane scroll = new ScrollPane(calibrationPanel);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background: #2b2b2b; -fx-background-color: #2b2b2b;");
        calibrationPanel.setStyle("-fx-background-color: #2b2b2b;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox musicSection = backgroundMusic.buildSection(MUSIC_DIR);
        musicSection.setPadding(new Insets(8, 0, 0, 0));
        musicSection.setStyle("-fx-background-color: #2b2b2b;");

        BorderPane rightColumn = new BorderPane();
        rightColumn.setCenter(scroll);
        rightColumn.setBottom(musicSection);
        rightColumn.setPadding(new Insets(12));
        rightColumn.setStyle("-fx-background-color: #2b2b2b;");
        rightColumn.setMinWidth(Region.USE_PREF_SIZE);
        rightColumn.setPrefWidth(360);
        rightColumn.setMaxWidth(360);

        root.setRight(rightColumn);

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        gameEngine.setupInputHandling(scene);
        gameEngine.startGameLoop();

        primaryStage.setTitle("2D Projectile Tank Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setOnHidden(e -> backgroundMusic.stop());
        primaryStage.show();
    }

    private static Image loadTelemetryTankImage(String resourcePath) {
        try (InputStream in = GameApp.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                return null;
            }
            return new Image(in);
        } catch (Exception e) {
            return null;
        }
    }

    private VBox buildCalibrationPanel(Slider windSlider,
                                       Label windValueLabel,
                                       Slider powerSlider,
                                       Label powerValueLabel,
                                       ImageView tankIconView,
                                       Label flightTableTitleLabel,
                                       Label flightTableBodyLabel,
                                       Label telemetryFormulasLabel,
                                       LineChart<Number, Number> chartVxVy,
                                       LineChart<Number, Number> chartParabola) {
        VBox panel = new VBox(10);

        Label header = new Label("Wind & power");
        header.setStyle(HDR);
        panel.getChildren().add(header);

        HBox windRow = new HBox(8);
        windRow.setAlignment(Pos.CENTER_LEFT);
        Label windLbl = new Label("Wind force u:");
        windLbl.setStyle(SUB);
        windRow.getChildren().addAll(windLbl, windSlider, windValueLabel);
        panel.getChildren().add(windRow);

        HBox powerRow = new HBox(8);
        powerRow.setAlignment(Pos.CENTER_LEFT);
        Label powerLbl = new Label("Power:");
        powerLbl.setStyle(SUB);
        powerRow.getChildren().addAll(powerLbl, powerSlider, powerValueLabel);
        panel.getChildren().add(powerRow);

        Label tel = new Label("In-flight / last shot");
        tel.setStyle(HDR);
        panel.getChildren().add(tel);

        VBox textCol = new VBox(6);
        textCol.getChildren().addAll(flightTableTitleLabel, flightTableBodyLabel, telemetryFormulasLabel);

        HBox shotRow = new HBox(10);
        shotRow.setAlignment(Pos.TOP_LEFT);
        shotRow.getChildren().addAll(tankIconView, textCol);
        panel.getChildren().add(shotRow);

        Label capVx = new Label("Vx and Vy vs time");
        capVx.setStyle(SUB);
        VBox.setMargin(capVx, new Insets(16, 0, 0, 0));
        panel.getChildren().addAll(capVx, chartVxVy);

        Label capPara = new Label("Trajectory (parabola, height vs x)");
        capPara.setStyle(SUB);
        VBox.setMargin(capPara, new Insets(12, 0, 0, 0));
        panel.getChildren().addAll(capPara, chartParabola);

        return panel;
    }

    static void main(String[] args) {
        launch(args);
    }
}
