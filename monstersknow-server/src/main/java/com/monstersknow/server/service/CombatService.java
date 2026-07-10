package com.monstersknow.server.service;

import com.monstersknow.core.ai.GoblinAi;
import com.monstersknow.core.combat.CombatEngine;
import com.monstersknow.core.entity.CustomAiGoblin;
import com.monstersknow.core.entity.Entity;
import com.monstersknow.core.entity.Goblin;
import com.monstersknow.server.ai.AiPluginLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service managing combat simulations.
 */
@Service
public class CombatService {
    private CombatEngine currentCombat;
    private List<Entity> activeCombatants;

    @Autowired
    private AiPluginLoader aiPluginLoader;

    public CombatService() {
        this.activeCombatants = new ArrayList<>();
    }

    /**
     * Start a new combat with default participants.
     */
    public void startNewCombat() {
        activeCombatants.clear();

        // Create goblins with wide spacing on a 5-foot grid
        Goblin goblin1 = new Goblin("goblin-1", "Grok");
        Goblin goblin2 = new Goblin("goblin-2", "Snagg");
        Goblin goblin3 = new Goblin("goblin-3", "Zargh");

        // Place them on grid squares (multiples of 40 pixels = 5-foot squares)
        // Grid positions are at 5-foot intervals across the battlefield
        goblin1.setPosition(new Entity.Position(100, 150));  // ~25 feet, 37 feet
        goblin2.setPosition(new Entity.Position(480, 80));   // ~120 feet, 20 feet
        goblin3.setPosition(new Entity.Position(240, 400));  // ~60 feet, 100 feet

        activeCombatants.add(goblin1);
        activeCombatants.add(goblin2);
        activeCombatants.add(goblin3);

        currentCombat = new CombatEngine(activeCombatants);
    }

    /**
     * Execute the next combat turn.
     */
    public void executeTurn() {
        if (currentCombat != null) {
            currentCombat.executeTurn();
        }
    }

    /**
     * Execute multiple turns.
     */
    public void executeTurns(int count) {
        for (int i = 0; i < count && currentCombat != null && currentCombat.isCombatActive(); i++) {
            executeTurn();
        }
    }

    /**
     * Check if combat is still active.
     */
    public boolean isCombatActive() {
        return currentCombat != null && currentCombat.isCombatActive();
    }

    /**
     * Get current combat data.
     */
    public CombatData getCombatData() {
        if (currentCombat == null) {
            return null;
        }

        return new CombatData(
                currentCombat.getTurnNumber(),
                currentCombat.getCombatants(),
                currentCombat.getLog().getEntries()
        );
    }

    /**
     * Reset combat.
     */
    public void reset() {
        currentCombat = null;
        activeCombatants.clear();
    }

    /**
     * Spawn a new entity in the current combat, using the default built-in AI.
     */
    public void spawnEntity(String type, double posX, double posY) {
        spawnEntity(type, posX, posY, null);
    }

    /**
     * Spawn a new entity in the current combat. If {@code aiName} names a
     * loaded AI plugin, the spawned goblin delegates its decisions to that
     * plugin instead of the default built-in ambush tactics.
     */
    public void spawnEntity(String type, double posX, double posY, String aiName) {
        if (currentCombat == null) {
            startNewCombat();
        }

        Entity newEntity = null;
        String entityName = type.substring(0, 1).toUpperCase() + type.substring(1);
        String entityId = type + "-" + UUID.randomUUID().toString().substring(0, 8);

        switch (type.toLowerCase()) {
            case "goblin":
                newEntity = createGoblin(entityId, entityName, aiName);
                break;
            case "hobgoblin":
                // Use Goblin for now as placeholder (same stats)
                newEntity = createGoblin(entityId, entityName, aiName);
                break;
            case "bugbear":
                // Use Goblin for now as placeholder (same stats)
                newEntity = createGoblin(entityId, entityName, aiName);
                break;
            default:
                newEntity = createGoblin(entityId, entityName, aiName);
        }

        newEntity.setPosition(new Entity.Position(posX, posY));
        activeCombatants.add(newEntity);

        // Reinitialize combat engine with updated combatants
        currentCombat = new CombatEngine(activeCombatants);
    }

    private Goblin createGoblin(String entityId, String entityName, String aiName) {
        if (aiName == null || aiName.isBlank()) {
            return new Goblin(entityId, entityName);
        }
        GoblinAi ai = aiPluginLoader.get(aiName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown AI: " + aiName));
        return new CustomAiGoblin(entityId, entityName, ai, aiName);
    }

    /**
     * DTO for transferring combat state to client.
     */
    public static class CombatData {
        public int turnNumber;
        public List<EntityData> entities;
        public List<String> log;

        public CombatData(int turnNumber, List<Entity> entities, List<String> log) {
            this.turnNumber = turnNumber;
            this.entities = entities.stream()
                    .map(EntityData::fromEntity)
                    .toList();
            this.log = log;
        }
    }

    public static class EntityData {
        public String id;
        public String name;
        public String type;
        public int currentHealth;
        public int maxHealth;
        public double posX;
        public double posY;
        public boolean hidden;
        public boolean alive;
        public String aiName;

        public EntityData() {}

        public static EntityData fromEntity(Entity entity) {
            EntityData data = new EntityData();
            data.id = entity.getId();
            data.name = entity.getName();
            data.type = entity.getType();
            data.currentHealth = entity.getCurrentHealth();
            data.maxHealth = entity.getStats().getMaxHealth();
            data.posX = entity.getPosition().x;
            data.posY = entity.getPosition().y;
            data.hidden = entity.isHidden();
            data.alive = entity.isAlive();
            if (entity instanceof CustomAiGoblin customAiGoblin) {
                data.aiName = customAiGoblin.getAiName();
            }
            return data;
        }
    }
}
