package com.monstersknow.core.ai;

import com.monstersknow.core.entity.Entity;

/**
 * Read-only view of another entity, exposed to AI plugins so they can query
 * the battlefield without touching mutable engine internals.
 */
public interface AiEntityView {
    String getId();
    String getName();
    String getType();
    int getCurrentHealth();
    int getMaxHealth();
    Entity.Position getPosition();
    boolean isHidden();
    boolean isAlive();
}
