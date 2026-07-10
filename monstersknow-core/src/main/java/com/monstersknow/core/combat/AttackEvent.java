package com.monstersknow.core.combat;

/**
 * Snapshot of a single attack resolved this turn, carrying the spatial data
 * (attacker/target positions) a client needs to animate a projectile flying
 * along its trajectory or a melee swing, instead of just logging text.
 */
public class AttackEvent {
    public enum Kind { RANGED, MELEE }

    private final Kind kind;
    private final String attackerId;
    private final String targetId;
    private final double attackerX;
    private final double attackerY;
    private final double targetX;
    private final double targetY;
    private final boolean hit;
    private final int damage;
    private final String weaponName;

    public AttackEvent(Kind kind, String attackerId, String targetId,
                        double attackerX, double attackerY, double targetX, double targetY,
                        boolean hit, int damage, String weaponName) {
        this.kind = kind;
        this.attackerId = attackerId;
        this.targetId = targetId;
        this.attackerX = attackerX;
        this.attackerY = attackerY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.hit = hit;
        this.damage = damage;
        this.weaponName = weaponName;
    }

    public Kind getKind() { return kind; }
    public String getAttackerId() { return attackerId; }
    public String getTargetId() { return targetId; }
    public double getAttackerX() { return attackerX; }
    public double getAttackerY() { return attackerY; }
    public double getTargetX() { return targetX; }
    public double getTargetY() { return targetY; }
    public boolean isHit() { return hit; }
    public int getDamage() { return damage; }
    public String getWeaponName() { return weaponName; }
}
