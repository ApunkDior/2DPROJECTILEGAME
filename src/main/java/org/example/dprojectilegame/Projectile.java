package org.example.dprojectilegame;

/**
 * Pure projectile state: position, velocity, {@link ProjectileType}, separate visual and hitbox radii, age.
 * Physics is applied only via {@link PhysicsUpdater} / {@link PhysicsEngine}.
 */
public class Projectile {

    private double x;
    private double y;
    private double vx;
    private double vy;
    private double ax;
    private double ay;

    private final double mass;
    private final double dragCoefficientK;
    private final double visualRadius;
    private final double collisionRadius;
    private final ProjectileType projectileType;

    private boolean active;
    private double ageSec;

    Projectile(double x, double y, double vx, double vy,
                 ProjectileType projectileType,
                 double mass, double dragCoefficientK,
                 double visualRadius, double collisionRadius) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.projectileType = projectileType;
        this.mass = mass;
        this.dragCoefficientK = dragCoefficientK;
        this.visualRadius = visualRadius;
        this.collisionRadius = collisionRadius;
        this.ax = 0.0;
        this.ay = 0.0;
        this.active = true;
        this.ageSec = 0.0;
    }


    public double getVisualRadius() {
        return visualRadius;
    }

    public double getCollisionRadius() {
        return collisionRadius;
    }


    public double getRadius() {
        return visualRadius;
    }

    public double getCraterDestructionRadius() {
        return visualRadius * projectileType.getCraterRadiusMultiplier();
    }

    public double getExplosionSpawnRadius() {
        return visualRadius * projectileType.getExplosionVisualRadiusMultiplier();
    }

    public ProjectileType getProjectileType() {
        return projectileType;
    }

    public boolean isNuke() {
        return projectileType.isExplosiveShell();
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getAgeSec() {
        return ageSec;
    }

    void addAge(double deltaSeconds) {
        this.ageSec += deltaSeconds;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getVx() {
        return vx;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public double getVy() {
        return vy;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public double getAx() {
        return ax;
    }

    public void setAx(double ax) {
        this.ax = ax;
    }

    public double getAy() {
        return ay;
    }

    public void setAy(double ay) {
        this.ay = ay;
    }

    public double getMass() {
        return mass;
    }

    public double getDragCoefficientK() {
        return dragCoefficientK;
    }
}
