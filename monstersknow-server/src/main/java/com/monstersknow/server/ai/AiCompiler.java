package com.monstersknow.server.ai;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import org.springframework.stereotype.Component;

/**
 * Compiles a single user-supplied Java source file in memory using the JDK's
 * built-in compiler. No .class or .java files ever touch disk.
 *
 * <p>Known limitation: the compile classpath is derived from the running
 * JVM's {@code java.class.path}, which is flat during {@code mvn spring-boot:run}
 * but collapses to just the fat jar itself once Spring Boot repackages the
 * app (nested-jar classloader) - compilation of new plugins against a
 * packaged jar is not supported without further work.
 */
@Component
public class AiCompiler {
    public Map<String, byte[]> compile(String fullyQualifiedClassName, String sourceCode) throws AiCompileException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new AiCompileException("No system Java compiler available - run on a JDK, not a JRE.");
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        InMemoryFileManager fileManager = new InMemoryFileManager(
                compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8));

        JavaFileObject sourceObject = new InMemorySourceFile(fullyQualifiedClassName, sourceCode);
        List<String> options = List.of("-classpath", System.getProperty("java.class.path"));

        StringWriter compilerOutput = new StringWriter();
        boolean success = compiler.getTask(compilerOutput, fileManager, diagnostics, options, null, List.of(sourceObject))
                .call();

        if (!success) {
            throw new AiCompileException(formatDiagnostics(diagnostics, compilerOutput.toString()));
        }
        return fileManager.getCompiledClasses();
    }

    private String formatDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics, String rawOutput) {
        StringBuilder sb = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            sb.append(diagnostic.getKind()).append(": line ").append(diagnostic.getLineNumber())
                    .append(": ").append(diagnostic.getMessage(null)).append('\n');
        }
        if (sb.isEmpty() && !rawOutput.isBlank()) {
            sb.append(rawOutput);
        }
        if (sb.isEmpty()) {
            sb.append("Compilation failed for an unknown reason.");
        }
        return sb.toString();
    }
}
