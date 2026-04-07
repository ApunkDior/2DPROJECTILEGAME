package org.example.dprojectilegame;

import javafx.scene.image.Image;
import java.io.File;

public class Tank {
    private double x;
    private double y;
    private double width;
    private double height;
    private int health;
    private double angle; // Cannon angle in degrees
    private double power; // Power setting for projectile launch
    private boolean isLeftTank;
    private Image[] angleImages; // Array of images for different angles
    
    private static final int MAX_HEALTH = 10;
    private static final double MOVEMENT_SPEED = 2.0;
    private static final double ANGLE_STEP = 5.0;
    private static final double MAX_ANGLE = 50.0;
    private static final double MIN_ANGLE = -50.0;
    
    public Tank(double x, double y, double width, double height, boolean isLeftTank) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.health = MAX_HEALTH;
        this.angle = 0;
        this.power = 50.0;
        this.isLeftTank = isLeftTank;
        loadAngleImages();
    }
    
    private void loadAngleImages() {
        angleImages = new Image[11]; // 0, 10, 20, 30, 40, 50 degrees
        String prefix = isLeftTank ? "L" : "R";
        String graphicsPath = "/Users/shakinatoussaint/Downloads/Graphics/";
        
        try {
            for (int i = 0; i <= 10; i++) {
                int angle = i * 10;
                String filename = graphicsPath + "TANKWAR(" + prefix + angle + "D).png";
                File file = new File(filename);
                if (file.exists()) {
                    angleImages[i] = new Image(file.toURI().toString());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading tank images: " + e.getMessage());
        }
    }
    
    public Image getCurrentImage() {
        int angleIndex = (int) Math.abs(angle) / 10;
        if (angleIndex > 5) angleIndex = 10;
        if (angleImages != null && angleIndex < angleImages.length && angleImages[angleIndex] != null) {
            return angleImages[angleIndex];
        }
        return null;
    }
    
    public void takeDamage(int amount) {
        health -= amount;
        if (health < 0) health = 0;
    }
    
    public boolean isDestroyed() {
        return health <= 0;
    }
    
    public void moveLeft() {
        x -= MOVEMENT_SPEED;
    }
    
    public void moveRight() {

        x += MOVEMENT_SPEED;
    }
    
    public void increaseAngle() {
        if (angle < MAX_ANGLE) {
            angle += ANGLE_STEP;
            if (angle > MAX_ANGLE) angle = MAX_ANGLE;
        }
    }
    public void decreaseAngle() {
        if (angle > MIN_ANGLE) {
            angle -= ANGLE_STEP;
            if (angle < MIN_ANGLE) angle = MIN_ANGLE;
        }
    }
    
    // Getters and setters
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public void setX(double x) {
        this.x = x;
    }
    public void setY(double y) {
        this.y = y;
    }
    public double getWidth() {
        return width;
    }
    public double getHeight() {
        return height;
    }
    public int getHealth() {
        return health;
    }
    public double getAngle() {
        return angle;
    }
    public void setAngle(double angle) {
        this.angle = Math.max(MIN_ANGLE, Math.min(MAX_ANGLE, angle));
    }
    public double getPower() {
        return power;
    }
    public void setPower(double power) {
        this.power = power;
    }
    public boolean isLeftTank() {
        return isLeftTank;
    }
    public int getMaxHealth() {
        return MAX_HEALTH;
    }
    public double getCannonTipX() {
        return isLeftTank ? x + width : x;
    }
    
    public double getCannonTipY() {
        return y + height * 0.25;
    }
}
