package org.example.dprojectilegame;

public class PhysicsEngine {
    private double gravity; // Gravitational acceleration (m/s^2)
    // Horizontal wind force {@code u} (same units as used in {ax = … + wind/m}); vertical v = 0 in this model.
    private double wind;
    
    private static final double DEFAULT_GRAVITY = 9.81;
    
    public PhysicsEngine() {
        this.gravity = DEFAULT_GRAVITY;
        this.wind = 0.0;
    }
    
    public PhysicsEngine(double gravity, double wind) {
        this.gravity = gravity;
        this.wind = wind;
    }
    

    // Integrates projectile motion with default gravity scale (1.0).

    public void updateProjectile(Projectile p, double deltaTime) {
        updateProjectile(p, deltaTime, 1.0);
    }

    public void updateProjectile(Projectile p, double deltaTime, double gravityScale) {
        if (!p.isActive()) return;

        // Read current state
        double k = p.getDragCoefficientK();
        double m = p.getMass();
        double vx = p.getVx();
        double vy = p.getVy();

        double g = gravity * gravityScale;

        // Compute accelerations (y increases downward)
        // Vertical acceleration: gravity downward minus drag opposing motion
        double ay = g - (k / m) * vy * Math.abs(vy);

        // Horizontal acceleration: drag opposing motion plus wind/m
        double ax = -(k / m) * vx * Math.abs(vx) + (wind / m);

        // Update position using kinematics with the current velocity and computed acceleration
        // x = x + vx * dt + 0.5 * ax * dt^2
        // y = y + vy * dt + 0.5 * ay * dt^2
        double newX = p.getX() + vx * deltaTime + 0.5 * ax * deltaTime * deltaTime;
        double newY = p.getY() + vy * deltaTime + 0.5 * ay * deltaTime * deltaTime;

        // Update velocities using v = v + a * dt
        double newVx = vx + ax * deltaTime;
        double newVy = vy + ay * deltaTime;

        // Apply updated values to projectile
        p.setAx(ax);
        p.setAy(ay);
        p.setVx(newVx);
        p.setVy(newVy);
        p.setX(newX);
        p.setY(newY);
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    public double getWind() {
        return wind;
    }
    public void setWind(double wind) {
        this.wind = wind;
    }
}
