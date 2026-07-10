package com.monstersknow.server.ai;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import javax.tools.SimpleJavaFileObject;

/**
 * Captures the bytecode {@link javax.tools.JavaCompiler} writes for a single
 * compiled class, in memory instead of on disk.
 */
final class InMemoryClassFile extends SimpleJavaFileObject {
    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    InMemoryClassFile(String className) {
        super(URI.create("string:///" + className.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);
    }

    @Override
    public OutputStream openOutputStream() {
        return bytes;
    }

    byte[] getBytes() {
        return bytes.toByteArray();
    }
}
