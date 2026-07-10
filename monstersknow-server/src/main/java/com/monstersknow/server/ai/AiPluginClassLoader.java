package com.monstersknow.server.ai;

import java.util.Map;

/**
 * Defines classes from in-memory bytecode produced by {@link AiCompiler}.
 * A fresh instance is created for every compile so that recompiling an AI
 * never reuses stale classes/statics - the old loader simply becomes
 * unreferenced and GC-eligible once nothing still points at it.
 *
 * <p>The parent loader must be the one that owns
 * {@code com.monstersknow.core.ai}/{@code com.monstersknow.core.entity} so
 * that types like {@link com.monstersknow.core.ai.GoblinAi} and
 * {@link com.monstersknow.core.entity.Action} resolve to the exact same
 * classes the engine uses, avoiding {@link ClassCastException} across
 * classloaders.
 */
final class AiPluginClassLoader extends ClassLoader {
    private final Map<String, byte[]> classBytes;

    AiPluginClassLoader(Map<String, byte[]> classBytes, ClassLoader parent) {
        super(parent);
        this.classBytes = classBytes;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = classBytes.get(name);
        if (bytes == null) {
            throw new ClassNotFoundException(name);
        }
        return defineClass(name, bytes, 0, bytes.length);
    }
}
