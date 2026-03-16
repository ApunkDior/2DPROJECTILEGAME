package org.example.dprojectilegame;

/**
 * Represents the game terrain with pixel-level height data.
 * Supports terrain destruction when projectiles hit the ground.
 */
public class Terrain {
    private double[] heights; // Height at each x position (pixel-level)
    private double width;
    private double baseHeight;
    private double groundY; // Y coordinate of ground level in screen coordinates
    private double minHeight;
    private double maxHeight;
    
    public Terrain(double width, double baseHeight, double groundY) {
        this.width = width;
        this.baseHeight = baseHeight;
        this.groundY = groundY;
        this.minHeight = baseHeight * 0.3;
        this.maxHeight = baseHeight * 1.5;
        this.heights = new double[(int) width];
        
        generateTerrain();
    }
    
    /**
     * Generates initial uneven terrain with hills and valleys.
     */
    private void generateTerrain() {
        for (int i = 0; i < heights.length; i++) {
            // Create uneven terrain with variation
            double variation = Math.sin(i * 0.05) * 30 + Math.cos(i * 0.03) * 20;
            double height = baseHeight + variation;
            height = Math.max(minHeight, Math.min(maxHeight, height));
            heights[i] = height;
        }
    }
    
    /**
     * Gets the terrain height at a given x position.
     */
    public double getHeightAt(double x) {
        if (x < 0 || x >= width) {
            return baseHeight;
        }
        int index = (int) x;
        if (index >= heights.length) {
            return baseHeight;
        }
        return heights[index];
    }
    
    /**
     * Gets the Y coordinate on screen for a given x position.
     */
    public double getYAt(double x) {
        return groundY - getHeightAt(x);
    }
    
    /**
     * Destroys terrain at impact point, creating a circular hole.
     * Updates each affected pixel's y-value to form a half-circle.
     */
    public void destroyTerrain(double impactX, double impactY, double radius) {
        int startX = (int) Math.max(0, impactX - radius);
        int endX = (int) Math.min(width - 1, impactX + radius);
        
        for (int x = startX; x <= endX; x++) {
            double distance = Math.abs(x - impactX);
            if (distance <= radius) {
                // Calculate depth based on circular shape
                double depth = Math.sqrt(radius * radius - distance * distance);
                double newHeight = (groundY - impactY) + depth;
                
                if (x < heights.length) {
                    double currentHeight = heights[x];
                    // Only lower terrain, don't raise it
                    if (newHeight < currentHeight) {
                        heights[x] = Math.max(minHeight, newHeight);
                    }
                }
            }
        }
    }
    
    /**
     * Checks if a point is below the terrain surface.
     */
    public boolean isBelowTerrain(double x, double y) {
        return y >= getYAt(x);
    }
    
    /**
     * Adjusts tank position to sit on terrain.
     */
    public void adjustTankToTerrain(Tank tank) {
        double tankX = tank.getX() + tank.getWidth() / 2;
        double terrainY = getYAt(tankX);
        tank.setY(terrainY - tank.getHeight());
    }
    
    // Getters
    public double getWidth() { return width; }
    public double getBaseHeight() { return baseHeight; }
    public double getGroundY() { return groundY; }
    public double[] getHeights() { return heights; }
}
