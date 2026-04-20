package org.example.dprojectilegame;

public enum ProjectileType {

    STANDARD(2.0, 0.3, 0.005, 150.0, 1.0, 1, 2.0, 1.5),

    BOUNCY(2.0, 0.3, 0.005, 150.0, 1.0, 1, 2.0, 1.5),

    EXPLOSIVE(4.0, 0.3, 0.005, 150.0, 1.0, 3, 2.0, 3.0);


    public static final double HITBOX_RADIUS_FRACTION = 0.5;

    /**
     * Shell draw/hitbox radius vs tank size (smaller on-screen projectile).
     * Velocity overlay in {@link Renderer} uses fixed scales, not this radius.
     */
    private static final double SHELL_RADIUS_SCALE = 0.16;

    private static final double AIR_DENSITY_KG_M3 = 1.225;

    private final double massKg;
    private final double dragCoefficient;
    private final double crossSectionalAreaM2;
    private final double initialSpeedMultiplier;
    private final double gravityScale;
    private final int impactDamage;

    private final double craterRadiusMultiplier;
    private final double explosionVisualRadiusMultiplier;

    ProjectileType(double massKg, double dragCoefficient, double crossSectionalAreaM2,
                     double initialSpeedMultiplier, double gravityScale,
                     int impactDamage, double craterRadiusMultiplier,
                     double explosionVisualRadiusMultiplier) {
        this.massKg = massKg;
        this.dragCoefficient = dragCoefficient;
        this.crossSectionalAreaM2 = crossSectionalAreaM2;
        this.initialSpeedMultiplier = initialSpeedMultiplier;
        this.gravityScale = gravityScale;
        this.impactDamage = impactDamage;
        this.craterRadiusMultiplier = craterRadiusMultiplier;
        this.explosionVisualRadiusMultiplier = explosionVisualRadiusMultiplier;
    }


    public double getMassKg() {
        return massKg;
    }


    public double getDragCoefficient() {
        return dragCoefficient;
    }


    public double getCrossSectionalAreaM2() {
        return crossSectionalAreaM2;
    }

    public double getInitialSpeedMultiplier() {
        return initialSpeedMultiplier;
    }

    public double getGravityScale() {
        return gravityScale;
    }

    public int getImpactDamage() {
        return impactDamage;
    }

    public double getCraterRadiusMultiplier() {
        return craterRadiusMultiplier;
    }

    public double getExplosionVisualRadiusMultiplier() {
        return explosionVisualRadiusMultiplier;
    }

    public boolean isExplosiveShell() {
        return this == EXPLOSIVE;
    }

    public double computeQuadraticDragConstant() {
        return 0.5 * AIR_DENSITY_KG_M3 * dragCoefficient * crossSectionalAreaM2;
    }


    public double getVisualRadiusScale() {
        return 1.0;
    }

    /**
     * Spawns at the given world position and velocity (e.g. from {@link Tank#getMuzzleWorldPosition(double[])}
     * and {@link Tank#getLaunchVelocityWorld(double, double, double[])}).
     */
    public static Projectile createProjectileAt(double worldX, double worldY, double vx, double vy,
                                                Tank tank, ProjectileType type) {
        double tankRadius = Math.max(tank.getWidth(), tank.getHeight()) / 2.0;
        double visualRadius = Math.max(2.5, tankRadius * SHELL_RADIUS_SCALE * type.getVisualRadiusScale());
        double collisionRadius = visualRadius * HITBOX_RADIUS_FRACTION;
        double dragK = type.computeQuadraticDragConstant();
        return new Projectile(
                worldX,
                worldY,
                vx,
                vy,
                type,
                type.getMassKg(),
                dragK,
                visualRadius,
                collisionRadius
        );
    }

    /** @deprecated use {@link #createProjectileAt(double, double, double, double, Tank, ProjectileType)} */
    @Deprecated
    public static Projectile createProjectile(Tank tank, double vx, double vy, ProjectileType type) {
        return createProjectileAt(tank.getCannonTipX(), tank.getCannonTipY(), vx, vy, tank, type);
    }
}
