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
    private static final double SCAN_STEP_DEGREES = 60.0;

    @Override
    public Action decideAction(AiContext ctx) {
        AiEntityView enemy = ctx.getNearestEnemy();
        if (enemy == null) {
            if (!ctx.hasAnyLivingEnemy()) {
                return Action.idle();
            }
            // Nobody in view right now - sweep the field of view around, searching.
            double newFacing = ctx.getFacingAngle() + Math.toRadians(SCAN_STEP_DEGREES);
            return Action.idle().withFacing(newFacing);
        }

        double angleToEnemy = ctx.getSelfPosition().angleTo(enemy.getPosition());
        double distance = ctx.getSelfPosition().distanceTo(enemy.getPosition());

        Action action = distance <= ctx.getMeleeWeaponRange()
                ? Action.attackMelee(enemy.getId())
                : Action.moveTowards(enemy.getPosition(), 30.0);

        return action.withFacing(angleToEnemy);
    }
}
