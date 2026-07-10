package com.monstersknow.ai.plugins;

import com.monstersknow.core.ai.AiContext;
import com.monstersknow.core.ai.AiEntityView;
import com.monstersknow.core.ai.GoblinAi;
import com.monstersknow.core.entity.Action;

/**
 * Sample custom AI: always charges the nearest enemy and fights in melee,
 * ignoring the default bow/hide ambush tactics.
 */
public class RusherAi implements GoblinAi {
    @Override
    public Action decideAction(AiContext ctx) {
        AiEntityView enemy = ctx.getNearestEnemy();
        if (enemy == null) {
            return Action.idle();
        }

        double distance = ctx.getSelfPosition().distanceTo(enemy.getPosition());
        if (distance <= ctx.getMeleeWeaponRange()) {
            return Action.attackMelee(enemy.getId());
        }
        return Action.moveTowards(enemy.getPosition(), 30.0);
    }
}
