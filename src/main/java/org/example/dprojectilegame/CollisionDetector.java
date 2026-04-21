package org.example.dprojectilegame;


public class CollisionDetector {
    //Uses Terrain as a variable
    private Terrain terrain;


    public CollisionDetector(Terrain terrain) {
        this.terrain = terrain;
    }

    public boolean checkTankCollision(Projectile p, Tank t) {
        //Finds the tank's center by finding the top left corner of the tank and adding hald of its width
        double tankCenterX = t.getX() + t.getWidth() / 2;
        double tankCenterY = t.getY() + t.getHeight() / 2;
        //finds the largest dimension between the width and height and get the radius of that
        double tankRadius = Math.max(t.getWidth(), t.getHeight()) / 2;
        //gets the distance between the each horizontal and vertical distances between the projectile and the tank
        double dx = p.getX() - tankCenterX;
        double dy = p.getY() - tankCenterY;
        //finds the vector of distance of the projectile onto the tank
        double distance = Math.sqrt(dx * dx + dy * dy);
        //if the distance is less than the sum of the radii, there a collision
        return distance < (p.getCollisionRadius() + tankRadius);
    }

    public boolean checkGroundCollision(Projectile p) {
        //Returns the ground height at that horizontal position because the terrain is not flat
        double terrainY = terrain.getYAt(p.getX());
        if (p.getY() >= terrainY) {
            //modified terain at impact point
            terrain.destroyTerrain(p.getX(), p.getY(), p.getCraterDestructionRadius());
            //Confirms ground collision happenend
            return true;
        }
        //if not its still in the air
        return false;
    }
}
