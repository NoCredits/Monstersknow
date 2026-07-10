package com.monstersknow.core.combat;

import com.monstersknow.core.entity.Action;
import com.monstersknow.core.entity.Entity;
import com.monstersknow.core.entity.CombatState;
import com.monstersknow.core.entity.TerrainFeature;
import com.monstersknow.core.entity.Weapon;
import java.util.*;

/**
 * Core combat engine that orchestrates turns and resolves actions.
 */
public class CombatEngine {
    // Screen boundaries (matching canvas dimensions)
    private static final double SCREEN_WIDTH = 800;
    private static final double SCREEN_HEIGHT = 600;
    private static final double BOUNDARY_MARGIN = 20; // Distance from edge before hitting wall

    // Terrain feature positions and sizes for hiding
    private static final List<TerrainFeature> TERRAIN_FEATURES = Arrays.asList(
            new TerrainFeature(100, 150, 40, "rock"),
            new TerrainFeature(300, 250, 50, "rock"),
            new TerrainFeature(400, 100, 35, "tree"),
            new TerrainFeature(500, 350, 35, "tree"),
            new TerrainFeature(150, 400, 80, "shadow"),
            new TerrainFeature(450, 200, 100, "shadow"),
            new TerrainFeature(200, 300, 25, "bush"),
            new TerrainFeature(550, 150, 25, "bush")
    );

    private List<Entity> combatants;
    private int turnNumber;
    private int roundNumber;
    private int actorIndex;
    private Entity currentActor;
    private Random random;
    private CombatLog log;

    public CombatEngine(List<Entity> combatants) {
        this.combatants = new ArrayList<>(combatants);
        this.turnNumber = 0;
        this.roundNumber = 0;
        this.actorIndex = 0;
        this.random = new Random();
        this.log = new CombatLog();
    }

    /**
     * Execute one full round of combat where all alive entities take a turn.
     * Each call to executeTurn() processes one entity's action.
     */
    public void executeTurn() {
        if (!isCombatActive()) {
            return;
        }

        // Start a new round if we've cycled through all entities
        if (actorIndex == 0) {
            roundNumber++;
            log.add("\n=== ROUND " + roundNumber + " ===\n");
        }

        // Find the next alive actor
        Entity actor = getNextAliveActor();
        if (actor == null) {
            return;
        }

        turnNumber++;
        currentActor = actor;
        CombatState state = new CombatState(combatants, actor, turnNumber, TERRAIN_FEATURES);
        Action action = actor.decideAction(state);
        resolveAction(actor, action, state);
        
        // Move to next entity for next turn
        actorIndex++;
        if (actorIndex >= combatants.size()) {
            actorIndex = 0;
        }
    }

    /**
     * Get the next alive combatant, starting from actorIndex.
     */
    private Entity getNextAliveActor() {
        int attempts = 0;
        while (attempts < combatants.size()) {
            Entity e = combatants.get(actorIndex);
            if (e.isAlive()) {
                return e;
            }
            actorIndex++;
            if (actorIndex >= combatants.size()) {
                actorIndex = 0;
            }
            attempts++;
        }
        return null;
    }

    /**
     * Resolve an action taken by an entity.
     */
    private void resolveAction(Entity actor, Action action, CombatState state) {
        if (action == null) return;

        switch (action.getType()) {
            case ATTACK_RANGED -> resolveRangedAttack(actor, action);
            case ATTACK_MELEE -> resolveMeleeAttack(actor, action);
            case MOVE_TOWARDS -> resolveMoveTowards(actor, action);
            case MOVE_AWAY -> resolveMoveAway(actor, action);
            case HIDE -> {
                // Check if near terrain before allowing hide
                if (state.isNearCover(actor)) {
                    actor.setHidden(true);
                    log.add("  " + actor.getName() + " hides in the shadows!");
                } else {
                    log.add("  " + actor.getName() + " tries to hide but finds no cover!");
                }
            }
            case DISENGAGE -> handleDisengage(actor);
            case DASH -> handleDash(actor, action);
            case FLEE -> resolveFlee(actor, action);
            case IDLE -> log.add("  " + actor.getName() + " does nothing.");
            default -> {}
        }
    }

    private void resolveRangedAttack(Entity actor, Action action) {
        Entity target = findEntityById(action.getTargetId());
        if (target == null || !target.isAlive()) return;

        Weapon weapon = actor.getRangedWeapon();
        if (weapon == null) return;

        if (weapon.usesAmmo() && actor.getArrowsRemaining() <= 0) {
            log.add("  " + actor.getName() + " reaches for an arrow but the quiver is empty!");
            return;
        }

        double distance = actor.getPosition().distanceTo(target.getPosition());
        if (weapon.isBeyondRange(distance)) {
            log.add("  " + actor.getName() + " has no shot - " + target.getName() + " is out of " + weapon.getName() + " range!");
            return;
        }

        // Attack roll: d20 + modifier
        int attackBonus = calculateAttackBonus(actor, weapon, true); // with advantage if hidden
        int roll = random.nextInt(20) + 1;
        int totalAttack = roll + attackBonus;
        boolean wasHidden = actor.isHidden();

        if (weapon.usesAmmo()) {
            actor.useArrow();
        }

        if (totalAttack >= target.getStats().getArmorClass()) {
            // Hit!
            int damage = weapon.rollDamage(random);
            target.takeDamage(damage);
            log.add("  " + actor.getName() + (wasHidden ? " (hidden)" : "") + " shoots " + target.getName() +
                    " with " + weapon.getName() + " for " + damage + " damage! (roll: " + roll + " + " + attackBonus + " = " + totalAttack +
                    " vs AC " + target.getStats().getArmorClass() + ")");
        } else {
            log.add("  " + actor.getName() + (wasHidden ? " (hidden)" : "") + " shoots at " + target.getName() +
                    " with " + weapon.getName() + " but misses! (roll: " + roll + " + " + attackBonus + " = " + totalAttack +
                    " vs AC " + target.getStats().getArmorClass() + ")");
        }

        // Attacking reveals position
        actor.setHidden(false);
    }

    private void resolveMeleeAttack(Entity actor, Action action) {
        Entity target = findEntityById(action.getTargetId());
        if (target == null || !target.isAlive()) return;

        Weapon weapon = actor.getMeleeWeapon();
        if (weapon == null) return;

        int attackBonus = calculateAttackBonus(actor, weapon, false);
        int roll = random.nextInt(20) + 1;
        int totalAttack = roll + attackBonus;

        if (totalAttack >= target.getStats().getArmorClass()) {
            int damage = weapon.rollDamage(random);
            target.takeDamage(damage);
            log.add("  " + actor.getName() + " slashes " + target.getName() +
                    " with " + weapon.getName() + " for " + damage + " damage! (roll: " + roll + " + " + attackBonus + " = " + totalAttack +
                    " vs AC " + target.getStats().getArmorClass() + ")");
        } else {
            log.add("  " + actor.getName() + " swings " + weapon.getName() + " at " + target.getName() +
                    " but misses! (roll: " + roll + " + " + attackBonus + " = " + totalAttack +
                    " vs AC " + target.getStats().getArmorClass() + ")");
        }

        actor.setHidden(false);
    }

    private void resolveMoveTowards(Entity actor, Action action) {
        Entity.Position current = actor.getPosition();
        Entity.Position target = action.getTargetPosition();
        double distance = current.distanceTo(target);
        double moveDistance = Math.min(action.getDistance(), distance);

        double angle = calculateAngle(current, target);
        Entity.Position newPos = current.move(moveDistance, angle);
        
        // Check boundaries BEFORE committing movement
        newPos = clampToScreen(newPos);
        
        // Only move if we actually moved (didn't hit wall)
        double actualDistance = current.distanceTo(newPos);
        actor.setPosition(newPos);
        log.add("  " + actor.getName() + " moves toward enemy [HOTRELOAD-TEST] (" +
                String.format("%.0f", actualDistance / 8.0) + " ft).");
    }

    private void resolveMoveAway(Entity actor, Action action) {
        Entity.Position current = actor.getPosition();
        Entity.Position threat = action.getTargetPosition();
        double moveDistance = action.getDistance();

        // Move away from threat
        double angle = calculateAngle(threat, current);
        Entity.Position newPos = current.move(moveDistance, angle);
        
        // Check boundaries BEFORE committing movement
        newPos = clampToScreen(newPos);
        
        // Only move if we actually moved (didn't hit wall)
        double actualDistance = current.distanceTo(newPos);
        actor.setPosition(newPos);
        log.add("  " + actor.getName() + " moves away (" + 
                String.format("%.0f", actualDistance / 8.0) + " ft).");
    }

    private void handleDisengage(Entity actor) {
        // Disengage prevents opportunity attacks (simplified)
        log.add("  " + actor.getName() + " disengages and backs away!");
    }

    private void handleDash(Entity actor, Action action) {
        // Dash allows full movement in a direction (usually away from danger)
        Entity.Position current = actor.getPosition();
        Entity.Position threat = action.getTargetPosition();
        double moveDistance = action.getDistance();

        // Calculate movement away from threat
        double angle = calculateAngle(threat, current);
        Entity.Position newPos = current.move(moveDistance, angle);
        
        // Check boundaries BEFORE committing movement
        newPos = clampToScreen(newPos);
        
        double actualDistance = current.distanceTo(newPos);
        actor.setPosition(newPos);
        log.add("  " + actor.getName() + " DASHES away at full speed (" + 
                String.format("%.0f", actualDistance / 8.0) + " ft)!");
    }

    private void resolveFlee(Entity actor, Action action) {
        resolveMoveAway(actor, action);
        log.add("  " + actor.getName() + " FLEES in terror!");
    }

    private int calculateAttackBonus(Entity actor, Weapon weapon, boolean withAdvantage) {
        int bonus = weapon.getAttackBonus();
        if (withAdvantage && actor.isHidden()) {
            // Hidden attacks have advantage (represented as +2)
            bonus += 2;
        }
        return bonus;
    }

    private double calculateAngle(Entity.Position from, Entity.Position to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        return Math.atan2(dy, dx);
    }

    private Entity findEntityById(String id) {
        return combatants.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public boolean isCombatActive() {
        long aliveCount = combatants.stream().filter(Entity::isAlive).count();
        return aliveCount > 1;
    }

    /**
     * Clamp position to screen boundaries (add walls/borders).
     */
    private Entity.Position clampToScreen(Entity.Position pos) {
        double x = Math.max(BOUNDARY_MARGIN, Math.min(SCREEN_WIDTH - BOUNDARY_MARGIN, pos.x));
        double y = Math.max(BOUNDARY_MARGIN, Math.min(SCREEN_HEIGHT - BOUNDARY_MARGIN, pos.y));
        return new Entity.Position(x, y);
    }


    // Getters
    public int getTurnNumber() { return turnNumber; }
    public Entity getCurrentActor() { return currentActor; }
    public List<Entity> getCombatants() { return new ArrayList<>(combatants); }
    public CombatLog getLog() { return log; }
}
