package org.example.dprojectilegame;

public class CollisionDetector {
    private Terrain terrain;
    
    public CollisionDetector(Terrain terrain) {
        this.terrain = terrain;
    }

    public boolean checkTankCollision(Projectile p, Tank t) {
        // Tank center position
        double tankCenterX = t.getX() + t.getWidth() / 2;
        double tankCenterY = t.getY() + t.getHeight() / 2;
        double tankRadius = Math.max(t.getWidth(), t.getHeight()) / 2;
        
        // Distance between projectile and tank center
        double dx = p.getX() - tankCenterX;
        double dy = p.getY() - tankCenterY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Check if distance is less than sum of radii
        return distance < (p.getRadius() + tankRadius);
    }

    public boolean checkGroundCollision(Projectile p) {
        double terrainY = terrain.getYAt(p.getX());
        
        if (p.getY() >= terrainY) {
            // Projectile hit the ground - destroy terrain
            terrain.destroyTerrain(p.getX(), p.getY(), p.getRadius() * 2);
            return true;
        }
        return false;
    }
    public boolean checkExplosionRadius(Projectile p, Tank t, double radius) {
        double tankCenterX = t.getX() + t.getWidth() / 2;
        double tankCenterY = t.getY() + t.getHeight() / 2;
        
        double dx = p.getX() - tankCenterX;
        double dy = p.getY() - tankCenterY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        return distance < radius;
    }
    
    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }
}
