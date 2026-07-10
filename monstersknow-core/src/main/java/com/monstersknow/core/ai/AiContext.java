package com.monstersknow.core.ai;

import com.monstersknow.core.entity.CombatState;
import com.monstersknow.core.entity.Entity;
import java.util.List;

/**
 * Read-only view of the current combat turn, exposed to AI plugins.
 * Deliberately narrower than {@link CombatState}/{@link Entity}: no setters,
 * no way to mutate another entity's health or position directly.
 */
public interface AiContext {
    static AiContext of(CombatState state, Entity self) {
        return new AiContextImpl(state, self);
    }

    // Self
    String getSelfId();
    String getSelfName();
    int getCurrentHealth();
    int getMaxHealth();
    Entity.Position getSelfPosition();
    boolean isHidden();
    int getArrowsRemaining();
    boolean hasRangedWeapon();
    boolean hasMeleeWeapon();
    double getRangedWeaponRange();
    double getMeleeWeaponRange();

    // World query
    AiEntityView getNearestEnemy();
    List<AiEntityView> getEnemiesWithinDistance(double distance);
    boolean isNearCover();
    boolean hasLineOfSightTo(String entityId);
    int getTurnNumber();
}
