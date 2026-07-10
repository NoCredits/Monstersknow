package com.monstersknow.core.entity;

import java.util.Random;

/**
 * A weapon carried by an entity - defines its damage, attack bonus and range.
 * Shared by melee and ranged weapons so new monster types can be given their
 * own loadout without touching the combat resolution code.
 */
public class Weapon {
    private final String name;
    private final int damageDiceCount;
    private final int damageDieSize;
    private final int damageBonus;
    private final int attackBonus;
    private final int normalRange;
    private final int maxRange;
    private final boolean usesAmmo;

    public Weapon(String name, int damageDiceCount, int damageDieSize, int damageBonus,
                  int attackBonus, int normalRange, int maxRange, boolean usesAmmo) {
        this.name = name;
        this.damageDiceCount = damageDiceCount;
        this.damageDieSize = damageDieSize;
        this.damageBonus = damageBonus;
        this.attackBonus = attackBonus;
        this.normalRange = normalRange;
        this.maxRange = maxRange;
        this.usesAmmo = usesAmmo;
    }

    /**
     * Roll this weapon's damage dice.
     */
    public int rollDamage(Random random) {
        int total = damageBonus;
        for (int i = 0; i < damageDiceCount; i++) {
            total += random.nextInt(damageDieSize) + 1;
        }
        return total;
    }

    public boolean isBeyondRange(double distance) {
        return distance > maxRange;
    }

    // Getters
    public String getName() { return name; }
    public int getAttackBonus() { return attackBonus; }
    public int getNormalRange() { return normalRange; }
    public int getMaxRange() { return maxRange; }
    public boolean usesAmmo() { return usesAmmo; }
}
