package com.monstersknow.core.entity;

/**
 * A battlefield feature (rock, tree, shadow, bush, ...) that can provide cover to hide behind.
 */
public class TerrainFeature {
    private final double x;
    private final double y;
    private final double radius;
    private final String type;

    public TerrainFeature(double x, double y, double radius, String type) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.type = type;
    }

    public boolean isNear(double posX, double posY) {
        double dx = posX - x;
        double dy = posY - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        return dist <= radius * 1.5; // Can hide if within 1.5x radius
    }

    public String getType() { return type; }
}
