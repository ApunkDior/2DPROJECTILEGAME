package org.example.dprojectilegame;

public class Projectile {
    // Position
    private double x;
    private double y;

    // Velocity (m/s)
    private double vx;
    private double vy;

    // Acceleration (m/s^2)
    private double ax;
    private double ay;

    // Physical properties
    private double mass; // kg
    private double dragCoefficientK; // Drag coefficient k from F = k * v^2
    private double radius; // pixels

    // State
    private boolean isActive;
    private boolean isNuke; // whether this projectile is a nuke

    // Constants
    private static final double GRAVITY = 9.81; // m/s^2

    public Projectile(double x, double y, double vx, double vy,
                      double mass, double dragCoefficientK, double radius) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.mass = mass;
        this.dragCoefficientK = dragCoefficientK;
        this.radius = radius;
        this.ax = 0.0;
        this.ay = 0.0;
        this.isActive = true;
        this.isNuke = false;
    }

    public void updatePosition(double deltaTime) {
        if (!isActive) return;

        // Update position using simple kinematics: p = p + v * Δt
        this.x += this.vx * deltaTime;
        this.y += this.vy * deltaTime;
    }
    public void applyGravityAndDrag(double deltaTime) {
        if (!isActive) return;

        // Calculate vertical acceleration
        // ay = -g - (k/m) * vy * |vy|
        // The drag term represents quadratic air resistance
        this.ay = -GRAVITY - (dragCoefficientK / mass) * this.vy * Math.abs(this.vy);

        // Update vertical velocity using kinematics: vy = vy + ay * Δt
        this.vy += this.ay * deltaTime;
    }


    public void applyHorizontalDrag(double deltaTime) {
        if (!isActive) return;

        // Calculate horizontal acceleration
        // ax = -(k/m) * vx * |vx|
        // The drag term represents quadratic air resistance
        this.ax = -(dragCoefficientK / mass) * this.vx * Math.abs(this.vx);

        // Update horizontal velocity using kinematics: vx = vx + ax * Δt
        this.vx += this.ax * deltaTime;
    }
    public void deactivate() {
        this.isActive = false;
    }

    // Nuke flag getter/setter
    public boolean isNuke() { return isNuke; }
    public void setNuke(boolean nuke) { this.isNuke = nuke; }

    // Getters and Setters

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getVx() { return vx; }
    public void setVx(double vx) { this.vx = vx; }

    public double getVy() { return vy; }
    public void setVy(double vy) { this.vy = vy; }

    public double getAx() { return ax; }
    public void setAx(double ax) { this.ax = ax; }

    public double getAy() { return ay; }
    public void setAy(double ay) { this.ay = ay; }

    public double getMass() { return mass; }
    public void setMass(double mass) { this.mass = mass; }

    public double getDragCoefficientK() { return dragCoefficientK; }
    public void setDragCoefficientK(double dragCoefficientK) { this.dragCoefficientK = dragCoefficientK; }

    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
