package com.monstersknow.core.entity;

/**
 * Base entity class representing a creature in combat.
 */
public abstract class Entity {
    // Default vision cone: matches goblin darkvision (60 ft) from the Monster Manual.
    private static final double DEFAULT_FIELD_OF_VIEW_DEGREES = 90.0;
    private static final double DEFAULT_VIEW_DISTANCE_FEET = 60.0;

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
    private double facingAngle;
    private double fieldOfViewDegrees;
    private double viewDistance;

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
        this.facingAngle = 0.0;
        this.fieldOfViewDegrees = DEFAULT_FIELD_OF_VIEW_DEGREES;
        this.viewDistance = DEFAULT_VIEW_DISTANCE_FEET * Position.PIXELS_PER_FOOT;
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

    // Vision
    public double getFacingAngle() { return facingAngle; }
    public void setFacingAngle(double facingAngle) { this.facingAngle = facingAngle; }
    public double getFieldOfViewDegrees() { return fieldOfViewDegrees; }
    public double getViewDistance() { return viewDistance; }

    /**
     * Whether this entity can currently see {@code target}: within view
     * distance AND inside the facing-centered field-of-view cone. Doesn't
     * account for terrain occlusion - only distance and facing direction.
     */
    public boolean canSee(Entity target) {
        double distance = position.distanceTo(target.getPosition());
        if (distance > viewDistance) {
            return false;
        }
        double angleToTarget = position.angleTo(target.getPosition());
        double angleDiff = Math.abs(normalizeAngle(angleToTarget - facingAngle));
        return angleDiff <= Math.toRadians(fieldOfViewDegrees / 2.0);
    }

    private static double normalizeAngle(double angle) {
        double twoPi = 2 * Math.PI;
        double normalized = angle % twoPi;
        if (normalized > Math.PI) {
            normalized -= twoPi;
        } else if (normalized < -Math.PI) {
            normalized += twoPi;
        }
        return normalized;
    }

    /**
     * Decide the next action for this entity based on game state.
     * Override in subclasses to implement different tactics.
     */
    public abstract Action decideAction(CombatState combatState);

    public static class Position {
        public static final double PIXELS_PER_FOOT = 8.0;

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

        /**
         * Angle (radians, atan2 convention) from this position towards {@code other}.
         */
        public double angleTo(Position other) {
            return Math.atan2(other.y - this.y, other.x - this.x);
        }

        public Position move(double distance, double angle) {
            // Distance is in FEET. Convert to pixels: 5 feet per 40-pixel square = 8 pixels/foot
            double pixelDistance = distance * PIXELS_PER_FOOT;
            double newX = this.x + pixelDistance * Math.cos(angle);
            double newY = this.y + pixelDistance * Math.sin(angle);
            return new Position(newX, newY);
        }
    }
}
