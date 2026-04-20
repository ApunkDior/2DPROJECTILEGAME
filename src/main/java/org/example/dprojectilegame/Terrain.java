package org.example.dprojectilegame;

import java.util.Arrays;

public class Terrain {
    private double[] heights; // Height at each x position (pixel-level)
    private double width;
    private double baseHeight;
    private double groundY; // Y coordinate of ground level in screen coordinates
    private double minHeight;
    private double maxHeight;


    private final int[] terrainControlX = {
            0, 80, 160, 245, 312, 400, 512, 512, 620, 760, 800, 820, 900, 950, 950,
            1000, 1037, 1040, 1042, 1050, 1060, 1070, 1080, 1100, 1140, 1160, 1175, 1177, 1179,
            1181, 1183, 1185, 1200
    };

    private final double[] terrainControlHeight = {
            364, 364, 364, 364, 285, 285, 280, 225, 225, 225, 225, 231, 231, 231, 250,
            263, 275, 278, 278, 295, 300, 300, 310, 320, 320, 320, 315, 320, 320, 323,
            327, 327, 336
    };

    public record TerrainSegment(double x1, double y1, double x2, double y2) {}

    public Terrain(double width, double baseHeight, double groundY) {
        this.width = width;
        this.baseHeight = baseHeight;
        this.groundY = groundY;
        this.minHeight = baseHeight * 0.3;
        this.maxHeight = baseHeight * 1.5;
        this.heights = new double[(int) width];
        
        generateTerrain();
    }

    private void generateTerrain() {
        int[] xPoints = terrainControlX;
        double[] yPoints = terrainControlHeight;

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


    public TerrainSegment getTerrainSegmentScreenAt(double x) {
        int[] xp = terrainControlX;
        double[] hp = terrainControlHeight;
        if (x <= xp[0]) {
            return new TerrainSegment(xp[0], screenYAtHeight(hp[0]), xp[1], screenYAtHeight(hp[1]));
        }
        if (x >= xp[xp.length - 1]) {
            int n = xp.length - 1;
            return new TerrainSegment(xp[n - 1], screenYAtHeight(hp[n - 1]), xp[n], screenYAtHeight(hp[n]));
        }
        for (int j = 0; j < xp.length - 1; j++) {
            if (x >= xp[j] && x <= xp[j + 1]) {
                return new TerrainSegment(xp[j], screenYAtHeight(hp[j]), xp[j + 1], screenYAtHeight(hp[j + 1]));
            }
        }
        int n = xp.length - 1;
        return new TerrainSegment(xp[n - 1], screenYAtHeight(hp[n - 1]), xp[n], screenYAtHeight(hp[n]));
    }

    private double screenYAtHeight(double heightSample) {
        return groundY - heightSample;
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

    public int getSurfaceSampleCount() {
        return heights.length;
    }

    public double[] copySurfaceHeightSamples() {
        return Arrays.copyOf(heights, heights.length);
    }

    public boolean collidesWithTerrain(AxisAlignedBounds bounds) {
        double minX = Math.max(0, Math.min(bounds.minX(), bounds.maxX()));
        double maxX = Math.min(width - 1, Math.max(bounds.minX(), bounds.maxX()));
        int x0 = (int) Math.floor(minX);
        int x1 = (int) Math.ceil(maxX);
        for (int xi = x0; xi <= x1; xi++) {
            double surfaceY = getYAt(xi);
            if (bounds.maxY() >= surfaceY) {
                return true;
            }
        }
        return false;
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
        double sampleX = tank.getX() + tank.getWidth() / 2.0;
        double terrainY = getYAt(sampleX);
        tank.setY(terrainY - tank.getHeight() + tank.getTreadSurfaceNudgeY());

        TerrainSegment seg = getTerrainSegmentScreenAt(sampleX);
        tank.setBodyRotationTargetDegrees(
                Tank.calculateTankRotation(seg.x1(), seg.y1(), seg.x2(), seg.y2()));
    }
    
    // Getters
    public double getWidth() {
        return width;
    }

    public double getBaseHeight() {
        return baseHeight;
    }
    public double getGroundY() {
        return groundY;
    }
}
