package com.monstersknow.core.ai;

import com.monstersknow.core.entity.Action;

/**
 * Implemented by user-authored goblin AI plugins. Compiled and loaded at
 * runtime, so implementations must have a public no-arg constructor.
 */
public interface GoblinAi {
    /**
     * Decide this turn's action for the entity that owns this AI.
     */
    Action decideAction(AiContext context);
}
