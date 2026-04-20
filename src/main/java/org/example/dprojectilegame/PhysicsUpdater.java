package org.example.dprojectilegame;

public final class PhysicsUpdater {

    private final PhysicsEngine engine;

    public PhysicsUpdater(PhysicsEngine engine) {
        this.engine = engine;
    }

    public void step(Projectile projectile, double deltaTime, Terrain terrain) {
        if (!projectile.isActive()) {
            return;
        }
        double gScale = projectile.getProjectileType().getGravityScale();
        engine.updateProjectile(projectile, deltaTime, gScale);
        projectile.addAge(deltaTime);
    }
}
