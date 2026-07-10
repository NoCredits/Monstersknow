package com.monstersknow.server.ai;

import com.monstersknow.core.ai.GoblinAi;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Compiles and registers {@link GoblinAi} plugins by name. Plugins can come
 * from the {@code ai-scripts/} directory (scanned once at startup) or from
 * the {@code /api/ai/compile} endpoint for live, no-restart updates.
 */
@Component
public class AiPluginLoader {
    private static final Logger LOG = LoggerFactory.getLogger(AiPluginLoader.class);
    private static final Path SCRIPTS_DIR = Path.of("ai-scripts");
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([\\w.]+)\\s*;");
    private static final Pattern CLASS_PATTERN = Pattern.compile("public\\s+class\\s+(\\w+)");

    private final AiCompiler aiCompiler;
    private final Map<String, LoadedAi> loaded = new ConcurrentHashMap<>();

    public AiPluginLoader(AiCompiler aiCompiler) {
        this.aiCompiler = aiCompiler;
    }

    public record LoadedAi(String aiName, String className, GoblinAi instance, Instant compiledAt) {}

    /**
     * Compiles the given source, discards any previously loaded AI with the
     * same name (its classloader is simply dereferenced, never reused), and
     * registers the freshly loaded instance under {@code aiName}.
     */
    public LoadedAi loadOrReplace(String aiName, String sourceCode) throws AiCompileException {
        String className = deriveClassName(sourceCode);
        Map<String, byte[]> compiled = aiCompiler.compile(className, sourceCode);

        AiPluginClassLoader classLoader = new AiPluginClassLoader(compiled, getClass().getClassLoader());
        try {
            Class<?> clazz = classLoader.loadClass(className);
            if (!GoblinAi.class.isAssignableFrom(clazz)) {
                throw new AiCompileException(className + " must implement " + GoblinAi.class.getName());
            }
            GoblinAi instance = (GoblinAi) clazz.getDeclaredConstructor().newInstance();
            LoadedAi entry = new LoadedAi(aiName, className, instance, Instant.now());
            loaded.put(aiName, entry);
            return entry;
        } catch (ReflectiveOperationException e) {
            throw new AiCompileException("Failed to instantiate " + className + ": " + e.getMessage(), e);
        }
    }

    public Optional<GoblinAi> get(String aiName) {
        return Optional.ofNullable(loaded.get(aiName)).map(LoadedAi::instance);
    }

    public List<String> listNames() {
        return List.copyOf(loaded.keySet());
    }

    private String deriveClassName(String sourceCode) throws AiCompileException {
        Matcher classMatcher = CLASS_PATTERN.matcher(sourceCode);
        if (!classMatcher.find()) {
            throw new AiCompileException("Could not find a 'public class <Name>' declaration in the source.");
        }
        String simpleName = classMatcher.group(1);

        Matcher packageMatcher = PACKAGE_PATTERN.matcher(sourceCode);
        return packageMatcher.find() ? packageMatcher.group(1) + "." + simpleName : simpleName;
    }

    @PostConstruct
    void loadBundledScripts() {
        if (!Files.isDirectory(SCRIPTS_DIR)) {
            return;
        }
        try (var files = Files.list(SCRIPTS_DIR)) {
            files.filter(path -> path.toString().endsWith(".java")).forEach(this::loadScriptFile);
        } catch (IOException e) {
            LOG.warn("Failed to scan {}: {}", SCRIPTS_DIR, e.getMessage());
        }
    }

    private void loadScriptFile(Path path) {
        String fileName = path.getFileName().toString();
        String aiName = fileName.substring(0, fileName.length() - ".java".length());
        try {
            String sourceCode = Files.readString(path);
            loadOrReplace(aiName, sourceCode);
            LOG.info("Loaded AI plugin '{}' from {}", aiName, path);
        } catch (IOException | AiCompileException e) {
            LOG.warn("Failed to load AI plugin from {}: {}", path, e.getMessage());
        }
    }
}
