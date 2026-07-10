package com.monstersknow.core.entity;

/**
 * Goblin entity - implements ambush tactics from the Monster Manual.
 * Key tactics:
 * - Attack from hiding with advantage
 * - Maintain 40-80 feet distance for ranged attacks
 * - Shortbow attack (action) then Hide again (bonus action, Nimble Escape)
 * - Only flee when critically wounded (1 HP)
 * - Fight aggressively otherwise
 */
public class Goblin extends Entity {
    private static final int OPTIMAL_MIN_DISTANCE = 40;
    private static final int OPTIMAL_MAX_DISTANCE = 80;
    private static final int MELEE_ENGAGE_DISTANCE = 30;
    private static final double MOVEMENT_SPEED = 30.0; // 30 feet per turn
    private static final double DASH_SPEED = 60.0; // 60 feet when dashing (action + bonus action)
    private static final int STARTING_ARROWS = 20;
    private static final double SEARCH_SCAN_STEP_DEGREES = 60.0; // sweep per turn while no enemy is in view

    // 1d4 + DEX(+2), attack +4 (DEX +2, proficiency +2), 5 ft reach
    private static final Weapon SCIMITAR = new Weapon("Scimitar", 1, 4, 2, 4, 5, 5, false);
    // 1d6 + DEX(+2), attack +4 (DEX +2, proficiency +2), normal 80 ft / long 320 ft, needs arrows
    private static final Weapon SHORTBOW = new Weapon("Shortbow", 1, 6, 2, 4, 80, 320, true);

    public Goblin(String id, String name) {
        super(id, name, "Goblin", createGoblinStats(), STARTING_ARROWS, SCIMITAR, SHORTBOW);
    }

    private static Stats createGoblinStats() {
        // STR: 8, DEX: 14, CON: 10, INT: 10, WIS: 8, CHA: 8
        // AC: 15 (leather armor, shield), HP: 7, Attack: +4
        return new Stats(8, 14, 10, 10, 8, 8, 15, 7);
    }

    @Override
    public Action decideAction(CombatState combatState) {
        if (!combatState.hasAnyLivingEnemy(this)) {
            return Action.idle();
        }

        // Only consider enemies currently within field of view - a goblin
        // looking the wrong way simply doesn't know they're there.
        Entity nearestEnemy = combatState.getNearestEnemy(this);
        if (nearestEnemy == null) {
            // Nobody in view - sweep the field of view around, searching.
            double newFacing = getFacingAngle() + Math.toRadians(SEARCH_SCAN_STEP_DEGREES);
            return Action.search().withFacing(newFacing);
        }

        // Enemy spotted - keep looking at it for the rest of this turn's decision.
        double angleToEnemy = getPosition().angleTo(nearestEnemy.getPosition());
        Action action = decideCombatAction(combatState, nearestEnemy);
        return action.withFacing(angleToEnemy);
    }

    private Action decideCombatAction(CombatState combatState, Entity nearestEnemy) {
        double distance = getPosition().distanceTo(nearestEnemy.getPosition());

        // ONLY flee if critically wounded (1 HP) - goblins are cowardly but will fight if cornered
        if (getCurrentHealth() == 1) {
            return Action.dash(nearestEnemy.getPosition(), DASH_SPEED);
        }

        // Otherwise, goblins are aggressive and prefer to fight
        // They use ranged attacks when at optimal distance (40-80 feet)
        if (distance > OPTIMAL_MAX_DISTANCE) {
            // Too far, move closer to get in range
            double moveAmount = Math.min(20.0, distance - OPTIMAL_MAX_DISTANCE);
            return Action.moveTowards(nearestEnemy.getPosition(), moveAmount);

        } else if (distance < OPTIMAL_MIN_DISTANCE) {
            // Too close for comfortable ranged combat - back away a bit
            if (distance < MELEE_ENGAGE_DISTANCE) {
                // Very close - drop the bow and engage with the scimitar instead
                return Action.attackMelee(nearestEnemy.getId());
            } else {
                // Moderately close, just move back
                double moveAmount = Math.min(10.0, OPTIMAL_MIN_DISTANCE - distance);
                return Action.moveAway(nearestEnemy.getPosition(), moveAmount);
            }
        } else {
            // Perfect range! (40-80 feet) - this is where goblins shine with bows
            if (getArrowsRemaining() > 0) {
                if (isHidden()) {
                    // Hidden and ready to shoot - FIRE!
                    return Action.attackRanged(nearestEnemy.getId());
                } else if (combatState.isNearCover(this)) {
                    // Not hidden, but there's cover nearby - take the shot, then
                    // vanish again with Nimble Escape (Hide as a bonus action)
                    // so next turn's shot gets advantage again.
                    return Action.attackRanged(nearestEnemy.getId()).withBonusAction(Action.hide());
                } else {
                    // No cover nearby - take the shot anyway rather than standing around
                    return Action.attackRanged(nearestEnemy.getId());
                }
            } else {
                // Out of arrows - still fight, try to reposition or move closer for melee
                double moveAmount = Math.min(5.0, distance - OPTIMAL_MIN_DISTANCE);
                if (moveAmount > 0) {
                    return Action.moveTowards(nearestEnemy.getPosition(), moveAmount);
                } else {
                    return Action.idle(); // Already in melee range, just attack mentally?
                }
            }
        }
    }
}
