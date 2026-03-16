package org.example.dprojectilegame;

/**
 * Entity class representing a projectile in the game.
 * Handles projectile position, velocity, and state.
 */
public class Projectile {
    private double x;
    private double y;
    private double vx; // horizontal velocity
    private double vy; // vertical velocity
    private double radius;
    private boolean active;
    private double mass;
    private double dragCoefficient;
    private double crossSectionalArea;
    private boolean isNuke;
    
    // Air density constant (kg/m^3)
    private static final double AIR_DENSITY = 1.225;
    
    public Projectile(double x, double y, double vx, double vy, double radius, 
                     double mass, double dragCoefficient, double crossSectionalArea, boolean isNuke) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.radius = radius;
        this.active = true;
        this.mass = mass;
        this.dragCoefficient = dragCoefficient;
        this.crossSectionalArea = crossSectionalArea;
        this.isNuke = isNuke;
    }
    
    /**
     * Updates the projectile's position based on current velocity.
     * Called by PhysicsEngine after applying forces.
     */
    public void updatePosition(double deltaTime) {
        if (!active) return;
        
        x += vx * deltaTime;
        y += vy * deltaTime;
    }
    
    public void deactivate() {
        this.active = false;
    }
    
    public boolean isActive() {
        return active;
    }
    
    // Getters and setters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public void setVx(double vx) { this.vx = vx; }
    public void setVy(double vy) { this.vy = vy; }
    public double getRadius() { return radius; }
    public double getMass() { return mass; }
    public double getDragCoefficient() { return dragCoefficient; }
    public double getCrossSectionalArea() { return crossSectionalArea; }
    public boolean isNuke() { return isNuke; }
    
    /**
     * Calculates the drag coefficient k = ½ ρCdA
     */
    public double getDragCoefficientK() {
        return 0.5 * AIR_DENSITY * dragCoefficient * crossSectionalArea;
    }
    
    /**
     * Gets the current speed of the projectile.
     */
    public double getSpeed() {
        return Math.sqrt(vx * vx + vy * vy);
    }
}
