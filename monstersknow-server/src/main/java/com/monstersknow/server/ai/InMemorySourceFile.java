package com.monstersknow.server.ai;

import java.io.IOException;
import java.net.URI;
import javax.tools.SimpleJavaFileObject;

/**
 * Wraps a Java source string so it can be handed to {@link javax.tools.JavaCompiler}
 * without writing it to disk first.
 */
final class InMemorySourceFile extends SimpleJavaFileObject {
    private final String sourceCode;

    InMemorySourceFile(String className, String sourceCode) {
        super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.sourceCode = sourceCode;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return sourceCode;
    }
}
