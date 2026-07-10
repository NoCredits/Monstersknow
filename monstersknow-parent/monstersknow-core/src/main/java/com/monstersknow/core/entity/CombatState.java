package com.monstersknow.core.entity;

import java.util.List;

/**
 * Represents the current state of combat.
 * Provides information to entities for decision making.
 */
public class CombatState {
    private List<Entity> allEntities;
    private Entity currentActor;
    private int turnNumber;

    public CombatState(List<Entity> allEntities, Entity currentActor, int turnNumber) {
        this.allEntities = allEntities;
        this.currentActor = currentActor;
        this.turnNumber = turnNumber;
    }

    /**
     * Find the nearest enemy to the given entity.
     */
    public Entity getNearestEnemy(Entity entity) {
        Entity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Entity other : allEntities) {
            if (!other.getId().equals(entity.getId()) && other.isAlive()) {
                double distance = entity.getPosition().distanceTo(other.getPosition());
                if (distance < nearestDistance) {
                    nearest = other;
                    nearestDistance = distance;
                }
            }
        }
        return nearest;
    }

    /**
     * Find all enemies within a certain distance.
     */
    public List<Entity> getEnemiesWithinDistance(Entity entity, double distance) {
        return allEntities.stream()
                .filter(e -> !e.getId().equals(entity.getId()) && e.isAlive())
                .filter(e -> entity.getPosition().distanceTo(e.getPosition()) <= distance)
                .toList();
    }

    /**
     * Check if line of sight exists between two entities.
     */
    public boolean hasLineOfSight(Entity from, Entity to) {
        // Simple implementation: always true for now
        // Can be enhanced with actual obstacle checking
        return true;
    }

    public List<Entity> getAllEntities() { return allEntities; }
    public Entity getCurrentActor() { return currentActor; }
    public int getTurnNumber() { return turnNumber; }
}
