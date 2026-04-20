package org.example.dprojectilegame;

/**
 * Axis-aligned rectangle in screen space for coarse terrain intersection tests.
 */
public record AxisAlignedBounds(double minX, double minY, double maxX, double maxY) {
}
