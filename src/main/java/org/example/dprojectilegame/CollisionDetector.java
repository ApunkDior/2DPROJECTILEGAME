package org.example.dprojectilegame;

/**
 * Projectile vs tank and projectile vs terrain checks using {@link Terrain} queries.
 */
public class CollisionDetector {

    private Terrain terrain;

    /**
     * @param terrain shared terrain instance (same reference as game world)
     */
    public CollisionDetector(Terrain terrain) {
        this.terrain = terrain;
    }

    /**
     * Circle–circle test using projectile {@linkplain Projectile#getCollisionRadius() hitbox} and tank bounds.
     */
    public boolean checkTankCollision(Projectile p, Tank t) {
        double tankCenterX = t.getX() + t.getWidth() / 2;
        double tankCenterY = t.getY() + t.getHeight() / 2;
        double tankRadius = Math.max(t.getWidth(), t.getHeight()) / 2;

        double dx = p.getX() - tankCenterX;
        double dy = p.getY() - tankCenterY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        return distance < (p.getCollisionRadius() + tankRadius);
    }

    /**
     * Ground hit when the projectile center crosses the terrain surface at {@code p.getX()}.
     * Uses the same {@link Terrain#getYAt(double)} sample as rendering (height table + extension off-screen),
     * so segment corners do not use a mismatched test vs the drawn polyline.
     */
    public boolean checkGroundCollision(Projectile p) {
        double terrainY = terrain.getYAt(p.getX());

        if (p.getY() >= terrainY) {
            terrain.destroyTerrain(p.getX(), p.getY(), p.getCraterDestructionRadius());
            return true;
        }
        return false;
    }

    /**
     * @param terrain replacement terrain (e.g. after full reload)
     */
    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }
}
