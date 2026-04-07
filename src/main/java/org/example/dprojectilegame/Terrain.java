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
        // x positions across the screen and their corresponding terrain heights
        int[] xPoints = {

                //1 2  3     4    5    6    7   8    9    10 11  12   13  14   15          16             17   18   19   20   21    22    23   24   25   26   27   28    29    30
                0, 80, 160, 245, 312, 400, 512,512, 620, 760,800,820,900, 950,950,  1000 ,1037,1040,1042,1050,1060,1070,1080,1100, 1140, 1160,1175,1177,1179,1181,1183, 1185, 1200
        };

        double[] yPoints = {
                //1   2    3    4    5    6    7   8    9   10  11  12  13  14  15        16          17  18  19  20  21   22   23   24  25  26  27  28  29  30
                364, 364, 364, 364, 285, 285, 280,225, 225, 225,225,231,231,231,250, 263,275,278,278,295,300,300,310,320, 320, 320, 315,320,320,323,327,327, 336

        };

        for (int i = 0; i < heights.length; i++) {
            // Find which segment this x belongs to
            for (int j = 0; j < xPoints.length - 1; j++) {
                if (i >= xPoints[j] && i <= xPoints[j + 1]) {
                    double t = (double)(i - xPoints[j]) / (xPoints[j + 1] - xPoints[j]);

                    // Linear interpolation between the two terrain points
                    heights[i] = yPoints[j] + t * (yPoints[j + 1] - yPoints[j]);
                    break;
                }
            }
        }
    }

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
    

    public double getYAt(double x) {
        return groundY - getHeightAt(x);
    }

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
    

    public boolean isBelowTerrain(double x, double y) {
        return y >= getYAt(x);
    }
    

    public void adjustTankToTerrain(Tank tank) {
        double tankX = tank.getX() + tank.getWidth() / 2;
        double terrainY = getYAt(tankX-10);
        tank.setY(terrainY - tank.getHeight());
    }
    
    // Getters
    public double getWidth() {
        return width; }

    public double getBaseHeight() {
        return baseHeight;
    }
    public double getGroundY() { return groundY; }
    public double[] getHeights() { return heights; }
}
