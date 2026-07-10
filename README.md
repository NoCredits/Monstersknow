# Monstersknow - Combat Simulation Game

A tactical combat simulator featuring D&D 5e-inspired monsters with AI-driven behavior. Watch creatures battle using realistic combat tactics and strategies — and write your own goblin AI without touching the game's source code.

## Architecture

```
.
├── monstersknow-core/           # Core game logic (combat engine, entities, AI contract)
├── monstersknow-server/         # Spring Boot REST API + web UI
│   └── ai-scripts/              # Drop-in custom AI .java files, loaded at startup
├── goblins.txt                  # Original goblin tactics document (D&D Monster Manual)
└── pom.xml                      # Maven multi-module parent
```

## Components

### 1. Core Module (`monstersknow-core`)
Game logic that's completely independent of the web framework.

**Key classes:**
- `Entity` — base class for all creatures
- `Goblin` — default goblin implementation with built-in ambush tactics
- `CustomAiGoblin` — a `Goblin` whose `decideAction` is delegated to a runtime-loaded [custom AI plugin](#writing-a-custom-goblin-ai)
- `Stats` — creature attributes (STR, DEX, CON, etc.)
- `Weapon` — damage dice, attack bonus, and range for a melee/ranged weapon
- `Action` — action types (Attack, Move, Hide, Flee, etc.)
- `CombatState` — current state of the battle
- `CombatEngine` — turn-based combat orchestrator
- `com.monstersknow.core.ai` — the plugin contract (`GoblinAi`, `AiContext`, `AiEntityView`) that custom AI code is written against

**Built-in goblin tactics:**
- Ambush attacks from hiding (advantage)
- Optimal distance maintenance (40-80 feet) with bow kiting
- Flee when critically wounded (1 HP)
- Hide/Move/Attack action sequences

### 2. Server Module (`monstersknow-server`)
Spring Boot REST API with a canvas-based web UI.

**Combat endpoints (`/api/combat`):**
- `POST /start` — start a new combat (three default goblins)
- `POST /turn` — execute a single turn
- `POST /turns/{count}` — execute multiple turns
- `GET /state` — get current combat state
- `GET /active` — check if combat is still active
- `POST /reset` — reset combat
- `POST /spawn` — spawn a new entity; body `{ type, posX, posY, aiName? }`. Omit `aiName` (or leave it blank) for the default built-in AI.

**AI plugin endpoints (`/api/ai`):**
- `POST /compile` — body `{ aiName, sourceCode }`, compiles and (re)registers a custom AI. Returns `400` with compiler diagnostics on failure.
- `GET /list` — names of currently loaded AI plugins, usable as `spawn`'s `aiName`.

**Web UI:**
- Real-time combat visualization (canvas)
- Entity status display (health, position, hidden state, active AI)
- Combat log with action history
- Spawn panel with an AI dropdown, populated from `/api/ai/list`
- Control buttons (Start, Next Turn, Auto Play)

## Getting Started

### Prerequisites
- JDK 17+ (a full JDK, not just a JRE — the AI plugin system uses `javax.tools.JavaCompiler`, which is JDK-only)
- Maven 3.8+

### Build & Run

```powershell
# Build project (use "install", not "package" — the server module
# depends on monstersknow-core via the local Maven repo, so "package"
# alone leaves that dependency unresolved)
mvn clean install -DskipTests

# Run server with hot reload (reactor mode)
mvn -pl monstersknow-server -am spring-boot:run
```

Note: `-pl monstersknow-server -am` runs the server as part of a multi-module
reactor build together with `monstersknow-core`, so the server picks up
`monstersknow-core/target/classes` directly instead of the jar installed in
`.m2`. That, combined with `spring-boot-devtools` (already on the classpath)
and `spring.devtools.restart.additional-paths` in `application.properties`,
means edits to core classes (e.g. `Goblin.java`) trigger an automatic restart
once recompiled — no need to manually stop the server, `mvn clean install`,
and restart every time.

If you only run `mvn spring-boot:run` from inside `monstersknow-server`
directly (not as part of the reactor), it resolves `monstersknow-core` from
`.m2` as a normal dependency and won't see local changes until you rerun
`mvn clean install`.

Server will start at `http://localhost:8080`

### Running tests

```powershell
mvn test
```

Covers the in-memory AI compiler/loader (`AiCompilerTest`): compiling valid and invalid sources, and verifying that recompiling an AI produces a fresh, independent instance.

## How It Works

1. **Start Combat** — three goblins spawn on the battlefield, using the default AI.
2. **Automatic AI** — each goblin decides its action based on current health, distance from enemies, hidden state, available arrows, and (if spawned with an `aiName`) its custom plugin's logic instead.
3. **Turn Execution** — `CombatEngine` processes each entity's action.
4. **Real-time Updates** — the web UI updates with entity positions, health changes, combat log entries, and hidden/visible status.

## Writing a Custom Goblin AI

Like RoboWars/RobotWars, you can write your own goblin tactics as a plain `.java` file — no changes to the game's source and no restart required. The server compiles it in memory and loads it via reflection.

**1. Implement `GoblinAi`:**

```java
package com.monstersknow.ai.plugins;

import com.monstersknow.core.ai.AiContext;
import com.monstersknow.core.ai.AiEntityView;
import com.monstersknow.core.ai.GoblinAi;
import com.monstersknow.core.entity.Action;

public class RusherAi implements GoblinAi {
    @Override
    public Action decideAction(AiContext ctx) {
        AiEntityView enemy = ctx.getNearestEnemy();
        if (enemy == null) {
            return Action.idle();
        }
        double distance = ctx.getSelfPosition().distanceTo(enemy.getPosition());
        if (distance <= ctx.getMeleeWeaponRange()) {
            return Action.attackMelee(enemy.getId());
        }
        return Action.moveTowards(enemy.getPosition(), 30.0);
    }
}
```

Requirements: `package com.monstersknow.ai.plugins;`, a `public class` implementing `GoblinAi`, and a public no-arg constructor. `AiContext`/`AiEntityView` are deliberately read-only — a plugin can query the battlefield (nearest enemy, distance, cover, arrows, weapon ranges, ...) but can't mutate anyone's health or position directly; only `Action` factory methods (`attackRanged`, `attackMelee`, `moveTowards`, `moveAway`, `hide`, `disengage`, `dash`, `flee`, `idle`) can express intent, which `CombatEngine` then resolves. See `monstersknow-server/ai-scripts/RusherAi.java` for a working example.

**2. Load it, either by:**
- Dropping the `.java` file in `monstersknow-server/ai-scripts/` — scanned once at server startup, `aiName` = filename without `.java`.
- `POST /api/ai/compile` with `{ "aiName": "RusherAi", "sourceCode": "..." }` — compiles and registers it live, no restart needed. Recompiling under the same `aiName` replaces the previous version; entities already spawned with the old version keep their old behavior until re-spawned.

**3. Use it:** `POST /api/combat/spawn` with `{ "type": "goblin", "posX": 400, "posY": 300, "aiName": "RusherAi" }`, or pick it from the AI dropdown in the web UI.

If a plugin throws or returns `null`, the goblin falls back to the default built-in ambush tactics for that turn rather than crashing combat.

**Security note:** this compiles and runs arbitrary Java with full JVM privileges (there's no sandboxing — Java's `SecurityManager` is deprecated for removal and isn't a viable option). Fine for local/hobby use with trusted contributors; don't expose `/api/ai/compile` beyond localhost.

## Expanding the System

To add a wholly new monster **type** (not just new goblin tactics), create a new class extending `Entity`:

```java
public class Bugbear extends Entity {
    // Override decideAction() with custom tactics
    @Override
    public Action decideAction(CombatState combatState) {
        // Implement bugbear AI
    }
}
```

...and wire it into `CombatService.createGoblin`-style dispatch (currently `hobgoblin`/`bugbear` are TODO placeholders reusing `Goblin`). For goblin tactics specifically, prefer the [custom AI plugin system](#writing-a-custom-goblin-ai) above — it needs no game rebuild at all.

## Future Enhancements

- [ ] Flanking mechanics and advantage system
- [ ] Terrain and obstacles on battlefield
- [ ] Different weapon types with varying damage
- [ ] Spell casting for more advanced creatures
- [ ] Save/Load combat scenarios
- [ ] Replay system with pause/step-through
- [ ] Web-based scenario builder
- [ ] More monster types (Hobgoblins, Bugbears, Orcs, etc.) with their own AI contracts
- [ ] Player-controlled entity
- [ ] Statistics and battle reports
- [ ] Timeout/interrupt protection for runaway custom AI plugins

## Class Design Notes

- **Stateless Actions** — all decisions made from observable game state only
- **Modular Tactics** — each creature type has isolated decision logic
- **Server-side Simulation** — all game logic runs on server (no client-side combat)
- **Extensible Architecture** — easy to add new features without changing core, including user-authored AI plugins compiled and loaded at runtime
