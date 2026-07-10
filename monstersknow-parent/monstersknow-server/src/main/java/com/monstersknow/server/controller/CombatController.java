package com.monstersknow.server.controller;

import com.monstersknow.server.service.CombatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * REST API controller for combat operations.
 */
@RestController
@RequestMapping("/api/combat")
@CrossOrigin(origins = "*")
public class CombatController {
    @Autowired
    private CombatService combatService;

    /**
     * Start a new combat.
     */
    @PostMapping("/start")
    public CombatService.CombatData startCombat() {
        combatService.startNewCombat();
        return combatService.getCombatData();
    }

    /**
     * Execute next turn.
     */
    @PostMapping("/turn")
    public CombatService.CombatData executeTurn() {
        if (!combatService.isCombatActive()) {
            return combatService.getCombatData();
        }
        combatService.executeTurn();
        return combatService.getCombatData();
    }

    /**
     * Execute multiple turns.
     */
    @PostMapping("/turns/{count}")
    public CombatService.CombatData executeTurns(@PathVariable("count") int count) {
        for (int i = 0; i < count && combatService.isCombatActive(); i++) {
            combatService.executeTurn();
        }
        return combatService.getCombatData();
    }

    /**
     * Get current combat state.
     */
    @GetMapping("/state")
    public CombatService.CombatData getState() {
        return combatService.getCombatData();
    }

    /**
     * Check if combat is active.
     */
    @GetMapping("/active")
    public boolean isActive() {
        return combatService.isCombatActive();
    }

    /**
     * Reset combat.
     */
    @PostMapping("/reset")
    public void reset() {
        combatService.reset();
    }

    /**
     * Spawn a new entity in combat.
     */
    @PostMapping("/spawn")
    public CombatService.CombatData spawnEntity(@RequestBody SpawnRequest request) {
        combatService.spawnEntity(request.type, request.posX, request.posY);
        return combatService.getCombatData();
    }

    /**
     * DTO for spawn requests.
     */
    public static class SpawnRequest {
        public String type;
        public double posX;
        public double posY;
    }
}
