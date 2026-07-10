package com.monstersknow.core.entity;

/**
 * Goblin entity - implements ambush tactics from the Monster Manual.
 * Key tactics:
 * - Attack from hiding with advantage
 * - Maintain 40-80 feet distance for ranged attacks
 * - Use Shortbow/Move/Hide action sequence
 * - Only flee when critically wounded (1 HP)
 * - Fight aggressively otherwise
 */
public class Goblin extends Entity {
    private static final int OPTIMAL_MIN_DISTANCE = 40;
    private static final int OPTIMAL_MAX_DISTANCE = 80;
    private static final int DISENGAGE_DISTANCE = 30;
    private static final double MOVEMENT_SPEED = 30.0; // 30 feet per turn
    private static final double DASH_SPEED = 60.0; // 60 feet when dashing (action + bonus action)
    private static final int STARTING_ARROWS = 20;

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
        // Get nearest enemy
        Entity nearestEnemy = combatState.getNearestEnemy(this);
        if (nearestEnemy == null) {
            return Action.idle();
        }

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
            if (distance < DISENGAGE_DISTANCE) {
                // Very close, use disengage to back away safely
                return Action.disengage();
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
                } else {
                    // Not hidden, try to hide first
                    return Action.hide();
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
