package org.example.dprojectilegame;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.File;

import static java.lang.Math.*;


public class Renderer {
    private Canvas canvas;
    private GraphicsContext graphics;
    private Image backgroundImage;
    
    private static final double CANVAS_WIDTH = 1200;
    private static final double CANVAS_HEIGHT = 700;

    private static final Color VELOCITY_COLOR_LEFT = Color.web("#FF69B4");
    private static final Color VELOCITY_COLOR_RIGHT = Color.web("#9B59B6");
    /** Drawn oval diameter = 2 × visualRadius × this (independent of arrow geometry). */
    private static final double PROJECTILE_VISUAL_DIAMETER_FACTOR = 0.4;
    /** Arrow lengths follow velocity × these factors only — not {@link Projectile#getVisualRadius()}. */
    private static final double VELOCITY_ARROW_SCALE_COMPONENT = 0.75;
    private static final double VELOCITY_ARROW_SCALE_RESULTANT = 0.75;
    private static final double ARROW_HEAD_SIZE = 10.0;
    
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
     * Full frame: background, terrain, tanks, projectile, velocity overlay, explosions (above tanks), HUD.
     */
    public void render(Player[] players, Projectile projectile, Terrain terrain,
                       Integer activeShooterPlayerIndex, ExplosionManager explosionManager) {
        clearScreen();
        
        // Draw background
        if (backgroundImage != null) {
            graphics.drawImage(backgroundImage, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        } else {
            graphics.setFill(Color.SKYBLUE);
            graphics.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        }
        
        drawTerrain(terrain);
        
        for (Player player : players) {
            if (player != null && player.getTank() != null) {
                drawTank(player.getTank());
            }
        }
        
        if (projectile != null && projectile.isActive()) {
            drawProjectile(projectile);
        }

        // Velocity vectors + angle labels (only while projectile active)
        if (projectile != null && projectile.isActive() && activeShooterPlayerIndex != null) {
            boolean shooterIsLeft = players[activeShooterPlayerIndex].getTank().isLeftTank();
            Color velColor = shooterIsLeft ? VELOCITY_COLOR_LEFT : VELOCITY_COLOR_RIGHT;
            drawProjectileVelocityOverlay(projectile, velColor);
            drawTankVelocityAngleLabels(players, projectile.getVx(), projectile.getVy());
        }

        explosionManager.render(graphics);

        // Draw UI elements
        for (Player player : players) {
            if (player != null && player.getTank() != null) {
                drawHealthBar(player.getTank());
            }
        }
    }

    private void drawProjectileVelocityOverlay(Projectile p, Color color) {
        // Origin at center of drawn ball (same geometry as fillOval in drawProjectile)
        double cx = projectileVisualCenterX(p);
        double cy = projectileVisualCenterY(p);
        double vx = p.getVx();
        double vy = p.getVy();

        graphics.setLineWidth(2.0);

        // Vx: horizontal from ball center
        double vxLen = vx * VELOCITY_ARROW_SCALE_COMPONENT;
        drawArrowLine(cx, cy, cx + vxLen, cy, color, ARROW_HEAD_SIZE);

        // Vy: vertical (screen y down = positive vy)
        double vyLen = vy * VELOCITY_ARROW_SCALE_COMPONENT;
        drawArrowLine(cx, cy, cx, cy + vyLen, color, ARROW_HEAD_SIZE);

        // Resultant V
        double vMag = hypot(vx, vy);
        if (vMag > 1e-6) {
            double scale = VELOCITY_ARROW_SCALE_RESULTANT;
            double ex = cx + vx * scale;
            double ey = cy + vy * scale;
            drawArrowLine(cx, cy, ex, ey, color, ARROW_HEAD_SIZE);
        }
    }

    /*so the arrow are the the */
    /** Projectile {@code (x,y)} is the shell center; matches centered {@link #drawProjectile}. */
    private static double projectileVisualCenterX(Projectile p) {
        return p.getX();
    }

    private static double projectileVisualCenterY(Projectile p) {
        return p.getY();
    }

    private void drawTankVelocityAngleLabels(Player[] players, double vx, double vy) {
        double angleDeg = Math.toDegrees(Math.atan2(vy, vx));
        graphics.setFont(Font.font(14));
        for (Player player : players) {
            if (player == null || player.getTank() == null) continue;
            Tank t = player.getTank();
            Color c = t.isLeftTank() ? VELOCITY_COLOR_LEFT : VELOCITY_COLOR_RIGHT;
            graphics.setFill(c);
            String text = String.format("V: %.1f°", angleDeg);
            double tx = t.getX() + t.getWidth() / 2 - 28;
            double ty = t.getY() - 8;
            graphics.fillText(text, tx, ty);
        }
    }

    private void drawArrowLine(double x0, double y0, double x1, double y1, Color color, double headLen) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        if (hypot(dx, dy) < 0.5) return;

        graphics.setStroke(color);
        graphics.setFill(color);
        graphics.strokeLine(x0, y0, x1, y1);

        double ang = atan2(dy, dx);
        double hx = x1 - headLen * cos(ang);
        double hy = y1 - headLen * sin(ang);
        double wing = headLen * 0.55;
        double lx = hx + wing * sin(ang);
        double ly = hy - wing * cos(ang);
        double rx = hx - wing * sin(ang);
        double ry = hy + wing * cos(ang);

        double[] xs = { x1, lx, rx };
        double[] ys = { y1, ly, ry };
        graphics.fillPolygon(xs, ys, 3);
    }
    private void clearScreen() {
        graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    private void drawTerrain(Terrain terrain) {
        graphics.setFill(Color.TRANSPARENT);
        graphics.setStroke(Color.PEACHPUFF);
        graphics.setLineWidth(2);
        
        double[] heights = terrain.copySurfaceHeightSamples();
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
        double pivotX = t.getX() + t.getWidth() / 2.0;
        double pivotY = t.getY() + t.getHeight();

        graphics.save();
        graphics.translate(pivotX, pivotY);
        graphics.rotate(t.getCanvasBodyRotationDegrees());
        graphics.translate(-pivotX, -pivotY);

        if (tankImage != null) {
            graphics.drawImage(tankImage, t.getX(), t.getY(), t.getWidth(), t.getHeight());
        } else {
            // Fallback: draw simple rectangle
            graphics.setFill(t.isLeftTank() ? Color.GREEN : Color.RED);
            graphics.fillRect(t.getX(), t.getY(), t.getWidth(), t.getHeight());
        }
        graphics.restore();

        // Draw angle indicator (screen-space, not rotated with hull)
        graphics.setFill(Color.WHITE);
        graphics.setFont(Font.font(12));
        String angleText = String.format("%d°", t.getDisplayAngleDegrees());
        graphics.fillText(angleText, t.getX(), t.getY() - 5);
    }
    private void drawProjectile(Projectile p) {
        if (p.isNuke()) {
            graphics.setFill(Color.ORANGE);
        } else {
            graphics.setFill(Color.BLACK);
        }
        double r = p.getVisualRadius();
        double d = 2.0 * r * PROJECTILE_VISUAL_DIAMETER_FACTOR;
        graphics.fillOval(p.getX() - d / 2.0, p.getY() - d / 2.0, d, d);
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
    public void drawGameOver(String message) {
        graphics.setFill(Color.BLACK);
        graphics.setGlobalAlpha(0.7);
        graphics.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        graphics.setGlobalAlpha(1.0);
        
        graphics.setFill(Color.WHITE);
        graphics.setFont(Font.font(48));
        graphics.fillText(message, CANVAS_WIDTH / 2 - 150, CANVAS_HEIGHT / 2);
    }
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
