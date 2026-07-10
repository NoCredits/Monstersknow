package com.monstersknow.server.ai;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

/**
 * Redirects compiler output to {@link InMemoryClassFile} instances instead of
 * writing .class files to disk, and collects the resulting bytecode.
 */
final class InMemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    private final Map<String, InMemoryClassFile> compiled = new LinkedHashMap<>();

    InMemoryFileManager(JavaFileManager fileManager) {
        super(fileManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind,
                                                FileObject sibling) throws IOException {
        InMemoryClassFile classFile = new InMemoryClassFile(className);
        compiled.put(className, classFile);
        return classFile;
    }

    Map<String, byte[]> getCompiledClasses() {
        Map<String, byte[]> result = new LinkedHashMap<>();
        compiled.forEach((name, file) -> result.put(name, file.getBytes()));
        return result;
    }
}
