package com.monstersknow.core.entity;

/**
 * Base entity class representing a creature in combat.
 */
public abstract class Entity {
    private String id;
    private String name;
    private String type;
    private Stats stats;
    private int currentHealth;
    private Position position;
    private boolean hidden;
    private int arrowsRemaining;
    private Weapon meleeWeapon;
    private Weapon rangedWeapon;

    public Entity(String id, String name, String type, Stats stats, int arrowsRemaining,
                  Weapon meleeWeapon, Weapon rangedWeapon) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.stats = stats;
        this.currentHealth = stats.getMaxHealth();
        this.position = new Position(0, 0);
        this.hidden = false;
        this.arrowsRemaining = arrowsRemaining;
        this.meleeWeapon = meleeWeapon;
        this.rangedWeapon = rangedWeapon;
    }

    // Combat status
    public boolean isAlive() {
        return currentHealth > 0;
    }

    public boolean isSeriouslyWounded() {
        return currentHealth <= 2;
    }

    public boolean isModeratelyWounded() {
        return currentHealth >= 3 && currentHealth <= 4;
    }

    public void takeDamage(int damage) {
        this.currentHealth -= damage;
        if (this.currentHealth < 0) {
            this.currentHealth = 0;
        }
    }

    public void heal(int amount) {
        this.currentHealth = Math.min(this.currentHealth + amount, stats.getMaxHealth());
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public Stats getStats() { return stats; }
    public int getCurrentHealth() { return currentHealth; }
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
    public int getArrowsRemaining() { return arrowsRemaining; }
    public void useArrow() { if (arrowsRemaining > 0) arrowsRemaining--; }
    public void replenishArrows(int count) { this.arrowsRemaining += count; }
    public Weapon getMeleeWeapon() { return meleeWeapon; }
    public Weapon getRangedWeapon() { return rangedWeapon; }

    /**
     * Decide the next action for this entity based on game state.
     * Override in subclasses to implement different tactics.
     */
    public abstract Action decideAction(CombatState combatState);

    public static class Position {
        public double x;
        public double y;

        public Position(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double distanceTo(Position other) {
            double dx = this.x - other.x;
            double dy = this.y - other.y;
            return Math.sqrt(dx * dx + dy * dy);
        }

        public Position move(double distance, double angle) {
            // Distance is in FEET. Convert to pixels: 5 feet per 40-pixel square = 8 pixels/foot
            double PIXELS_PER_FOOT = 8.0;
            double pixelDistance = distance * PIXELS_PER_FOOT;
            double newX = this.x + pixelDistance * Math.cos(angle);
            double newY = this.y + pixelDistance * Math.sin(angle);
            return new Position(newX, newY);
        }
    }
}
