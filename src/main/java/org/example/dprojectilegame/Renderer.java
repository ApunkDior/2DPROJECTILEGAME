package org.example.dprojectilegame;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.File;

/**
 * Boundary class responsible for rendering all game elements.
 */
public class Renderer {
    private Canvas canvas;
    private GraphicsContext graphics;
    private Image backgroundImage;
    
    private static final double CANVAS_WIDTH = 1200;
    private static final double CANVAS_HEIGHT = 700;
    
    public Renderer(Canvas canvas) {
        this.canvas = canvas;
        this.graphics = canvas.getGraphicsContext2D();
        loadBackground();
    }
    // Main Page

    //Second BackGound
    private void loadBackground() {
        try {
            String backgroundPath = "/Users/shakinatoussaint/Downloads/Graphics/TANKWAR_Background.jpg";
            File file = new File(backgroundPath);
            if (file.exists()) {
                backgroundImage = new Image(file.toURI().toString());
            }
        } catch (Exception e) {
            System.err.println("Error loading background: " + e.getMessage());
        }
    }
    
    /**
     * Main render method that draws all game elements.
     */
    public void render(Player[] players, Projectile projectile, Terrain terrain) {
        clearScreen();
        
        // Draw background
        if (backgroundImage != null) {
            graphics.drawImage(backgroundImage, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        } else {
            graphics.setFill(Color.SKYBLUE);
            graphics.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        }
        
        // Draw terrain
        drawTerrain(terrain);
        
        // Draw tanks
        for (Player player : players) {
            if (player != null && player.getTank() != null) {
                drawTank(player.getTank());
            }
        }
        
        // Draw projectile
        if (projectile != null && projectile.isActive()) {
            drawProjectile(projectile);
        }
        
        // Draw UI elements
        for (Player player : players) {
            if (player != null && player.getTank() != null) {
                drawHealthBar(player.getTank());
            }
        }
    }
    
    private void clearScreen() {
        graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    
    private void drawTerrain(Terrain terrain) {
        graphics.setFill(Color.SADDLEBROWN);
        graphics.setStroke(Color.DARKGREEN);
        graphics.setLineWidth(2);
        
        double[] heights = terrain.getHeights();
        double groundY = terrain.getGroundY();
        double width = terrain.getWidth();
        
        // Draw terrain as a polygon
        double[] xPoints = new double[heights.length + 2];
        double[] yPoints = new double[heights.length + 2];
        
        xPoints[0] = 0;
        yPoints[0] = CANVAS_HEIGHT;
        
        for (int i = 0; i < heights.length; i++) {
            xPoints[i + 1] = i;
            yPoints[i + 1] = groundY - heights[i];
        }
        
        xPoints[heights.length + 1] = width;
        yPoints[heights.length + 1] = CANVAS_HEIGHT;
        
        graphics.fillPolygon(xPoints, yPoints, xPoints.length);
        graphics.strokePolyline(xPoints, yPoints, xPoints.length);
    }
    
    private void drawTank(Tank t) {
        Image tankImage = t.getCurrentImage();
        if (tankImage != null) {
            graphics.drawImage(tankImage, t.getX(), t.getY(), t.getWidth(), t.getHeight());
        } else {
            // Fallback: draw simple rectangle
            graphics.setFill(t.isLeftTank() ? Color.GREEN : Color.RED);
            graphics.fillRect(t.getX(), t.getY(), t.getWidth(), t.getHeight());
        }
        
        // Draw angle indicator
        graphics.setFill(Color.WHITE);
        graphics.setFont(Font.font(12));
        String angleText = String.format("%.0f°", t.getAngle());
        graphics.fillText(angleText, t.getX(), t.getY() - 5);
    }
    
    private void drawProjectile(Projectile p) {
        if (p.isNuke()) {
            graphics.setFill(Color.ORANGE);
        } else {
            graphics.setFill(Color.BLACK);
        }
        graphics.fillOval(p.getX() - p.getRadius(), 
                         p.getY() - p.getRadius(),
                         p.getRadius() * 2, 
                         p.getRadius() * 2);
    }
    
    private void drawHealthBar(Tank t) {
        double barWidth = 200;
        double barHeight = 20;
        double x = t.isLeftTank() ? 20 : CANVAS_WIDTH - 220;
        double y = 20;
        double healthPercent = (double) t.getHealth() / t.getMaxHealth();
        
        // Background (red)
        graphics.setFill(Color.RED);
        graphics.fillRect(x, y, barWidth, barHeight);
        
        // Health (green)
        graphics.setFill(Color.GREEN);
        graphics.fillRect(x, y, barWidth * healthPercent, barHeight);
        
        // Border
        graphics.setStroke(Color.BLACK);
        graphics.setLineWidth(2);
        graphics.strokeRect(x, y, barWidth, barHeight);
        
        // Text
        graphics.setFill(Color.WHITE);
        graphics.setFont(Font.font(14));
        graphics.fillText(String.format("%d/%d", t.getHealth(), t.getMaxHealth()), 
                         x + 5, y + 15);
    }
    
    /**
     * Draws game over message.
     */
    public void drawGameOver(String message) {
        graphics.setFill(Color.BLACK);
        graphics.setGlobalAlpha(0.7);
        graphics.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        graphics.setGlobalAlpha(1.0);
        
        graphics.setFill(Color.WHITE);
        graphics.setFont(Font.font(48));
        graphics.fillText(message, CANVAS_WIDTH / 2 - 150, CANVAS_HEIGHT / 2);
    }
    
    /**
     * Draws current player indicator and game info.
     */
    public void drawGameInfo(String currentPlayerName, double wind, double power, int consecutiveHits) {
        graphics.setFill(Color.WHITE);
        graphics.setFont(Font.font(16));
        
        graphics.fillText("Current Player: " + currentPlayerName, CANVAS_WIDTH / 2 - 100, 30);
        graphics.fillText(String.format("Wind: %.1f", wind), CANVAS_WIDTH / 2 - 50, 60);
        graphics.fillText(String.format("Power: %.0f", power), CANVAS_WIDTH / 2 - 50, 90);
        
        if (consecutiveHits > 0) {
            graphics.fillText(String.format("Consecutive Hits: %d/5", consecutiveHits), 
                            CANVAS_WIDTH / 2 - 100, 120);
        }
    }
    
    public Canvas getCanvas() {
        return canvas;
    }
}
