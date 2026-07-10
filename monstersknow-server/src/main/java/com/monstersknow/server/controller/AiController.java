package com.monstersknow.server.controller;

import com.monstersknow.server.ai.AiCompileException;
import com.monstersknow.server.ai.AiPluginLoader;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for compiling and listing user-authored goblin AI plugins.
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {
    @Autowired
    private AiPluginLoader aiPluginLoader;

    /**
     * Compile (or recompile) a plugin from source and register it under the
     * given name. A recompile replaces the previously loaded plugin for that
     * name; entities already spawned with the old version keep behaving as
     * before until re-spawned.
     */
    @PostMapping("/compile")
    public ResponseEntity<CompileResult> compile(@RequestBody CompileRequest request) {
        try {
            AiPluginLoader.LoadedAi loadedAi = aiPluginLoader.loadOrReplace(request.aiName, request.sourceCode);
            return ResponseEntity.ok(CompileResult.success(loadedAi));
        } catch (AiCompileException e) {
            return ResponseEntity.badRequest().body(CompileResult.failure(e.getMessage()));
        }
    }

    /**
     * List the names of currently loaded AI plugins, usable as the
     * {@code aiName} field on a spawn request.
     */
    @GetMapping("/list")
    public List<String> list() {
        return aiPluginLoader.listNames();
    }

    public static class CompileRequest {
        public String aiName;
        public String sourceCode;
    }

    public static class CompileResult {
        public boolean success;
        public String aiName;
        public String className;
        public String error;

        static CompileResult success(AiPluginLoader.LoadedAi loadedAi) {
            CompileResult result = new CompileResult();
            result.success = true;
            result.aiName = loadedAi.aiName();
            result.className = loadedAi.className();
            return result;
        }

        static CompileResult failure(String error) {
            CompileResult result = new CompileResult();
            result.success = false;
            result.error = error;
            return result;
        }
    }
}
