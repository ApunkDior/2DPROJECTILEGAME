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
    /*Rotate the tank*/
    private double bodyRotationDegrees;
    /*  set each frame from terrain. */
    private double bodyRotationTargetDegrees;
    private double power; // Power setting for projectile launch
    private boolean isLeftTank;
    // Array of images for different angles
    private Image[] angleImages;
    
    private static final int MAX_HEALTH = 10;
    private static final double MOVEMENT_SPEED = 2.0;
    /*elevation calibration*/
    public static final double CANNON_ANGLE_STEP = 10.0;
    private static final double MAX_ANGLE = 50.0;
    private static final double MIN_ANGLE = 0.0;

    /*Positive Y nudge (screen down)*/
    public static final double TREAD_SURFACE_NUDGE_Y = 5.0;

    /*Optional tread vs terrain line offset */
    public static final double TREAD_OFFSET_Y = TREAD_SURFACE_NUDGE_Y;

    public static final double BODY_ROTATION_SMOOTHING = 0.12;
    public static final double BODY_ROTATION_MAX_TILT_DEG = 60.0;

    public static final double RIGHT_HULL_DISPLAY_OFFSET_DEG = 0.0;

    public Tank(double x, double y, double width, double height, boolean isLeftTank) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.health = MAX_HEALTH;
        this.angle = 0;
        this.bodyRotationDegrees = 0.0;
        this.bodyRotationTargetDegrees = 0.0;
        this.power = 50.0;
        this.isLeftTank = isLeftTank;
        loadAngleImages();
    }

    public static double calculateTankRotation(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.toDegrees(Math.atan2(dy, dx));
    }

    public static double normalizeAngle180(double degrees) {
        double a = (degrees % 360.0 + 360.0) % 360.0;
        if (a > 180.0) {
            a -= 360.0;
        }
        return a;
    }

    public static double shortestAngleDelta(double fromDeg, double toDeg) {
        double from = normalizeAngle180(fromDeg);
        double to = normalizeAngle180(toDeg);
        double d = to - from;
        if (d > 180.0) {
            d -= 360.0;
        }
        if (d < -180.0) {
            d += 360.0;
        }
        return d;
    }

    public void stepBodyRotationTowardTarget() {
        double diff = shortestAngleDelta(bodyRotationDegrees, bodyRotationTargetDegrees);
        bodyRotationDegrees = normalizeAngle180(bodyRotationDegrees + diff * BODY_ROTATION_SMOOTHING);
    }

    public static double calculateTankRotationDesignLegacy(double x1, double y1, double x2, double y2, String direction) {
        double deltaX = Math.abs(x2 - x1);
        double deltaY = Math.abs(y2 - y1);
        double degreeChange = Math.toDegrees(Math.atan2(deltaY, deltaX));
        if ("left".equalsIgnoreCase(direction)) {
            return 360.0 - degreeChange;
        }
        if ("right".equalsIgnoreCase(direction)) {
            return 180.0 + degreeChange;
        }
        throw new IllegalArgumentException("direction must be \"left\" or \"right\"");
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
            angle += CANNON_ANGLE_STEP;
            if (angle > MAX_ANGLE) {
                angle = MAX_ANGLE;
            }
            angle = roundToCannonStep(angle);
        }
    }

    public void decreaseAngle() {
        if (angle > MIN_ANGLE) {
            angle -= CANNON_ANGLE_STEP;
            if (angle < MIN_ANGLE) {
                angle = MIN_ANGLE;
            }
            angle = roundToCannonStep(angle);
        }
    }

    private static double roundToCannonStep(double deg) {
        return Math.round(deg / CANNON_ANGLE_STEP) * CANNON_ANGLE_STEP;
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
    /* Clamps to [-50,0] and snaps to the nearest {@link #CANNON_ANGLE_STEP} degree.*/

    public int getDisplayAngleDegrees() {
        return (int) Math.round(angle);
    }

    public double getTreadSurfaceNudgeY() {
        return TREAD_OFFSET_Y;
    }

    public void getMuzzleWorldPosition(double[] out) {
        double pivotX = x + width / 2.0;
        double pivotY = y + height;
        double lx = isLeftTank ? width / 2.0 : -width / 2.0;
        double ly = -0.75 * height;
        double phi = Math.toRadians(getCanvasBodyRotationDegrees());
        double c = Math.cos(phi);
        double s = Math.sin(phi);
        out[0] = pivotX + lx * c + ly * s;
        out[1] = pivotY - lx * s + ly * c;
    }


    public void getLaunchVelocityWorld(double cannonAngleDeg, double speed, double[] out) {
        double a = Math.toRadians(cannonAngleDeg);
        double lvx = speed * Math.cos(a);
        double lvy = -speed * Math.sin(a);
        if (!isLeftTank) {
            lvx = -lvx;
        }
        double phi = Math.toRadians(getCanvasBodyRotationDegrees());
        double c = Math.cos(phi);
        double s = Math.sin(phi);
        out[0] = lvx * c + lvy * s;
        out[1] = -lvx * s + lvy * c;
    }
    public double getBodyRotationDegrees() {
        return bodyRotationDegrees;
    }
    public void setBodyRotationDegrees(double bodyRotationDegrees) {
        double clamped = clampTilt(normalizeAngle180(bodyRotationDegrees));
        this.bodyRotationDegrees = clamped;
        this.bodyRotationTargetDegrees = clamped;
    }
    public void setBodyRotationTargetDegrees(double rawTerrainDegrees) {
        bodyRotationTargetDegrees = clampTilt(normalizeAngle180(rawTerrainDegrees));
    }
    public double getBodyRotationTargetDegrees() {
        return bodyRotationTargetDegrees;
    }
    private static double clampTilt(double degrees) {
        return Math.max(-BODY_ROTATION_MAX_TILT_DEG, Math.min(BODY_ROTATION_MAX_TILT_DEG, degrees));
    }
    public double getCanvasBodyRotationDegrees() {
        double base = bodyRotationDegrees + (isLeftTank ? 0.0 : RIGHT_HULL_DISPLAY_OFFSET_DEG);
        return normalizeAngle180(base);
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
