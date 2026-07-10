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
    private List<TerrainFeature> terrainFeatures;

    public CombatState(List<Entity> allEntities, Entity currentActor, int turnNumber,
                        List<TerrainFeature> terrainFeatures) {
        this.allEntities = allEntities;
        this.currentActor = currentActor;
        this.turnNumber = turnNumber;
        this.terrainFeatures = terrainFeatures;
    }

    /**
     * Check whether the given entity is near terrain that provides cover to hide behind.
     */
    public boolean isNearCover(Entity entity) {
        Entity.Position pos = entity.getPosition();
        for (TerrainFeature feature : terrainFeatures) {
            if (feature.isNear(pos.x, pos.y)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find the nearest enemy to the given entity that is currently visible
     * to it (within view distance and facing towards it).
     */
    public Entity getNearestEnemy(Entity entity) {
        Entity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Entity other : allEntities) {
            if (!other.getId().equals(entity.getId()) && other.isAlive() && entity.canSee(other)) {
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
     * Whether any living enemy exists anywhere on the battlefield, regardless
     * of whether {@code entity} can currently see them. Used to distinguish
     * "combat is effectively over" from "nobody is in view right now".
     */
    public boolean hasAnyLivingEnemy(Entity entity) {
        return allEntities.stream().anyMatch(e -> !e.getId().equals(entity.getId()) && e.isAlive());
    }

    /**
     * Find all visible enemies within a certain distance.
     */
    public List<Entity> getEnemiesWithinDistance(Entity entity, double distance) {
        return allEntities.stream()
                .filter(e -> !e.getId().equals(entity.getId()) && e.isAlive())
                .filter(e -> entity.getPosition().distanceTo(e.getPosition()) <= distance)
                .filter(entity::canSee)
                .toList();
    }

    /**
     * Check if {@code from} can see {@code to}: within view distance and
     * inside its facing-centered field of view.
     */
    public boolean hasLineOfSight(Entity from, Entity to) {
        return from.canSee(to);
    }

    public List<Entity> getAllEntities() { return allEntities; }
    public Entity getCurrentActor() { return currentActor; }
    public int getTurnNumber() { return turnNumber; }
}
