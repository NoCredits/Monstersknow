package com.monstersknow.core.entity;

/**
 * Base statistics for a monster entity.
 */
public class Stats {
    private int strength;
    private int dexterity;
    private int constitution;
    private int intelligence;
    private int wisdom;
    private int charisma;
    private int armorClass;
    private int maxHealth;

    public Stats(int strength, int dexterity, int constitution, 
                 int intelligence, int wisdom, int charisma, 
                 int armorClass, int maxHealth) {
        this.strength = strength;
        this.dexterity = dexterity;
        this.constitution = constitution;
        this.intelligence = intelligence;
        this.wisdom = wisdom;
        this.charisma = charisma;
        this.armorClass = armorClass;
        this.maxHealth = maxHealth;
    }

    // Getters
    public int getStrength() { return strength; }
    public int getDexterity() { return dexterity; }
    public int getConstitution() { return constitution; }
    public int getIntelligence() { return intelligence; }
    public int getWisdom() { return wisdom; }
    public int getCharisma() { return charisma; }
    public int getArmorClass() { return armorClass; }
    public int getMaxHealth() { return maxHealth; }
}
