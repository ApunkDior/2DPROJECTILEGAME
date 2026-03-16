package org.example.dprojectilegame;

/**
 * Control class that handles all physics calculations for projectiles.
 * Implements air resistance, drag, and gravity according to the provided formulas.
 */
public class PhysicsEngine {
    private double gravity; // Gravitational acceleration (m/s^2)
    private double wind; // Wind force affecting projectiles
    
    private static final double DEFAULT_GRAVITY = 9.81;
    
    public PhysicsEngine() {
        this.gravity = DEFAULT_GRAVITY;
        this.wind = 0.0;
    }
    
    public PhysicsEngine(double gravity, double wind) {
        this.gravity = gravity;
        this.wind = wind;
    }
    

    public void updateProjectile(Projectile p, double deltaTime) {
        if (!p.isActive()) return;
        
        // Apply gravity and air resistance to vertical velocity
        applyGravity(p, deltaTime);
        
        // Apply wind and air resistance to horizontal velocity
        applyWind(p, deltaTime);
        
        // Update position based on velocity
        p.updatePosition(deltaTime);
    }
    

    public void applyGravity(Projectile p, double deltaTime) {
        double k = p.getDragCoefficientK();
        double m = p.getMass();
        double vy = p.getVy();
        
        // Calculate vertical acceleration
        // ay = -g - (k/m) * vy * |vy|
        double ay = -gravity - (k / m) * vy * Math.abs(vy);
        
        // Update vertical velocity: vy = vy + ay * Δt
        double newVy = vy + ay * deltaTime;
        p.setVy(newVy);
    }
    

    public void applyWind(Projectile p, double deltaTime) {
        double k = p.getDragCoefficientK();
        double m = p.getMass();
        double vx = p.getVx();
        
        // Calculate horizontal acceleration
        // ax = -(k/m) * vx * |vx| + wind/m
        double ax = -(k / m) * vx * Math.abs(vx) + (wind / m);
        
        // Update horizontal velocity: vx = vx + ax * Δt
        double newVx = vx + ax * deltaTime;
        p.setVx(newVx);
    }
    
    // Getters and setters
    public double getGravity() { return gravity; }
    public void setGravity(double gravity) { this.gravity = gravity; }
    public double getWind() { return wind; }
    public void setWind(double wind) { this.wind = wind; }
}
