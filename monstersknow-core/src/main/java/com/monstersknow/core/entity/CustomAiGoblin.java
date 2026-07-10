package com.monstersknow.core.entity;

import com.monstersknow.core.ai.AiContext;
import com.monstersknow.core.ai.GoblinAi;

/**
 * A Goblin whose decideAction is delegated to a user-supplied, runtime-loaded
 * AI plugin. Falls back to the default Goblin ambush tactics if the plugin
 * throws or returns null, so a buggy plugin never crashes combat.
 */
public class CustomAiGoblin extends Goblin {
    private final GoblinAi ai;
    private final String aiName;

    public CustomAiGoblin(String id, String name, GoblinAi ai, String aiName) {
        super(id, name);
        this.ai = ai;
        this.aiName = aiName;
    }

    public String getAiName() { return aiName; }

    @Override
    public Action decideAction(CombatState combatState) {
        try {
            Action action = ai.decideAction(AiContext.of(combatState, this));
            return action != null ? action : Action.idle();
        } catch (Exception | LinkageError | StackOverflowError e) {
            return super.decideAction(combatState);
        }
    }
}
