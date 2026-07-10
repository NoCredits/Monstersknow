package com.monstersknow.core.combat;

import java.util.ArrayList;
import java.util.List;

/**
 * Log of events that happen during combat.
 */
public class CombatLog {
    private List<String> entries;

    public CombatLog() {
        this.entries = new ArrayList<>();
    }

    public void add(String entry) {
        entries.add(entry);
    }

    public List<String> getEntries() {
        return new ArrayList<>(entries);
    }

    public void clear() {
        entries.clear();
    }

    @Override
    public String toString() {
        return String.join("\n", entries);
    }
}
