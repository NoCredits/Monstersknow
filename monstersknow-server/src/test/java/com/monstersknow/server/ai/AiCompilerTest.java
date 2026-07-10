package com.monstersknow.server.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class AiCompilerTest {
    private static final String VALID_SOURCE = """
            package com.monstersknow.ai.plugins;

            import com.monstersknow.core.ai.AiContext;
            import com.monstersknow.core.ai.GoblinAi;
            import com.monstersknow.core.entity.Action;

            public class TestIdleAi implements GoblinAi {
                @Override
                public Action decideAction(AiContext context) {
                    return Action.idle();
                }
            }
            """;

    private static final String INVALID_SOURCE = """
            package com.monstersknow.ai.plugins;

            public class TestBrokenAi {
                this is not valid java
            }
            """;

    @Test
    void compilesValidGoblinAiSource() throws AiCompileException {
        AiCompiler compiler = new AiCompiler();
        Map<String, byte[]> compiled = compiler.compile("com.monstersknow.ai.plugins.TestIdleAi", VALID_SOURCE);

        assertTrue(compiled.containsKey("com.monstersknow.ai.plugins.TestIdleAi"));
        assertFalse(compiled.get("com.monstersknow.ai.plugins.TestIdleAi").length == 0);
    }

    @Test
    void invalidSourceThrowsWithDiagnostics() {
        AiCompiler compiler = new AiCompiler();
        AiCompileException exception = assertThrows(AiCompileException.class,
                () -> compiler.compile("com.monstersknow.ai.plugins.TestBrokenAi", INVALID_SOURCE));

        assertFalse(exception.getMessage().isBlank());
    }

    @Test
    void loadOrReplaceProducesDistinctInstancesOnRecompile() throws AiCompileException {
        AiPluginLoader loader = new AiPluginLoader(new AiCompiler());

        AiPluginLoader.LoadedAi first = loader.loadOrReplace("test-idle", VALID_SOURCE);
        AiPluginLoader.LoadedAi second = loader.loadOrReplace("test-idle", VALID_SOURCE);

        assertNotSame(first.instance(), second.instance());
        assertTrue(loader.get("test-idle").isPresent());
        assertTrue(loader.listNames().contains("test-idle"));
    }
}
