# Monstersknow - Combat Simulation Game

A tactical combat simulator featuring D&D 5e-inspired monsters with AI-driven behavior. Watch creatures battle using realistic combat tactics and strategies.

## Architecture

```
monstersknow-parent/
├── monstersknow-core/          # Core game logic (combat engine, entities, tactics)
├── monstersknow-server/        # Spring Boot REST API server
└── goblins.txt                 # Original goblin tactics document
```

## Components

### 1. **Core Module** (`monstersknow-core`)
Game logic that's completely independent of the web framework.

**Key Classes:**
- `Entity` - Base class for all creatures
- `Goblin` - Goblin implementation with ambush tactics
- `Stats` - Creature attributes (STR, DEX, CON, etc.)
- `Action` - Action types (Attack, Move, Hide, etc.)
- `CombatState` - Current state of the battle
- `CombatEngine` - Turn-based combat orchestrator

**Tactics Implemented:**
- Ambush attacks from hiding (advantage)
- Optimal distance maintenance (40-80 feet)
- Flee when seriously wounded (1-2 HP)
- Counter-attack when moderately wounded (3-4 HP)
- Hide/Move/Attack action sequences

### 2. **Server Module** (`monstersknow-server`)
Spring Boot REST API with web UI.

**REST Endpoints:**
- `POST /api/combat/start` - Start new combat
- `POST /api/combat/turn` - Execute single turn
- `POST /api/combat/turns/{count}` - Execute multiple turns
- `GET /api/combat/state` - Get current combat state
- `GET /api/combat/active` - Check if combat is active
- `POST /api/combat/reset` - Reset combat

**Web UI:**
- Real-time combat visualization (canvas)
- Entity status display (health, position, hidden state)
- Combat log with action history
- Control buttons (Start, Next Turn, Auto Play)

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+

### Build & Run

```powershell
# Navigate to parent directory
cd monstersknow-parent

# Build project (use "install", not "package" — the server module
# depends on monstersknow-core via the local Maven repo, so "package"
# alone leaves that dependency unresolved)
mvn clean install -DskipTests

# Run server
cd monstersknow-server
mvn spring-boot:run
```

Note: run `mvn spring-boot:run` from inside `monstersknow-server`, not from
the parent directory — running it from the parent with `-pl` fails with
`No plugin found for prefix 'spring-boot'`, since the parent POM (packaging
`pom`) doesn't declare the plugin.

Server will start at `http://localhost:8080`

## How It Works

1. **Start Combat** - Three goblins spawn on the battlefield
2. **Automatic AI** - Each goblin decides its action based on:
   - Current health status
   - Distance from enemies
   - Hidden state
   - Available arrows
3. **Turn Execution** - CombatEngine processes each entity's action
4. **Real-time Updates** - Web UI updates with:
   - Entity positions
   - Health changes
   - Combat log entries
   - Hidden/visible status

## Expanding the System

To add new monster types:

1. **Create a new class extending `Entity`**:
```java
public class Bugbear extends Entity {
    // Override decideAction() with custom tactics
    @Override
    public Action decideAction(CombatState combatState) {
        // Implement bugbear AI
    }
}
```

2. **Add to CombatService**:
```java
Bugbear bugbear = new Bugbear("bugbear-1", "Gornak");
activeCombatants.add(bugbear);
```

## Future Enhancements

- [ ] Flanking mechanics and advantage system
- [ ] Terrain and obstacles on battlefield
- [ ] Different weapon types with varying damage
- [ ] Spell casting for more advanced creatures
- [ ] Save/Load combat scenarios
- [ ] Replay system with pause/step-through
- [ ] Web-based scenario builder
- [ ] More monster types (Hobgoblins, Bugbears, Orcs, etc.)
- [ ] Player-controlled entity
- [ ] Statistics and battle reports

## Class Design Notes

- **Stateless Actions** - All decisions made from observable game state only
- **Modular Tactics** - Each creature type has isolated decision logic
- **Server-side Simulation** - All game logic runs on server (no client-side combat)
- **Extensible Architecture** - Easy to add new features without changing core
